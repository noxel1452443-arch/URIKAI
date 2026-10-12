package com.urikai.app.ui

import com.urikai.app.game.fmt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.urikai.app.data.AssetRankBoard
import com.urikai.app.game.ChartLineStyle
import com.urikai.app.game.LcdThemeMode
import com.urikai.app.game.RelicFrameStyle
import com.urikai.app.game.RelicId
import com.urikai.app.game.RelicKind
import com.urikai.app.game.RelicUpgradeGroup
import com.urikai.app.game.SyncRating
import com.urikai.app.game.UiStrings
import com.urikai.app.game.frameStyle
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.draw.drawWithContent
import kotlin.math.sin

/** 報酬箱の1行（単独 or 段階グループ）。 */
private sealed class RelicBoxItem {
    data class Single(val id: RelicId) : RelicBoxItem()
    data class Group(
        val group: RelicUpgradeGroup,
        val owned: List<RelicId>,
        val viewing: RelicId
    ) : RelicBoxItem()
}

private fun buildRelicBoxItems(
    claimed: List<RelicId>,
    stagePrefs: Map<RelicUpgradeGroup, RelicId>
): List<RelicBoxItem> {
    val claimedSet = claimed.toSet()
    val seenGroups = mutableSetOf<RelicUpgradeGroup>()
    val items = mutableListOf<RelicBoxItem>()
    for (id in claimed) {
        val group = RelicUpgradeGroup.of(id)
        if (group != null) {
            if (group in seenGroups) continue
            seenGroups.add(group)
            val owned = group.members.filter { it in claimedSet }
            if (owned.isEmpty()) continue
            if (owned.size == 1) {
                items.add(RelicBoxItem.Single(owned.first()))
            } else {
                val viewing = stagePrefs[group]?.takeIf { it in owned }
                    ?: owned.maxBy { it.threshold }
                items.add(RelicBoxItem.Group(group, owned, viewing))
            }
        } else {
            items.add(RelicBoxItem.Single(id))
        }
    }
    return items
}

/**
 * リザルト導入後の短い獲得ポップ。受取自体は START 右下のまま。
 */
@Composable
fun RelicRewardToast(
    count: Int,
    onDismiss: () -> Unit,
    onAppear: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (count <= 0) return
    val lang = LocalDisplayLanguage.current
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.62f) }
    val sparkle = rememberInfiniteTransition(label = "relic_reward_sparkle")
    val sparkleAlpha by sparkle.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "relic_reward_sparkle_alpha"
    )

    LaunchedEffect(count) {
        onAppear()
        alpha.snapTo(0f)
        scale.snapTo(0.62f)
        launch {
            alpha.animateTo(1f, tween(120))
        }
        scale.animateTo(1.14f, tween(180, easing = FastOutSlowInEasing))
        scale.animateTo(0.96f, tween(90))
        scale.animateTo(1.04f, tween(90, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(110))
        delay(2_600)
        alpha.animateTo(0f, tween(220))
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.42f * alpha.value))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .graphicsLayer {
                    this.alpha = alpha.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(horizontal = 18.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "✦  ✦  ✦",
                color = Ink,
                fontSize = 14.sp,
                fontFamily = PixelFont,
                modifier = Modifier.alpha(sparkleAlpha)
            )
            Text(
                text = UiStrings.relicRewardToastTitle(lang),
                color = Ink,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center
            )
            Text(
                text = UiStrings.relicRewardToastBody(lang),
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            if (count > 1) {
                Text(
                    text = "×$count",
                    color = InkMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont
                )
            }
            Text(
                text = "✦  ✦  ✦",
                color = Ink,
                fontSize = 14.sp,
                fontFamily = PixelFont,
                modifier = Modifier.alpha(sparkleAlpha)
            )
        }
    }
}

