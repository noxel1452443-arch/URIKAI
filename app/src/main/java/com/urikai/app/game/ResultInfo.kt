package com.urikai.app.game

/**
 * リザルト表示用。GameUiState には入れない。
 */
data class ResultInfo(
    val brandName: String,
    val calendarStart: String,
    val calendarEnd: String,
    val instrumentName: String,
    val scenarioType: String,
    val historyComment: String,
    val finalAsset: Int,
    val tradeGainLoss: Int,
    val totalInterest: Int,
    val theoreticalMaxAsset: Int,
    val syncPercent: Float,
    val niceTradeCount: Int = 0,
    val missTradeCount: Int = 0,
    val filledTradeCount: Int = 0,
    /** 一度も約定せず終了 */
    val isGhostRun: Boolean = false,
    /** 最終資産が初期資産ちょうど */
    val isBreakEven: Boolean = false,
    /** 到達率が表示上 50.0% */
    val isHalfSync: Boolean = false,
    /** このステージを初めて解放したクリア */
    val isFirstClear: Boolean = false,
    /** このステージの自己ベスト（シンクロ率）を更新した */
    val isNewBest: Boolean = false,
    /** 吹き出し用。今回記録前の BEST 資産（未記録は null） */
    val priorBestAsset: Int? = null,
    /** 通算加算ボーナス回だったか（出金時に倍率適用） */
    val ledgerBoostApplied: Boolean = false,
    /** 適用倍率（未適用時は 1f） */
    val ledgerBoostMult: Float = 1f,
    /** 終了時プラス株損益にレリック倍率を乗せたか */
    val tradePnlBoostApplied: Boolean = false,
    val tradePnlBoostMult: Float = 1f,
    /** 試合中の利子にレリック倍率を乗せたか */
    val interestBoostApplied: Boolean = false,
    val interestBoostMult: Float = 1f,
    /** 試合中の最大 COMBO */
    val maxCombo: Int = 0,
    /** 体力切れによるリタイア */
    val isRetired: Boolean = false,
    /** リタイア時フレーバー（0..n-1）。非リタイア時は -1 */
    val retireFlavorIndex: Int = -1,
)
