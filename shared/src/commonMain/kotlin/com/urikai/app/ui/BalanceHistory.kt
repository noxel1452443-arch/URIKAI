package com.urikai.app.ui

import com.urikai.app.game.fmt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.data.BalanceEventKind
import com.urikai.app.data.BalancePoint
import com.urikai.app.data.DailyAccrualResult
import com.urikai.app.data.LifetimePnlRepository
import com.urikai.app.game.InvestPlan
import com.urikai.app.game.UiStrings
import com.urikai.app.res.Res
import com.urikai.app.res.allDrawableResources
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource

/** 全プラン共通のカード高さ（文言差でサイズが変わらないようにする） */
private val InvestPlanCardHeight = 136.dp

/**
 * 運用方針カード。1枚表示＋左右で閲覧、下部で選択。
 * カード面: 商品名＋ぶれタグ＋フレーバー／挙動の2文。重複ラベルは出さない。
 */
@Composable
fun InvestPlanPicker(
    selected: InvestPlan,
    peakBalance: Long,
    onSelect: (InvestPlan) -> Unit,
    planYieldMult: Float = 1f,
    maxAwayDays: Int = 30,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val plans = InvestPlan.entries
    var browseIndex by remember {
        mutableIntStateOf(plans.indexOf(selected).coerceAtLeast(0))
    }
    LaunchedEffect(selected) {
        browseIndex = plans.indexOf(selected).coerceAtLeast(0)
    }
    val plan = plans[browseIndex]
    val unlocked = plan.isUnlocked(peakBalance)
    val isSelected = plan == selected
    val canPrev = plans.size > 1
    val canNext = plans.size > 1

    var tipText by remember { mutableStateOf<String?>(null) }
    Box(modifier = modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = UiStrings.investPlanTitle(lang),
                color = InkMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier.weight(1f)
            )
            InvestStatChip(
                label = UiStrings.investYieldChip(planYieldMult, lang),
                onClick = { tipText = UiStrings.investYieldTip(planYieldMult, lang) }
            )
            InvestStatChip(
                label = UiStrings.investAwayChip(maxAwayDays, lang),
                onClick = { tipText = UiStrings.investAwayTip(maxAwayDays, lang) }
            )
            InvestStatChip(
                label = UiStrings.investPlanDetailChip(lang),
                onClick = { tipText = UiStrings.investPlanBlurb(plan, lang) }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(InvestPlanCardHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "‹",
                color = if (canPrev) Ink else InkFaint,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .size(36.dp)
                    .lcdBorder(if (canPrev) Ink else InkFaint)
                    .uiClickable(
                        kind = UiSoundKind.SELECT,
                        enabled = canPrev,
                        onClick = {
                            browseIndex = (browseIndex - 1 + plans.size) % plans.size
                        }
                    )
                    .padding(top = 2.dp),
            )
            InvestPlanCard(
                plan = plan,
                unlocked = unlocked,
                selected = isSelected,
                onSelect = { onSelect(plan) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            Text(
                text = "›",
                color = if (canNext) Ink else InkFaint,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .size(36.dp)
                    .lcdBorder(if (canNext) Ink else InkFaint)
                    .uiClickable(
                        kind = UiSoundKind.SELECT,
                        enabled = canNext,
                        onClick = {
                            browseIndex = (browseIndex + 1) % plans.size
                        }
                    )
                    .padding(top = 2.dp),
            )
        }
    }
        val tip = tipText
        if (tip != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Ink.copy(alpha = 0.35f))
                    .clickable { tipText = null },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tip,
                    color = Ink,
                    fontSize = 11.sp,
                    fontFamily = PixelFont,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .lcdDoubleBorder()
                        .background(LcdBackground)
                        .clickable(enabled = false, onClick = {})
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun InvestStatChip(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Ink,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        modifier = Modifier
            .lcdBorder()
            .uiClickable(kind = UiSoundKind.SELECT, onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}

@Composable
private fun InvestPlanCard(
    plan: InvestPlan,
    unlocked: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val borderInk = if (unlocked) Ink else InkFaint
    val motifInk = when {
        unlocked -> Ink.copy(alpha = if (selected) 0.22f else 0.16f)
        else -> InkFaint.copy(alpha = 0.4f)
    }
    val watermarkInk = when {
        unlocked -> Ink.copy(alpha = if (selected) 0.07f else 0.1f)
        else -> InkFaint.copy(alpha = 0.22f)
    }
    val fg = if (unlocked) Ink else InkFaint
    val muted = if (unlocked) InkMuted else InkFaint
    val selectedWash = Ink.copy(alpha = 0.06f)
    val canSelect = unlocked && !selected
    Box(
        modifier = modifier
            .clipToBounds()
            .background(LcdBackground)
            .investPlanCardBorder(plan, borderInk)
            .then(
                if (selected && unlocked) {
                    Modifier.drawBehind {
                        drawRect(selectedWash)
                    }
                } else {
                    Modifier
                }
            )
            .drawBehind {
                drawInvestPlanMotif(plan, motifInk, watermarkInk)
            }
            .uiClickable(
                kind = UiSoundKind.CONFIRM,
                enabled = canSelect,
                onClick = onSelect
            )
            .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 4.dp)
    ) {
        // 文言は全幅。イラストは右下に小さく重ね、1文目の行幅を奪わない。
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = plan.displayName(lang),
                        color = fg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (unlocked) {
                        Text(
                            text = UiStrings.investPlanTemperTag(plan, lang),
                            color = if (selected) LcdInvertedText else fg,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelFont,
                            maxLines = 1,
                            modifier = Modifier
                                .then(
                                    if (selected) {
                                        Modifier.background(Ink)
                                    } else {
                                        Modifier.border(1.dp, fg, RectangleShape)
                                    }
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                if (unlocked) {
                    Text(
                        text = UiStrings.investPlanFlavor(plan, lang),
                        color = fg,
                        fontSize = 12.sp,
                        fontFamily = PixelFont,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    )
                    // 2文目は右下イラストと重ならないよう右に余白
                    Text(
                        text = UiStrings.investPlanCatch(plan, lang),
                        color = muted,
                        fontSize = 10.sp,
                        fontFamily = PixelFont,
                        lineHeight = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 44.dp)
                    )
                } else {
                    Text(
                        text = UiStrings.investPlanLocked(plan.unlockPeakBalance, lang),
                        color = muted,
                        fontSize = 10.sp,
                        fontFamily = PixelFont,
                        lineHeight = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, end = 44.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // カード内フッタで選択状態を区別（別ボタンは置かない）
                Text(
                    text = when {
                        selected -> UiStrings.investPlanSelected(lang)
                        unlocked -> UiStrings.investPlanSelect(lang)
                        else -> UiStrings.investPlanLocked(plan.unlockPeakBalance, lang)
                    },
                    color = when {
                        selected -> LcdInvertedText
                        unlocked -> fg
                        else -> muted
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            when {
                                selected -> Modifier.background(Ink)
                                unlocked -> Modifier.border(1.dp, fg, RectangleShape)
                                else -> Modifier.border(1.dp, InkFaint, RectangleShape)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
            InvestPlanArt(
                plan = plan,
                tint = fg,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 22.dp)
                    .size(36.dp)
            )
        }
    }
}

private fun planArtRes(plan: InvestPlan): DrawableResource? {
    val key = when (plan) {
        InvestPlan.DEPOSIT -> "plan_deposit"
        InvestPlan.BALANCED -> "plan_balanced"
        InvestPlan.GROWTH -> "plan_growth"
        InvestPlan.EMERGING -> "plan_emerging"
        InvestPlan.ACORN -> "plan_acorn"
    }
    return Res.allDrawableResources[key]
}

/** 生成イラスト（アルファマスク）をテーマ色で着色。 */
@Composable
private fun InvestPlanArt(
    plan: InvestPlan,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val res = planArtRes(plan)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (res != null) {
            Image(
                bitmap = imageResource(res),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                colorFilter = ColorFilter.tint(tint),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * プランごとの枠。モチーフ:
 * 預金=通帳／バランス=目論見書の二重線／グロース=証券の太枠／
 * 新興=スタンプ破線／ドングリ=硬貨の二重厚枠。
 */
@Composable
private fun Modifier.investPlanCardBorder(plan: InvestPlan, color: Color): Modifier = when (plan) {
    InvestPlan.DEPOSIT -> border(1.dp, color, RectangleShape)
    InvestPlan.BALANCED -> this
        .border(1.dp, color, RectangleShape)
        .padding(2.dp)
        .border(1.dp, color.copy(alpha = 0.45f), RectangleShape)
    InvestPlan.GROWTH -> border(2.dp, color, RectangleShape)
    InvestPlan.EMERGING -> this
        .border(1.dp, Color.Transparent, RectangleShape)
        .drawBehind {
            val inset = 2.dp.toPx()
            val stroke = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )
            drawRect(
                color = color,
                topLeft = Offset(inset, inset),
                size = Size(
                    (size.width - inset * 2).coerceAtLeast(0f),
                    (size.height - inset * 2).coerceAtLeast(0f)
                ),
                style = stroke
            )
        }
    InvestPlan.ACORN -> this
        .border(2.dp, color, RectangleShape)
        .padding(2.dp)
        .border(1.dp, color, RectangleShape)
}

/**
 * 左帯モチーフ＋薄い透かし。
 * 通帳背表紙／円グラフ／上昇バー／波／コイン格子。
 */
private fun DrawScope.drawInvestPlanMotif(
    plan: InvestPlan,
    motif: Color,
    watermark: Color
) {
    val w = size.width
    val h = size.height
    val band = 18.dp.toPx()

    // 左帯（証書の背・通帳の綴じ）
    drawRect(motif.copy(alpha = motif.alpha * 1.4f), size = Size(band, h))
    drawLine(
        color = motif.copy(alpha = (motif.alpha * 2f).coerceAtMost(0.55f)),
        start = Offset(band, 0f),
        end = Offset(band, h),
        strokeWidth = 1.2f
    )

    when (plan) {
        InvestPlan.DEPOSIT -> {
            // 通帳: 綴じ穴＋横罫
            val holeX = band * 0.45f
            for (i in 1..4) {
                val y = h * i / 5f
                drawCircle(watermark, radius = 2.2f, center = Offset(holeX, y))
                drawCircle(
                    motif.copy(alpha = motif.alpha * 0.8f),
                    radius = 1.1f,
                    center = Offset(holeX, y),
                    style = Stroke(width = 1f)
                )
            }
            var y = 14f
            while (y < h - 8f) {
                drawLine(
                    watermark,
                    Offset(band + 10f, y),
                    Offset(w - 10f, y),
                    strokeWidth = 1f
                )
                y += 11f
            }
            // 小さな銀行印
            val cx = w - 22f
            val cy = h - 20f
            drawCircle(motif, radius = 9f, center = Offset(cx, cy), style = Stroke(width = 1.2f))
            drawCircle(watermark, radius = 5f, center = Offset(cx, cy))
        }
        InvestPlan.BALANCED -> {
            // 投信: 簡易パイ（3分割の弧）
            val cx = band * 0.5f
            val cy = h * 0.5f
            val r = (band * 0.32f).coerceAtMost(h * 0.18f)
            drawCircle(watermark, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.4f))
            drawLine(motif, Offset(cx, cy), Offset(cx, cy - r), strokeWidth = 1.2f)
            drawLine(motif, Offset(cx, cy), Offset(cx + r * 0.85f, cy + r * 0.5f), strokeWidth = 1.2f)
            drawLine(motif, Offset(cx, cy), Offset(cx - r * 0.85f, cy + r * 0.5f), strokeWidth = 1.2f)
            // 本文側の薄い同心円
            val wx = w * 0.72f
            val wy = h * 0.55f
            drawCircle(watermark, radius = h * 0.28f, center = Offset(wx, wy), style = Stroke(width = 1f))
            drawCircle(watermark, radius = h * 0.16f, center = Offset(wx, wy), style = Stroke(width = 1f))
        }
        InvestPlan.GROWTH -> {
            // 証券: 上昇バー＋右上がり
            val barBase = h - 10f
            val bars = floatArrayOf(0.25f, 0.4f, 0.55f, 0.75f, 0.95f)
            val gap = (band - 6f) / bars.size
            bars.forEachIndexed { i, t ->
                val bh = (h - 20f) * t
                val x = 3f + i * gap
                drawRect(
                    motif,
                    topLeft = Offset(x, barBase - bh),
                    size = Size((gap * 0.65f).coerceAtLeast(2f), bh)
                )
            }
            val path = Path().apply {
                moveTo(band + 12f, h * 0.72f)
                lineTo(band + (w - band) * 0.35f, h * 0.55f)
                lineTo(band + (w - band) * 0.55f, h * 0.6f)
                lineTo(w - 48f, h * 0.28f)
            }
            drawPath(path, watermark, style = Stroke(width = 1.6f, join = StrokeJoin.Round))
        }
        InvestPlan.EMERGING -> {
            // 遠方相場: 帯にジグザグ、本文に波
            val zig = Path().apply {
                var x = 2f
                var up = true
                moveTo(x, h * 0.55f)
                while (x < band - 2f) {
                    x += 4f
                    lineTo(x, if (up) h * 0.28f else h * 0.72f)
                    up = !up
                }
            }
            drawPath(zig, motif, style = Stroke(width = 1.4f))
            val wave = Path().apply {
                moveTo(band + 8f, h * 0.5f)
                var x = band + 8f
                var phase = 0
                while (x < w - 40f) {
                    x += 14f
                    val y = h * 0.5f + if (phase % 2 == 0) -h * 0.12f else h * 0.12f
                    lineTo(x, y)
                    phase++
                }
            }
            drawPath(wave, watermark, style = Stroke(width = 1.3f))
        }
        InvestPlan.ACORN -> {
            // コイン積立: 帯に硬貨、本文に格子
            val cx = band * 0.5f
            for (i in 0..2) {
                val cy = h * (0.22f + i * 0.28f)
                drawCircle(motif, radius = band * 0.28f, center = Offset(cx, cy), style = Stroke(width = 1.3f))
                drawLine(
                    watermark,
                    Offset(cx - band * 0.16f, cy),
                    Offset(cx + band * 0.16f, cy),
                    strokeWidth = 1f
                )
            }
            val step = 9f
            var x = band + 4f
            while (x < w) {
                drawLine(watermark, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < h) {
                drawLine(watermark, Offset(band, y), Offset(w, y), strokeWidth = 1f)
                y += step
            }
        }
    }
}

/** 留守の間に運用プランで動いた分。START で一度だけ。 */
@Composable
fun AwayReportOverlay(
    report: DailyAccrualResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val diff = report.after - report.before
    Box(
        modifier = modifier
            .background(Ink.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .lcdDoubleBorder()
                .background(LcdBackground)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GuideBuddyFace(
                    mood = if (diff >= 0) GuideBuddyMood.SMILE else GuideBuddyMood.THINK,
                    size = 52.dp
                )
                Text(
                    text = UiStrings.kaiAwayLine(diff, lang),
                    color = Ink,
                    fontSize = 12.sp,
                    fontFamily = PixelFont,
                    lineHeight = 17.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = UiStrings.awayReportTitle(report.days, lang),
                color = InkMuted,
                fontSize = 11.sp,
                fontFamily = PixelFont
            )
            Text(
                text = report.plan.displayName(lang),
                color = Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont
            )
            Text(
                text = UiStrings.formatSignedYen(diff),
                color = if (diff < 0) InkMuted else Ink,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont
            )
            Text(
                text = "¥%,d → ¥%,d".fmt(report.before, report.after),
                color = InkMuted,
                fontSize = 11.sp,
                fontFamily = PixelFont
            )
            Text(
                text = UiStrings.relicClose(lang),
                color = Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .lcdBorder()
                    .uiClickable(kind = UiSoundKind.SELECT, onClick = onDismiss)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

/** 現在残高・開始額からの増減率・最高到達額。 */
@Composable
fun BalanceSummary(
    balance: Long,
    peakBalance: Long,
    modifier: Modifier = Modifier,
    /** false なら金額は親見出し側。増減率と最高だけ出す。 */
    showAmount: Boolean = true
) {
    val lang = LocalDisplayLanguage.current
    val initial = LifetimePnlRepository.INITIAL_BALANCE
    val returnPercent = (balance - initial) * 100f / initial
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showAmount) {
            Text(
                text = "¥%,d".fmt(balance),
                color = Ink,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier.guideSpotlightTarget(GuideSpotlightTarget.BALANCE_AMOUNT)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = UiStrings.balanceFromStartLine(returnPercent, lang),
                color = Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont
            )
            Text(
                text = UiStrings.balancePeakLine(peakBalance, lang),
                color = InkMuted,
                fontSize = 11.sp,
                fontFamily = PixelFont
            )
        }
    }
}

/**
 * 銀行アプリ風の残高推移。横軸は出来事の順番（時刻の間隔は詰める）。
 * 点線は初期額 ¥10,000、白抜きの点は破産からの再スタート。
 */
@Composable
fun BalanceHistoryChart(
    history: List<BalancePoint>,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val ink = Ink
    val inkFaint = InkFaint
    val bg = LcdBackground
    val initial = LifetimePnlRepository.INITIAL_BALANCE
    if (history.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = UiStrings.balanceHistoryEmpty(lang),
                color = InkMuted,
                fontSize = 11.sp,
                fontFamily = PixelFont
            )
        }
        return
    }
    val values = history.map { it.balance }
    val hi = maxOf(values.max(), initial)
    val lo = minOf(values.min(), initial)
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val padTop = h * 0.14f
            val padBottom = h * 0.14f
            val span = (hi - lo).toFloat().takeIf { it > 0f } ?: 1f
            val last = values.size - 1
            fun xAt(i: Int) = w * i / last
            fun yAt(v: Long) = padTop + (h - padTop - padBottom) * (1f - (v - lo) / span)

            val baseY = yAt(initial)
            drawLine(
                color = inkFaint,
                start = Offset(0f, baseY),
                end = Offset(w, baseY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
            )

            val line = Path().apply {
                moveTo(xAt(0), yAt(values[0]))
                for (i in 1..last) lineTo(xAt(i), yAt(values[i]))
            }
            val area = Path().apply {
                addPath(line)
                lineTo(xAt(last), h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = area, color = ink.copy(alpha = 0.12f))
            drawPath(
                path = line,
                color = ink,
                style = Stroke(width = 2.5f, join = StrokeJoin.Round)
            )
            history.forEachIndexed { i, point ->
                if (point.kind == BalanceEventKind.RESTART) {
                    val c = Offset(xAt(i), yAt(point.balance))
                    drawCircle(color = bg, radius = 5f, center = c)
                    drawCircle(color = ink, radius = 5f, center = c, style = Stroke(width = 2f))
                }
            }
            drawCircle(color = ink, radius = 5f, center = Offset(xAt(last), yAt(values[last])))
        }
        Text(
            text = "¥%,d".fmt(hi),
            color = InkMuted,
            fontSize = 9.sp,
            fontFamily = PixelFont,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Text(
            text = "¥%,d".fmt(lo),
            color = InkMuted,
            fontSize = 9.sp,
            fontFamily = PixelFont,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}
