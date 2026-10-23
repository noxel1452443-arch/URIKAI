package com.urikai.app.game

import kotlin.math.floor
import kotlin.math.pow

/**
 * カイのすみか（設計: store/SUMIKA_DESIGN.md）。
 *
 * - Lv0 = 未所持（ホーム非表示）
 * - 見た目グレードは報酬受け取りで解放。出金回数ゲートは使わない
 * - 解放済みグレード内だけレベルアップ可。キャップ到達時は「今が最大」に見せる
 */
object Sumika {
    const val MAX_LEVEL = 99
    /** パッシブお給料の刻み（5分）。 */ 
    const val PASSIVE_INTERVAL_MS = 5L * 60L * 1000L
    /** 1時間あたりのパッシブ回数（60/5）。時給セリフ用。 */
    const val PASSIVE_TICKS_PER_HOUR = 12L
    /** ほしぐさ（g1）を報酬として出せる最高残高。 */
    const val FIRST_GRADE_PEAK_BALANCE = 11_000L

    /** grade 1..5 の開始 Lv（グレードアップ先）。 */
    private val GRADE_START = intArrayOf(0, 1, 10, 25, 45, 70)
    /** grade 1..5 の強化上限 Lv。 */
    private val GRADE_CAP = intArrayOf(0, 9, 24, 44, 69, 99)

    fun gradeIndex(level: Int): Int = when {
        level <= 0 -> 0
        level < 10 -> 1
        level < 25 -> 2
        level < 45 -> 3
        level < 70 -> 4
        else -> 5
    }

    fun gradeNameJp(gradeOrLevel: Int, asGradeIndex: Boolean = false): String {
        val g = if (asGradeIndex) gradeOrLevel else gradeIndex(gradeOrLevel)
        return when (g) {
            0 -> "なし"
            1 -> "ほしぐさのねどこ"
            2 -> "あさのしきぬの"
            3 -> "ふかふかくッション"
            4 -> "うさぎドーム"
            else -> "雪魔女の部屋"
        }
    }

    fun gradeNameEn(gradeOrLevel: Int, asGradeIndex: Boolean = false): String {
        val g = if (asGradeIndex) gradeOrLevel else gradeIndex(gradeOrLevel)
        return when (g) {
            0 -> "None"
            1 -> "Hay bed"
            2 -> "Hemp mat"
            3 -> "Fluffy cushion"
            4 -> "Bunny dome"
            else -> "Snow witch room"
        }
    }

    /** inside／enter があるのは g4 以降。 */
    fun supportsInside(gradeOrLevel: Int, asGradeIndex: Boolean = false): Boolean {
        val g = if (asGradeIndex) gradeOrLevel else gradeIndex(gradeOrLevel)
        return g >= 4
    }

    fun maxLevelForUnlockedGrade(unlockedGrade: Int): Int =
        GRADE_CAP.getOrElse(unlockedGrade.coerceIn(0, 5)) { 0 }

    fun startLevelForGrade(grade: Int): Int =
        GRADE_START.getOrElse(grade.coerceIn(0, 5)) { 0 }

    fun isGradeBoundaryLevel(level: Int): Boolean =
        level == 10 || level == 25 || level == 45 || level == 70

    /** 通常レベルアップ（グレード内）。キャップでは next を出さない。 */
    fun nextNormalLevel(level: Int, unlockedGrade: Int): Int? {
        if (level <= 0 || unlockedGrade <= 0) return null
        val visual = gradeIndex(level)
        val softCap = if (unlockedGrade > visual) {
            maxLevelForUnlockedGrade(visual)
        } else {
            maxLevelForUnlockedGrade(unlockedGrade)
        }
        val next = level + 1
        if (next > softCap) return null
        if (isGradeBoundaryLevel(next) && gradeIndex(next) > visual) return null
        return next
    }

