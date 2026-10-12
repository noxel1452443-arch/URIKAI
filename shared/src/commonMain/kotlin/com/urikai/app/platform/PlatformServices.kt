package com.urikai.app.platform

/** 効果音ファイル（拡張子なし）。Android は res/raw、Web は同名 wav を読む。 */
enum class SoundEffect(val fileName: String) {
    BUY("se_buy"),
    SELL("se_sell"),
    MISS("se_miss"),
    OK("se_ok"),
    GREAT("se_great"),
    PERFECT("se_perfect"),
    FANTASTIC("se_fantastic"),
    JUDGE_GREAT("se_judge_great"),
    JUDGE_PERFECT("se_judge_perfect"),
    JUDGE_FANTASTIC("se_judge_fantastic"),
    TIME_UP("se_timeup"),
    TICK("se_tick"),
    CHARGE_I("se_charge_i"),
    CHARGE_II("se_charge_ii"),
    COUNTDOWN("se_countdown"),
    FINISH("se_finish"),
    PAYOUT_COUNT("se_payout_count"),
    PAYOUT_CLANG("se_payout_clang"),
    TYPE("se_type"),
    STAR("se_star"),
    STAMP("se_stamp"),
    PANEL("se_panel"),
    UI_SELECT("se_ui_select"),
    UI_CONFIRM("se_ui_confirm"),
}

interface SoundBackend {
    /** volume 0..1、rate 0.5..2.0 */
    fun play(effect: SoundEffect, rate: Float, volume: Float)
    fun release() {}
}

interface HapticBackend {
    fun oneShot(durationMs: Long, amplitude: Int)
    fun waveform(timingsMs: LongArray, amplitudes: IntArray)
}

/** アンビエント BGM の制御。 */
interface BgmController {
    fun setTargetVolume(volume: Float)
    fun setForeground(isForeground: Boolean)
    fun setAmbientAllowed(allowed: Boolean)
    fun release()
}

object NoSound : SoundBackend {
    override fun play(effect: SoundEffect, rate: Float, volume: Float) {}
}

object NoHaptics : HapticBackend {
    override fun oneShot(durationMs: Long, amplitude: Int) {}
    override fun waveform(timingsMs: LongArray, amplitudes: IntArray) {}
}

object NoBgm : BgmController {
    override fun setTargetVolume(volume: Float) {}
    override fun setForeground(isForeground: Boolean) {}
    override fun setAmbientAllowed(allowed: Boolean) {}
    override fun release() {}
}

/** プラットフォームごとの実装をまとめて ViewModel に渡す。 */
class PlatformServices(
    val openStore: (name: String) -> KeyValueStore,
    val sound: SoundBackend = NoSound,
    val haptics: HapticBackend = NoHaptics,
    val bgm: BgmController = NoBgm,
    val isDebugBuild: Boolean = false,
    /** 共有テキストを OS の共有 UI 等へ渡す。title は選択画面の見出し。 */
    val shareText: (text: String, title: String) -> Unit = { _, _ -> },
)
