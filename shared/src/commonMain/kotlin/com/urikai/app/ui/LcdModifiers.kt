package com.urikai.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.urikai.app.ui.theme.lcdColors

@Composable
fun Modifier.lcdBorder(
    color: Color? = null,
    width: Dp = 1.dp
): Modifier {
    val ink = color ?: lcdColors().ink
    return border(width, ink, RectangleShape)
}

@Composable
fun Modifier.lcdDoubleBorder(
    color: Color? = null,
    gap: Dp = 3.dp
): Modifier {
    val ink = color ?: lcdColors().ink
    return this
        .border(2.dp, ink, RectangleShape)
        .padding(gap)
        .border(1.dp, ink, RectangleShape)
}

@Composable
fun Modifier.lcdHatch(
    lineColor: Color? = null,
    step: Float = 6f,
    strokeWidth: Float = 1f
): Modifier {
    val hatch = lineColor ?: lcdColors().inkHatch
    return drawBehind {
        val w = size.width
        val h = size.height
        var x = -h
        while (x < w + h) {
            drawLine(
                color = hatch,
                start = Offset(x, h),
                end = Offset(x + h, 0f),
                strokeWidth = strokeWidth
            )
            x += step
        }
    }
}

@Composable
fun LcdInvertedPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = lcdColors()
    Box(
        modifier = modifier
            .background(colors.ink)
            .lcdBorder(colors.invertedText, width = 1.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        content()
    }
}

@Composable
fun LcdHatchOverlay(modifier: Modifier = Modifier) {
    val hatch = lcdColors().inkHatch
    Canvas(modifier = modifier) {
        val step = 5f
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = hatch,
                start = Offset(x, size.height),
                end = Offset(x + size.height, 0f),
                strokeWidth = 1f
            )
            x += step
        }
    }
}
