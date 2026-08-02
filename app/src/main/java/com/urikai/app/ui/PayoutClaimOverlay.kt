package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.PixelFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 出金演出。元手から最終資産へ。
 * カウントSEと連動し、最後の手前で一瞬静止 → チャキーン。
 * muted=true（リタイア）時はチャキーン・最終加速ポップなし。
 */
@Composable
fun PayoutClaimOverlay(
    fromYen: Int,
    toYen: Int,
    onFinished: () -> Unit,
    onCountTick: (progress: Float) -> Unit = {},
    onClang: () -> Unit = {},
    muted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val lang = LocalDisplayLanguage.current
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.72f) }
    val progress = remember { Animatable(0f) }
    var displayYen by remember { mutableIntStateOf(fromYen) }
    var lastTickBucket by remember { mutableIntStateOf(-1) }

    LaunchedEffect(fromYen, toYen, muted) {
        fun yenAt(p: Float) = (fromYen + (toYen - fromYen) * p).roundToInt()
        fun maybeTick(p: Float) {
            val bucket = (p * 24f).toInt()
            if (bucket != lastTickBucket) {
                lastTickBucket = bucket
                onCountTick(p)
            }
        }

        alpha.snapTo(0f)
        scale.snapTo(0.72f)
        progress.snapTo(0f)
        displayYen = fromYen
        lastTickBucket = -1
        alpha.animateTo(1f, tween(100))
        scale.animateTo(1.06f, tween(160, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(90))
        delay(200)

        if (fromYen == toYen) {
            delay(420)
            displayYen = toYen
            if (!muted) onClang()
            delay(400)
            alpha.animateTo(0f, tween(220))
            onFinished()
            return@LaunchedEffect
        }

        if (muted) {
            // 単調カウントのみ（静止・加速ポップ・チャキーンなし）
            progress.animateTo(1f, tween(1_100, easing = EaseInOutCubic)) {
                displayYen = yenAt(value)
                maybeTick(value)
            }
            displayYen = toYen
            delay(360)
            alpha.animateTo(0f, tween(240))
            onFinished()
            return@LaunchedEffect
        }

        val tease = if (toYen > fromYen) 0.78f else 0.22f
        launch {
            scale.animateTo(1.08f, tween(180))
            scale.animateTo(1.02f, tween(700, easing = EaseInOutCubic))
        }
        progress.animateTo(tease, tween(880, easing = EaseInOutCubic)) {
            displayYen = yenAt(value)
            maybeTick(value)
        }
        // 最後の手前で一瞬静止
        delay(280)

        launch {
            scale.snapTo(0.94f)
            scale.animateTo(1.2f, tween(160, easing = FastOutSlowInEasing))
            scale.animateTo(1f, tween(200))
        }
        progress.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) {
            displayYen = yenAt(value)
            maybeTick(value)
        }
        displayYen = toYen
        onClang()
        delay(520)
        alpha.animateTo(0f, tween(240))
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha.value)
            .background(Ink),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .lcdDoubleBorder(LcdBackground)
                .background(Ink)
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Text(
                text = UiStrings.payoutClaimTitle(lang),
                color = LcdBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                letterSpacing = 2.sp
            )
            Text(
                text = "¥%,d".format(displayYen),
                color = LcdBackground,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
