package com.urikai.app.ui

import com.urikai.app.game.degToRad
import com.urikai.app.platform.currentTimeMillis
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import com.urikai.app.ui.theme.PixelFont
import androidx.compose.ui.graphics.toArgb
import com.urikai.app.game.LagFxTier
import com.urikai.app.game.ChartFrame
import com.urikai.app.game.ChartLineStyle
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.GameViewModel
import com.urikai.app.game.PriceMath
import com.urikai.app.game.ResultRegretMoment
import com.urikai.app.game.TimingFocus
import com.urikai.app.game.TradeMarker
import com.urikai.app.game.TradeTimingEvaluator
import com.urikai.app.game.TradeTimingGrade
import com.urikai.app.game.UiStrings
import com.urikai.app.game.VisibleWindow
import com.urikai.app.game.matches
import com.urikai.app.ui.theme.LocalChartLineStyle
import com.urikai.app.ui.theme.LocalDisplayLanguage
import com.urikai.app.ui.theme.LcdColors
import com.urikai.app.ui.theme.lcdColors
import androidx.compose.animation.core.Animatable
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin
import kotlinx.coroutines.launch

/**
 * 過去窓のウィンドウ追従チャート。
 * 可視窓は常時 20s。経過が窓未満のときは span=elapsed で全幅に伸ばす。
 */
