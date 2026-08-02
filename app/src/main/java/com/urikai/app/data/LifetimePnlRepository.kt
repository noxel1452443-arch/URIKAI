package com.urikai.app.data

import android.content.Context
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.RelicEffects
import com.urikai.app.game.RelicId
import kotlin.random.Random

/**
 * 通算・出金回数・レリック受け取り／ボーナス回／適用状態。
 * 図鑑 prefs とは分離。
 */
class LifetimePnlRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLifetimePnl(): Int = prefs.getInt(KEY_LIFETIME_PNL, 0)

    fun getCashOutCount(): Int = prefs.getInt(KEY_CASH_OUT_COUNT, 0)

    /** いま開始〜プレイ中の試合がボーナス対象か。 */
    fun isActiveBonusRun(): Boolean = prefs.getBoolean(KEY_ACTIVE_BONUS_RUN, false)

    fun hasSeenBonusTutorial(): Boolean = prefs.getBoolean(KEY_BONUS_TUTORIAL_SEEN, false)

    fun isBonusTutorialInProgress(): Boolean =
        prefs.getBoolean(KEY_BONUS_TUTORIAL_IN_PROGRESS, false)

    fun beginBonusTutorial() {
        prefs.edit().putBoolean(KEY_BONUS_TUTORIAL_IN_PROGRESS, true).apply()
    }

    fun completeBonusTutorial() {
        prefs.edit()
            .putBoolean(KEY_BONUS_TUTORIAL_IN_PROGRESS, false)
            .putBoolean(KEY_BONUS_TUTORIAL_SEEN, true)
            .apply()
    }

    fun clearBonusTutorialSeen() {
        prefs.edit()
            .putBoolean(KEY_BONUS_TUTORIAL_SEEN, false)
            .putBoolean(KEY_BONUS_TUTORIAL_IN_PROGRESS, false)
            .apply()
    }

    fun recordFinalAsset(asset: Int) {
        val next = (getFinalAssetHistory() + asset).takeLast(MAX_ASSET_HISTORY)
        prefs.edit().putString(KEY_ASSET_HISTORY, next.joinToString(",")).apply()
    }

    fun recordSessionPnlHistory(
        brandNameJp: String,
        brandNameEn: String,
        pnl: Int
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
            val pnl = parts[2].toIntOrNull() ?: return@mapNotNull null
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

    fun getFinalAssetHistory(): List<Int> {
        val raw = prefs.getString(KEY_ASSET_HISTORY, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(',').mapNotNull { it.toIntOrNull() }
    }

    /** 今回の資産を含む上位10。ランク外なら currentOutOfRank に載せる。 */
    fun buildAssetRankBoard(currentAsset: Int): AssetRankBoard {
        val history = getFinalAssetHistory()
        if (history.isEmpty()) {
            return AssetRankBoard(
                top = listOf(AssetRankEntry(rank = 1, asset = currentAsset, isCurrent = true)),
                currentOutOfRank = null
            )
        }
        val sorted = history.sortedDescending()
        var currentMarked = false
        val ranked = sorted.mapIndexed { index, asset ->
            val isCurrent = !currentMarked && asset == currentAsset
            if (isCurrent) currentMarked = true
            AssetRankEntry(rank = index + 1, asset = asset, isCurrent = isCurrent)
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
     * 出金確定時: 通算加算（ボーナス回なら倍率）＋出金回数＋次試合の抽選＋閾値未受け取り化。
     *
     * - ボーナス倍率は「この試合がアクティブボーナス」だった場合のみ適用
     * - 出金後に次試合を 10% で抽選（ただし累計出金が 3 回未満なら抽選しない）
     */
    fun onCashOut(sessionPnl: Int, random: Random = Random.Default): CashOutPersistResult {
        val previous = getLifetimePnl()
        val boost = isActiveBonusRun()
        val mult = if (boost) effects().tenClearBonusMult else 1f
        val credited = if (boost) {
            (sessionPnl * mult).toInt()
        } else {
            sessionPnl
        }
        val next = previous + credited
        val cashOuts = getCashOutCount() + 1
        val newly = RelicId.newlyUnlocked(previous, next)
        val pending = getPendingRelics().toMutableSet()
        val claimed = getClaimedRelics()
        newly.filter { it !in claimed }.forEach { pending.add(it) }

        // この試合のボーナスは消費。次試合を抽選（初回3出金までは抽選なし）。
        val armNext = cashOuts >= MIN_CASHOUTS_BEFORE_BONUS_ROLL &&
            (DEBUG_FORCE_BONUS_ROLL || random.nextFloat() < BONUS_ROLL_CHANCE)

        prefs.edit()
            .putInt(KEY_LIFETIME_PNL, next)
            .putInt(KEY_CASH_OUT_COUNT, cashOuts)
            .putBoolean(KEY_ACTIVE_BONUS_RUN, false)
            .putBoolean(KEY_PENDING_BONUS_RUN, armNext)
            .putStringSet(KEY_PENDING, encodeRelics(pending))
            .apply()

        return CashOutPersistResult(
            lifetimePnl = next,
            creditedPnl = credited,
            sessionPnl = sessionPnl,
            appliedTenClearBoost = boost,
            tenClearMult = if (boost) mult else 1f,
            cashOutCount = cashOuts,
            newlyPending = newly.filter { it !in claimed },
            pendingRelics = RelicId.ordered.filter { it in pending },
            effects = effects()
        )
    }

    /**
     * START 復帰時に呼び出し。保留中ボーナスを今試合のアクティブにする。
     * @return 今試合がボーナスになったか
     */
    fun activatePendingBonusRun(): Boolean {
        val pending = prefs.getBoolean(KEY_PENDING_BONUS_RUN, false)
        if (!pending) return isActiveBonusRun()
        prefs.edit()
            .putBoolean(KEY_PENDING_BONUS_RUN, false)
            .putBoolean(KEY_ACTIVE_BONUS_RUN, true)
            .apply()
        return true
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
        private const val KEY_ACTIVE_BONUS_RUN = "active_bonus_run"
        private const val KEY_PENDING_BONUS_RUN = "pending_bonus_run"
        private const val KEY_BONUS_TUTORIAL_SEEN = "bonus_tutorial_seen"
        private const val KEY_BONUS_TUTORIAL_IN_PROGRESS = "bonus_tutorial_in_progress"
        private const val KEY_ASSET_HISTORY = "final_asset_history"
        private const val KEY_SESSION_PNL_HISTORY = "session_pnl_history"
        private const val KEY_CLAIMED = "relics_claimed"
        private const val KEY_PENDING = "relics_pending"

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

data class AssetRankEntry(
    val rank: Int,
    val asset: Int,
    val isCurrent: Boolean,
)

data class AssetRankBoard(
    val top: List<AssetRankEntry>,
    val currentOutOfRank: AssetRankEntry?,
)

data class SessionPnlHistoryEntry(
    val brandNameJp: String,
    val brandNameEn: String,
    val pnl: Int,
) {
    fun brandName(lang: DisplayLanguage): String =
        when (lang) {
            DisplayLanguage.EN -> brandNameEn.ifBlank { brandNameJp }
            DisplayLanguage.JP -> brandNameJp.ifBlank { brandNameEn }
        }
}

data class CashOutPersistResult(
    val lifetimePnl: Int,
    val creditedPnl: Int,
    val sessionPnl: Int,
    val appliedTenClearBoost: Boolean,
    val tenClearMult: Float,
    val cashOutCount: Int,
    val newlyPending: List<RelicId>,
    val pendingRelics: List<RelicId>,
    val effects: RelicEffects,
)
