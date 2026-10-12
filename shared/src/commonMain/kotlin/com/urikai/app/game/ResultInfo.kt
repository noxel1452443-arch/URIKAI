package com.urikai.app.game

/**
 * リザルト表示用。GameUiState には入れない。
 */
data class ResultInfo(
    val stageId: String = "",
    val brandName: String,
    val calendarStart: String,
    val calendarEnd: String,
    val instrumentName: String,
    val scenarioType: String,
    val historyComment: String,
    /** ¥10,000 換算の最終資産（★・BEST・目標との比較用） */
    val finalAsset: Int,
    /** 勝負前の口座残高（＝元手） */
    val startBalance: Long = 10_000L,
    /** 勝負後の口座残高 */
    val endBalance: Long = 10_000L,
    /** 増減率（%） */
    val returnPercent: Float = 0f,
    val tradeGainLoss: Int,
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
    /** 終了時プラス株損益にレリック倍率を乗せたか */
    val tradePnlBoostApplied: Boolean = false,
    val tradePnlBoostMult: Float = 1f,
    /** 試合中の最大 COMBO */
    val maxCombo: Int = 0,
    /** 体力切れによるリタイア */
    val isRetired: Boolean = false,
    /** リタイア時フレーバー（0..n-1）。非リタイア時は -1 */
    val retireFlavorIndex: Int = -1,
)
