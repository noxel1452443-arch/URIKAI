package com.urikai.app.ui

import com.urikai.app.platform.currentTimeMillis
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.ComboFxTier
import com.urikai.app.game.GameViewModel
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.PixelFont

/**
 * チャート上の COMBO 常駐。
 * ComboFxTier.ON で反転バー表示へ置換。
 */
@Composable
fun ComboSpotHud(
    combo: Int,
    expiresAtEpochMs: Long,
    bounceToken: Long,
    frameTick: Long,
    fxTier: ComboFxTier = ComboFxTier.NONE,
    modifier: Modifier = Modifier
) {
    if (combo <= 0 || expiresAtEpochMs <= 0L) return

    val holdMs = GameViewModel.FILL_COMBO_HOLD_MS.toFloat()
    val remainingMs = (expiresAtEpochMs - currentTimeMillis()).coerceAtLeast(0L)
    @Suppress("UNUSED_EXPRESSION")
    frameTick
    val life = (remainingMs / holdMs).coerceIn(0f, 1f)

    val bounce = remember { Animatable(1f) }
    LaunchedEffect(bounceToken, fxTier) {
        if (bounceToken <= 0L) return@LaunchedEffect
        val peak = if (fxTier == ComboFxTier.ON) 1.32f else 1.2f
        bounce.snapTo(0.84f)
        bounce.animateTo(peak, tween(90, easing = FastOutSlowInEasing))
        bounce.animateTo(1f, tween(120))
    }

    val shrink = 0.62f + 0.38f * life
    val fade = 0.55f + 0.45f * life
    val wordSize = if (fxTier == ComboFxTier.ON) 24.sp else 22.sp
    val numSize = if (fxTier == ComboFxTier.ON) 30.sp else 28.sp

    val content = @Composable {
        Row(verticalAlignment = Alignment.Bottom) {
            when (fxTier) {
                ComboFxTier.NONE -> {
                    ComboMarkerStack(
                        text = "COMBO",
                        fontSize = wordSize,
                        letterSpacing = 2.sp,
                        layers = 2,
                        step = 1.5.dp,
                        color = Ink,
                        modifier = Modifier.padding(end = 10.dp, bottom = 1.dp)
                    )
                    ComboMarkerStack(
                        text = "x$combo",
                        fontSize = numSize,
                        letterSpacing = 1.4.sp,
                        layers = 2,
                        step = 1.5.dp,
                        color = Ink
                    )
                }
                ComboFxTier.ON -> {
                    Text(
                        text = "COMBO  x$combo",
                        color = LcdInvertedText,
                        fontSize = wordSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PixelFont,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = shrink * bounce.value
                scaleX = s
                scaleY = s
            }
            .alpha(fade)
    ) {
        when (fxTier) {
            ComboFxTier.NONE -> content()
            ComboFxTier.ON -> {
                Box(
                    modifier = Modifier
                        .background(Ink)
                        .border(2.dp, LcdBackground, RectangleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) { content() }
            }
        }
    }
}

@Composable
private fun ComboMarkerStack(
    text: String,
    fontSize: TextUnit,
    letterSpacing: TextUnit,
    layers: Int,
    step: Dp,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        for (i in layers downTo 1) {
            Text(
                text = text,
                color = color.copy(alpha = 0.16f + 0.06f * (layers - i)),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                letterSpacing = letterSpacing,
                modifier = Modifier.offset(x = step * i, y = step * i)
            )
        }
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            letterSpacing = letterSpacing
        )
    }
}
