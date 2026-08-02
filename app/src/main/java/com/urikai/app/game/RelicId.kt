package com.urikai.app.game

/**
 * 通算閾値で解禁→受け取り→報酬箱に残るレリック。
 * 上位段階は下位効果を上書きする（グループ内の選択で切替可）。
 */
enum class RelicId(val threshold: Int, val kind: RelicKind) {
    PAPER(2_000, RelicKind.VISUAL),
    INTEREST_120(3_500, RelicKind.EARN),
    SMOOTH(5_000, RelicKind.VISUAL),
    JUDGE(7_000, RelicKind.FX),
    BONUS_10_150(9_000, RelicKind.EARN),
    COMBO(12_000, RelicKind.FX),
    COOLDOWN_25(15_000, RelicKind.EARN),
    PNL_BONUS_120(18_000, RelicKind.EARN),
    DARK(22_000, RelicKind.VISUAL),
    MISS_LIFE_1(26_000, RelicKind.EARN),
    LAG_25(30_000, RelicKind.EARN),
    INTEREST_150(35_000, RelicKind.EARN),
    MISS_COMBO_HEAL(40_000, RelicKind.EARN),
    COOLDOWN_50(50_000, RelicKind.EARN),
    PNL_BONUS_150(70_000, RelicKind.EARN),
    BONUS_10_200(99_999, RelicKind.EARN),
    LAG_50(111_111, RelicKind.EARN);

    companion object {
        val ordered: List<RelicId> = entries.sortedBy { it.threshold }

        fun newlyUnlocked(previousPnl: Int, nextPnl: Int): List<RelicId> {
            if (nextPnl <= previousPnl) return emptyList()
            return ordered.filter { previousPnl < it.threshold && nextPnl >= it.threshold }
        }

        /** 旧 enum 名からの移行。 */
        fun fromPersistedName(name: String): RelicId? = when (name) {
            "JUDGE_I", "JUDGE_II", "JUDGE" -> JUDGE
            "COMBO_I", "COMBO_II", "COMBO" -> COMBO
            "MISS_ABSORB_1", "MISS_LIFE_1" -> MISS_LIFE_1
            "MISS_ABSORB_3", "MISS_COMBO_HEAL" -> MISS_COMBO_HEAL
            else -> entries.find { it.name == name }
        }
    }
}

enum class RelicKind {
    VISUAL,
    EARN,
    FX,
}

/** 段階強化グループ（報酬箱で同一行にまとめ、切替可能）。 */
enum class RelicUpgradeGroup(
    val members: List<RelicId>,
    /** true: 所持中の選択1つのみ効果適用。false: 所持分を累積適用（表示切替のみ）。 */
    val exclusiveEffect: Boolean,
) {
    INTEREST(listOf(RelicId.INTEREST_120, RelicId.INTEREST_150), exclusiveEffect = true),
    BONUS_10(listOf(RelicId.BONUS_10_150, RelicId.BONUS_10_200), exclusiveEffect = true),
    PNL(listOf(RelicId.PNL_BONUS_120, RelicId.PNL_BONUS_150), exclusiveEffect = true),
    COOLDOWN(listOf(RelicId.COOLDOWN_25, RelicId.COOLDOWN_50), exclusiveEffect = true),
    LAG(listOf(RelicId.LAG_25, RelicId.LAG_50), exclusiveEffect = true),
    MISS(listOf(RelicId.MISS_LIFE_1, RelicId.MISS_COMBO_HEAL), exclusiveEffect = false);

    companion object {
        fun of(id: RelicId): RelicUpgradeGroup? = entries.find { id in it.members }
    }
}

/** 報酬箱枠・輝き用の見た目ランク。 */
enum class RelicFrameStyle {
    THEME,
    TIER1,
    TIER2,
}

fun RelicId.frameStyle(): RelicFrameStyle = when (this) {
    RelicId.PAPER, RelicId.SMOOTH, RelicId.DARK -> RelicFrameStyle.THEME
    RelicId.INTEREST_120, RelicId.BONUS_10_150, RelicId.PNL_BONUS_120,
    RelicId.COOLDOWN_25, RelicId.LAG_25, RelicId.JUDGE, RelicId.COMBO,
    RelicId.MISS_LIFE_1 -> RelicFrameStyle.TIER1
    RelicId.INTEREST_150, RelicId.BONUS_10_200, RelicId.PNL_BONUS_150,
    RelicId.COOLDOWN_50, RelicId.LAG_50, RelicId.MISS_COMBO_HEAL -> RelicFrameStyle.TIER2
}

enum class JudgeFxTier { NONE, ON }