@Composable
fun PriceChart(
    frame: ChartFrame,
    modifier: Modifier = Modifier,
    lineColor: Color? = null,
    showFullSession: Boolean = false,
    showTradeReview: Boolean = false,
    focusTarget: TimingFocus? = null,
    lagFxTier: LagFxTier = LagFxTier.NONE,
) {
    if (frame.priceRatios.isEmpty()) return
    val colors = lcdColors()
    val ink = lineColor ?: colors.ink
    val lineStyle = LocalChartLineStyle.current
    val displayLanguage = LocalDisplayLanguage.current

    val elapsedSec = if (showFullSession) {
        GameViewModel.GAME_DURATION_SECONDS.toFloat()
    } else {
        frame.elapsedSeconds.coerceAtLeast(0f)
    }
    val windowSec = if (showFullSession) {
        GameViewModel.GAME_DURATION_SECONDS.toFloat()
    } else {
        VisibleWindow.targetSeconds(elapsedSec)
    }
    // 経過 < 窓の序盤は windowStart=0 → span=elapsed で全幅に伸ばす
    val windowStart = if (showFullSession) {
        0f
    } else {
        max(0f, elapsedSec - windowSec)
    }
    val windowEnd = if (showFullSession) {
        GameViewModel.GAME_DURATION_SECONDS.toFloat()
    } else {
        max(elapsedSec, 0.001f)
    }
    val plotStart = windowStart
    val nowXFraction = if (showFullSession) 1f else GameViewModel.NOW_X_FRACTION

    var targetMin = Float.POSITIVE_INFINITY
    var targetMax = Float.NEGATIVE_INFINITY
    val sampleStep = 0.05f
    var t = plotStart
    while (t <= windowEnd + 0.0001f) {
        val abs = frame.startOffsetSeconds + t
        val ratio = PriceMath.ratioAt(frame.priceRatios, abs)
        targetMin = min(targetMin, ratio)
        targetMax = max(targetMax, ratio)
        t += sampleStep
    }
    frame.markers.forEach { marker ->
        if (marker.gameTimeSec in plotStart..windowEnd + 0.5f) {
            targetMin = min(targetMin, marker.priceRatio)
            targetMax = max(targetMax, marker.priceRatio)
        }
    }

    if (!targetMin.isFinite() || !targetMax.isFinite()) return
    if (targetMax - targetMin < 1.0f) {
        val mid = (targetMin + targetMax) / 2f
        targetMin = mid - 0.5f
        targetMax = mid + 0.5f
    }
    val pad = max((targetMax - targetMin) * GameViewModel.Y_PAD_RATIO, GameViewModel.Y_PAD_MIN_RATIO)
    targetMin -= pad
    targetMax += pad

    val animMin = remember(frame.startOffsetSeconds) { Animatable(targetMin) }
    val animMax = remember(frame.startOffsetSeconds) { Animatable(targetMax) }

    LaunchedEffect(targetMin, targetMax, frame.startOffsetSeconds) {
        if (targetMin < animMin.value) {
            animMin.snapTo(targetMin)
        } else {
            launch {
                animMin.animateTo(targetMin, tween(GameViewModel.Y_ZOOM_ANIM_MS, easing = LinearEasing))
            }
        }
        if (targetMax > animMax.value) {
            animMax.snapTo(targetMax)
        } else {
            launch {
                animMax.animateTo(targetMax, tween(GameViewModel.Y_ZOOM_ANIM_MS, easing = LinearEasing))
            }
        }
    }

    val reviewHighlights = remember(frame.markers, frame.startOffsetSeconds, showTradeReview) {
        if (!showTradeReview) {
            emptyList()
        } else {
            TradeTimingEvaluator.pickReviewHighlights(
                priceRatios = frame.priceRatios,
                startOffsetSeconds = frame.startOffsetSeconds,
                markers = frame.markers
            )
        }
    }

    val focusTransition = rememberInfiniteTransition(label = "timing_focus_pulse")
    val focusPulse by focusTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timing_focus_pulse_value"
    )
    val emphasisScale = if (focusTarget != null) focusPulse else 1f

    val yMin = animMin.value
    val yMax = animMax.value
    val yRange = (yMax - yMin).coerceAtLeast(0.01f)

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val reviewBandH = if (showTradeReview) h * 0.10f else 0f
        val baseInset = if (showTradeReview) h * 0.03f else h * 0.04f
        val topInset = baseInset + reviewBandH
        val bottomInset = baseInset + reviewBandH
        val plotH = h - topInset - bottomInset
        // peek 出来事は上帯、◎× は下帯に固定（重なり回避）
        val historyLaneY = topInset - reviewBandH * 0.48f
        val reviewLaneY = h - bottomInset + reviewBandH * 0.42f

        if (showTradeReview && reviewBandH > 0f) {
            drawLine(
                color = colors.inkFaint.copy(alpha = 0.35f),
                start = Offset(0f, topInset),
                end = Offset(w, topInset),
                strokeWidth = 1f
            )
            drawLine(
                color = colors.inkFaint.copy(alpha = 0.35f),
                start = Offset(0f, h - bottomInset),
                end = Offset(w, h - bottomInset),
                strokeWidth = 1f
            )
        }

        fun xOf(gameSec: Float): Float {
            val span = (windowEnd - windowStart).coerceAtLeast(0.001f)
            return snap(((gameSec - windowStart) / span) * w * nowXFraction)
        }

        fun yOf(ratio: Float): Float {
            val normalized = ((ratio - yMin) / yRange).coerceIn(0f, 1f)
            return snap(topInset + (plotH - normalized * plotH))
        }

        val historyAnchor = if (
            showTradeReview &&
            frame.anchorWhen.isNotBlank()
        ) {
            val bandStartGame = frame.pivotBandStart - frame.startOffsetSeconds.toFloat()
            val bandEndGame = frame.pivotBandEnd - frame.startOffsetSeconds.toFloat()
            val focalGame = (bandStartGame + bandEndGame) / 2f
            if (focalGame in plotStart..windowEnd) {
                Pair(
                    xOf(focalGame),
                    yOf(PriceMath.ratioAt(frame.priceRatios, frame.startOffsetSeconds + focalGame))
                )
            } else {
                null
            }
        } else {
            null
        }

        historyAnchor?.let { (markerX, _) ->
            drawHistoryMarginGuide(
                markerX = markerX,
                plotTop = topInset,
                plotBottom = h - bottomInset,
                ink = colors.ink
            )
        }

        val path = Path()
        var started = false
        var prevX = 0f
        var prevY = 0f
        var sample = plotStart
        while (sample <= windowEnd + 0.0001f) {
            val abs = frame.startOffsetSeconds + sample
            val ratio = PriceMath.ratioAt(frame.priceRatios, abs)
            val x = xOf(sample)
            val y = yOf(ratio)
            if (!started) {
                path.moveTo(x, y)
                started = true
                prevX = x
                prevY = y
            } else {
                when (lineStyle) {
                    ChartLineStyle.STEP -> {
                        if (x != prevX) path.lineTo(x, prevY)
                        if (y != prevY) path.lineTo(x, y)
                    }
                    ChartLineStyle.SMOOTH -> path.lineTo(x, y)
                }
                prevX = x
                prevY = y
            }
            sample += sampleStep
        }

        drawPath(
            path = path,
            color = ink,
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Square,
                join = StrokeJoin.Miter
            )
        )

        val nowRatio = PriceMath.ratioAt(
            frame.priceRatios,
            frame.startOffsetSeconds + elapsedSec
        )
        val now = Offset(xOf(elapsedSec), yOf(nowRatio))
        val pointSize = 10f

        if (frame.hasStock) {
            drawRect(
                color = ink,
                topLeft = Offset(now.x - pointSize / 2f, now.y - pointSize / 2f),
                size = Size(pointSize, pointSize)
            )
        } else {
            drawRect(
                color = ink,
                topLeft = Offset(now.x - pointSize / 2f, now.y - pointSize / 2f),
                size = Size(pointSize, pointSize),
                style = Stroke(width = 2.5f)
            )
            drawRect(
                color = colors.background,
                topLeft = Offset(now.x - pointSize / 2f + 2f, now.y - pointSize / 2f + 2f),
                size = Size(pointSize - 4f, pointSize - 4f)
            )
        }

        val nowEpoch = currentTimeMillis()
        val drawOrder = frame.markers.sortedWith(
            compareBy<TradeMarker> { when {
                it.isCancelled -> 0
                it.isPending -> 1
                else -> 2
            } }.thenBy { it.gameTimeSec }
        )
        val occupiedMarks = mutableListOf<Rect>()
        drawOrder.forEach { marker ->
            if (marker.isCancelled && showFullSession) return@forEach
            if (marker.gameTimeSec < plotStart - 0.2f) return@forEach
            if (marker.gameTimeSec > windowEnd + 0.2f && !marker.isPending && !marker.isCancelled) {
                return@forEach
            }
            val center = Offset(xOf(marker.gameTimeSec), yOf(marker.priceRatio))
            val submitCenter = if (marker.hasSubmitAnchor) {
                Offset(xOf(marker.submitGameTimeSec), yOf(marker.submitPriceRatio))
            } else {
                null
            }
            val isFocused = focusTarget?.matches(marker) == true
            val dimOthers = focusTarget != null && !isFocused
            val shakeAge = if (marker.isPending && frame.pendingShakeEpochMs > 0L) {
                (nowEpoch - frame.pendingShakeEpochMs).coerceAtLeast(0L)
            } else {
                Long.MAX_VALUE
            }
            val shake = if (shakeAge < 280L) {
                val t = shakeAge / 280f
                val baseAmp = when (lagFxTier) {
                    LagFxTier.II -> 6.5f
                    LagFxTier.I -> 5f
                    LagFxTier.NONE -> 3.5f
                }
                val amp = baseAmp * (1f - t)
                Offset(
                    x = kotlin.math.sin(shakeAge / 18.0).toFloat() * amp,
                    y = kotlin.math.cos(shakeAge / 15.0).toFloat() * amp * 0.7f
                )
            } else {
                Offset.Zero
            }
            val lagMs = frame.activeOrderLagMs.takeIf { it > 0L }
                ?: GameViewModel.ORDER_LAG_MS
            drawTradeMarker(
                marker = marker,
                center = center + shake,
                submitCenter = submitCenter,
                nowEpochMs = nowEpoch,
                orderLagMs = lagMs,
                lagFxTier = lagFxTier,
                colors = colors,
                displayLanguage = displayLanguage,
                showFullSession = showFullSession,
                showTradeReview = showTradeReview,
                reviewHighlights = reviewHighlights,
                reviewPlacementIndex = reviewHighlights.indexOfFirst {
                    TradeTimingEvaluator.matchesReviewHighlight(marker, it)
                }.coerceAtLeast(0),
                plotTop = topInset,
                plotBottom = h - bottomInset,
                reviewLaneY = reviewLaneY,
                emphasisScale = if (isFocused) emphasisScale else 1f,
                dimmed = dimOthers,
                occupiedMarks = occupiedMarks,
                textMeasurer = textMeasurer
            )
        }

        historyAnchor?.let { (markerX, _) ->
            drawHistoryMarginCallout(
                markerX = markerX,
                eventLine = frame.anchorWhen,
                plotTop = topInset,
                historyLaneY = historyLaneY,
                canvasWidth = w,
                background = colors.background,
                ink = colors.ink,
                textMeasurer = textMeasurer
            )
        }
    }
}

