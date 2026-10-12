package com.urikai.app.game

import kotlin.math.abs

/**
 * 約定時刻付近のローカル極値と比較し、1約定ごとに NICE / MISS を付ける。
 * 厳しめの閾値 + NICE が0件なら最良1件を保証。
 */
object TradeTimingEvaluator {

    private const val WINDOW_SEC = 3
    private const val NICE_ABS_RATIO = 0.9f
    private const val NICE_RANGE_FRAC = 0.09f
    private const val MISS_RANGE_FRAC = 0.22f
    private const val MAX_REVIEW_ITEMS = 3
    private const val TRADE_CONTEXT_MIN_RATIO = 1.2f

    fun applyReviews(
        priceRatios: FloatArray,
        startOffsetSeconds: Int,
        markers: List<TradeMarker>
    ): List<TradeMarker> {
        val scored = markers.map { marker ->
            if (marker.isPending || marker.isCancelled || marker.isForcedClose) {
                marker to null
            } else {
                marker to windowMetrics(
                    priceRatios = priceRatios,
                    startOffsetSeconds = startOffsetSeconds,
                    fillGameSec = marker.gameTimeSec,
                    fillRatio = marker.priceRatio,
                    isBuy = marker.isBuy
                )
            }
        }

        var reviewed = scored.map { (marker, metrics) ->
            if (metrics == null) {
                marker
            } else {
                marker.copy(timingGrade = classifyStrict(metrics))
            }
        }

        return reviewed
    }

    fun countNice(markers: List<TradeMarker>): Int =
        markers.count { it.timingGrade == TradeTimingGrade.NICE && !it.isForcedClose }

    fun countMiss(markers: List<TradeMarker>): Int =
        markers.count { it.timingGrade == TradeTimingGrade.MISS && !it.isForcedClose }

    /**
     * chartPeek 用。ローカル極値 + 直前売買比較でスコアリングし、強い順に最大 [maxItems] 件。
     */
    fun pickReviewHighlights(
        priceRatios: FloatArray,
        startOffsetSeconds: Int,
        markers: List<TradeMarker>,
        maxItems: Int = MAX_REVIEW_ITEMS,
    ): List<ResultRegretMoment> {
        val filled = markers
            .filter { !it.isPending && !it.isCancelled && !it.isForcedClose }
            .sortedBy { it.gameTimeSec }
        if (filled.isEmpty()) return emptyList()

        val metricsByMarker = filled.associateWith { marker ->
            windowMetrics(
                priceRatios = priceRatios,
                startOffsetSeconds = startOffsetSeconds,
                fillGameSec = marker.gameTimeSec,
                fillRatio = marker.priceRatio,
                isBuy = marker.isBuy
            )
        }

        val candidates = buildList {
            filled.forEach { marker ->
                val window = metricsByMarker.getValue(marker)
                val tradeContexts = tradeContextsFor(marker, filled)
                when (marker.timingGrade) {
                    TradeTimingGrade.MISS -> add(
                        ScoredMoment(
                            moment = ResultRegretMoment(
                                gameTimeSec = marker.gameTimeSec,
                                isBuy = marker.isBuy,
                                kind = ResultRegretKind.MISS,
                                tradeContexts = tradeContexts,
                            ),
                            strength = missStrength(window, tradeContexts),
                        )
                    )
                    TradeTimingGrade.NICE -> add(
                        ScoredMoment(
                            moment = ResultRegretMoment(
                                gameTimeSec = marker.gameTimeSec,
                                isBuy = marker.isBuy,
                                kind = ResultRegretKind.BEST_NICE,
                                tradeContexts = tradeContexts,
                            ),
                            strength = niceStrength(window, tradeContexts),
                        )
                    )
                    TradeTimingGrade.NONE -> Unit
                }
            }
        }

        if (candidates.isEmpty()) {
            val nearMiss = filled
                .map { marker -> marker to metricsByMarker.getValue(marker) }
                .map { (marker, window) -> marker to (window.distFromBest - window.niceThreshold) }
                .filter { it.second >= 0f }
                .minByOrNull { it.second }
                ?.first
                ?.let {
                    ResultRegretMoment(
                        gameTimeSec = it.gameTimeSec,
                        isBuy = it.isBuy,
                        kind = ResultRegretKind.NEAR_MISS,
                        tradeContexts = setOf(RegretTradeContextFlag.LOCAL_WINDOW),
                    )
                }
            return listOfNotNull(nearMiss)
        }

        return candidates
            .sortedByDescending { it.strength }
            .take(maxItems.coerceAtLeast(1))
            .map { it.moment }
            .sortedBy { it.gameTimeSec }
    }

