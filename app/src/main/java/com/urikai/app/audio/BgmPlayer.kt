package com.urikai.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.urikai.app.R
import com.urikai.app.platform.BgmController
import kotlin.random.Random

/**
 * アンビエントBGM。プレイ中以外・BGM音量ON・フォアグラウンド時のみ。
 * 曲末フェード → 約10秒ギャップ → ランダム次曲（直近の連続回避）。
 */
class BgmPlayer(context: Context) : BgmController {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var targetVolume: Float = DEFAULT_VOLUME

    @Volatile
    var enabled: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            mainHandler.post { reconcile() }
        }

    private var foreground: Boolean = true
    private var ambientAllowed: Boolean = true

    private var player: MediaPlayer? = null
    private var lastTrackIndex: Int = -1
    private var fading: Boolean = false
    private var fadeStep: Float = DEFAULT_VOLUME / FADE_STEPS

    private val tracks = intArrayOf(
        R.raw.bgm_ambient,
        R.raw.bgm_bossa,
        R.raw.bgm_calm,
    )

    private val fadeStepRunnable = object : Runnable {
        override fun run() {
            val p = player ?: return
            if (!fading) return
            try {
                val next = (currentVolume - fadeStep).coerceAtLeast(0f)
                currentVolume = next
                p.setVolume(next, next)
                if (next <= 0.001f) {
                    finishFadeAndScheduleGap()
                } else {
                    mainHandler.postDelayed(this, FADE_STEP_MS)
                }
            } catch (_: IllegalStateException) {
                finishFadeAndScheduleGap()
            }
        }
    }

    private val startFadeRunnable = Runnable { beginFadeOut() }
    private val gapThenPlayRunnable = Runnable {
        if (shouldPlay()) startNextTrack()
    }

    private var currentVolume: Float = DEFAULT_VOLUME
    private var resumeScheduled: Boolean = false

    override fun setTargetVolume(volume: Float) {
        mainHandler.post {
            targetVolume = volume.coerceIn(0f, 1f)
            fadeStep = (targetVolume / FADE_STEPS).coerceAtLeast(0.001f)
            enabled = targetVolume > 0.001f
            if (player != null && !fading && enabled) {
                currentVolume = targetVolume
                try {
                    player?.setVolume(targetVolume, targetVolume)
                } catch (_: IllegalStateException) {
                }
            }
            reconcile()
        }
    }

    override fun setForeground(isForeground: Boolean) {
        mainHandler.post {
            foreground = isForeground
            reconcile()
        }
    }

    override fun setAmbientAllowed(allowed: Boolean) {
        mainHandler.post {
            ambientAllowed = allowed
            reconcile()
        }
    }

    override fun release() {
        mainHandler.post {
            cancelScheduled()
            releasePlayer()
        }
    }

    private fun shouldPlay(): Boolean =
        enabled && foreground && ambientAllowed && targetVolume > 0.001f

    private fun reconcile() {
        if (shouldPlay()) {
            if (player == null && !resumeScheduled) {
                resumeScheduled = true
                mainHandler.postDelayed(gapThenPlayRunnable, RESUME_GAP_MS)
            }
        } else {
            cancelScheduled()
            if (player != null) {
                beginFadeOut(fast = true)
            }
        }
    }

    private fun startNextTrack() {
        resumeScheduled = false
        if (!shouldPlay()) return
        releasePlayer()
        fading = false
        val index = pickTrackIndex()
        lastTrackIndex = index
        val resId = tracks[index]
        val mp = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnCompletionListener {
                mainHandler.post {
                    releasePlayer()
                    scheduleGap()
                }
            }
            setOnErrorListener { _, _, _ ->
                mainHandler.post {
                    releasePlayer()
                    scheduleGap()
                }
                true
            }
        }
        try {
            val afd = appContext.resources.openRawResourceFd(resId)
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.prepare()
            currentVolume = targetVolume
            mp.setVolume(targetVolume, targetVolume)
            mp.start()
            player = mp
            val duration = mp.duration.coerceAtLeast(0)
            val fadeAt = (duration - FADE_MS.toInt()).coerceAtLeast(0)
            mainHandler.postDelayed(startFadeRunnable, fadeAt.toLong())
        } catch (_: Exception) {
            mp.release()
            player = null
            scheduleGap()
        }
    }

    private fun pickTrackIndex(): Int {
        if (tracks.size <= 1) return 0
        var next: Int
        do {
            next = Random.nextInt(tracks.size)
        } while (next == lastTrackIndex)
        return next
    }

    private fun beginFadeOut(fast: Boolean = false) {
        val p = player ?: return
        if (fading) return
        fading = true
        mainHandler.removeCallbacks(startFadeRunnable)
        mainHandler.removeCallbacks(fadeStepRunnable)
        if (fast) {
            try {
                p.setVolume(0f, 0f)
            } catch (_: IllegalStateException) {
            }
            currentVolume = 0f
            finishFadeAndScheduleGap(afterStopOnly = true)
            return
        }
        fadeStep = (currentVolume / FADE_STEPS).coerceAtLeast(0.001f)
        mainHandler.post(fadeStepRunnable)
    }

    private fun finishFadeAndScheduleGap(afterStopOnly: Boolean = false) {
        fading = false
        mainHandler.removeCallbacks(fadeStepRunnable)
        releasePlayer()
        if (shouldPlay() && !afterStopOnly) {
            scheduleGap()
        }
    }

    private fun scheduleGap() {
        mainHandler.removeCallbacks(gapThenPlayRunnable)
        resumeScheduled = false
        if (shouldPlay()) {
            resumeScheduled = true
            mainHandler.postDelayed(gapThenPlayRunnable, GAP_MS)
        }
    }

    private fun cancelScheduled() {
        mainHandler.removeCallbacks(startFadeRunnable)
        mainHandler.removeCallbacks(fadeStepRunnable)
        mainHandler.removeCallbacks(gapThenPlayRunnable)
        fading = false
        resumeScheduled = false
    }

    private fun releasePlayer() {
        mainHandler.removeCallbacks(startFadeRunnable)
        mainHandler.removeCallbacks(fadeStepRunnable)
        try {
            player?.stop()
        } catch (_: IllegalStateException) {
        }
        try {
            player?.release()
        } catch (_: IllegalStateException) {
        }
        player = null
        fading = false
    }

    companion object {
        /** 旧 BASE 0.15 より少し上げた既定（MID） */
        const val DEFAULT_VOLUME = 0.32f
        private const val FADE_MS = 1800L
        private const val FADE_STEP_MS = 50L
        private const val FADE_STEPS = FADE_MS / FADE_STEP_MS
        private const val GAP_MS = 10_000L
        private const val RESUME_GAP_MS = 800L
    }
}
