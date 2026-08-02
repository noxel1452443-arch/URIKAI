package com.urikai.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.urikai.app.game.ChartLineStyle
import com.urikai.app.game.HudMode
import com.urikai.app.game.LcdThemeMode
import com.urikai.app.game.PanelContrast
import com.urikai.app.game.ScanlineStrength
import com.urikai.app.game.SurfaceTone
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.InkMuted
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.PixelFont
import com.urikai.app.ui.theme.lcdColors
import com.urikai.app.ui.theme.paletteFor

@Composable
fun ConfigPreviewBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .width(72.dp)
            .height(40.dp)
            .lcdBorder(InkMuted)
            .background(LcdBackground)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun HudModePreview(hudMode: HudMode, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(72.dp)
            .height(40.dp)
            .lcdBorder(InkMuted)
            .background(LcdBackground)
            .padding(horizontal = 4.dp, vertical = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text("TIME 42", color = Ink, fontSize = 6.sp, fontFamily = PixelFont, maxLines = 1, softWrap = false)
            Text(
                text = "¥1,234",
                color = Ink,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                maxLines = 1,
                softWrap = false
            )
            if (hudMode == HudMode.FULL) {
                Text(
                    text = "ASSET",
                    color = InkMuted,
                    fontSize = 6.sp,
                    fontFamily = PixelFont,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun ChartStylePreview(
    lineStyle: ChartLineStyle,
    modifier: Modifier = Modifier
) {
    val colors = lcdColors()
    ConfigPreviewBox(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // 斜め移動を含む4点。STEPとSMOOTHで明確に見え方が変わる
            val dataPoints = listOf(
                0.06f to 0.78f,
                0.38f to 0.28f,
                0.62f to 0.62f,
                0.92f to 0.18f
            )
            val path = Path()
            dataPoints.forEachIndexed { i, (xf, yf) ->
                val x = w * xf
                val y = h * yf
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevY = h * dataPoints[i - 1].second
                    when (lineStyle) {
                        ChartLineStyle.STEP -> {
                            path.lineTo(x, prevY)
                            path.lineTo(x, y)
                        }
                        ChartLineStyle.SMOOTH -> path.lineTo(x, y)
                    }
                }
            }
            drawPath(
                path = path,
                color = colors.ink,
                style = Stroke(width = 2.5f, cap = StrokeCap.Square, join = StrokeJoin.Miter)
            )
        }
    }
}

@Composable
fun ScanPreview(
    strength: ScanlineStrength,
    modifier: Modifier = Modifier
) {
    ConfigPreviewBox(modifier = modifier) {
        ScanlinePreviewCanvas(strength = strength, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun ContrastPreview(
    contrast: PanelContrast,
    lcdTheme: LcdThemeMode,
    surfaceTone: SurfaceTone,
    modifier: Modifier = Modifier
) {
    val sample = paletteFor(lcdTheme, contrast, surfaceTone)
    ConfigPreviewBox(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(sample.background)
                .padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("MAIN", color = sample.ink, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = PixelFont)
            Text("muted", color = sample.inkMuted, fontSize = 7.sp, fontFamily = PixelFont)
            Text("faint", color = sample.inkFaint, fontSize = 7.sp, fontFamily = PixelFont)
        }
    }
}
