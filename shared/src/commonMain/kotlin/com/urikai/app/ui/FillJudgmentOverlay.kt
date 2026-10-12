package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.FillQuality
import com.urikai.app.game.JudgeFxTier
import com.urikai.app.game.UiStrings
import com.urikai.app.game.fillJudgmentDurationMs
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import kotlinx.coroutines.delay

/**
 * 約定瞬間の判定（チャート中央）。
 * JudgeFxTier.ON 時は二重枠エンブレムへ置換（放射線なし）。
 */
@Composable
fun FillJudgmentOverlay(
    quality: FillQuality,
    token: Long,
    fxTier: JudgeFxTier = JudgeFxTier.NONE,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val scale = remember(token) { Animatable(0.7f) }
    val alpha = remember(token) { Animatable(0f) }
    val tilt = remember(token) { Animatable(0f) }

    LaunchedEffect(token, fxTier) {
        val hold = fillJudgmentDurationMs(quality)
        scale.snapTo(0.55f)
        alpha.snapTo(0f)
        tilt.snapTo(if (quality == FillQuality.MISS) -10f else 0f)

        val enhanced = fxTier == JudgeFxTier.ON
        val peakMul = if (enhanced) 1.14f else 1f

        if (quality == FillQuality.FANTASTIC) {
            alpha.animateTo(1f, tween(60))
            scale.animateTo(1.55f * peakMul, tween(140, easing = FastOutSlowInEasing))
            scale.animateTo(1.18f * peakMul, tween(90))
            val pulses = if (enhanced) 6 else 4
            repeat(pulses) {
                scale.animateTo(1.32f * peakMul, tween(80))
                scale.animateTo(1.14f * peakMul, tween(80))
            }
            delay((hold - 280L).coerceAtLeast(200L))
            alpha.animateTo(0f, tween(260))
            return@LaunchedEffect
        }

        alpha.animateTo(1f, tween(70))
        val peak = when (quality) {
            FillQuality.PERFECT -> 1.28f
            FillQuality.GREAT -> 1.16f
            FillQuality.OK -> 1.06f
            FillQuality.MISS -> 1.12f
            FillQuality.FANTASTIC -> 1.28f
        } * peakMul
        scale.animateTo(peak, tween(110, easing = FastOutSlowInEasing))
        scale.animateTo(
            when (quality) {
                FillQuality.PERFECT -> 1.06f
                FillQuality.OK -> 0.96f
                FillQuality.MISS -> 0.94f
                else -> 1f
            } * peakMul,
            tween(100)
        )
        if (quality == FillQuality.PERFECT || enhanced) {
            val pulses = if (enhanced) 3 else 2
            if (quality == FillQuality.PERFECT ||
                (enhanced && (quality == FillQuality.GREAT || quality == FillQuality.FANTASTIC))
            ) {
                repeat(pulses) {
                    scale.animateTo(1.12f * peakMul, tween(85))
                    scale.animateTo(1.04f * peakMul, tween(85))
                }
            }
        }
        delay((hold - 160L).coerceAtLeast(80L))
        if (quality == FillQuality.MISS) {
            tilt.animateTo(-14f, tween(160))
            alpha.animateTo(0f, tween(220))
        } else {
            alpha.animateTo(0f, tween(160))
        }
    }

    val title = UiStrings.fillJudgmentLabel(quality, lang)
    val titleSize = when (quality) {
        FillQuality.FANTASTIC -> if (fxTier == JudgeFxTier.ON) 54.sp else 52.sp
        FillQuality.PERFECT -> if (fxTier == JudgeFxTier.ON) 44.sp else 42.sp
        FillQuality.GREAT -> if (fxTier == JudgeFxTier.ON) 36.sp else 34.sp
        FillQuality.OK -> if (fxTier == JudgeFxTier.ON) 26.sp else 24.sp
        FillQuality.MISS -> if (fxTier == JudgeFxTier.ON) 36.sp else 34.sp
    }
    val tracking = when (quality) {
        FillQuality.FANTASTIC -> 4.0.sp
        FillQuality.PERFECT -> 3.2.sp
        FillQuality.GREAT -> 2.4.sp
        FillQuality.OK -> 1.2.sp
        FillQuality.MISS -> 2.0.sp
    }
    val layers = when (quality) {
        FillQuality.FANTASTIC -> 5
        FillQuality.PERFECT -> 3
        FillQuality.GREAT -> 2
        FillQuality.MISS -> 1
        FillQuality.OK -> 0
    } + if (fxTier == JudgeFxTier.ON) 2 else 0
    val step = if (fxTier == JudgeFxTier.ON) {
        2.8.dp
    } else {
        when (quality) {
            FillQuality.FANTASTIC -> 3.2.dp
            FillQuality.PERFECT -> 2.5.dp
            FillQuality.GREAT -> 1.8.dp
            else -> 1.2.dp
        }
    }
    val fadeMul = when (quality) {
        FillQuality.OK -> 0.78f
        else -> 1f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha.value * fadeMul),
        contentAlignment = Alignment.Center
    ) {
        val stampMod = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            rotationZ = tilt.value
        }
        when (fxTier) {
            JudgeFxTier.NONE -> {
                JudgmentMarkerStack(
                    text = title,
                    fontSize = titleSize,
                    letterSpacing = tracking,
                    layers = layers,
                    step = step,
                    color = Ink,
                    modifier = stampMod
                )
            }
            JudgeFxTier.ON -> {
                Box(
                    modifier = stampMod
                        .border(3.dp, Ink, RectangleShape)
                        .padding(3.dp)
                        .border(2.dp, LcdBackground, RectangleShape)
                        .background(Ink)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JudgmentMarkerStack(
                        text = title,
                        fontSize = titleSize,
                        letterSpacing = tracking,
                        layers = layers.coerceAtLeast(3),
                        step = step,
                        color = LcdInvertedText,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun JudgmentMarkerStack(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit,
    layers: Int,
    step: androidx.compose.ui.unit.Dp,
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
            letterSpacing = letterSpacing,
            textAlign = TextAlign.Center
        )
    }
}
