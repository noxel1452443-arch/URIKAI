package com.urikai.app.game

/** chartPeek でハイライトと連動する約定キー。 */
data class TimingFocus(
    val gameTimeSec: Float,
    val isBuy: Boolean,
)

fun ResultRegretMoment.toTimingFocus(): TimingFocus =
    TimingFocus(gameTimeSec = gameTimeSec, isBuy = isBuy)

fun TimingFocus.matches(marker: TradeMarker): Boolean =
    !marker.isPending &&
        !marker.isCancelled &&
        marker.gameTimeSec == gameTimeSec &&
        marker.isBuy == isBuy