private fun snap(value: Float): Float = round(value).toFloat()

/** 押下点（ゴースト）→ 約定点。ラグの帰属を視線でつなぐ。 */
private fun DrawScope.drawSubmitFillAttribution(
    submit: Offset,
    fill: Offset,
    ink: Color,
    inkStrong: Color
) {
    drawLine(
        color = ink.copy(alpha = 0.75f),
        start = submit,
        end = fill,
        strokeWidth = 1.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f))
    )
    drawCircle(
        color = inkStrong.copy(alpha = 0.35f),
        radius = 7f,
        center = submit,
        style = Stroke(width = 2f)
    )
    drawCircle(
        color = ink.copy(alpha = 0.55f),
        radius = 2.5f,
        center = submit
    )
}

private fun DrawScope.drawTradeMarker(
    marker: TradeMarker,
    center: Offset,
    submitCenter: Offset? = null,
    nowEpochMs: Long,
    orderLagMs: Long,
    lagFxTier: LagFxTier = LagFxTier.NONE,
    colors: LcdColors,
    displayLanguage: DisplayLanguage,
    showFullSession: Boolean = false,
    showTradeReview: Boolean = false,
    reviewHighlights: List<ResultRegretMoment> = emptyList(),
    reviewPlacementIndex: Int = 0,
    plotTop: Float = 0f,
    plotBottom: Float = size.height,
    reviewLaneY: Float = plotBottom,
    emphasisScale: Float = 1f,
    dimmed: Boolean = false,
    occupiedMarks: MutableList<Rect>,
    textMeasurer: TextMeasurer
) {
    val ink = if (dimmed) colors.ink.copy(alpha = 0.38f) else colors.ink
    val inkMuted = if (dimmed) colors.inkMuted.copy(alpha = 0.32f) else colors.inkMuted

    if (marker.isCancelled) {
        if (!showFullSession) {
            drawCancelledOrder(center, colors, dimmed)
        }
        return
    }
    if (marker.isPending) {
        drawOrderTimer(
            center = center,
            submittedAtEpochMs = marker.submittedAtEpochMs,
            nowEpochMs = nowEpochMs,
            orderLagMs = orderLagMs,
            fxTier = lagFxTier,
            colors = colors,
            dimmed = dimmed
        )
        return
    }

    val showLabel = marker.labelUntilEpochMs > nowEpochMs
    if (submitCenter != null &&
        (showLabel || (showTradeReview && !marker.isForcedClose))
    ) {
        drawSubmitFillAttribution(
            submit = submitCenter,
            fill = center,
            ink = inkMuted,
            inkStrong = ink
        )
    }

    val markerSize = 18f * if (emphasisScale > 1f) emphasisScale else 1f
    val layoutMarkerSize = 18f
    if (emphasisScale > 1f) {
        drawMarkerFocusBracket(center, markerSize, ink)
    }
    if (marker.isBuy) {
        val path = Path()
        path.moveTo(center.x, center.y - markerSize)
        path.lineTo(center.x - markerSize * 0.75f, center.y + markerSize * 0.45f)
        path.lineTo(center.x + markerSize * 0.75f, center.y + markerSize * 0.45f)
        path.close()
        drawPath(path, color = ink)
    } else {
        drawSellMarker(center, markerSize, ink)
    }

    if (showLabel) {
        val mark = when {
            marker.fillQuality != null ->
                UiStrings.fillQualityMark(marker.isBuy, marker.fillQuality)
            marker.submitDisplayPrice > 0 ->
                UiStrings.lagQualityMark(
                    isBuy = marker.isBuy,
                    submitPrice = marker.submitDisplayPrice,
                    fillPrice = marker.displayPrice
                )
            else -> ""
        }
        val label = buildString {
            if (mark.isNotEmpty()) {
                append(mark)
                append(' ')
            }
            append(UiStrings.markerLabel(marker.isBuy, marker.displayPrice, displayLanguage))
        }
        val layout = textMeasurer.measure(
            label,
            TextStyle(fontFamily = PixelFont, fontSize = 28f.toSp(), color = ink)
        )
        val textWidth = layout.size.width.toFloat()
        val x = (center.x - textWidth / 2f).coerceIn(4f, (size.width - textWidth - 4f).coerceAtLeast(4f))
        val y = (center.y - markerSize - 10f).coerceAtLeast(36f)
        drawText(layout, topLeft = Offset(x, y - layout.firstBaseline))
    } else if (showTradeReview) {
        val highlight = reviewHighlights.firstOrNull { TradeTimingEvaluator.matchesReviewHighlight(marker, it) }
        if (highlight != null &&
            (marker.timingGrade == TradeTimingGrade.NICE || marker.timingGrade == TradeTimingGrade.MISS)
        ) {
            val markAnchor = Offset(center.x, center.y)
            val visualScale = if (emphasisScale > 1f) emphasisScale * 1.08f else 1f
            val markRect = drawTimingMark(
                center = center,
                markAnchor = markAnchor,
                markerSize = layoutMarkerSize,
                grade = marker.timingGrade,
                colors = colors,
                ink = ink,
                inkMuted = inkMuted,
                placementIndex = reviewPlacementIndex,
                reviewLaneY = reviewLaneY,
                canvasWidth = size.width,
                visualScale = visualScale,
                occupied = occupiedMarks
            )
            occupiedMarks.add(markRect)
        }
    }
}

