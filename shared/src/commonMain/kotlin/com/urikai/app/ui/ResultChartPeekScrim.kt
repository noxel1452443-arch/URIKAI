package com.urikai.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.lcdColors

/**
 * RESULT カード背面。中心は濃く・四隅は薄く脈打つことで、チャートへのタップを示す。
 */
@Composable
fun ResultChartPeekScrim(
    onPeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = lcdColors()
    val bg = LcdBackground
    val uiSound = LocalUiSound.current
    val transition = rememberInfiniteTransition(label = "peek_scrim")
    val edgePulse by transition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edge_pulse"
    )
    val cornerPulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corner_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    uiSound.play(UiSoundKind.SELECT)
                    onPeek()
                }
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        bg.copy(alpha = 0.74f),
                        bg.copy(alpha = edgePulse)
                    ),
                    center = center,
                    radius = size.maxDimension * 0.72f
                )
            )

            val arm = 18f
            val inset = 10f
            val ink = colors.ink.copy(alpha = cornerPulse)
            val corners = listOf(
                Offset(inset, inset) to Offset(inset + arm, inset),
                Offset(inset, inset) to Offset(inset, inset + arm),
                Offset(size.width - inset, inset) to Offset(size.width - inset - arm, inset),
                Offset(size.width - inset, inset) to Offset(size.width - inset, inset + arm),
                Offset(inset, size.height - inset) to Offset(inset + arm, size.height - inset),
                Offset(inset, size.height - inset) to Offset(inset, size.height - inset - arm),
                Offset(size.width - inset, size.height - inset) to Offset(size.width - inset - arm, size.height - inset),
                Offset(size.width - inset, size.height - inset) to Offset(size.width - inset, size.height - inset - arm),
            )
            corners.forEach { (start, end) ->
                drawLine(
                    color = ink,
                    start = start,
                    end = end,
                    strokeWidth = 3f,
                    cap = StrokeCap.Square
                )
            }
        }
    }
}
