package com.urikai.app.game

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 売買＋現金単利を含む理論最大資産（HUMAN MAX）とシンクロ率。
 *
 * 分母は「平滑化したマクロ山・谷を、1秒ラグ＋1秒クールで完璧に取った資産」。
 * 下落で見送る方が良い場合は現金放置（利子込み）を下限にする。
 * ノイズヒゲの一点必中は分母に含めない → 実プレイで超えれば OVER SYNC（100%超）。
 *
 * リザルトの主役は最終資産。SYNC/★は参考指標（到達の目安）。
 */
object SyncCalculator {

    /** 理論最大が使う注文ラグ（秒）。プレイヤー既定と一致。 */
    const val ORDER_LAG_SECONDS: Int = 1

    /** 理論最大が使う約定後クールダウン（秒）。プレイヤー既定と一致。 */
    const val ORDER_COOLDOWN_SECONDS: Int = 1

    /** 価格平滑化の移動平均窓（秒）。 */
    const val SMOOTH_MA_SECONDS: Int = 3

    /** マクロピボットとみなす最小相対振幅。 */
    const val MIN_SWING_FRACTION: Float = 0.02f

    fun theoreticalMaxAsset(
        priceRatios: FloatArray,
        startOffsetSeconds: Int,
        durationSeconds: Int = GameViewModel.GAME_DURATION_SECONDS,
        initialAsset: Int = GameViewModel.INITIAL_ASSET,
        lagSeconds: Int = ORDER_LAG_SECONDS,
        cooldownSeconds: Int = ORDER_COOLDOWN_SECONDS,
        maSeconds: Int = SMOOTH_MA_SECONDS,
        minSwingFraction: Float = MIN_SWING_FRACTION,
    ): Int {
        if (priceRatios.isEmpty()) return initialAsset

        val cashOnly = cashOnlyAsset(initialAsset, durationSeconds)
        val n = durationSeconds + 1
        val prices = FloatArray(n) { sec ->
            sampleDisplayPrice(priceRatios, startOffsetSeconds + sec)
        }
        val mas = movingAverage(prices, maSeconds)
        val pivots = findMacroPivots(mas, minSwingFraction)

        var cash = initialAsset.toFloat()
        var shares = 0f
        var holding = false
        var interestPrincipal = initialAsset
        var readyAtSec = 0
        var pendingFillSec: Int? = null
        var pendingIsBuy = false
        var pivotIndex = 0

        for (sec in 0..durationSeconds) {
            if (!holding && sec < durationSeconds) {
                cash += CashInterest.perSecondAmount(interestPrincipal)
            }

            val fillDue = pendingFillSec
            if (fillDue != null && fillDue == sec) {
                val fillPrice = prices[sec]
                if (pendingIsBuy) {
                    if (fillPrice > 0f) {
                        shares = cash / fillPrice
                        cash = 0f
                        holding = true
                    }
                } else {
                    cash = shares * fillPrice
                    shares = 0f
                    holding = false
                    interestPrincipal = cash.roundToPositiveInt()
                }
                pendingFillSec = null
                readyAtSec = sec + cooldownSeconds
            }

            while (pendingFillSec == null && sec >= readyAtSec && pivotIndex < pivots.size) {
                val (pivotSec, kind) = pivots[pivotIndex]
                val submitSec = pivotSec - lagSeconds
                val fillSec = pivotSec
                if (fillSec > durationSeconds) {
                    pivotIndex++
                    continue
                }
                if (sec > submitSec) {
                    pivotIndex++
                    continue
                }
                if (sec < submitSec) break

                when {
                    kind == PivotKind.LO && !holding -> {
                        pendingFillSec = fillSec
                        pendingIsBuy = true
                        pivotIndex++
                        break
                    }
                    kind == PivotKind.HI && holding -> {
                        pendingFillSec = fillSec
                        pendingIsBuy = false
                        pivotIndex++
                        break
                    }
                    else -> pivotIndex++
                }
            }
        }

        if (holding) {
            cash = shares * prices[durationSeconds]
        }
        val traded = cash.roundToPositiveInt()
        return maxOf(traded, cashOnly)
    }

    fun syncPercent(playerAsset: Int, theoreticalMax: Int): Float {
        if (theoreticalMax <= 0) return 0f
        return (playerAsset.toFloat() / theoreticalMax.toFloat() * 100f).coerceAtLeast(0f)
    }

    private enum class PivotKind { LO, HI }

    private fun cashOnlyAsset(initialAsset: Int, durationSeconds: Int): Int {
        var cash = initialAsset
        val principal = initialAsset
        repeat(durationSeconds) {
            cash += CashInterest.perSecondAmount(principal)
        }
        return cash
    }

    private fun movingAverage(prices: FloatArray, maSeconds: Int): FloatArray {
        val window = maSeconds.coerceAtLeast(1)
        return FloatArray(prices.size) { i ->
            val from = (i - window + 1).coerceAtLeast(0)
            var sum = 0f
            for (j in from..i) sum += prices[j]
            sum / (i - from + 1)
        }
    }

    private fun findMacroPivots(
        mas: FloatArray,
        minSwingFraction: Float,
    ): List<Pair<Int, PivotKind>> {
        if (mas.size < 3) return emptyList()
        val raw = ArrayList<Triple<Int, PivotKind, Float>>()
        for (i in 1 until mas.lastIndex) {
            when {
                mas[i] <= mas[i - 1] && mas[i] <= mas[i + 1] ->
                    raw.add(Triple(i, PivotKind.LO, mas[i]))
                mas[i] >= mas[i - 1] && mas[i] >= mas[i + 1] ->
                    raw.add(Triple(i, PivotKind.HI, mas[i]))
            }
        }
        if (raw.isEmpty()) return emptyList()

        val filtered = ArrayList<Triple<Int, PivotKind, Float>>()
        filtered.add(raw[0])
        for (idx in 1 until raw.size) {
            val (t, kind, val_) = raw[idx]
            val (_, lastKind, lastVal) = filtered.last()
            if (kind == lastKind) {
                val replace = when (kind) {
                    PivotKind.LO -> val_ < lastVal
                    PivotKind.HI -> val_ > lastVal
                }
                if (replace) filtered[filtered.lastIndex] = Triple(t, kind, val_)
                continue
            }
            if (lastVal <= 0f) continue
            if (abs(val_ - lastVal) / lastVal >= minSwingFraction) {
                filtered.add(Triple(t, kind, val_))
            }
        }
        return filtered.map { it.first to it.second }
    }

    private fun sampleDisplayPrice(priceRatios: FloatArray, absoluteSeconds: Int): Float {
        val ratio = PriceMath.ratioAt(priceRatios, absoluteSeconds.toFloat())
        return PriceMath.ratioToDisplayPrice(ratio).toFloat()
    }

    private fun Float.roundToPositiveInt(): Int =
        roundToInt().coerceAtLeast(0)
}