enum class ComboFxTier { NONE, ON }

enum class CoolFxTier { NONE, I, II }

enum class LagFxTier { NONE, I, II }

enum class RetirePhase {
    NONE,
    SHOCK,
    BANNER,
}

/**
 * 受け取り済みレリック＋段階選択から導く実行時補正。
 * @param stagePrefs グループ → 優先して使う段階（所持していない場合は無視）
 */
data class RelicEffects(
    val interestMult: Float = 1f,
    val tradePnlMult: Float = 1f,
    val tenClearBonusMult: Float = 1.2f,
    val cooldownFactor: Float = 1f,
    val lagFactor: Float = 1f,
    /** 基本4に加算する最大LIFE */
    val maxHealthBonus: Int = 0,
    /** COMBO がこの回数ごと（3,6,9…）に LIFE +1。0で無効 */
    val comboHealEvery: Int = 0,
    val judgeFx: JudgeFxTier = JudgeFxTier.NONE,
    val comboFx: ComboFxTier = ComboFxTier.NONE,
    val coolFx: CoolFxTier = CoolFxTier.NONE,
    val lagFx: LagFxTier = LagFxTier.NONE,
    val paperUnlocked: Boolean = false,
    val darkUnlocked: Boolean = false,
    val smoothUnlocked: Boolean = false,
) {
    companion object {
        fun fromClaimed(
            claimed: Set<RelicId>,
            stagePrefs: Map<RelicUpgradeGroup, RelicId> = emptyMap(),
        ): RelicEffects {
            fun activeIn(group: RelicUpgradeGroup): RelicId? {
                val owned = group.members.filter { it in claimed }
                if (owned.isEmpty()) return null
                if (!group.exclusiveEffect) return null
                val pref = stagePrefs[group]
                return if (pref != null && pref in owned) pref else owned.maxBy { it.threshold }
            }

            var interest = 1f
            var tradePnl = 1f
            var tenClear = 1.2f
            var cooldown = 1f
            var lag = 1f
            var cool = CoolFxTier.NONE
            var lagFx = LagFxTier.NONE

            when (activeIn(RelicUpgradeGroup.INTEREST)) {
                RelicId.INTEREST_120 -> interest = 1.2f
                RelicId.INTEREST_150 -> interest = 1.5f
                else -> Unit
            }
            when (activeIn(RelicUpgradeGroup.PNL)) {
                RelicId.PNL_BONUS_120 -> tradePnl = 1.2f
                RelicId.PNL_BONUS_150 -> tradePnl = 1.5f
                else -> Unit
            }
            when (activeIn(RelicUpgradeGroup.BONUS_10)) {
                RelicId.BONUS_10_150 -> tenClear = 1.5f
                RelicId.BONUS_10_200 -> tenClear = 2.0f
                else -> Unit
            }
            when (activeIn(RelicUpgradeGroup.COOLDOWN)) {
                RelicId.COOLDOWN_25 -> {
                    cooldown = 0.75f
                    cool = CoolFxTier.I
                }
                RelicId.COOLDOWN_50 -> {
                    cooldown = 0.50f
                    cool = CoolFxTier.II
                }
                else -> Unit
            }
            when (activeIn(RelicUpgradeGroup.LAG)) {
                RelicId.LAG_25 -> {
                    lag = 0.75f
                    lagFx = LagFxTier.I
                }
                RelicId.LAG_50 -> {
                    lag = 0.50f
                    lagFx = LagFxTier.II
                }
                else -> Unit
            }

            // MISS系は累積
            var maxHealthBonus = 0
            var comboHealEvery = 0
            if (RelicId.MISS_LIFE_1 in claimed) maxHealthBonus = 1
            if (RelicId.MISS_COMBO_HEAL in claimed) comboHealEvery = 3

            return RelicEffects(
                interestMult = interest,
                tradePnlMult = tradePnl,
                tenClearBonusMult = tenClear,
                cooldownFactor = cooldown,
                lagFactor = lag,
                maxHealthBonus = maxHealthBonus,
                comboHealEvery = comboHealEvery,
                judgeFx = if (RelicId.JUDGE in claimed) JudgeFxTier.ON else JudgeFxTier.NONE,
                comboFx = if (RelicId.COMBO in claimed) ComboFxTier.ON else ComboFxTier.NONE,
                coolFx = cool,
                lagFx = lagFx,
                paperUnlocked = RelicId.PAPER in claimed,
                darkUnlocked = RelicId.DARK in claimed,
                smoothUnlocked = RelicId.SMOOTH in claimed,
            )
        }
    }
}
