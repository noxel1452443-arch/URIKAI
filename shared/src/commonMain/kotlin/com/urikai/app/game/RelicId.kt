package com.urikai.app.game

/**
 * 通算閾値で解禁→受け取り→報酬箱に残るレリック。
 * 上位段階は下位効果を上書きする（グループ内の選択で切替可）。
 *
 * @param threshold 最高残高が初期額からこの額以上増えたときに解禁
 * （UI では口座残高 = 初期 + threshold として表示）
 */
enum class RelicId(val threshold: Int, val kind: RelicKind) {
    // 閾値は最高残高−初期額。すみか(g)導入に合わせ外延ばし（SUMIKA_DESIGN §8.2）
    PAPER(4_000, RelicKind.VISUAL),
    INTEREST_120(8_000, RelicKind.EARN),
    SMOOTH(13_000, RelicKind.VISUAL),
    JUDGE(19_000, RelicKind.FX),
    BONUS_10_150(26_000, RelicKind.EARN),
    COMBO(35_000, RelicKind.FX),
    COOLDOWN_25(46_000, RelicKind.EARN),
    PNL_BONUS_120(60_000, RelicKind.EARN),
    DARK(78_000, RelicKind.VISUAL),
    MISS_LIFE_1(100_000, RelicKind.EARN),
    LAG_25(130_000, RelicKind.EARN),
    INTEREST_150(170_000, RelicKind.EARN),
    MISS_COMBO_HEAL(220_000, RelicKind.EARN),
    COOLDOWN_50(290_000, RelicKind.EARN),
    /** 効果は 1.3×（旧 1.5× を緩和）。名前はセーブ互換のため維持 */
    PNL_BONUS_150(380_000, RelicKind.EARN),
    BONUS_10_200(500_000, RelicKind.EARN),
    LAG_50(650_000, RelicKind.EARN);

