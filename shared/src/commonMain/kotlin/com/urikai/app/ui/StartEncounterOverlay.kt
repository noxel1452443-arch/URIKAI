package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.graphicsLayer
import com.urikai.app.game.GameViewModel
import com.urikai.app.ui.theme.Ink
import kotlin.math.hypot
import kotlinx.coroutines.delay

/**
 * ホーム→プレイのアイリスワイプ（円が閉じて開く）。
 *
 * [onCovered]＝全閉（セッション準備）、[onOpenStarted]＝開き始め（点スイープ開始）、
 * [onRevealed]＝開き終わり（定位置＋START）。
 */
@Composable
fun StartEncounterOverlay(
    onCovered: () -> Unit,
    onOpenStarted: () -> Unit = {},
    onRevealed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ink = Ink
    // 0=全開、1=全閉
    val closed = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        closed.animateTo(
            1f,
            tween(
                durationMillis = GameViewModel.START_IRIS_CLOSE_MS.toInt(),
                easing = FastOutSlowInEasing
            )
        )
        onCovered()
        delay(GameViewModel.START_IRIS_HOLD_MS)
        onOpenStarted()
        closed.animateTo(
            0f,
            tween(
                durationMillis = GameViewModel.START_IRIS_OPEN_MS.toInt(),
                easing = LinearEasing
            )
        )
        onRevealed()
    }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val c = closed.value.coerceIn(0f, 1f)
        if (c <= 0.001f) return@Canvas
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val maxR = hypot(size.width.toDouble(), size.height.toDouble()).toFloat() * 0.55f
        val holeR = maxR * (1f - c)
        val mask = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            if (holeR > 0.5f) {
                addOval(
                    Rect(
                        left = cx - holeR,
                        top = cy - holeR,
                        right = cx + holeR,
                        bottom = cy + holeR
                    )
                )
            }
        }
        drawPath(path = mask, color = ink)
    }
}