    /** 次グレードへ見た目を上げる（受け取り済みのときだけ）。 */
    fun nextGradeUpLevel(level: Int, unlockedGrade: Int): Int? {
        if (level <= 0) return null
        if (unlockedGrade <= gradeIndex(level)) return null
        val next = level + 1
        if (!isGradeBoundaryLevel(next)) return null
        if (gradeIndex(next) > unlockedGrade) return null
        return next
    }

    fun canNormalLevelUp(
        level: Int,
        unlockedGrade: Int,
        balance: Long,
        discountStacks: Int = 0,
    ): Boolean {
        val next = nextNormalLevel(level, unlockedGrade) ?: return false
        return balance >= discountedCost(costToReach(next), discountStacks)
    }

    fun canGradeUp(
        level: Int,
        unlockedGrade: Int,
        balance: Long,
        discountStacks: Int = 0,
    ): Boolean {
        val next = nextGradeUpLevel(level, unlockedGrade) ?: return false
        return balance >= discountedCost(costToReach(next), discountStacks)
    }

    /** 半額スタック適用後の費用。0円にはしない。 */
    fun discountedCost(base: Long, stacks: Int): Long {
        if (base <= 0L) return 0L
        var cost = base
        repeat(stacks.coerceAtLeast(0)) {
            cost = (cost / 2L).coerceAtLeast(1L)
        }
        return cost
    }

    /** 1日あたりの半額／給料2倍の利用上限。 */
    const val REWARD_DAILY_CAP = 3

    fun payPerPlay(level: Int): Long {
        if (level <= 0) return 0L
        val anchors = listOf(
            1 to 300L, 9 to 420L, 10 to 480L, 24 to 650L, 25 to 700L,
            44 to 900L, 45 to 1000L, 69 to 1250L, 70 to 1400L, 99 to 1800L
        )
        if (level >= 99) return 1800L
        for (i in 0 until anchors.lastIndex) {
            val (aLv, aPay) = anchors[i]
            val (bLv, bPay) = anchors[i + 1]
            if (level == aLv) return aPay
            if (level in aLv..bLv) {
                val t = (level - aLv).toDouble() / (bLv - aLv).toDouble()
                return (aPay + (bPay - aPay) * t).toLong()
            }
        }
        return 300L
    }

    /**
     * 5分ごとのお給料。
     * 「旧・6分額（play/10）の半分」＝ play/20。
     * 例: Lv45 play=¥1000 → 5分ごと ¥50。
     */
    fun passivePerTick(level: Int): Long = payPerPlay(level) / 20L

    /** 未受取上限。プレイ給料の 2 倍。 */
    fun pendingCap(level: Int): Long = payPerPlay(level) * 2L

    /** 時給目安（5分額 × 12）。 */
    fun passivePerHour(level: Int): Long = passivePerTick(level) * PASSIVE_TICKS_PER_HOUR

    fun costToReach(level: Int): Long {
        if (level <= 0) return 0L
        if (level == 1) return 1_200L
        when (level) {
            10 -> return 7_000L
            25 -> return 16_000L
            45 -> return 32_000L
            70 -> return 64_000L
        }
        return floor(700.0 * 1.055.pow((level - 2).toDouble())).toLong().coerceAtLeast(700L)
    }

    /** 既存セーブの Lv から、解放済みグレードを推定。 */
    fun unlockedGradeForLevel(level: Int): Int = gradeIndex(level).coerceAtLeast(0)

    fun flavorJp(level: Int): String = when (gradeIndex(level)) {
        0 -> "まだ場所がない。"
        1 -> "ほしぐさのねどこ。仮のねぐら。"
        2 -> "あさのしきぬの。まくら付き。"
        3 -> "ふかふかくッション。まくらを立てかけた。"
        4 -> "両耳立ちの、うさぎドーム。"
        else -> "雪魔女の部屋。片耳倒しの帽子すみか。"
    }

    fun flavorEn(level: Int): String = when (gradeIndex(level)) {
        0 -> "No nest yet."
        1 -> "A hay bed. Temporary roost."
        2 -> "A hemp mat with a thin white pillow."
        3 -> "A fluffy cushion with a matching propped pillow."
        4 -> "A bunny dome with both ears up."
        else -> "Snow witch room. One floppy ear, snow at the feet."
    }