private fun DrawScope.drawTimingMark(
    center: Offset,
    markAnchor: Offset,
    markerSize: Float,
    grade: TradeTimingGrade,
    colors: LcdColors,
    ink: Color,
    inkMuted: Color,
    placementIndex: Int,
    reviewLaneY: Float,
    canvasWidth: Float,
    visualScale: Float,
    occupied: List<Rect>
): Rect {
    val layoutRadius = 15f
    val pos = findReviewLanePosition(
        markerX = center.x,
        markRadius = layoutRadius,
        placementIndex = placementIndex,
        laneY = reviewLaneY,
        canvasWidth = canvasWidth,
        occupied = occupied
    )
    val drawRadius = layoutRadius * visualScale
    val connectorStart = markAnchorToward(pos, markAnchor, 8f)
    val connectorEnd = markAnchorToward(
        markAnchor,
        Offset(pos.x, reviewLaneY),
        layoutRadius + 6f
    )
    drawLine(
        color = inkMuted.copy(alpha = 0.85f),
        start = connectorStart,
        end = connectorEnd,
        strokeWidth = 1.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
    )
    drawRect(
        color = colors.background.copy(alpha = 0.88f),
        topLeft = Offset(pos.x - drawRadius - 3f, pos.y - drawRadius - 3f),
        size = Size((drawRadius + 3f) * 2f, (drawRadius + 3f) * 2f)
    )
    when (grade) {
        TradeTimingGrade.NICE -> {
            drawCircle(
                color = ink,
                radius = drawRadius,
                center = pos,
                style = Stroke(width = 3f * visualScale)
            )
            drawCircle(color = ink, radius = 3.5f * visualScale, center = pos)
        }
        TradeTimingGrade.MISS -> {
            val r = drawRadius * 0.85f
            val triangle = Path().apply {
                moveTo(pos.x, pos.y - r)
                lineTo(pos.x + r * 0.9f, pos.y + r * 0.6f)
                lineTo(pos.x - r * 0.9f, pos.y + r * 0.6f)
                close()
            }
            drawPath(triangle, color = inkMuted, style = Stroke(width = 3f * visualScale))
        }
        TradeTimingGrade.NONE -> Unit
    }
    return Rect(
        left = pos.x - layoutRadius - 2f,
        top = pos.y - layoutRadius - 2f,
        right = pos.x + layoutRadius + 2f,
        bottom = pos.y + layoutRadius + 2f
    )
}

