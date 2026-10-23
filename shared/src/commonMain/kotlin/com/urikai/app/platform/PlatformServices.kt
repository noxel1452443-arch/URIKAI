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

/**
 * 広告・課金ゲート。
 * M1: スタブ可。M2 で AdMob / Play Billing を接続。
 */
interface MonetizationController {
    /** リザルト→ホーム前。広告なし／未準備ならすぐ [onContinue]。 */
    fun maybeShowInterstitial(adsRemoved: Boolean, onContinue: () -> Unit)

    /**
     * リワード広告（寝床クールダウン短縮など）。
     * [adsRemoved] なら視聴なしで成功扱い。未準備／失敗は false。
     */
    fun showRewarded(adsRemoved: Boolean, onResult: (rewarded: Boolean) -> Unit)

    /** 広告除去 IAP。成功時 true。 */
    fun purchaseRemoveAds(onResult: (purchased: Boolean) -> Unit)

    /** 購入復元。 */
    fun restorePurchases(onResult: (adsRemoved: Boolean) -> Unit)

    /** 広告除去の表示価格（Billing 未取得時はフォールバック）。 */
    fun removeAdsPriceLabel(): String = "¥290"

    /**
     * MobileAds 初期化後に呼ぶ。起動直後の同時プリロードを避ける。
     * 既定は何もしない（スタブ／非 Android）。
     */
    fun warmUpAds() {}

    fun release() {}
}

object NoMonetization : MonetizationController {
    override fun maybeShowInterstitial(adsRemoved: Boolean, onContinue: () -> Unit) {
        onContinue()
    }

    override fun showRewarded(adsRemoved: Boolean, onResult: (rewarded: Boolean) -> Unit) {
        onResult(adsRemoved)
    }

    override fun purchaseRemoveAds(onResult: (purchased: Boolean) -> Unit) {
        onResult(false)
    }

    override fun restorePurchases(onResult: (adsRemoved: Boolean) -> Unit) {
        onResult(false)
    }
}

/** プラットフォームごとの実装をまとめて ViewModel に渡す。 */
class PlatformServices(
    val openStore: (name: String) -> KeyValueStore,
    val sound: SoundBackend = NoSound,
    val haptics: HapticBackend = NoHaptics,
    val bgm: BgmController = NoBgm,
    val monetization: MonetizationController = NoMonetization,
    val isDebugBuild: Boolean = false,
    /**
     * 広告・リワード導線（インタースティシャル／半額／給料2倍／広告除去購入）を出すか。
     * Web（GitHub Pages）では false。
     */
    val enableAdFeatures: Boolean = true,
    /** Android versionCode（進捗マイグレーション判定用）。Web は 0 可。 */
    val appVersionCode: Int = 0,
    /** 共有テキストを OS の共有 UI 等へ渡す。title は選択画面の見出し。 */
    val shareText: (text: String, title: String) -> Unit = { _, _ -> },
    /** デバッグ用: 進捗ワイプ後にプロセスを立ち上げ直す。 */
    val restartApp: () -> Unit = {},
    /** ストアのアプリページ（レビュー投稿）を開く。 */
    val openStoreListing: () -> Unit = {},
)
