package com.urikai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.ChartFrame
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.ResultRegretMoment
import com.urikai.app.game.TimingFocus
import com.urikai.app.game.TradeTimingEvaluator
import com.urikai.app.game.UiStrings
import com.urikai.app.game.toTimingFocus
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont

@Composable
fun ResultChartPeekFooter(
    frame: ChartFrame,
    focusTarget: TimingFocus?,
    onHighlightClick: (ResultRegretMoment) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val highlights = TradeTimingEvaluator.pickReviewHighlights(
        priceRatios = frame.priceRatios,
        startOffsetSeconds = frame.startOffsetSeconds,
        markers = frame.markers
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LcdBackground.copy(alpha = 0.96f))
            .lcdBorder()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(UiStrings.chartLegendBuy(lang), color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = PixelFont)
            Text(UiStrings.chartLegendSell(lang), color = InkMuted, fontSize = 11.sp, fontFamily = PixelFont)
            Text(UiStrings.chartLegendTimingNice(lang), color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = PixelFont)
            Text(UiStrings.chartLegendTimingMiss(lang), color = InkMuted, fontSize = 11.sp, fontFamily = PixelFont)
        }

        highlights.forEach { moment ->
            TimingHighlightLine(
                moment = moment,
                lang = lang,
                selected = focusTarget == moment.toTimingFocus(),
                onClick = { onHighlightClick(moment) }
            )
        }
    }
}

@Composable
private fun TimingHighlightLine(
    moment: ResultRegretMoment,
    lang: DisplayLanguage,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val positive = UiStrings.resultRegretIsPositive(moment.kind)
    val text = UiStrings.resultRegretHeadline(moment, lang)
    val uiSound = LocalUiSound.current
    val frameMod = modifier
        .fillMaxWidth()
        .highlightLineFrame(selected)
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = {
                uiSound.play(UiSoundKind.SELECT)
                onClick()
            }
        )

    if (positive) {
        LcdInvertedPanel(modifier = frameMod) {
            Text(
                text = text,
                color = LcdInvertedText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
    } else {
        Text(
            text = text,
            color = if (selected) Ink else InkMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = PixelFont,
            textAlign = TextAlign.Center,
            modifier = frameMod
                .background(LcdBackground)
                .padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

/** 選択時も二重枠と同じ占有サイズを保ち、グラフ領域のレイアウトシフトを防ぐ。 */
@Composable
private fun Modifier.highlightLineFrame(selected: Boolean): Modifier {
    val outer = if (selected) Ink else Color.Transparent
    val inner = if (selected) Ink else InkMuted
    return border(2.dp, outer, RectangleShape)
        .padding(3.dp)
        .border(1.dp, inner, RectangleShape)
}
