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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.urikai.app.data.AssetRankBoard
import com.urikai.app.game.RelicFrameStyle
import com.urikai.app.game.RelicId
import com.urikai.app.game.SyncRating
import com.urikai.app.game.UiStrings
import com.urikai.app.game.frameStyle
import com.urikai.app.res.Res
import com.urikai.app.res.allDrawableResources
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource

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
                text = "◆  ◆  ◆",
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
                text = "◆  ◆  ◆",
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
            ClaimUnlockArt(relic = relic)
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
            Text(
                text = UiStrings.relicPlacementHint(relic, lang),
                color = InkFaint,
                fontSize = 10.sp,
                fontFamily = PixelFont,
                lineHeight = 14.sp,
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

/** 受取画面のビジュアル（PLAY=切手、他=簡易グリフ）。 */
@Composable
private fun ClaimUnlockArt(relic: RelicId) {
    val stampKey = claimStampArtKey(relic)
    val res = stampKey?.let { Res.allDrawableResources[it] }
    if (res != null) {
        val paper = LcdBackground
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.88f)
                    .background(paper)
            )
            Image(
                bitmap = imageResource(res),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                colorFilter = ColorFilter.tint(Ink),
                modifier = Modifier.fillMaxSize(0.92f)
            )
        }
    } else {
        RelicGlyph(relic = relic, large = true)
    }
}

private fun claimStampArtKey(relic: RelicId): String? = when (relic) {
    RelicId.COOLDOWN_25 -> "stamp_cooldown_t1"
    RelicId.COOLDOWN_50 -> "stamp_cooldown_t2"
    RelicId.LAG_25 -> "stamp_lag_t1"
    RelicId.LAG_50 -> "stamp_lag_t2"
    RelicId.PNL_BONUS_120 -> "stamp_pnl_t1"
    RelicId.PNL_BONUS_150 -> "stamp_pnl_t2"
    RelicId.MISS_LIFE_1 -> "stamp_life_t1"
    RelicId.MISS_COMBO_HEAL -> "stamp_combo_t1"
    else -> null
}

/** レリックごとの簡易グリフ（LOOK / INVEST 受取用）。 */
@Composable
fun RelicGlyph(relic: RelicId, large: Boolean) {
    val mark = when (relic) {
        RelicId.PAPER -> "□"
        RelicId.DARK -> "■"
        RelicId.SMOOTH -> "～"
        RelicId.INTEREST_120, RelicId.INTEREST_150 -> "%"
        RelicId.PNL_BONUS_120, RelicId.PNL_BONUS_150 -> "¥"
        RelicId.BONUS_10_150, RelicId.BONUS_10_200 -> "月"
        RelicId.COOLDOWN_25, RelicId.COOLDOWN_50 -> "▷"
        RelicId.LAG_25, RelicId.LAG_50 -> "→"
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
        LadderRow("★2", "≧ ${pct(thresholds.star2Min)}", starCount == 2),
        LadderRow("★3", "≧ ${pct(thresholds.star3Min)}", starCount == 3),
        LadderRow("★4", "≧ ${pct(thresholds.star4Min)}", starCount == 4),
        LadderRow("★5", "≧ ${pct(thresholds.star5Min)}", starCount == 5 && !over),
        LadderRow("★5+", "≧ ${pct(thresholds.star5PlusMin)}", over),
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
    claimedUnlocks: List<RelicId> = emptyList(),
    pendingUnlocks: List<RelicId> = emptyList(),
    planYieldMult: Float = 1f,
    maxAwayDays: Int = 30,
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
            // 縦が短い画面でも閉じるボタンが押し出されないよう、見出しと閉じるの間をまとめてスクロールさせる
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                BalanceSummary(balance = balance, peakBalance = peakBalance)
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .lcdBorder()
                ) {
                    AccountMilestoneStrip(
                        peakBalance = peakBalance,
                        claimed = claimedUnlocks.toSet(),
                        pending = pendingUnlocks.toSet()
                    )
                    BalanceHistoryChart(
                        history = history,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                InvestPlanPicker(
                    selected = investPlan,
                    peakBalance = peakBalance,
                    onSelect = onSelectInvestPlan,
                    planYieldMult = planYieldMult,
                    maxAwayDays = maxAwayDays
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = UiStrings.sessionPnlHistoryTitle(lang),
                    color = InkMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
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