@Composable
fun RelicGiftBadge(
    pendingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (pendingCount <= 0) return
    val lang = LocalDisplayLanguage.current
    val blink = rememberInfiniteTransition(label = "relic_gift")
    val alpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "relic_gift_alpha"
    )
    LcdChromeChip(
        onClick = onClick,
        modifier = modifier.alpha(alpha)
    ) {
        Text(
            text = "${UiStrings.relicGiftBadge(lang)} ×$pendingCount",
            color = Ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            letterSpacing = 1.sp
        )
    }
}

/** 受け取り済みレリックが1つ以上あるときだけ表示。LCD色の宝箱（白背景なし）。 */
@Composable
fun RelicBoxButton(
    unlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!unlocked) return
    val ink = Ink
    val bg = LcdBackground
    LcdChromeChip(
        onClick = onClick,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val w = size.width
            val h = size.height
            drawRect(
                color = ink,
                topLeft = Offset(w * 0.12f, h * 0.52f),
                size = Size(w * 0.76f, h * 0.36f)
            )
            drawRect(
                color = bg,
                topLeft = Offset(w * 0.18f, h * 0.58f),
                size = Size(w * 0.64f, h * 0.24f)
            )
            drawRect(
                color = ink,
                topLeft = Offset(w * 0.10f, h * 0.22f),
                size = Size(w * 0.80f, h * 0.32f)
            )
            drawRect(
                color = bg,
                topLeft = Offset(w * 0.16f, h * 0.28f),
                size = Size(w * 0.68f, h * 0.18f)
            )
            drawRect(
                color = ink,
                topLeft = Offset(w * 0.44f, h * 0.42f),
                size = Size(w * 0.12f, h * 0.18f)
            )
            drawRect(
                color = bg,
                topLeft = Offset(w * 0.47f, h * 0.48f),
                size = Size(w * 0.06f, h * 0.06f)
            )
        }
    }
}

