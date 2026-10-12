package com.urikai.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import com.urikai.app.res.Res
import com.urikai.app.res.allDrawableResources
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.data.ScrapbookEntry
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont

/** `composeResources/drawable/stage_<stageId>.png` があればそれ、なければ null。 */
private fun stageIllustrationRes(stageId: String): DrawableResource? =
    Res.allDrawableResources["stage_$stageId"]

/**
 * ステージのイラスト。画像はアルファのみのマスク（tools/make_stage_art.py）で、テーマのインク色に着色する。
 * 画像が未作成のステージは形のシルエットで代用する。
 */
@Composable
fun StageIllustration(
    stageId: String,
    macroShape: String,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
) {
    val res = stageIllustrationRes(stageId)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (res != null) {
            Image(
                bitmap = imageResource(res),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                colorFilter = ColorFilter.tint(if (locked) InkFaint else Ink),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            MacroShapeSilhouette(
                macroShape = if (locked) "" else macroShape,
                color = if (locked) InkFaint else InkMuted,
                strokeWidth = 3f,
                modifier = Modifier.fillMaxSize(0.7f)
            )
        }
    }
}

/**
 * @param newsLabel 初回クリア直後の「ニュース」表示ではラベル（NEW など）を出す
 */
@Composable
fun ScrapbookDetailOverlay(
    entry: ScrapbookEntry,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    newsLabel: String? = null,
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
                .fillMaxHeight(0.94f)
                .wrapContentHeight()
                .lcdDoubleBorder()
                .background(LcdBackground)
                .clickable(enabled = false, onClick = {})
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (newsLabel != null) {
                    Text(
                        text = newsLabel,
                        color = LcdBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        letterSpacing = 2.sp,
                        modifier = Modifier
                            .background(Ink)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                StageIllustration(
                    stageId = entry.stageId,
                    macroShape = entry.macroShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                        .lcdBorder()
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = UiStrings.instrumentWithCalendarRange(
                            entry.instrumentName,
                            entry.calendarStart,
                            entry.calendarEnd,
                            lang
                        ),
                        color = InkMuted,
                        fontSize = 10.sp,
                        fontFamily = PixelFont
                    )
                    Text(
                        text = entry.brandName,
                        color = Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        lineHeight = 20.sp
                    )
                }
                if (entry.flavorText.isNotBlank()) {
                    Text(
                        text = entry.flavorText,
                        color = Ink,
                        fontSize = 12.sp,
                        fontFamily = PixelFont,
                        lineHeight = 18.sp
                    )
                }
                StageShapeChart(
                    entry = entry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .lcdBorder()
                        .padding(6.dp)
                )
                if (entry.anchorWhen.isNotBlank()) {
                    Text(
                        text = "┆ ${entry.anchorWhen}",
                        color = InkMuted,
                        fontSize = 10.sp,
                        fontFamily = PixelFont
                    )
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

/** 図鑑詳細用の全体チャート（70秒）と出来事の位置。 */
@Composable
private fun StageShapeChart(
    entry: ScrapbookEntry,
    modifier: Modifier = Modifier
) {
    val ratios = entry.priceRatios
    val ink = Ink
    val inkFaint = InkFaint
    Canvas(modifier = modifier) {
        if (ratios.size < 2) return@Canvas
        val lo = ratios.min()
        val hi = ratios.max()
        val span = (hi - lo).takeIf { it > 0f } ?: 1f
        val w = size.width
        val h = size.height
        val last = ratios.size - 1
        fun xAt(i: Float) = w * i / last
        fun yAt(v: Float) = h - (v - lo) / span * h

        val markerX = xAt((entry.anchorBandStart + entry.anchorBandEnd) / 2f)
        drawLine(
            color = inkFaint,
            start = Offset(markerX, 0f),
            end = Offset(markerX, h),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))
        )

        val path = Path().apply {
            moveTo(xAt(0f), yAt(ratios[0]))
            for (i in 1..last) lineTo(xAt(i.toFloat()), yAt(ratios[i]))
        }
        drawPath(
            path = path,
            color = ink,
            style = Stroke(width = 2.5f, join = StrokeJoin.Round)
        )
        drawCircle(
            color = ink,
            radius = 4f,
            center = Offset(markerX, yAt(ratios[((entry.anchorBandStart + entry.anchorBandEnd) / 2).coerceIn(0, last)]))
        )
    }
}

@Composable
fun StageIllustrationThumb(
    entry: ScrapbookEntry,
    modifier: Modifier = Modifier
) {
    StageIllustration(
        stageId = entry.stageId,
        macroShape = entry.macroShape,
        modifier = modifier
            .size(64.dp, 48.dp)
            .lcdBorder(InkFaint)
    )
}