private fun findReviewLanePosition(
    markerX: Float,
    markRadius: Float,
    placementIndex: Int,
    laneY: Float,
    canvasWidth: Float,
    occupied: List<Rect>
): Offset {
    val xShifts = listOf(0f, 34f, -34f, 52f, -52f, 68f, -68f)
    val markMargin = 6f
    for (shift in xShifts) {
        val pos = Offset(
            x = (markerX + shift).coerceIn(markRadius + 6f, canvasWidth - markRadius - 6f),
            y = laneY
        )
        val rect = Rect(
            pos.x - markRadius - markMargin,
            pos.y - markRadius - markMargin,
            pos.x + markRadius + markMargin,
            pos.y + markRadius + markMargin
        )
        if (occupied.none { it.inflate(markMargin).overlaps(rect) }) {
            return pos
        }
    }
    val fallbackShift = if (placementIndex % 2 == 0) 40f else -40f
    return Offset(
        x = (markerX + fallbackShift).coerceIn(markRadius + 6f, canvasWidth - markRadius - 6f),
        y = laneY
    )
}

/** ハイライト時の ▲▼ 強調。タイミング ◎ レーンと区別する角括弧。 */
private fun DrawScope.drawMarkerFocusBracket(center: Offset, markerSize: Float, ink: Color) {
    val half = markerSize * 1.15f
    val corner = half * 0.42f
    val stroke = 2.5f
    val left = center.x - half
    val right = center.x + half
    val top = center.y - half
    val bottom = center.y + half
    drawLine(ink, Offset(left, top + corner), Offset(left, top), strokeWidth = stroke, cap = StrokeCap.Square)
    drawLine(ink, Offset(left, top), Offset(left + corner, top), strokeWidth = stroke, cap = StrokeCap.Square)
    drawLine(ink, Offset(right - corner, top), Offset(right, top), strokeWidth = stroke, cap = StrokeCap.Square)
    drawLine(ink, Offset(right, top), Offset(right, top + corner), strokeWidth = stroke, cap = StrokeCap.Square)
    drawLine(ink, Offset(left, bottom - corner), Offset(left, bottom), strokeWidth = stroke, cap = StrokeCap.Square)
    drawLine(ink, Offset(left, bottom), Offset(left + corner, bottom), strokeWidth = stroke, cap = StrokeCap.Square)
    drawLine(ink, Offset(right - corner, bottom), Offset(right, bottom), strokeWidth = stroke, cap = StrokeCap.Square)
    drawLine(ink, Offset(right, bottom - corner), Offset(right, bottom), strokeWidth = stroke, cap = StrokeCap.Square)
}