@Composable
fun RelicClaimOverlay(
    relic: RelicId,
    celebrating: Boolean,
    onReceive: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RelicGlyph(relic = relic, large = true)
            Text(
                text = UiStrings.relicName(relic, lang),
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center
            )
            Text(
                text = UiStrings.relicThresholdReached(relic.threshold, lang),
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center
            )
            Text(
                text = UiStrings.relicDescription(relic, lang),
                color = InkMuted,
                fontSize = 12.sp,
                fontFamily = PixelFont,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (celebrating) {
                Text(
                    text = UiStrings.relicGetBanner(lang),
                    color = Ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont
                )
            } else {
                Text(
                    text = UiStrings.relicReceive(lang),
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    modifier = Modifier
                        .lcdDoubleBorder()
                        .uiClickable(kind = UiSoundKind.CONFIRM, onClick = onReceive)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun RelicBoxOverlay(
    claimed: List<RelicId>,
    lcdTheme: LcdThemeMode,
    chartLineStyle: ChartLineStyle,
    stagePrefs: Map<RelicUpgradeGroup, RelicId> = emptyMap(),
    onToggleVisual: (RelicId) -> Unit,
    onCycleStage: (RelicUpgradeGroup) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val items = remember(claimed, stagePrefs) {
        buildRelicBoxItems(claimed, stagePrefs)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxSize(0.82f)
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(12.dp)
        ) {
            Text(
                text = UiStrings.relicBoxLabel(lang),
                color = Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    when (item) {
                        is RelicBoxItem.Single -> RelicBoxRow(
                            relic = item.id,
                            lcdTheme = lcdTheme,
                            chartLineStyle = chartLineStyle,
                            stageLabel = null,
                            onToggleVisual = { onToggleVisual(item.id) },
                            onCycleStage = null
                        )
                        is RelicBoxItem.Group -> RelicBoxRow(
                            relic = item.viewing,
                            lcdTheme = lcdTheme,
                            chartLineStyle = chartLineStyle,
                            stageLabel = "${item.owned.indexOf(item.viewing) + 1}/${item.owned.size}",
                            onToggleVisual = { onToggleVisual(item.viewing) },
                            onCycleStage = { onCycleStage(item.group) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = UiStrings.relicClose(lang),
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .lcdBorder()
                    .uiClickable(kind = UiSoundKind.SELECT, onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun RelicBoxRow(
    relic: RelicId,
    lcdTheme: LcdThemeMode,
    chartLineStyle: ChartLineStyle,
    stageLabel: String?,
    onToggleVisual: () -> Unit,
    onCycleStage: (() -> Unit)?
) {
    val lang = LocalDisplayLanguage.current
    val canToggle = relic.kind == RelicKind.VISUAL
    val isActive = when (relic) {
        RelicId.PAPER -> lcdTheme == LcdThemeMode.E_PAPER
        RelicId.DARK -> lcdTheme == LcdThemeMode.E_PAPER_INVERT
        RelicId.SMOOTH -> chartLineStyle == ChartLineStyle.SMOOTH
        else -> false
    }
    val actionLabel = when {
        onCycleStage != null -> UiStrings.relicCycleStage(lang)
        !canToggle -> null
        relic == RelicId.SMOOTH && isActive -> UiStrings.relicRevertToStep(lang)
        (relic == RelicId.PAPER || relic == RelicId.DARK) && isActive -> UiStrings.relicRevertToGb(lang)
        else -> UiStrings.relicApply(lang)
    }
    val actionClick = when {
        onCycleStage != null -> onCycleStage
        canToggle -> onToggleVisual
        else -> null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(relicFrameModifier(relic.frameStyle()))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RelicGlyph(relic = relic, large = false)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = UiStrings.relicName(relic, lang),
                    color = Ink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont
                )
                if (stageLabel != null) {
                    Text(
                        text = stageLabel,
                        color = InkMuted,
                        fontSize = 9.sp,
                        fontFamily = PixelFont
                    )
                }
            }
            Text(
                text = UiStrings.relicThresholdQuiet(relic.threshold, lang),
                color = InkFaint,
                fontSize = 9.sp,
                fontFamily = PixelFont
            )
            Text(
                text = UiStrings.relicDescription(relic, lang),
                color = InkMuted,
                fontSize = 10.sp,
                fontFamily = PixelFont,
                lineHeight = 13.sp
            )
        }
        // 適用／切替ボタンの有無で行レイアウトがズレないよう幅を確保
        Box(
            modifier = Modifier.width(100.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (actionLabel != null && actionClick != null) {
                Text(
                    text = actionLabel,
                    color = Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    modifier = Modifier
                        .lcdBorder()
                        .uiClickable(kind = UiSoundKind.SELECT, onClick = actionClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun relicFrameModifier(style: RelicFrameStyle): Modifier {
    val ink = Ink
    val bg = LcdBackground
    return when (style) {
        RelicFrameStyle.THEME -> {
            Modifier
                .border(1.dp, ink.copy(alpha = 0.55f), RectangleShape)
                .border(3.dp, ink.copy(alpha = 0.2f), RectangleShape)
                .padding(1.dp)
                .background(bg)
        }
        RelicFrameStyle.TIER1 -> {
            Modifier
                .lcdBorder()
                .background(bg)
        }
        RelicFrameStyle.TIER2 -> {
            val shimmer = rememberInfiniteTransition(label = "relic_tier2")
            val phase by shimmer.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "relic_tier2_phase"
            )
            Modifier
                .border(2.dp, ink, RectangleShape)
                .padding(2.dp)
                .border(1.dp, ink.copy(alpha = 0.45f), RectangleShape)
                .background(bg)
                .drawWithContent {
                    drawContent()
                    val w = size.width
                    val h = size.height
                    val x = -w * 0.3f + (w * 1.6f) * phase
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                ink.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            start = Offset(x, 0f),
                            end = Offset(x + w * 0.35f, h)
                        ),
                        size = size
                    )
                    // 角の金っぽい点滅
                    val spark = (0.35f + 0.65f * ((sin(phase * kotlin.math.PI * 2).toFloat() + 1f) / 2f))
                    drawCircle(
                        color = ink.copy(alpha = 0.55f * spark),
                        radius = 3.5f,
                        center = Offset(6f, 6f)
                    )
                    drawCircle(
                        color = ink.copy(alpha = 0.4f * spark),
                        radius = 2.5f,
                        center = Offset(w - 6f, h - 6f)
                    )
                }
        }
    }
}

/** レリックごとの簡易グリフ（ユニーク見え）。 */
@Composable
fun RelicGlyph(relic: RelicId, large: Boolean) {
    val mark = when (relic) {
        RelicId.PAPER -> "▥"
        RelicId.DARK -> "▦"
        RelicId.SMOOTH -> "∿"
        RelicId.INTEREST_120, RelicId.INTEREST_150 -> "%"
        RelicId.PNL_BONUS_120, RelicId.PNL_BONUS_150 -> "¥"
        RelicId.BONUS_10_150, RelicId.BONUS_10_200 -> "☾"
        RelicId.COOLDOWN_25, RelicId.COOLDOWN_50 -> "▷"
        RelicId.LAG_25, RelicId.LAG_50 -> "⇢"
        RelicId.JUDGE -> "◎"
        RelicId.COMBO -> "‡"
        RelicId.MISS_LIFE_1, RelicId.MISS_COMBO_HEAL -> "◇"
    }
    val frame = relic.frameStyle()
    val glyphMod = when {
        large && frame == RelicFrameStyle.TIER2 -> Modifier
            .border(3.dp, Ink, RectangleShape)
            .padding(2.dp)
            .border(2.dp, Ink.copy(alpha = 0.5f), RectangleShape)
        large -> Modifier.lcdDoubleBorder()
        frame == RelicFrameStyle.THEME -> Modifier
            .border(2.dp, Ink.copy(alpha = 0.7f), RectangleShape)
        else -> Modifier.lcdBorder()
    }
    Text(
        text = mark,
        color = Ink,
        fontSize = if (large) 36.sp else 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        modifier = Modifier
            .then(glyphMod)
            .padding(if (large) 12.dp else 6.dp)
    )
}

@Composable
fun AssetRankOverlay(
    board: AssetRankBoard,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = UiStrings.assetRankTitle(lang),
                color = Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont
            )
            board.top.forEach { entry ->
                AssetRankRow(entry = entry, outOfRank = false)
            }
            board.currentOutOfRank?.let { entry ->
                Spacer(modifier = Modifier.height(4.dp))
                AssetRankRow(entry = entry, outOfRank = true)
            }
            Text(
                text = UiStrings.relicClose(lang),
                color = Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier
                    .lcdBorder()
                    .uiClickable(kind = UiSoundKind.SELECT, onClick = onDismiss)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

/** リザルトの％★タップ用。この試合の★ごとの増減率の目安。 */
@Composable
fun SyncRatingBreakdownOverlay(
    finalAsset: Int,
    theoreticalMax: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val thresholds = SyncRating.starThresholdAssets(theoreticalMax)
    val starCount = SyncRating.stars(finalAsset, theoreticalMax)
    val over = SyncRating.isOverSync(finalAsset, theoreticalMax)
    fun pct(asset: Int): String = UiStrings.formatSignedPercent(SyncRating.returnPercent(asset))

    data class LadderRow(val label: String, val range: String, val active: Boolean)

    val rows = listOf(
        LadderRow("★1", "< ${pct(SyncRating.STAR2_FLOOR_ASSET)}", starCount == 1),
        LadderRow("★2", "≥ ${pct(thresholds.star2Min)}", starCount == 2),
        LadderRow("★3", "≥ ${pct(thresholds.star3Min)}", starCount == 3),
        LadderRow("★4", "≥ ${pct(thresholds.star4Min)}", starCount == 4),
        LadderRow("★5", "≥ ${pct(thresholds.star5Min)}", starCount == 5 && !over),
        LadderRow("★5+", "≥ ${pct(thresholds.star5PlusMin)}", over),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "%s  %s".fmt(
                    pct(finalAsset),
                    SyncRating.starsText(finalAsset, theoreticalMax)
                ),
                color = Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            rows.forEach { row ->
                val bg = if (row.active) Ink else Color.Transparent
                val fg = if (row.active) LcdInvertedText else Ink
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.label,
                        color = fg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        modifier = Modifier.width(48.dp)
                    )
                    Text(
                        text = row.range,
                        color = fg,
                        fontSize = 12.sp,
                        fontFamily = PixelFont,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Text(
                text = UiStrings.relicClose(lang),
                color = Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .lcdBorder()
                    .uiClickable(kind = UiSoundKind.SELECT, onClick = onDismiss)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

/** START の口座残高帯から開く。残高推移グラフ＋直近の勝負。 */
@Composable
fun BalanceHistoryOverlay(
    balance: Long,
    peakBalance: Long,
    investPlan: com.urikai.app.game.InvestPlan,
    onSelectInvestPlan: (com.urikai.app.game.InvestPlan) -> Unit,
    history: List<com.urikai.app.data.BalancePoint>,
    entries: List<com.urikai.app.data.SessionPnlHistoryEntry>,
    playCount: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxSize(0.92f)
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = UiStrings.balanceLabel(lang),
                    color = Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = UiStrings.totalPlayCountLine(playCount, lang),
                    color = InkMuted,
                    fontSize = 10.sp,
                    fontFamily = PixelFont,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            BalanceSummary(balance = balance, peakBalance = peakBalance)
            Spacer(modifier = Modifier.height(6.dp))
            BalanceHistoryChart(
                history = history,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .lcdBorder()
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            InvestPlanPicker(
                selected = investPlan,
                peakBalance = peakBalance,
                onSelect = onSelectInvestPlan
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = UiStrings.sessionPnlHistoryTitle(lang),
                color = InkMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = UiStrings.sessionPnlHistoryEmpty(lang),
                        color = InkMuted,
                        fontSize = 12.sp,
                        fontFamily = PixelFont,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entries.forEachIndexed { index, entry ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(if (index == 0) 1f else 0f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = if (index == 0) 10.dp else 0.dp)
                                    .lcdBorder()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = entry.brandName(lang),
                                    color = Ink,
                                    fontSize = 11.sp,
                                    fontFamily = PixelFont,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    maxLines = 2
                                )
                                Text(
                                    text = UiStrings.formatSignedYen(entry.pnl),
                                    color = if (entry.pnl < 0) InkFaint else Ink,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelFont
                                )
                            }
                            if (index == 0) {
                                Text(
                                    text = UiStrings.historyNewestMark(),
                                    color = LcdInvertedText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelFont,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = 1.dp)
                                        .background(Ink)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = UiStrings.relicClose(lang),
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .lcdBorder()
                    .uiClickable(kind = UiSoundKind.SELECT, onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun AssetRankRow(
    entry: com.urikai.app.data.AssetRankEntry,
    outOfRank: Boolean
) {
    val lang = LocalDisplayLanguage.current
    val highlight = entry.isCurrent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (highlight) {
                    Modifier
                        .background(Ink)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                } else {
                    Modifier
                        .lcdBorder()
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val fg = if (highlight) LcdInvertedText else Ink
        Text(
            text = if (outOfRank) {
                UiStrings.assetRankOutOfRank(lang)
            } else {
                "#${entry.rank}"
            },
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = UiStrings.formatSignedPercent(
                    com.urikai.app.game.SyncRating.returnPercent(entry.asset)
                ),
                color = fg,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont
            )
            Text(
                text = UiStrings.formatSignedYen(entry.pnlYen),
                color = if (highlight) fg else InkMuted,
                fontSize = 11.sp,
                fontFamily = PixelFont,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            text = if (highlight) UiStrings.assetRankCurrent(lang) else " ",
            color = fg,
            fontSize = 11.sp,
            fontFamily = PixelFont
        )
    }
}
