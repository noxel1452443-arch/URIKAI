package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.urikai.app.game.GameState
import com.urikai.app.game.GameViewModel
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.PixelFont
import kotlinx.coroutines.delay

@Composable
fun EndgameOverlays(
    gameState: GameState,
    currentSecond: Int,
    showFinishBanner: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (gameState == GameState.PLAYING &&
            currentSecond in GameViewModel.ENDGAME_COUNTDOWN_FROM_SECOND until GameViewModel.GAME_DURATION_SECONDS
        ) {
            val digit = GameViewModel.GAME_DURATION_SECONDS - currentSecond
            CountdownPopup(digit = digit)
        }
        if (showFinishBanner && gameState == GameState.FINISHING) {
            FinishBanner()
        }
    }
}

/** 現在点が定位置に着いたあとの中央 START バン → 本編。 */
@Composable
fun StartBangOverlay(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.55f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        // 開き終わり＝定位置の拍で即出す。短く叩いて本編へ。
        alpha.snapTo(1f)
        scale.snapTo(0.7f)
        scale.animateTo(1.12f, tween(120, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(80))
        delay(260)
        alpha.animateTo(0f, tween(100))
        onFinished()
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "START",
            color = Ink,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelFont,
            letterSpacing = 2.sp,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        )
    }
}

@Composable
private fun BoxScope.CountdownPopup(digit: Int) {
    val scale = remember(digit) { Animatable(2.2f) }
    LaunchedEffect(digit) {
        scale.snapTo(2.2f)
        scale.animateTo(1f, tween(650))
    }
    Text(
        text = digit.toString(),
        color = Ink,
        fontSize = 56.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        modifier = Modifier
            .align(Alignment.Center)
            .scale(scale.value)
    )
}

@Composable
private fun BoxScope.FinishBanner() {
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        scale.snapTo(0.6f)
        scale.animateTo(1f, tween(350))
    }
    Text(
        text = "FINISH!",
        color = Ink,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = PixelFont,
        modifier = Modifier
            .align(Alignment.Center)
            .scale(scale.value)
    )
}
