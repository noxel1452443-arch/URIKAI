package com.urikai.app.game

/**
 * 保有中の含み損益ゾーン（HUD 心情マーク用）。
 * ±3% を閾値にし、数字を増やさず上下を伝える。
 */
enum class HoldingPnlMood {
    UP,
    FLAT,
    DOWN,
}

fun ChartFrame.holdingEntryPrice(): Int? {
    if (!hasStock) return null
    return markers
        .asReversed()
        .firstOrNull { it.isBuy && !it.isPending && !it.isCancelled }
        ?.displayPrice
        ?.takeIf { it > 0 }
}

fun holdingPnlMood(currentPrice: Int, entryPrice: Int): HoldingPnlMood {
    if (entryPrice <= 0) return HoldingPnlMood.FLAT
    val ratio = (currentPrice - entryPrice).toFloat() / entryPrice.toFloat()
    return when {
        ratio >= 0.03f -> HoldingPnlMood.UP
        ratio <= -0.03f -> HoldingPnlMood.DOWN
        else -> HoldingPnlMood.FLAT
    }
}
