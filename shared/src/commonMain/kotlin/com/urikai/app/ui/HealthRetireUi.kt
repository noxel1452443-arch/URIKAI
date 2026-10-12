package com.urikai.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urikai.app.game.GameViewModel
import com.urikai.app.game.RetirePhase
import com.urikai.app.game.UiStrings
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.LcdBackground
import com.urikai.app.ui.theme.LcdInvertedText
import com.urikai.app.ui.theme.PixelFont
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * ナノテック風 LIFE：透明球の中で光の軌道が回る球体セル。
 * MISSガード所持時は maxHealth=5（同じ球が1つ増えるだけ）。
 * MISS 後は判定スタンプの半拍あとに、消えた球が割れて -1 が見える。
 */
@Composable
fun HealthNanotechBar(
    health: Int,
    maxHealth: Int = GameViewModel.MAX_HEALTH,
    lifeLossTokenEpochMs: Long = 0L,
    lifeLossOrbIndex: Int = -1,
    modifier: Modifier = Modifier
) {
    val hp = health.coerceIn(0, maxHealth)
    val pulse = rememberInfiniteTransition(label = "life_nano")
    val phase by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "life_nano_phase"
    )
    val glow by pulse.animateFloat(
        initialValue = 0.62f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "life_nano_glow"
    )

    var holdLostOrb by remember { mutableStateOf(false) }
    var showMinusChip by remember { mutableStateOf(false) }
    val barShake = remember { Animatable(0f) }
    val lostOrbFlash = remember { Animatable(0f) }
    val chipAlpha = remember { Animatable(0f) }

    LaunchedEffect(lifeLossTokenEpochMs, lifeLossOrbIndex) {
        if (lifeLossTokenEpochMs <= 0L || lifeLossOrbIndex < 0) {
            holdLostOrb = false
            showMinusChip = false
            barShake.snapTo(0f)
            lostOrbFlash.snapTo(0f)
            chipAlpha.snapTo(0f)
            return@LaunchedEffect
        }
        holdLostOrb = true
        showMinusChip = false
        barShake.snapTo(0f)
        lostOrbFlash.snapTo(0f)
        chipAlpha.snapTo(0f)
        delay(GameViewModel.LIFE_LOSS_FEEDBACK_DELAY_MS)
        holdLostOrb = false
        showMinusChip = true
        lostOrbFlash.snapTo(1f)
        chipAlpha.snapTo(1f)
        repeat(3) {
            barShake.snapTo(if (it % 2 == 0) 5f else -5f)
            delay(45)
        }
        barShake.snapTo(0f)
        lostOrbFlash.animateTo(0f, tween(420))
        delay(280)
        chipAlpha.animateTo(0f, tween(220))
        showMinusChip = false
    }

    val lossFxActive =
        holdLostOrb || showMinusChip || lostOrbFlash.value > 0.05f
    val heart = rememberInfiniteTransition(label = "life_heartbeat")
    // 鼓動量 0=縮み 1=膨らみ。振幅を大きくしてLCDでも一発で分かるようにする
    val beatAt2 by heart.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "life_beat_2"
    )
    val beatAt1 by heart.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "life_beat_1"
    )
    val heartBeat = when {
        lossFxActive -> -1f // 無効
        hp == 1 -> beatAt1
        hp == 2 -> beatAt2
        else -> -1f
    }

    Column(
        modifier = modifier.graphicsLayer { translationX = barShake.value },
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIFE",
                color = Ink.copy(alpha = 0.5f + 0.28f * glow),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier.padding(end = 2.dp)
            )
            repeat(maxHealth) { i ->
                val stillHoldingLost = holdLostOrb && i == lifeLossOrbIndex
                val filled = i < hp || stillHoldingLost
                val cracking = !holdLostOrb &&
                    lifeLossOrbIndex == i &&
                    lostOrbFlash.value > 0.05f
                NanotechLifeOrb(
                    filled = filled,
                    phase = phase,
                    cellIndex = i,
                    glow = glow,
                    crackFlash = if (cracking) lostOrbFlash.value else 0f,
                    heartBeat = if (filled) heartBeat else -1f
                )
            }
        }
        if (showMinusChip || chipAlpha.value > 0.02f) {
            Text(
                text = UiStrings.lifeMinusOne(),
                color = LcdInvertedText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .alpha(chipAlpha.value)
                    .background(Ink)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun NanotechLifeOrb(
    filled: Boolean,
    phase: Float,
    cellIndex: Int,
    glow: Float,
    crackFlash: Float = 0f,
    /** 0..1 鼓動。負でオフ */
    heartBeat: Float = -1f
) {
    val ink = Ink
    val bg = LcdBackground
    val beating = heartBeat >= 0f
    val beat = heartBeat.coerceIn(0f, 1f)
    // 縮み↔膨らみのみ（反転フラッシュなし）
    val orbScale = if (beating) 0.78f + 0.34f * beat else 1f
    Canvas(
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer {
                scaleX = orbScale
                scaleY = orbScale
            }
    ) {
        val r = min(size.width, size.height) * 0.46f
        val c = Offset(size.width / 2f, size.height / 2f)
        val local = (phase + cellIndex * 0.11f) % 1f
        val flash = crackFlash.coerceIn(0f, 1f)

        // 外側の淡いオーラ
        drawCircle(
            ink.copy(alpha = if (filled) 0.10f + 0.08f * glow else 0.05f + 0.18f * flash),
            r * (1.1f + 0.12f * flash),
            c
        )
        // ガラス球シェル
        drawCircle(bg.copy(alpha = if (filled) 0.38f else 0.12f), r * 0.92f, c)
        drawCircle(
            ink.copy(alpha = if (filled) 0.55f else 0.28f + 0.4f * flash),
            r * 0.92f,
            c,
            style = Stroke(width = 1.5f + flash)
        )
        // ハイライト弧（透明感）
        drawArc(
            color = ink.copy(alpha = 0.22f),
            startAngle = -140f,
            sweepAngle = 55f,
            useCenter = false,
            topLeft = Offset(c.x - r * 0.72f, c.y - r * 0.72f),
            size = Size(r * 1.44f, r * 1.44f),
            style = Stroke(width = 1.2f, cap = StrokeCap.Round)
        )

        if (!filled) {
            drawCircle(ink.copy(alpha = 0.12f + 0.35f * flash), r * 0.16f, c)
            if (flash > 0.05f) {
                // 割れ線
                val crack = ink.copy(alpha = 0.55f * flash)
                drawLine(crack, Offset(c.x - r * 0.35f, c.y - r * 0.4f), Offset(c.x + r * 0.1f, c.y + r * 0.45f), 1.4f)
                drawLine(crack, Offset(c.x + r * 0.2f, c.y - r * 0.35f), Offset(c.x - r * 0.15f, c.y + r * 0.4f), 1.2f)
            }
            return@Canvas
        }

        val sphere = Path().apply {
            addOval(Rect(c.x - r * 0.86f, c.y - r * 0.86f, c.x + r * 0.86f, c.y + r * 0.86f))
        }
        clipPath(sphere) {
            drawCircle(ink.copy(alpha = 0.16f + 0.1f * glow), r * 0.2f, c)

            // 軌道リングはほぼ固定（ごくゆっくりだけ傾き）、光がリング上を走る
            val orbits = listOf(
                Triple(12f + cellIndex * 7f, 0.78f, 0.40f),
                Triple(58f + cellIndex * 5f, 0.70f, 0.48f),
                Triple(108f + cellIndex * 9f, 0.74f, 0.36f),
                Triple(152f + cellIndex * 4f, 0.66f, 0.44f)
            )
            orbits.forEachIndexed { oi, (tilt, rxFactor, ryFactor) ->
                // 輪っか自体はほぼ静止（ごく遅いドリフトのみ）
                val ringDrift = local * 18f * (if (oi % 2 == 0) 1f else -1f)
                rotate(tilt + ringDrift, c) {
                    val rx = r * rxFactor
                    val ry = r * ryFactor
                    // 淡い軌道線
                    drawOval(
                        color = ink.copy(alpha = 0.20f + 0.08f * glow),
                        topLeft = Offset(c.x - rx, c.y - ry),
                        size = Size(rx * 2f, ry * 2f),
                        style = Stroke(width = 1.15f)
                    )
                    // 軌道上を走る光＋残像
                    val travel = (local * (1.35f + oi * 0.45f) + oi * 0.22f + cellIndex * 0.07f) % 1f
                    val dir = if (oi % 2 == 0) 1f else -1f
                    val trail = 8
                    for (t in trail downTo 0) {
                        var u = travel - dir * t * 0.028f
                        u = ((u % 1f) + 1f) % 1f
                        val a = u * (kotlin.math.PI * 2.0)
                        val pt = Offset(
                            c.x + cos(a).toFloat() * rx,
                            c.y + sin(a).toFloat() * ry
                        )
                        val fade = 1f - t / (trail + 1f).toFloat()
                        val rad = if (t == 0) 2.4f + glow * 0.7f else 1.1f + fade * 1.1f
                        drawCircle(
                            color = ink.copy(alpha = (0.25f + 0.7f * fade) * (0.75f + 0.25f * glow)),
                            radius = rad,
                            center = pt
                        )
                        if (t == 0) {
                            drawCircle(bg.copy(alpha = 0.65f), 0.9f, pt)
                        }
                    }
                }
            }
        }

        drawCircle(
            ink.copy(alpha = 0.4f + 0.2f * glow),
            r * 0.92f,
            c,
            style = Stroke(width = 1.35f)
        )
    }
}

/**
 * リタイア：ショック揺れ → 暗転＋RETIRE。
 * 枠が見切れないよう傾けない。
 */
@Composable
fun RetireOverlay(
    phase: RetirePhase,
    modifier: Modifier = Modifier
) {
    if (phase == RetirePhase.NONE) return

    val shake = remember { Animatable(0f) }
    val bannerScale = remember { Animatable(0.6f) }
    val bannerAlpha = remember { Animatable(0f) }
    val dimAlpha = remember { Animatable(0f) }

    LaunchedEffect(phase) {
        when (phase) {
            RetirePhase.SHOCK -> {
                dimAlpha.snapTo(0.15f)
                repeat(4) {
                    shake.snapTo(if (it % 2 == 0) 8f else -8f)
                    delay(55)
                }
                shake.snapTo(0f)
            }
            RetirePhase.BANNER -> {
                shake.snapTo(0f)
                dimAlpha.animateTo(0.55f, tween(280))
                bannerAlpha.snapTo(0f)
                bannerScale.snapTo(1.45f)
                bannerAlpha.animateTo(1f, tween(70))
                bannerScale.animateTo(0.92f, tween(140, easing = FastOutSlowInEasing))
                bannerScale.animateTo(1f, tween(110))
            }
            RetirePhase.NONE -> Unit
        }
    }

    val pulse = rememberInfiniteTransition(label = "retire_pulse")
    val pulseA by pulse.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420),
            repeatMode = RepeatMode.Reverse
        ),
        label = "retire_pulse_a"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shake.value }
            .background(Ink.copy(alpha = dimAlpha.value)),
        contentAlignment = Alignment.Center
    ) {
        if (phase == RetirePhase.BANNER) {
            Text(
                text = UiStrings.retireBanner(),
                color = LcdBackground,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                letterSpacing = 6.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        alpha = bannerAlpha.value * pulseA
                        scaleX = bannerScale.value.coerceAtMost(1.08f)
                        scaleY = bannerScale.value.coerceAtMost(1.08f)
                    }
                    .border(3.dp, LcdBackground, RectangleShape)
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            )
        }
    }
}
