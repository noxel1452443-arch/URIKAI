package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.urikai.app.ui.theme.LcdBackground
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 押せないUI向けの「ぷに」感。押下で少し沈み、離すとばね戻し。
 */
fun Modifier.lcdSquish(
    onTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    tapSound: UiSoundKind? = UiSoundKind.SELECT,
    longPressSound: UiSoundKind? = UiSoundKind.SELECT,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val bus = LocalUiSound.current
    pointerInput(onTap, onLongPress, tapSound, longPressSound) {
        detectTapGestures(
            onPress = {
                scope.launch { scale.animateTo(0.94f, spring(stiffness = 900f)) }
                tryAwaitRelease()
                scope.launch { scale.animateTo(1f, spring(stiffness = 500f, dampingRatio = 0.55f)) }
            },
            onTap = {
                if (onTap != null) {
                    tapSound?.let { bus.play(it) }
                    onTap()
                }
            },
            onLongPress = {
                if (onLongPress != null) {
                    longPressSound?.let { bus.play(it) }
                    onLongPress()
                }
            }
        )
    }.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * START周辺の共通チップ（通算行と同系: 二重枠＋押し込み）。
 * ナビ用途なので決定音。
 */
@Composable
fun LcdChromeChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sound: UiSoundKind = UiSoundKind.CONFIRM,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .lcdDoubleBorder()
            .background(LcdBackground)
            .lcdSquish(onTap = if (enabled) onClick else null, tapSound = sound)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Balatro風タイトルカード: ドラッグで傾き、大きく弾いたらコールバック。
 */
fun Modifier.lcdTitleCard(
    onFlingShuffle: () -> Unit,
    onLongPress: () -> Unit,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val rot = remember { Animatable(0f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                scope.launch { scale.animateTo(0.96f, spring(stiffness = 800f)) }
                tryAwaitRelease()
                scope.launch { scale.animateTo(1f, spring(stiffness = 450f, dampingRatio = 0.5f)) }
            },
            onLongPress = { onLongPress() }
        )
    }.pointerInput(onFlingShuffle) {
        detectDragGestures(
            onDragEnd = {
                val flung = abs(offsetX.value) > 48f || abs(offsetY.value) > 48f || abs(rot.value) > 8f
                if (flung) onFlingShuffle()
                scope.launch {
                    offsetX.animateTo(0f, spring(stiffness = 280f, dampingRatio = 0.55f))
                    offsetY.animateTo(0f, spring(stiffness = 280f, dampingRatio = 0.55f))
                    rot.animateTo(0f, spring(stiffness = 280f, dampingRatio = 0.55f))
                }
            },
            onDrag = { change, drag ->
                change.consume()
                scope.launch {
                    offsetX.snapTo((offsetX.value + drag.x).coerceIn(-56f, 56f))
                    offsetY.snapTo((offsetY.value + drag.y).coerceIn(-40f, 40f))
                    rot.snapTo((rot.value + drag.x * 0.12f).coerceIn(-14f, 14f))
                }
            }
        )
    }.graphicsLayer {
        translationX = offsetX.value
        translationY = offsetY.value
        rotationZ = rot.value
        scaleX = scale.value
        scaleY = scale.value
    }
}
