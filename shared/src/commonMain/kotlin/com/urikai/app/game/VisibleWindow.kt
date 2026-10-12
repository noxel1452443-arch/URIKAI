package com.urikai.app.game

/**
 * プレイ中チャートの横軸可視窓。常時 20s 固定。
 * 終盤の焦り感は TIME パルス／心拍 SE・触覚側（窓圧縮はしない）。
 */
object VisibleWindow {

    const val AT_START_SEC = 20f

    fun targetSeconds(elapsedSec: Float): Float = AT_START_SEC

    fun timePulsePeak(second: Int, finishing: Boolean): Float {
        if (finishing) return 1.28f
        return when {
            second >= GameViewModel.ENDGAME_COUNTDOWN_FROM_SECOND -> 1.28f
            second >= GameViewModel.ENDGAME_PULSE_FROM_SECOND -> 1.20f
            second >= GameViewModel.ENDGAME_TENSION_FROM_SECOND -> 1.12f
            else -> 1f
        }
    }

    fun shouldPulseTime(second: Int, finishing: Boolean): Boolean =
        finishing || second >= GameViewModel.ENDGAME_TENSION_FROM_SECOND
}
