package com.urikai.app.game

/**
 * チャート上の売買マーカー（GameUiState には入れない）。
 */
data class TradeMarker(
    val gameTimeSec: Float,
    val priceRatio: Float,
    val displayPrice: Int,
    val isBuy: Boolean,
    val isPending: Boolean = false,
    /** TIME UP 等で約定しなかった注文。タイマー位置に × を描く。 */
    val isCancelled: Boolean = false,
    /** 終了時の強制売却（TIME UP 決済）。 */
    val isForcedClose: Boolean = false,
    /** 注文中マーカー用。押下時刻（epoch ms）。進捗円の計算に使う。 */
    val submittedAtEpochMs: Long = 0L,
    /** 約定直後の価格ラベル表示期限（epoch ms）。0 なら非表示。 */
    val labelUntilEpochMs: Long = 0L,
    /** 押下時点のゲーム秒（約定マーカーがラグ後位置に移ったあとも残す）。 */
    val submitGameTimeSec: Float = Float.NaN,
    /** 押下時点の価格比。 */
    val submitPriceRatio: Float = Float.NaN,
    /** 押下時点の表示価格（ラグ矢印用）。0 なら未設定。 */
    val submitDisplayPrice: Int = 0,
    /** 売りラウンドトリップ判定。買いでは null。 */
    val fillQuality: FillQuality? = null,
    /** RESULT 振り返り。プレイ中は NONE */
    val timingGrade: TradeTimingGrade = TradeTimingGrade.NONE,
) {
    val hasSubmitAnchor: Boolean
        get() = !submitGameTimeSec.isNaN() && !submitPriceRatio.isNaN()
}
