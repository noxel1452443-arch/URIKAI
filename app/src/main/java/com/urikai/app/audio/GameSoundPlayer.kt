package com.urikai.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.urikai.app.R
import com.urikai.app.game.CoolFxTier
import com.urikai.app.game.FillQuality

/**
 * WAV + SoundPool。判定の明暗・レリック強化・演出連動を担う。
 */
class GameSoundPlayer(context: Context) {

    @Volatile
    var enabled: Boolean = true

    /** CONFIG の SE 段階倍率。各SE相対音量に掛ける。 */
    @Volatile
    var volumeScale: Float = 1f

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(12)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val buyId: Int = soundPool.load(context, R.raw.se_buy, 1)
    private val sellId: Int = soundPool.load(context, R.raw.se_sell, 1)
    private val missId: Int = soundPool.load(context, R.raw.se_miss, 1)
    private val okId: Int = soundPool.load(context, R.raw.se_ok, 1)
    private val greatId: Int = soundPool.load(context, R.raw.se_great, 1)
    private val perfectId: Int = soundPool.load(context, R.raw.se_perfect, 1)
    private val fantasticId: Int = soundPool.load(context, R.raw.se_fantastic, 1)
    private val judgeGreatId: Int = soundPool.load(context, R.raw.se_judge_great, 1)
    private val judgePerfectId: Int = soundPool.load(context, R.raw.se_judge_perfect, 1)
    private val judgeFantasticId: Int = soundPool.load(context, R.raw.se_judge_fantastic, 1)
    private val timeUpId: Int = soundPool.load(context, R.raw.se_timeup, 1)
    private val tickId: Int = soundPool.load(context, R.raw.se_tick, 1)
    private val chargeIId: Int = soundPool.load(context, R.raw.se_charge_i, 1)
    private val chargeIiId: Int = soundPool.load(context, R.raw.se_charge_ii, 1)
    private val countdownId: Int = soundPool.load(context, R.raw.se_countdown, 1)
    private val finishId: Int = soundPool.load(context, R.raw.se_finish, 1)
    private val payoutCountId: Int = soundPool.load(context, R.raw.se_payout_count, 1)
    private val payoutClangId: Int = soundPool.load(context, R.raw.se_payout_clang, 1)
    private val typeId: Int = soundPool.load(context, R.raw.se_type, 1)
    private val starId: Int = soundPool.load(context, R.raw.se_star, 1)
    private val stampId: Int = soundPool.load(context, R.raw.se_stamp, 1)
    private val panelId: Int = soundPool.load(context, R.raw.se_panel, 1)
    private val uiSelectId: Int = soundPool.load(context, R.raw.se_ui_select, 1)
    private val uiConfirmId: Int = soundPool.load(context, R.raw.se_ui_confirm, 1)

    fun playChargeReady(tier: CoolFxTier = CoolFxTier.NONE) {
        when (tier) {
            CoolFxTier.NONE -> play(tickId, rate = 1.22f, volume = 0.58f)
            CoolFxTier.I -> play(chargeIId, rate = 1f, volume = 0.78f)
            CoolFxTier.II -> play(chargeIiId, rate = 1f, volume = 0.92f)
        }
    }

    fun playOrderTick() = play(tickId, rate = 1f, volume = 0.42f)

    /** 軽い選択（トグル・チップ・図鑑項目など） */
    fun playUiSelect() = play(uiSelectId, rate = 1f, volume = 0.55f)

    /** 決定（大きなボタン・画面進行） */
    fun playUiConfirm() = play(uiConfirmId, rate = 1f, volume = 0.72f)

    @Deprecated("Use playUiSelect / playUiConfirm", ReplaceWith("playUiSelect()"))
    fun playUiClick() = playUiSelect()

    fun playCountdown(digit: Int) {
        val rate = (0.82f + (6 - digit.coerceIn(1, 5)) * 0.09f).coerceIn(0.75f, 1.25f)
        play(countdownId, rate = rate, volume = 0.72f)
    }

