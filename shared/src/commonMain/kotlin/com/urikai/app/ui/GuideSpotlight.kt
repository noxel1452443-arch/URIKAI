package com.urikai.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.LcdBackground

/** チュート中に抜き出して見せる対象。 */
enum class GuideSpotlightTarget {
    NONE,
    LIFE_ORBS,
    BALANCE_AMOUNT,
    PRIMARY_BUTTON,
    STYLE_ENTRY,
    TITLE_CARD,
    SUMIKA_PAY_STATS,
    SUMIKA_UPGRADE,
}

class GuideSpotlightController {
    var target by mutableStateOf(GuideSpotlightTarget.NONE)
    var holeInRoot by mutableStateOf<Rect?>(null)

    fun clear() {
        target = GuideSpotlightTarget.NONE
        holeInRoot = null
    }
}

val LocalGuideSpotlight = staticCompositionLocalOf<GuideSpotlightController?> { null }

/** アクティブなスポットライト対象のとき、穴座標を登録しパルス枠を付ける。 */
fun Modifier.guideSpotlightTarget(target: GuideSpotlightTarget): Modifier = composed {
    val ctrl = LocalGuideSpotlight.current
    val active = ctrl != null && ctrl.target == target
    val pulse = rememberInfiniteTransition(label = "guide_spot_pulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(720, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "guide_spot_alpha"
    )
    val ink = Ink
    val panel = LcdBackground
    val density = LocalDensity.current
    val stroke = with(density) { 2.5.dp.toPx() }
    val pad = with(density) { 5.dp.toPx() }
    Modifier
        .onGloballyPositioned { coords ->
            if (active) {
                ctrl.holeInRoot = coords.boundsInRoot()
            }
        }
        .drawWithContent {
            drawContent()
            if (!active) return@drawWithContent
            drawRect(
                color = panel.copy(alpha = 0.18f * pulseAlpha),
                topLeft = Offset(-pad, -pad),
                size = Size(size.width + pad * 2, size.height + pad * 2)
            )
            drawRect(
                color = ink.copy(alpha = pulseAlpha),
                topLeft = Offset(-pad, -pad),
                size = Size(size.width + pad * 2, size.height + pad * 2),
                style = Stroke(width = stroke)
            )
        }
}

/** 穴あき暗転。穴がないときは全面スクラム。 */
@Composable
fun GuideSpotlightScrim(
    holeInRoot: Rect?,
    modifier: Modifier = Modifier,
    dimAlpha: Float = 0.52f
) {
    var scrimBounds by remember { mutableStateOf<Rect?>(null) }
    val ink = Ink
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { scrimBounds = it.boundsInRoot() }
    ) {
        val dim = ink.copy(alpha = dimAlpha)
        val scrim = scrimBounds
        val hole = holeInRoot
        if (scrim == null || hole == null || hole.width <= 0f || hole.height <= 0f) {
            drawRect(dim)
            return@Canvas
        }
        val pad = 6.dp.toPx()
        val local = Rect(
            left = (hole.left - scrim.left - pad).coerceAtLeast(0f),
            top = (hole.top - scrim.top - pad).coerceAtLeast(0f),
            right = (hole.right - scrim.left + pad).coerceAtMost(size.width),
            bottom = (hole.bottom - scrim.top + pad).coerceAtMost(size.height)
        )
        if (local.top > 0f) {
            drawRect(dim, topLeft = Offset.Zero, size = Size(size.width, local.top))
        }
        if (local.bottom < size.height) {
            drawRect(
                dim,
                topLeft = Offset(0f, local.bottom),
                size = Size(size.width, size.height - local.bottom)
            )
        }
        if (local.left > 0f && local.height > 0f) {
            drawRect(
                dim,
                topLeft = Offset(0f, local.top),
                size = Size(local.left, local.height)
            )
        }
        if (local.right < size.width && local.height > 0f) {
            drawRect(
                dim,
                topLeft = Offset(local.right, local.top),
                size = Size(size.width - local.right, local.height)
            )
        }
    }
}

@Composable
fun rememberGuideSpotlightBinding(target: GuideSpotlightTarget) {
    val ctrl = LocalGuideSpotlight.current
    LaunchedEffect(target) {
        if (ctrl == null) return@LaunchedEffect
        ctrl.target = target
        if (target == GuideSpotlightTarget.NONE) {
            ctrl.holeInRoot = null
        }
    }
    DisposableEffect(Unit) {
        onDispose { ctrl?.clear() }
    }
}

fun guideSpotlightFor(entryId: com.urikai.app.game.NotebookEntryId, step: Int): GuideSpotlightTarget =
    when (entryId) {
        com.urikai.app.game.NotebookEntryId.HOW_TO_PLAY ->
            if (step == 0) GuideSpotlightTarget.PRIMARY_BUTTON else GuideSpotlightTarget.NONE
        com.urikai.app.game.NotebookEntryId.LIFE ->
            if (step == 0) GuideSpotlightTarget.LIFE_ORBS else GuideSpotlightTarget.NONE
        com.urikai.app.game.NotebookEntryId.BALANCE ->
            if (step == 0) GuideSpotlightTarget.BALANCE_AMOUNT else GuideSpotlightTarget.NONE
        com.urikai.app.game.NotebookEntryId.STAMPS ->
            GuideSpotlightTarget.TITLE_CARD
        else -> GuideSpotlightTarget.NONE
    }
