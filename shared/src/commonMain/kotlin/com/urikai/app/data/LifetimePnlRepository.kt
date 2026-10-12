package com.urikai.app.data

import com.urikai.app.platform.KeyValueEditor
import com.urikai.app.platform.KeyValueStore
import com.urikai.app.platform.currentTimeMillis
import com.urikai.app.platform.todayEpochDay as platformTodayEpochDay
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.InvestPlan
import com.urikai.app.game.RelicEffects
import com.urikai.app.game.RelicId
import kotlin.random.Random

/**
 * 通算・出金回数・レリック受け取り／ボーナス回／適用状態。
 * 図鑑 prefs とは分離。
 */
class LifetimePnlRepository(openStore: (String) -> KeyValueStore) {

    private val prefs = openStore(PREFS_NAME)

    fun getLifetimePnl(): Int = prefs.getInt(KEY_LIFETIME_PNL, 0)

    /** 口座残高。毎回この全額で勝負する。 */
    fun getBalance(): Long = prefs.getLong(KEY_BALANCE, INITIAL_BALANCE)

    /** 過去最高の口座残高（レリック解放の基準。再スタートしても下がらない）。 */
    fun getPeakBalance(): Long = prefs.getLong(KEY_PEAK_BALANCE, INITIAL_BALANCE)

