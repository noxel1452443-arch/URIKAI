package com.urikai.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.urikai.app.R
import com.urikai.app.platform.SoundBackend
import com.urikai.app.platform.SoundEffect

class SoundPoolBackend(context: Context) : SoundBackend {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(12)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids: Map<SoundEffect, Int> = SoundEffect.entries.associateWith { effect ->
        soundPool.load(context, rawRes(effect), 1)
    }

    override fun play(effect: SoundEffect, rate: Float, volume: Float) {
        val id = ids[effect] ?: return
        if (id == 0) return
        soundPool.play(id, volume, volume, 1, 0, rate)
    }

    override fun release() {
        soundPool.release()
    }

    private fun rawRes(effect: SoundEffect): Int = when (effect) {
        SoundEffect.BUY -> R.raw.se_buy
        SoundEffect.SELL -> R.raw.se_sell
        SoundEffect.MISS -> R.raw.se_miss
        SoundEffect.OK -> R.raw.se_ok
        SoundEffect.GREAT -> R.raw.se_great
        SoundEffect.PERFECT -> R.raw.se_perfect
        SoundEffect.FANTASTIC -> R.raw.se_fantastic
        SoundEffect.JUDGE_GREAT -> R.raw.se_judge_great
        SoundEffect.JUDGE_PERFECT -> R.raw.se_judge_perfect
        SoundEffect.JUDGE_FANTASTIC -> R.raw.se_judge_fantastic
        SoundEffect.TIME_UP -> R.raw.se_timeup
        SoundEffect.TICK -> R.raw.se_tick
        SoundEffect.CHARGE_I -> R.raw.se_charge_i
        SoundEffect.CHARGE_II -> R.raw.se_charge_ii
        SoundEffect.COUNTDOWN -> R.raw.se_countdown
        SoundEffect.FINISH -> R.raw.se_finish
        SoundEffect.PAYOUT_COUNT -> R.raw.se_payout_count
        SoundEffect.PAYOUT_CLANG -> R.raw.se_payout_clang
        SoundEffect.TYPE -> R.raw.se_type
        SoundEffect.STAR -> R.raw.se_star
        SoundEffect.STAMP -> R.raw.se_stamp
        SoundEffect.PANEL -> R.raw.se_panel
        SoundEffect.UI_SELECT -> R.raw.se_ui_select
        SoundEffect.UI_CONFIRM -> R.raw.se_ui_confirm
    }
}
