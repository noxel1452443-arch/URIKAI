package com.urikai.app.feedback

import com.urikai.app.game.FillQuality
import com.urikai.app.platform.HapticBackend

/**
 * 約定・終了時の短い触覚。画面追加なし。
 */
class HapticPlayer(private val backend: HapticBackend) {

    var enabled: Boolean = true

    fun tickChargeReady() = vibrate(24, amplitude = 175)

    fun tickBuy(quality: FillQuality = FillQuality.OK) = tickFill(isBuy = true, quality = quality)

    fun tickSell(quality: FillQuality = FillQuality.OK) = tickFill(isBuy = false, quality = quality)

    fun tickPayoutClaim(big: Boolean) {
        if (big) {
            vibrate(48, amplitude = 220)
        } else {
            vibrate(32, amplitude = 130)
        }
    }

    fun tickTimeUp(highSync: Boolean) {
        if (highSync) {
            vibrate(40, amplitude = 200)
        } else {
            vibrate(35, amplitude = 140)
        }
    }

    fun tickLifeLoss() {
        vibrateWave(
            timingsMs = longArrayOf(0, 36, 50, 48),
            amplitudes = intArrayOf(0, 200, 0, 255)
        )
    }

    /** 終盤心拍。intensity 0..1 で長さ・振幅を上げる。 */
    fun tickEndgameHeart(intensity: Float) {
        val i = intensity.coerceIn(0f, 1f)
        vibrate(
            durationMs = (12L + (10f * i).toLong()).coerceIn(12L, 22L),
            amplitude = (90 + (90f * i).toInt()).coerceIn(90, 180)
        )
    }

    private fun tickFill(isBuy: Boolean, quality: FillQuality) {
        val baseDuration = if (isBuy) 18L else 28L
        val baseAmp = if (isBuy) 120 else 160
        when (quality) {
            FillQuality.FANTASTIC -> vibrateWave(
                timingsMs = longArrayOf(0, 28, 36, 28, 36, 28, 40, 60),
                amplitudes = intArrayOf(0, 255, 0, 240, 0, 255, 0, 255)
            )
            FillQuality.PERFECT -> vibrate(
                durationMs = (baseDuration * 0.7f).toLong().coerceAtLeast(12L),
                amplitude = 255
            )
            FillQuality.GREAT -> vibrate(
                durationMs = (baseDuration * 0.75f).toLong().coerceAtLeast(12L),
                amplitude = (baseAmp + 55).coerceAtMost(255)
            )
            FillQuality.OK -> vibrate(baseDuration, amplitude = baseAmp)
            FillQuality.MISS -> vibrate(
                durationMs = (baseDuration * 1.45f).toLong().coerceAtMost(42L),
                amplitude = (baseAmp * 0.55f).toInt().coerceAtLeast(55)
            )
        }
    }

    private fun vibrateWave(timingsMs: LongArray, amplitudes: IntArray) {
        if (!enabled) return
        backend.waveform(timingsMs, amplitudes)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        if (!enabled) return
        backend.oneShot(durationMs, amplitude.coerceIn(1, 255))
    }
}