    fun gradeRewardFlavorJp(grade: Int): String = when (grade) {
        1 -> "ほしぐさのねどこを手に入れた。カイのすみかが始まる。"
        2 -> "あさのしきぬの。白いまくら付き。"
        3 -> "ふかふかくッション。立てかけまくら。"
        4 -> "うさぎドーム。なかに入れる。"
        5 -> "雪魔女の部屋。最上のすみか。"
        else -> ""
    }

    fun gradeRewardFlavorEn(grade: Int): String = when (grade) {
        1 -> "Hay bed unlocked. Kai's nest begins."
        2 -> "Hemp mat with a white pillow."
        3 -> "Fluffy cushion with a matching pillow."
        4 -> "Bunny dome. A nest you can enter."
        5 -> "Snow witch room. The finest nest."
        else -> ""
    }
}

enum class SumikaUpgradeFail {
    MAXED,
    FUNDS,
}

sealed class SumikaUpgradeResult {
    data class Ok(val level: Int, val balance: Long, val peakBalance: Long) : SumikaUpgradeResult()
    data class Fail(val reason: SumikaUpgradeFail) : SumikaUpgradeResult()
}

data class SumikaCollectResult(
    val amount: Long,
    val balance: Long,
    val peakBalance: Long,
    val newlyPending: List<RelicId> = emptyList(),
    val pendingRelics: List<RelicId> = emptyList(),
)

data class SumikaGradeClaimResult(
    val grade: Int,
    val level: Int,
    val unlockedGrade: Int,
)

/** 半額適用直前の値段カウントダウン演出。 */
data class SumikaCostReveal(
    val fromCost: Long,
    val toCost: Long,
)

data class SumikaSnapshot(
    val level: Int,
    val pendingYen: Long,
    val balance: Long,
    val cashOutCount: Int,
    val unlockedGrade: Int,
    val pendingGrade: Int,
    val upgradeDiscountStacks: Int = 0,
    val discountUsesLeftToday: Int = Sumika.REWARD_DAILY_CAP,
    val payDoubled: Boolean = false,
    val doubleUsesLeftToday: Int = Sumika.REWARD_DAILY_CAP,
) {
    val payPerPlay: Long get() = Sumika.payPerPlay(level)
    val passivePerTick: Long get() = Sumika.passivePerTick(level)
    val cap: Long get() = Sumika.pendingCap(level)
    val nextNormalLevel: Int? get() = Sumika.nextNormalLevel(level, unlockedGrade)
    val nextGradeUpLevel: Int? get() = Sumika.nextGradeUpLevel(level, unlockedGrade)
    val nextCostBase: Long
        get() {
            val n = nextNormalLevel ?: nextGradeUpLevel ?: return 0L
            return Sumika.costToReach(n)
        }
    val nextCost: Long get() = Sumika.discountedCost(nextCostBase, upgradeDiscountStacks)
    val canNormalLevelUp: Boolean
        get() = Sumika.canNormalLevelUp(level, unlockedGrade, balance, upgradeDiscountStacks)
    val canGradeUp: Boolean
        get() = Sumika.canGradeUp(level, unlockedGrade, balance, upgradeDiscountStacks)
    val canOfferUpgradeDiscount: Boolean
        get() = (nextNormalLevel != null || nextGradeUpLevel != null) &&
            discountUsesLeftToday > 0 &&
            nextCostBase > 1L
    val canOfferPayDouble: Boolean
        get() = pendingYen > 0L && !payDoubled && doubleUsesLeftToday > 0
    val atGradeCap: Boolean
        get() = level > 0 && nextNormalLevel == null && nextGradeUpLevel == null
    val gradeNameJp: String get() = Sumika.gradeNameJp(level)
    val gradeNameEn: String get() = Sumika.gradeNameEn(level)
}
