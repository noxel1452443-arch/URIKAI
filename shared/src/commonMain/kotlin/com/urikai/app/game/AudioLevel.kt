package com.urikai.app.game

/**
 * CONFIG の SE / BGM 音量段階。OFF で無音。
 */
enum class AudioLevel {
    OFF,
    LOW,
    MID,
    HIGH,
    ;

    /** SE の再生倍率（各SEの相対音量に掛ける） */
    fun seScale(): Float = when (this) {
        OFF -> 0f
        LOW -> 0.55f
        MID -> 1f
        HIGH -> 1.2f
    }

    /** BGM の MediaPlayer 音量（SEより控えめ） */
    fun bgmVolume(): Float = when (this) {
        OFF -> 0f
        LOW -> 0.22f
        MID -> 0.32f
        HIGH -> 0.45f
    }

    val isOn: Boolean get() = this != OFF
}
