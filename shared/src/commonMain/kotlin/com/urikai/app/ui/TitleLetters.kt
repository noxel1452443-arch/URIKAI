package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.urikai.app.game.StartScreenCopy
import com.urikai.app.ui.theme.PixelFont
import kotlinx.coroutines.launch

/**
 * タイトル URIKAI を1文字ずつドラッグできる。
 * 離すとばねで戻る（意図が「触って楽しい」になる本線）。
 */
@Composable
fun UriKaiTitleLetters(
    color: Color,
    fontSize: TextUnit = 36.sp,
    modifier: Modifier = Modifier
) {
    val letters = remember { StartScreenCopy.title().map { it.toString() } }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        letters.forEach { letter ->
            DraggableTitleLetter(
                letter = letter,
                color = color,
                fontSize = fontSize
            )
        }
    }
}

@Composable
private fun DraggableTitleLetter(
    letter: String,
    color: Color,
    fontSize: TextUnit
) {
    val ox = remember { Animatable(0f) }
    val oy = remember { Animatable(0f) }
    val rot = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Text(
        text = letter,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            launch {
                                ox.animateTo(0f, spring(stiffness = 320f, dampingRatio = 0.52f))
                            }
                            launch {
                                oy.animateTo(0f, spring(stiffness = 320f, dampingRatio = 0.52f))
                            }
                            launch {
                                rot.animateTo(0f, spring(stiffness = 280f, dampingRatio = 0.55f))
                            }
                        }
                    },
                    onDrag = { change, drag ->
                        change.consume()
                        scope.launch {
                            ox.snapTo((ox.value + drag.x).coerceIn(-42f, 42f))
                            oy.snapTo((oy.value + drag.y).coerceIn(-56f, 56f))
                            rot.snapTo((ox.value * 0.18f).coerceIn(-16f, 16f))
                        }
                    }
                )
            }
            .graphicsLayer {
                translationX = ox.value
                translationY = oy.value
                rotationZ = rot.value
            }
    )
}
