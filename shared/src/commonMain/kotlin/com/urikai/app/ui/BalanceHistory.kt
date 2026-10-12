package com.urikai.app.ui

import com.urikai.app.game.fmt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.data.BalanceEventKind
import com.urikai.app.data.BalancePoint
import com.urikai.app.data.DailyAccrualResult
import com.urikai.app.data.LifetimePnlRepository
import com.urikai.app.game.InvestPlan
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont

/** 運用プランの選択。選択中は反転、未解放は解放条件を出す。 */
@Composable
fun InvestPlanPicker(
    selected: InvestPlan,
    peakBalance: Long,
    onSelect: (InvestPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = UiStrings.investPlanTitle(lang),
            color = InkMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont
        )
        InvestPlan.entries.forEach { plan ->
            val unlocked = plan.isUnlocked(peakBalance)
            val active = plan == selected
            val fg = when {
                active -> LcdInvertedText
                unlocked -> Ink
                else -> InkFaint
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (active) Modifier.background(Ink) else Modifier.lcdBorder(if (unlocked) Ink else InkFaint))
                    .uiClickable(
                        kind = UiSoundKind.SELECT,
                        enabled = unlocked && !active,
                        onClick = { onSelect(plan) }
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.displayName(lang),
                    color = fg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelFont,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (unlocked) {
                        UiStrings.investPlanRate(plan, lang)
                    } else {
                        UiStrings.investPlanLocked(plan.unlockPeakBalance, lang)
                    },
                    color = fg,
                    fontSize = 10.sp,
                    fontFamily = PixelFont
                )
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
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val initial = LifetimePnlRepository.INITIAL_BALANCE
    val returnPercent = (balance - initial) * 100f / initial
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¥%,d".fmt(balance),
            color = Ink,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont
        )
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