    companion object {
        val ordered: List<RelicId> = entries.sortedBy { it.threshold }

        fun newlyUnlocked(previousPnl: Int, nextPnl: Int): List<RelicId> {
            if (nextPnl <= previousPnl) return emptyList()
            return ordered.filter { previousPnl < it.threshold && nextPnl >= it.threshold }
        }

        /** 最高残高から次のレリック到達口座残高。全部到達なら null。 */
        fun nextBalanceTarget(peakBalance: Long, initialBalance: Long = 10_000L): Long? {
            val pnl = (peakBalance - initialBalance).coerceAtLeast(0L)
            val next = ordered.firstOrNull { it.threshold > pnl } ?: return null
            return initialBalance + next.threshold
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

/**
 * UI上の置き場。内部IDは互換のため RelicId のまま、表示はカテゴリで分ける。
 * - MILESTONE: 口座画面の進行表示のみ（実体は各カテゴリの解禁）
 * - LOOK: 見た目・演出ページ
 * - PLAY: ホームの幾何アイコン
 * - INVEST: 運用方針の近く
 */
enum class UnlockPlacement {
    LOOK,
    PLAY,
    INVEST,
}

fun RelicId.placement(): UnlockPlacement = when (this) {
    RelicId.PAPER, RelicId.SMOOTH, RelicId.DARK,
    RelicId.JUDGE, RelicId.COMBO -> UnlockPlacement.LOOK
    RelicId.INTEREST_120, RelicId.INTEREST_150,
    RelicId.BONUS_10_150, RelicId.BONUS_10_200 -> UnlockPlacement.INVEST
    else -> UnlockPlacement.PLAY
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
    /** 運用プランの平均利回りに掛ける（ぶれ幅は変えない） */
    val planYieldMult: Float = 1f,
    /** 留守の間に反映する最大日数 */
    val maxAwayDays: Int = 30,
    val tradePnlMult: Float = 1f,
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
            disabled: Set<RelicId> = emptySet(),
            disabledGroups: Set<RelicUpgradeGroup> = emptySet(),
        ): RelicEffects {
            val activeClaimed = claimed - disabled

            fun activeIn(group: RelicUpgradeGroup): RelicId? {
                if (group in disabledGroups) return null
                val owned = group.members.filter { it in activeClaimed }
                if (owned.isEmpty()) return null
                if (!group.exclusiveEffect) return null
                val pref = stagePrefs[group]
                return if (pref != null && pref in owned) pref else owned.maxBy { it.threshold }
            }

            var planYield = 1f
            var awayDays = 30
            var tradePnl = 1f
            var cooldown = 1f
            var lag = 1f
            var cool = CoolFxTier.NONE
            var lagFx = LagFxTier.NONE

            when (activeIn(RelicUpgradeGroup.INTEREST)) {
                RelicId.INTEREST_120 -> planYield = 1.2f
                RelicId.INTEREST_150 -> planYield = 1.5f
                else -> Unit
            }
            when (activeIn(RelicUpgradeGroup.PNL)) {
                RelicId.PNL_BONUS_120 -> tradePnl = 1.2f
                RelicId.PNL_BONUS_150 -> tradePnl = 1.3f
                else -> Unit
            }
            when (activeIn(RelicUpgradeGroup.BONUS_10)) {
                RelicId.BONUS_10_150 -> awayDays = 45
                RelicId.BONUS_10_200 -> awayDays = 60
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

            var maxHealthBonus = 0
            var comboHealEvery = 0
            if (RelicId.MISS_LIFE_1 in activeClaimed) maxHealthBonus = 1
            if (RelicId.MISS_COMBO_HEAL in activeClaimed) comboHealEvery = 3

            return RelicEffects(
                planYieldMult = planYield,
                maxAwayDays = awayDays,
                tradePnlMult = tradePnl,
                cooldownFactor = cooldown,
                lagFactor = lag,
                maxHealthBonus = maxHealthBonus,
                comboHealEvery = comboHealEvery,
                judgeFx = if (RelicId.JUDGE in activeClaimed) JudgeFxTier.ON else JudgeFxTier.NONE,
                comboFx = if (RelicId.COMBO in activeClaimed) ComboFxTier.ON else ComboFxTier.NONE,
                coolFx = cool,
                lagFx = lagFx,
                paperUnlocked = RelicId.PAPER in claimed,
                darkUnlocked = RelicId.DARK in claimed,
                smoothUnlocked = RelicId.SMOOTH in claimed,
            )
        }
    }
}

/** タイトルカード裏面などで扱うプレイ強化スロット。 */
enum class PlayBuffSlot(
    val group: RelicUpgradeGroup?,
    val single: RelicId?,
) {
    COOLDOWN(RelicUpgradeGroup.COOLDOWN, null),
    LAG(RelicUpgradeGroup.LAG, null),
    PNL(RelicUpgradeGroup.PNL, null),
    LIFE(null, RelicId.MISS_LIFE_1),
    COMBO_HEAL(null, RelicId.MISS_COMBO_HEAL);

    fun ownedIn(claimed: Set<RelicId>): List<RelicId> = when {
        group != null -> group.members.filter { it in claimed }
        single != null && single in claimed -> listOf(single)
        else -> emptyList()
    }

    fun activeId(
        claimed: Set<RelicId>,
        stagePrefs: Map<RelicUpgradeGroup, RelicId>,
        disabledGroups: Set<RelicUpgradeGroup>,
        disabledRelics: Set<RelicId>,
    ): RelicId? {
        val owned = ownedIn(claimed)
        if (owned.isEmpty()) return null
        val g = group
        if (g != null) {
            if (g in disabledGroups) return null
            val pref = stagePrefs[g]?.takeIf { it in owned }
            return pref ?: owned.maxBy { it.threshold }
        }
        val s = single ?: return null
        return if (s in disabledRelics) null else s
    }

    fun tierOf(active: RelicId?): Int = when (active) {
        RelicId.COOLDOWN_50, RelicId.LAG_50, RelicId.PNL_BONUS_150 -> 2
        RelicId.COOLDOWN_25, RelicId.LAG_25, RelicId.PNL_BONUS_120,
        RelicId.MISS_LIFE_1, RelicId.MISS_COMBO_HEAL -> 1
        else -> 0
    }
}
