package com.urikai.app.game

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 現金保有中の単利（元本ベース・複利なし）。
 * 60秒フル現金で約4.5%（¥10,000 → +¥450）。
 */
object CashInterest {

    /** 60秒間ずっと現金で保有した場合の利率（単利総量） */
    const val TOTAL_RATE = 0.045f

    fun perSecondAmount(principal: Int): Int {
        if (principal <= 0) return 0
        return max(
            1,
            (principal * TOTAL_RATE / GameViewModel.GAME_DURATION_SECONDS).roundToInt()
        )
    }
}
