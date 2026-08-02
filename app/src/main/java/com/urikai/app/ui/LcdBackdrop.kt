package com.urikai.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.urikai.app.game.ScanlineStrength
import com.urikai.app.ui.theme.LocalScanlineStrength
import com.urikai.app.ui.theme.ScanlineStyle
import com.urikai.app.ui.theme.lcdColors

@Composable
fun LcdBackdrop(modifier: Modifier = Modifier) {
    val colors = lcdColors()
    val scanlineStrength = LocalScanlineStrength.current
    val scanAlpha = ScanlineStyle.alpha(scanlineStrength)
    val step = ScanlineStyle.step(scanlineStrength)
    val stroke = ScanlineStyle.stroke(scanlineStrength)

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(colors.background)
        if (scanAlpha <= 0f) return@Canvas
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = colors.ink.copy(alpha = scanAlpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = stroke
            )
            y += step
        }
    }
}

/** CONFIGプレビュー用 */
@Composable
fun ScanlinePreviewCanvas(
    strength: ScanlineStrength,
    modifier: Modifier = Modifier
) {
    val colors = lcdColors()
    val scanAlpha = ScanlineStyle.alpha(strength)
    val step = ScanlineStyle.step(strength)
    val stroke = ScanlineStyle.stroke(strength)

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(colors.background)
        if (scanAlpha <= 0f) return@Canvas
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = colors.ink.copy(alpha = scanAlpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = stroke
            )
            y += step
        }
    }
}