private fun markAnchorToward(target: Offset, from: Offset, inset: Float): Offset {
    val dx = target.x - from.x
    val dy = target.y - from.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy)
    if (len <= inset) return target
    val ratio = (len - inset) / len
    return Offset(from.x + dx * ratio, from.y + dy * ratio)
}

private fun offsetDistance(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun DrawScope.drawSellMarker(center: Offset, markerSize: Float, ink: Color) {
    val path = Path()
    path.moveTo(center.x, center.y + markerSize)
    path.lineTo(center.x - markerSize * 0.75f, center.y - markerSize * 0.45f)
    path.lineTo(center.x + markerSize * 0.75f, center.y - markerSize * 0.45f)
    path.close()
    drawPath(path, color = ink, style = Stroke(width = 2.5f))
}

private fun DrawScope.drawOrderTimer(
    center: Offset,
    submittedAtEpochMs: Long,
    nowEpochMs: Long,
    orderLagMs: Long,
    fxTier: LagFxTier,
    colors: LcdColors,
    dimmed: Boolean = false
) {
    val ink = if (dimmed) colors.ink.copy(alpha = 0.38f) else colors.ink
    val inkMuted = if (dimmed) colors.inkMuted.copy(alpha = 0.32f) else colors.inkMuted
    val lag = orderLagMs.coerceAtLeast(1L).toFloat()
    val progress = if (submittedAtEpochMs <= 0L) {
        0f
    } else {
        ((nowEpochMs - submittedAtEpochMs) / lag).coerceIn(0f, 1f)
    }

    when (fxTier) {
        LagFxTier.NONE -> {
            val radius = 44f
            val topLeft = Offset(center.x - radius, center.y - radius)
            val box = Size(radius * 2f, radius * 2f)
            drawCircle(colors.inkFaint.copy(alpha = 0.25f), radius, center)
            drawArc(inkMuted, -90f, 360f * progress, true, topLeft, box)
            drawCircle(ink, radius, center, style = Stroke(width = 3f))
            drawCircle(ink, 5f, center)
            val rad = degToRad((-90.0 + 360.0 * progress))
            val handLen = radius * 0.82f
            drawLine(
                ink,
                center,
                Offset(
                    center.x + cos(rad).toFloat() * handLen,
                    center.y + sin(rad).toFloat() * handLen
                ),
                strokeWidth = 3f,
                cap = StrokeCap.Square
            )
        }
        LagFxTier.I -> {
            val radius = 52f
            val topLeft = Offset(center.x - radius, center.y - radius)
            val box = Size(radius * 2f, radius * 2f)
            drawCircle(colors.inkFaint.copy(alpha = 0.3f), radius + 6f, center)
            drawCircle(colors.background, radius, center)
            drawArc(ink.copy(alpha = 0.85f), -90f, 360f * progress, true, topLeft, box)
            drawCircle(ink, radius, center, style = Stroke(width = 4f))
            drawCircle(ink, radius - 8f, center, style = Stroke(width = 2f))
            // 目盛り
            for (i in 0 until 12) {
                val a = degToRad(-90.0 + i * 30.0)
                val inner = radius - 4f
                val outer = radius + 2f
                drawLine(
                    ink.copy(alpha = 0.55f),
                    Offset(center.x + cos(a).toFloat() * inner, center.y + sin(a).toFloat() * inner),
                    Offset(center.x + cos(a).toFloat() * outer, center.y + sin(a).toFloat() * outer),
                    strokeWidth = 2f
                )
            }
            val rad = degToRad((-90.0 + 360.0 * progress))
            drawLine(
                ink,
                center,
                Offset(
                    center.x + cos(rad).toFloat() * radius * 0.78f,
                    center.y + sin(rad).toFloat() * radius * 0.78f
                ),
                strokeWidth = 4.5f,
                cap = StrokeCap.Square
            )
            drawCircle(colors.background, 6f, center)
            drawCircle(ink, 6f, center, style = Stroke(width = 2.5f))
        }
        LagFxTier.II -> {
            val radius = 58f
            // 八角外枠
            val oct = Path()
            for (i in 0 until 8) {
                val a = degToRad(-90.0 + i * 45.0)
                val x = center.x + cos(a).toFloat() * (radius + 8f)
                val y = center.y + sin(a).toFloat() * (radius + 8f)
                if (i == 0) oct.moveTo(x, y) else oct.lineTo(x, y)
            }
            oct.close()
            drawPath(oct, ink, style = Stroke(width = 3.5f))
            drawCircle(ink, radius, center)
            drawCircle(colors.background, radius - 3f, center)
            val topLeft = Offset(center.x - radius + 3f, center.y - radius + 3f)
            val box = Size((radius - 3f) * 2f, (radius - 3f) * 2f)
            drawArc(colors.background, -90f, 360f, true, topLeft, box)
            drawArc(ink, -90f, 360f * progress, true, topLeft, box)
            // クロスヘア
            drawLine(
                colors.background,
                Offset(center.x - 10f, center.y),
                Offset(center.x + 10f, center.y),
                strokeWidth = 2.5f
            )
            drawLine(
                colors.background,
                Offset(center.x, center.y - 10f),
                Offset(center.x, center.y + 10f),
                strokeWidth = 2.5f
            )
            val rad = degToRad((-90.0 + 360.0 * progress))
            drawLine(
                colors.background,
                center,
                Offset(
                    center.x + cos(rad).toFloat() * (radius - 10f),
                    center.y + sin(rad).toFloat() * (radius - 10f)
                ),
                strokeWidth = 5f,
                cap = StrokeCap.Square
            )
        }
    }
}

/** TIME UP で約定しなかった注文。小さな × のみ（円なし）。 */
private fun DrawScope.drawCancelledOrder(
    center: Offset,
    colors: LcdColors,
    dimmed: Boolean = false
) {
    val inkMuted = if (dimmed) colors.inkMuted.copy(alpha = 0.28f) else colors.inkMuted
    val arm = 10f
    drawLine(
        color = inkMuted,
        start = Offset(center.x - arm, center.y - arm),
        end = Offset(center.x + arm, center.y + arm),
        strokeWidth = 2.5f,
        cap = StrokeCap.Square
    )
    drawLine(
        color = inkMuted,
        start = Offset(center.x + arm, center.y - arm),
        end = Offset(center.x - arm, center.y + arm),
        strokeWidth = 2.5f,
        cap = StrokeCap.Square
    )
}

private fun DrawScope.drawHistoryMarginGuide(
    markerX: Float,
    plotTop: Float,
    plotBottom: Float,
    ink: Color,
) {
    val guideColor = ink.copy(alpha = 0.42f)
    val tick = 7f
    drawLine(
        color = guideColor,
        start = Offset(markerX, plotTop),
        end = Offset(markerX, plotBottom),
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
    )
    drawLine(
        color = ink.copy(alpha = 0.72f),
        start = Offset(markerX - tick, plotTop),
        end = Offset(markerX + tick, plotTop),
        strokeWidth = 2.5f,
        cap = StrokeCap.Square
    )
    drawLine(
        color = ink.copy(alpha = 0.72f),
        start = Offset(markerX - tick, plotBottom),
        end = Offset(markerX + tick, plotBottom),
        strokeWidth = 2.5f,
        cap = StrokeCap.Square
    )
}

private fun DrawScope.drawHistoryMarginCallout(
    markerX: Float,
    eventLine: String,
    plotTop: Float,
    historyLaneY: Float,
    canvasWidth: Float,
    background: Color,
    ink: Color,
    textMeasurer: TextMeasurer,
) {
    val textSize = 30f
    val padX = 10f
    val padY = 8f

    val layout = textMeasurer.measure(
        eventLine,
        TextStyle(fontFamily = PixelFont, fontSize = textSize.toSp(), fontWeight = FontWeight.Bold, color = ink)
    )

    val boxW = layout.size.width + padX * 2f
    val boxH = padY * 2f + textSize

    val boxTop = (historyLaneY - boxH / 2f).coerceAtLeast(4f)
    val boxLeft = (markerX - boxW / 2f).coerceIn(4f, canvasWidth - boxW - 4f)

    drawRect(
        color = background.copy(alpha = 0.96f),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxW, boxH)
    )
    drawRect(
        color = ink,
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxW, boxH),
        style = Stroke(width = 2f)
    )

    val textX = boxLeft + padX
    val baseline = boxTop + padY + textSize * 0.85f
    drawText(layout, topLeft = Offset(textX, baseline - layout.firstBaseline))

    val connectorX = markerX.coerceIn(boxLeft + 8f, boxLeft + boxW - 8f)
    drawLine(
        color = ink.copy(alpha = 0.65f),
        start = Offset(connectorX, boxTop + boxH),
        end = Offset(connectorX, plotTop),
        strokeWidth = 2f,
        cap = StrokeCap.Square
    )
}
