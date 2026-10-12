package com.urikai.app.platform

private fun jsPlaySe(name: String, rate: Double, volume: Double): Unit =
    js("{ if (window.urikaiAudio) window.urikaiAudio.playSe(name, rate, volume); }")

private fun jsSetBgmVolume(volume: Double): Unit =
    js("{ if (window.urikaiAudio) window.urikaiAudio.setBgmVolume(volume); }")

private fun jsSetForeground(value: Boolean): Unit =
    js("{ if (window.urikaiAudio) window.urikaiAudio.setForeground(value); }")

private fun jsSetAmbientAllowed(value: Boolean): Unit =
    js("{ if (window.urikaiAudio) window.urikaiAudio.setAmbientAllowed(value); }")

private fun jsVibrate(patternCsv: String): Unit =
    js("{ if (window.urikaiAudio) window.urikaiAudio.vibrate(patternCsv.split(',').map(Number)); }")

/** audio.js の Web Audio で効果音を鳴らす。 */
object WebSoundBackend : SoundBackend {
    override fun play(effect: SoundEffect, rate: Float, volume: Float) {
        jsPlaySe(effect.fileName, rate.toDouble(), volume.toDouble())
    }
}

object WebBgm : BgmController {
    override fun setTargetVolume(volume: Float) = jsSetBgmVolume(volume.toDouble())
    override fun setForeground(isForeground: Boolean) = jsSetForeground(isForeground)
    override fun setAmbientAllowed(allowed: Boolean) = jsSetAmbientAllowed(allowed)
    override fun release() = jsSetAmbientAllowed(false)
}

/** navigator.vibrate（Android Chrome のみ有効。iOS Safari は非対応で無音）。振幅は表現できない。 */
object WebHaptics : HapticBackend {
    override fun oneShot(durationMs: Long, amplitude: Int) = jsVibrate(durationMs.toString())

    override fun waveform(timingsMs: LongArray, amplitudes: IntArray) {
        // Android の (時間, 振幅) 列 → navigator.vibrate の [振動, 休止, 振動, ...] 列
        val pattern = mutableListOf<Long>()
        var on = true
        for (i in timingsMs.indices) {
            val segmentOn = amplitudes.getOrElse(i) { 0 } > 0
            val ms = timingsMs[i]
            if (pattern.isEmpty() && !segmentOn) {
                if (ms > 0) {
                    pattern += 0L
                    pattern += ms
                    on = true
                }
                continue
            }
            if (segmentOn == on) {
                pattern += ms
                on = !on
            } else if (pattern.isNotEmpty()) {
                pattern[pattern.lastIndex] = pattern.last() + ms
            }
        }
        if (pattern.isNotEmpty()) jsVibrate(pattern.joinToString(","))
    }
}