    /**
     * 終盤の心拍 SE（se_tick 流用）。intensity 0..1 で音量・ピッチを上げる。
     */
    fun playEndgameHeart(intensity: Float) {
        val i = intensity.coerceIn(0f, 1f)
        play(
            tickId,
            rate = 0.92f + 0.28f * i,
            volume = 0.28f + 0.22f * i
        )
    }

    fun playFinish() = play(finishId, rate = 1f, volume = 0.92f)

    fun playBuy(quality: FillQuality = FillQuality.OK, judgeEnhanced: Boolean = false) {
        playQuality(quality, judgeEnhanced, buySide = true)
    }

    fun playSell(quality: FillQuality = FillQuality.OK, judgeEnhanced: Boolean = false) {
        playQuality(quality, judgeEnhanced, buySide = false)
    }

    private fun playQuality(quality: FillQuality, judgeEnhanced: Boolean, buySide: Boolean) {
        when (quality) {
            FillQuality.MISS -> play(missId, rate = 1f, volume = 0.88f)
            FillQuality.OK -> {
                if (buySide) {
                    play(buyId, rate = 1f, volume = 0.72f)
                } else {
                    play(okId, rate = 1f, volume = 0.78f)
                }
            }
            FillQuality.GREAT -> {
                val id = if (judgeEnhanced) judgeGreatId else greatId
                play(id, rate = 1f, volume = 0.92f)
            }
            FillQuality.PERFECT -> {
                val id = if (judgeEnhanced) judgePerfectId else perfectId
                play(id, rate = 1f, volume = 1f)
            }
            FillQuality.FANTASTIC -> {
                val id = if (judgeEnhanced) judgeFantasticId else fantasticId
                play(id, rate = 1f, volume = 1f)
                if (!judgeEnhanced) {
                    play(finishId, rate = 1.22f, volume = 0.55f)
                }
            }
        }
    }

    fun playTimeUp(syncPercent: Float) {
        val rate = when {
            syncPercent >= 100f -> 1.22f
            syncPercent >= 90f -> 1.18f
            syncPercent >= 75f -> 1.08f
            else -> 0.90f
        }
        val volume = when {
            syncPercent >= 100f -> 1f
            syncPercent >= 90f -> 1f
            syncPercent >= 75f -> 0.92f
            else -> 0.8f
        }
        play(timeUpId, rate = rate, volume = volume)
    }

    /** 出金カウント：progress でピッチ上昇。 */
    fun playPayoutCount(progress: Float, profitOrEven: Boolean) {
        val p = progress.coerceIn(0f, 1f)
        val rate = if (profitOrEven) {
            0.72f + 0.85f * p
        } else {
            1.05f - 0.35f * p
        }
        play(payoutCountId, rate = rate.coerceIn(0.55f, 1.9f), volume = 0.42f + 0.28f * p)
    }

    fun playPayoutClang(profitOrEven: Boolean) {
        if (profitOrEven) {
            play(payoutClangId, rate = 1f, volume = 1f)
        } else {
            play(missId, rate = 0.85f, volume = 0.85f)
            play(timeUpId, rate = 0.88f, volume = 0.55f)
        }
    }

    fun playResultType() = play(typeId, rate = 1f, volume = 0.38f)

    fun playResultStar(index: Int, total: Int) {
        val t = if (total <= 1) 1f else index.toFloat() / (total - 1).coerceAtLeast(1)
        play(starId, rate = (0.85f + 0.45f * t).coerceIn(0.7f, 1.6f), volume = 0.62f)
    }

    fun playResultStamp() = play(stampId, rate = 1f, volume = 0.9f)

    fun playResultPanelIn() = play(panelId, rate = 1f, volume = 0.55f)

    private fun play(soundId: Int, rate: Float, volume: Float = 1f) {
        if (!enabled || soundId == 0 || volumeScale <= 0.001f) return
        val v = (volume * volumeScale).coerceIn(0f, 1f)
        soundPool.play(soundId, v, v, 1, 0, rate.coerceIn(0.5f, 2.0f))
    }

    fun release() {
        soundPool.release()
    }
}
