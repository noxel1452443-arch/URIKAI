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
    const val PASSIVE_INTERVAL_MS = 6L * 60L * 1000L
    /** 草のしき布を報酬として出せる最高残高。 */
    const val FIRST_GRADE_PEAK_BALANCE = 12_000L

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
            1 -> "わらのす"
            2 -> "ふかふかざぶとん"
            3 -> "ちいさなドーム"
            4 -> "みみや"
            else -> "ヘッドセットハウス"
        }
    }

    fun gradeNameEn(gradeOrLevel: Int, asGradeIndex: Boolean = false): String {
        val g = if (asGradeIndex) gradeOrLevel else gradeIndex(gradeOrLevel)
        return when (g) {
            0 -> "None"
            1 -> "Straw nest"
            2 -> "Plush cushion"
            3 -> "Little dome"
            4 -> "Ear hut"
            else -> "Headset house"
        }
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

    fun canNormalLevelUp(level: Int, unlockedGrade: Int, balance: Long): Boolean {
        val next = nextNormalLevel(level, unlockedGrade) ?: return false
        return balance >= costToReach(next)
    }

    fun canGradeUp(level: Int, unlockedGrade: Int, balance: Long): Boolean {
        val next = nextGradeUpLevel(level, unlockedGrade) ?: return false
        return balance >= costToReach(next)
    }

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

    fun passivePerTick(level: Int): Long = payPerPlay(level) / 10L

    fun pendingCap(level: Int): Long = payPerPlay(level) * 3L

    fun costToReach(level: Int): Long {
        if (level <= 0) return 0L
        if (level == 1) return 600L
        when (level) {
            10 -> return 3_500L
            25 -> return 8_000L
            45 -> return 16_000L
            70 -> return 32_000L
        }
        return floor(350.0 * 1.055.pow((level - 2).toDouble())).toLong().coerceAtLeast(350L)
    }

    /** 既存セーブの Lv から、解放済みグレードを推定。 */
    fun unlockedGradeForLevel(level: Int): Int = gradeIndex(level).coerceAtLeast(0)

    fun flavorJp(level: Int): String = when (gradeIndex(level)) {
        0 -> "まだ場所がない。"
        1 -> "わらを編んだす。仮のねぐら。"
        2 -> "ふかふかのざぶとん。沈むと気持ちいい。"
        3 -> "ちいさなペットドーム。穴からひょっこり。"
        4 -> "片耳をひさしにした、みみや。"
        else -> "ヘッドセットハウス。通信の本拠。"
    }

    fun flavorEn(level: Int): String = when (gradeIndex(level)) {
        0 -> "No nest yet."
        1 -> "A straw nest. Temporary roost."
        2 -> "A plush cushion. Nice to sink into."
        3 -> "A little pet dome. Peek from the hole."
        4 -> "An ear hut with one floppy awning ear."
        else -> "Headset house. Home base of signals."
    }

    fun gradeRewardFlavorJp(grade: Int): String = when (grade) {
        1 -> "わらのすを手に入れた。カイのすみかが始まる。"
        2 -> "ふかふかざぶとん。やわらかいねぐら。"
        3 -> "ちいさなドーム。中に入れるすみか。"
        4 -> "みみや。耳がひさしになった小屋。"
        5 -> "ヘッドセットハウス。最上のすみか。"
        else -> ""
    }

    fun gradeRewardFlavorEn(grade: Int): String = when (grade) {
        1 -> "Straw nest unlocked. Kai's nest begins."
        2 -> "Plush cushion. A softer roost."
        3 -> "Little dome. A nest you can enter."
        4 -> "Ear hut. Ears become the awning."
        5 -> "Headset house. The finest nest."
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

data class SumikaSnapshot(
    val level: Int,
    val pendingYen: Long,
    val balance: Long,
    val cashOutCount: Int,
    val unlockedGrade: Int,
    val pendingGrade: Int,
) {
    val payPerPlay: Long get() = Sumika.payPerPlay(level)
    val passivePerTick: Long get() = Sumika.passivePerTick(level)
    val cap: Long get() = Sumika.pendingCap(level)
    val nextNormalLevel: Int? get() = Sumika.nextNormalLevel(level, unlockedGrade)
    val nextGradeUpLevel: Int? get() = Sumika.nextGradeUpLevel(level, unlockedGrade)
    val nextCost: Long
        get() {
            val n = nextNormalLevel ?: nextGradeUpLevel ?: return 0L
            return Sumika.costToReach(n)
        }
    val canNormalLevelUp: Boolean get() = Sumika.canNormalLevelUp(level, unlockedGrade, balance)
    val canGradeUp: Boolean get() = Sumika.canGradeUp(level, unlockedGrade, balance)
    val atGradeCap: Boolean
        get() = level > 0 && nextNormalLevel == null && nextGradeUpLevel == null
    val gradeNameJp: String get() = Sumika.gradeNameJp(level)
    val gradeNameEn: String get() = Sumika.gradeNameEn(level)
}