    fun matchesReviewHighlight(marker: TradeMarker, moment: ResultRegretMoment): Boolean {
        if (marker.gameTimeSec != moment.gameTimeSec || marker.isBuy != moment.isBuy) return false
        return when (moment.kind) {
            ResultRegretKind.MISS -> marker.timingGrade == TradeTimingGrade.MISS
            ResultRegretKind.BEST_NICE -> marker.timingGrade == TradeTimingGrade.NICE
            ResultRegretKind.NEAR_MISS -> marker.timingGrade == TradeTimingGrade.NONE
        }
    }

    fun pickRegretHighlights(
        priceRatios: FloatArray,
        startOffsetSeconds: Int,
        markers: List<TradeMarker>,
        maxItems: Int = 2
    ): List<ResultRegretMoment> = pickReviewHighlights(
        priceRatios = priceRatios,
        startOffsetSeconds = startOffsetSeconds,
        markers = markers,
        maxItems = maxItems,
    )

    private data class ScoredMoment(
        val moment: ResultRegretMoment,
        val strength: Float,
    )

    private data class WindowMetrics(
        val distFromBest: Float,
        val distFromWorst: Float,
        val niceThreshold: Float,
        val range: Float,
    )

    private fun missStrength(window: WindowMetrics, tradeContexts: Set<RegretTradeContextFlag>): Float {
        val local = window.distFromBest / window.range.coerceAtLeast(0.8f)
        val tradeBonus = tradeContexts.count { it != RegretTradeContextFlag.LOCAL_WINDOW } * 0.4f
        return local + tradeBonus
    }

    private fun niceStrength(window: WindowMetrics, tradeContexts: Set<RegretTradeContextFlag>): Float {
        val tightness = 1f - (window.distFromBest / window.niceThreshold.coerceAtLeast(0.1f)).coerceIn(0f, 1f)
        val tradeBonus = tradeContexts.count { it != RegretTradeContextFlag.LOCAL_WINDOW } * 0.35f
        return tightness + tradeBonus
    }

    private fun tradeContextsFor(
        marker: TradeMarker,
        filledAsc: List<TradeMarker>,
    ): Set<RegretTradeContextFlag> {
        val contexts = mutableSetOf(RegretTradeContextFlag.LOCAL_WINDOW)
        val prior = filledAsc
            .filter { it.gameTimeSec < marker.gameTimeSec && it.isBuy != marker.isBuy }
            .maxByOrNull { it.gameTimeSec }
            ?: return contexts

        if (marker.isBuy && !prior.isBuy &&
            marker.priceRatio > prior.priceRatio + TRADE_CONTEXT_MIN_RATIO
        ) {
            contexts.add(RegretTradeContextFlag.VS_PRIOR_EXIT)
        }
        if (!marker.isBuy && prior.isBuy &&
            marker.priceRatio < prior.priceRatio - TRADE_CONTEXT_MIN_RATIO
        ) {
            contexts.add(RegretTradeContextFlag.VS_PRIOR_ENTRY)
        }
        return contexts
    }

    private fun windowMetrics(
        priceRatios: FloatArray,
        startOffsetSeconds: Int,
        fillGameSec: Float,
        fillRatio: Float,
        isBuy: Boolean
    ): WindowMetrics {
        val centerAbs = startOffsetSeconds + fillGameSec
        var best = fillRatio
        var worst = fillRatio

        for (delta in -WINDOW_SEC..WINDOW_SEC) {
            val sample = PriceMath.ratioAt(priceRatios, centerAbs + delta)
            if (isBuy) {
                if (sample < best) best = sample
                if (sample > worst) worst = sample
            } else {
                if (sample > best) best = sample
                if (sample < worst) worst = sample
            }
        }

        val range = (worst - best).coerceAtLeast(0.8f)
        val niceThreshold = minOf(NICE_ABS_RATIO, range * NICE_RANGE_FRAC)
        return WindowMetrics(
            distFromBest = abs(fillRatio - best),
            distFromWorst = abs(fillRatio - worst),
            niceThreshold = niceThreshold,
            range = range
        )
    }

    private fun classifyStrict(metrics: WindowMetrics): TradeTimingGrade {
        if (metrics.distFromBest <= metrics.niceThreshold) {
            return TradeTimingGrade.NICE
        }
        if (metrics.distFromWorst <= metrics.range * MISS_RANGE_FRAC) {
            return TradeTimingGrade.MISS
        }
        return TradeTimingGrade.NONE
    }
}