    /** 古い順。 */
    fun getBalanceHistory(): List<BalancePoint> {
        val raw = prefs.getString(KEY_BALANCE_HISTORY, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split('\n').mapNotNull { line ->
            val parts = line.split(',')
            if (parts.size < 3) return@mapNotNull null
            val t = parts[0].toLongOrNull() ?: return@mapNotNull null
            val b = parts[1].toLongOrNull() ?: return@mapNotNull null
            val kind = BalanceEventKind.fromCode(parts[2]) ?: return@mapNotNull null
            BalancePoint(t, b, kind)
        }
    }

    private fun appendBalanceHistory(
        editor: KeyValueEditor,
        point: BalancePoint
    ) = appendBalanceHistory(editor, listOf(point))

    private fun appendBalanceHistory(
        editor: KeyValueEditor,
        points: List<BalancePoint>
    ) {
        if (points.isEmpty()) return
        val existing = prefs.getString(KEY_BALANCE_HISTORY, "").orEmpty()
            .split('\n').filter { it.isNotBlank() }
        val seed = if (existing.isEmpty()) {
            listOf("${points.first().epochMs},$INITIAL_BALANCE,${BalanceEventKind.OPEN.code}")
        } else {
            emptyList()
        }
        val lines = points.map { "${it.epochMs},${it.balance},${it.kind.code}" }
        val next = (existing + seed + lines).takeLast(MAX_BALANCE_HISTORY)
        editor.putString(KEY_BALANCE_HISTORY, next.joinToString("\n"))
    }

    /** 破産後の再スタート（残高を初期額へ）。 */
    fun restartBalance(nowMs: Long = currentTimeMillis()) {
        val editor = prefs.edit().putLong(KEY_BALANCE, INITIAL_BALANCE)
        appendBalanceHistory(editor, BalancePoint(nowMs, INITIAL_BALANCE, BalanceEventKind.RESTART))
        editor.apply()
    }

    fun isBankrupt(): Boolean = getBalance() < BANKRUPT_BELOW

    fun getInvestPlan(): InvestPlan =
        InvestPlan.fromPersisted(prefs.getString(KEY_INVEST_PLAN, null))

    /** 未解放のプランは選べない。翌日の反映分から効く。 */
    fun setInvestPlan(plan: InvestPlan): Boolean {
        if (!plan.isUnlocked(getPeakBalance())) return false
        prefs.edit().putString(KEY_INVEST_PLAN, plan.name).apply()
        return true
    }

    /**
     * 前回反映日から今日までの日数分、選択中プランで残高を動かす（上限 [MAX_ACCRUAL_DAYS] 日）。
     * 初回は基準日を記録するだけ。日ごとの増減はその日付から決まる乱数で、再起動しても変わらない。
     */
    fun accrueDailyPlan(
        yieldMult: Float = 1f,
        maxDays: Int = MAX_ACCRUAL_DAYS,
        todayEpochDay: Long = platformTodayEpochDay(),
        nowMs: Long = currentTimeMillis()
    ): DailyAccrualResult? {
        val lastDay = prefs.getLong(KEY_LAST_ACCRUAL_DAY, -1L)
        if (lastDay < 0L || todayEpochDay < lastDay) {
            prefs.edit().putLong(KEY_LAST_ACCRUAL_DAY, todayEpochDay).apply()
            return null
        }
        val elapsed = todayEpochDay - lastDay
        if (elapsed <= 0L) return null
        val days = elapsed.coerceAtMost(maxDays.toLong()).toInt()
        val plan = getInvestPlan()
        val before = getBalance()
        val previousPeak = getPeakBalance()
        var balance = before
        val editor = prefs.edit()
        val firstDay = todayEpochDay - days + 1
        val points = ArrayList<BalancePoint>(days)
        for (i in 0 until days) {
            val day = firstDay + i
            balance = applyPlanDay(balance, plan, day, yieldMult)
            val t = nowMs - (days - 1 - i) * MS_PER_DAY
            points.add(BalancePoint(t, balance, BalanceEventKind.DAILY))
        }
        appendBalanceHistory(editor, points)
        val nextPeak = maxOf(previousPeak, balance)
        val newly = RelicId.newlyUnlocked(
            (previousPeak - INITIAL_BALANCE).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
            (nextPeak - INITIAL_BALANCE).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        )
        val claimed = getClaimedRelics()
        val pending = getPendingRelics().toMutableSet()
        newly.filter { it !in claimed }.forEach { pending.add(it) }
        editor
            .putLong(KEY_BALANCE, balance)
            .putLong(KEY_PEAK_BALANCE, nextPeak)
            .putLong(KEY_LAST_ACCRUAL_DAY, todayEpochDay)
            .putStringSet(KEY_PENDING, encodeRelics(pending))
            .apply()
        return DailyAccrualResult(plan = plan, days = days, before = before, after = balance)
    }

    private fun applyPlanDay(balance: Long, plan: InvestPlan, epochDay: Long, yieldMult: Float): Long {
        val rng = Random(epochDay * 7_919L + plan.ordinal)
        val gauss = if (plan.isGuaranteed) {
            0.0
        } else {
            // Box–Muller
            val u1 = rng.nextDouble().coerceAtLeast(1e-12)
            val u2 = rng.nextDouble()
            kotlin.math.sqrt(-2.0 * kotlin.math.ln(u1)) * kotlin.math.cos(2.0 * kotlin.math.PI * u2)
        }
        val pct = (plan.dailyMeanPercent * yieldMult + plan.dailyVolPercent * gauss).coerceAtLeast(-50.0)
        return kotlin.math.round(balance * (1.0 + pct / 100.0)).toLong().coerceAtLeast(0L)
    }

    fun getCashOutCount(): Int = prefs.getInt(KEY_CASH_OUT_COUNT, 0)

    /** @param pnlYen その試合での口座残高の増減額 */
    fun recordFinalAsset(asset: Int, pnlYen: Long) {
        val next = (getFinalAssetHistory() + FinalAssetRecord(asset, pnlYen))
            .takeLast(MAX_ASSET_HISTORY)
        prefs.edit()
            .putString(KEY_ASSET_HISTORY, next.joinToString(",") { "${it.asset}:${it.pnlYen}" })
            .apply()
    }

    fun recordSessionPnlHistory(
        brandNameJp: String,
        brandNameEn: String,
        pnl: Long
    ) {
        val entry = listOf(
            sanitizeField(brandNameJp),
            sanitizeField(brandNameEn),
            pnl.toString()
        ).joinToString(SESSION_FIELD_SEP)
        val next = (sessionHistoryRawLines() + entry).takeLast(MAX_SESSION_HISTORY)
        prefs.edit().putString(KEY_SESSION_PNL_HISTORY, next.joinToString("\n")).apply()
    }

    /** 新しい順（直近の出金が先頭）。 */
    fun getSessionPnlHistory(): List<SessionPnlHistoryEntry> {
        return sessionHistoryRawLines().mapNotNull { line ->
            val parts = line.split(SESSION_FIELD_SEP)
            if (parts.size < 3) return@mapNotNull null
            val pnl = parts[2].toLongOrNull() ?: return@mapNotNull null
            SessionPnlHistoryEntry(
                brandNameJp = parts[0],
                brandNameEn = parts[1],
                pnl = pnl
            )
        }.asReversed()
    }

    private fun sessionHistoryRawLines(): List<String> {
        val raw = prefs.getString(KEY_SESSION_PNL_HISTORY, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split('\n').filter { it.isNotBlank() }
    }

    private fun sanitizeField(value: String): String =
        value.replace(SESSION_FIELD_SEP, " ").replace("\n", " ").replace("\r", "")

    /** 旧形式（資産のみ）は ¥10,000 で勝負した扱いで増減額を補う。 */
    fun getFinalAssetHistory(): List<FinalAssetRecord> {
        val raw = prefs.getString(KEY_ASSET_HISTORY, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(',').mapNotNull { token ->
            val parts = token.split(':')
            val asset = parts[0].toIntOrNull() ?: return@mapNotNull null
            val pnl = parts.getOrNull(1)?.toLongOrNull() ?: (asset - INITIAL_BALANCE)
            FinalAssetRecord(asset, pnl)
        }
    }

    /** 今回の結果を含む増減率の上位10。ランク外なら currentOutOfRank に載せる。 */
    fun buildAssetRankBoard(currentAsset: Int, currentPnlYen: Long): AssetRankBoard {
        val history = getFinalAssetHistory()
        if (history.isEmpty()) {
            return AssetRankBoard(
                top = listOf(
                    AssetRankEntry(
                        rank = 1,
                        asset = currentAsset,
                        pnlYen = currentPnlYen,
                        isCurrent = true
                    )
                ),
                currentOutOfRank = null
            )
        }
        val sorted = history.sortedWith(
            compareByDescending<FinalAssetRecord> { it.asset }.thenByDescending { it.pnlYen }
        )
        var currentMarked = false
        val ranked = sorted.mapIndexed { index, record ->
            val isCurrent = !currentMarked &&
                record.asset == currentAsset &&
                record.pnlYen == currentPnlYen
            if (isCurrent) currentMarked = true
            AssetRankEntry(
                rank = index + 1,
                asset = record.asset,
                pnlYen = record.pnlYen,
                isCurrent = isCurrent
            )
        }
        val top = ranked.take(TOP_ASSET_RANKS)
        val out = ranked.firstOrNull { it.isCurrent && it.rank > TOP_ASSET_RANKS }
        return AssetRankBoard(top = top, currentOutOfRank = out)
    }

    fun getClaimedRelics(): Set<RelicId> =
        decodeRelics(prefs.getStringSet(KEY_CLAIMED, emptySet()) ?: emptySet())

    fun getPendingRelics(): List<RelicId> {
        val set = decodeRelics(prefs.getStringSet(KEY_PENDING, emptySet()) ?: emptySet())
        return RelicId.ordered.filter { it in set }
    }

    fun effects(
        stagePrefs: Map<com.urikai.app.game.RelicUpgradeGroup, RelicId> = emptyMap()
    ): RelicEffects = RelicEffects.fromClaimed(getClaimedRelics(), stagePrefs)

    /**
     * 入金確定時: 口座残高を更新し、最高到達額でレリックを解放する。
     * レリックの閾値は「初期残高からの増加額」で判定する。
     */
    fun onCashOut(newBalance: Long, nowMs: Long = currentTimeMillis()): CashOutPersistResult {
        val previousBalance = getBalance()
        val previousPeak = getPeakBalance()
        val nextBalance = newBalance.coerceAtLeast(0L)
        val nextPeak = maxOf(previousPeak, nextBalance)
        val cashOuts = getCashOutCount() + 1
        val newly = RelicId.newlyUnlocked(
            (previousPeak - INITIAL_BALANCE).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
            (nextPeak - INITIAL_BALANCE).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        )
        val pending = getPendingRelics().toMutableSet()
        val claimed = getClaimedRelics()
        newly.filter { it !in claimed }.forEach { pending.add(it) }

        val editor = prefs.edit()
            .putLong(KEY_BALANCE, nextBalance)
            .putLong(KEY_PEAK_BALANCE, nextPeak)
            .putInt(KEY_CASH_OUT_COUNT, cashOuts)
            .putStringSet(KEY_PENDING, encodeRelics(pending))
        appendBalanceHistory(editor, BalancePoint(nowMs, nextBalance, BalanceEventKind.PLAY))
        editor.apply()

        return CashOutPersistResult(
            previousBalance = previousBalance,
            balance = nextBalance,
            peakBalance = nextPeak,
            cashOutCount = cashOuts,
            newlyPending = newly.filter { it !in claimed },
            pendingRelics = RelicId.ordered.filter { it in pending },
        )
    }

    /** 廃止したボーナス回の旧セーブ値を消す。 */
    fun clearLegacyBonusFlags() {
        if (LEGACY_BONUS_KEYS.none { prefs.contains(it) }) return
        val editor = prefs.edit()
        LEGACY_BONUS_KEYS.forEach { editor.remove(it) }
        editor.apply()
    }

    fun claimRelic(id: RelicId): Boolean {
        val pending = getPendingRelics().toMutableSet()
        if (id !in pending) return false
        pending.remove(id)
        val claimed = getClaimedRelics().toMutableSet()
        claimed.add(id)
        prefs.edit()
            .putStringSet(KEY_PENDING, encodeRelics(pending))
            .putStringSet(KEY_CLAIMED, encodeRelics(claimed))
            .apply()
        return true
    }

    fun isClaimed(id: RelicId): Boolean = id in getClaimedRelics()

    private fun decodeRelics(raw: Set<String>): Set<RelicId> =
        raw.mapNotNull { name -> RelicId.fromPersistedName(name) }.toSet()

    private fun encodeRelics(ids: Collection<RelicId>): Set<String> =
        ids.map { it.name }.toSet()

    companion object {
        private const val PREFS_NAME = "urikai_lifetime_pnl"
        private const val KEY_LIFETIME_PNL = "lifetime_pnl"
        private const val KEY_CASH_OUT_COUNT = "cash_out_count"
        private val LEGACY_BONUS_KEYS = listOf(
            "active_bonus_run",
            "pending_bonus_run",
            "bonus_tutorial_seen",
            "bonus_tutorial_in_progress",
        )
        private const val KEY_ASSET_HISTORY = "final_asset_history"
        private const val KEY_SESSION_PNL_HISTORY = "session_pnl_history"
        private const val KEY_CLAIMED = "relics_claimed"
        private const val KEY_PENDING = "relics_pending"
        private const val KEY_BALANCE = "account_balance"
        private const val KEY_PEAK_BALANCE = "account_peak_balance"
        private const val KEY_BALANCE_HISTORY = "account_balance_history"
        private const val KEY_INVEST_PLAN = "invest_plan"
        private const val KEY_LAST_ACCRUAL_DAY = "invest_last_accrual_day"
        const val MAX_ACCRUAL_DAYS = 30
        private const val MS_PER_DAY = 86_400_000L

        const val INITIAL_BALANCE = 10_000L
        /** これを下回ると破産（¥10,000 で再スタート）。 */
        const val BANKRUPT_BELOW = 5_000L
        private const val MAX_BALANCE_HISTORY = 2_000

        private const val SESSION_FIELD_SEP = "\u001f"

        /** 累計出金がこの回数以上になってから次試合を抽選可能。 */
        const val MIN_CASHOUTS_BEFORE_BONUS_ROLL = 3

        /** 次試合がボーナス対象になる確率。 */
        const val BONUS_ROLL_CHANCE = 0.10f

        const val TOP_ASSET_RANKS = 10
        private const val MAX_ASSET_HISTORY = 200
        private const val MAX_SESSION_HISTORY = 100

        /**
         * QA用: true なら出金3回以降は毎回「次試合ボーナス」を引く。
         * 演出確認が終わったら false に戻すこと。
         */
        const val DEBUG_FORCE_BONUS_ROLL = false
    }
}

data class FinalAssetRecord(
    val asset: Int,
    val pnlYen: Long,
)

data class AssetRankEntry(
    val rank: Int,
    val asset: Int,
    val pnlYen: Long,
    val isCurrent: Boolean,
)

data class AssetRankBoard(
    val top: List<AssetRankEntry>,
    val currentOutOfRank: AssetRankEntry?,
)

data class SessionPnlHistoryEntry(
    val brandNameJp: String,
    val brandNameEn: String,
    val pnl: Long,
) {
    fun brandName(lang: DisplayLanguage): String =
        when (lang) {
            DisplayLanguage.EN -> brandNameEn.ifBlank { brandNameJp }
            DisplayLanguage.JP -> brandNameJp.ifBlank { brandNameEn }
        }
}

data class CashOutPersistResult(
    val previousBalance: Long,
    val balance: Long,
    val peakBalance: Long,
    val cashOutCount: Int,
    val newlyPending: List<RelicId>,
    val pendingRelics: List<RelicId>,
)

enum class BalanceEventKind(val code: String) {
    OPEN("o"),
    PLAY("p"),
    RESTART("r"),
    DAILY("d");

    companion object {
        fun fromCode(code: String): BalanceEventKind? = entries.firstOrNull { it.code == code }
    }
}

data class DailyAccrualResult(
    val plan: InvestPlan,
    val days: Int,
    val before: Long,
    val after: Long,
)

data class BalancePoint(
    val epochMs: Long,
    val balance: Long,
    val kind: BalanceEventKind,
)
