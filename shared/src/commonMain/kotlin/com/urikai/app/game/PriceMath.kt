package com.urikai.app.game

/**
 * 画面表示用の擬似株価。
 * CSV の price_ratio（開始=100）を初期株価 ¥1,000 に変換する。
 */
object PriceMath {
    const val BASE_RATIO = 100f
    const val INITIAL_DISPLAY_PRICE = 1_000

    fun ratioToDisplayPrice(ratio: Float): Int {
        return (ratio / BASE_RATIO * INITIAL_DISPLAY_PRICE).toInt().coerceAtLeast(1)
    }

    /** 内部株価を、開始時点 = quoteBase となる見せ株価へ。 */
    fun toQuotePrice(internalPrice: Int, openPrice: Int, quoteBase: Int): Int {
        if (openPrice <= 0) return internalPrice
        return (internalPrice.toLong() * quoteBase / openPrice).toInt().coerceAtLeast(1)
    }

    /** 開始時点からの株価の増減率（%）。 */
    fun quoteChangePercent(internalPrice: Int, openPrice: Int): Float {
        if (openPrice <= 0) return 0f
        return (internalPrice - openPrice) * 100f / openPrice
    }

    fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    /**
     * ステージ絶対時刻（秒, 小数可）での ratio。
     * second 0..69 の点列を線形補間する。
     */
    fun ratioAt(priceRatios: FloatArray, absoluteSeconds: Float): Float {
        if (absoluteSeconds <= 0f) return priceRatios.first()
        val lastIndex = priceRatios.lastIndex
        if (absoluteSeconds >= lastIndex.toFloat()) return priceRatios.last()

        val i0 = absoluteSeconds.toInt()
        val frac = absoluteSeconds - i0
        return lerp(priceRatios[i0], priceRatios[i0 + 1], frac)
    }
}
