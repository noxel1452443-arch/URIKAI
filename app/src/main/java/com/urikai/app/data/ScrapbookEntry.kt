package com.urikai.app.data

/**
 * 歴史スクラップ図鑑の1行。
 */
data class ScrapbookEntry(
    val stageId: String,
    val brandName: String,
    val calendarStart: String,
    val calendarEnd: String,
    val instrumentName: String,
    val scenarioType: String,
    val historyComment: String,
    val macroShape: String,
    val unlocked: Boolean,
    val bestSyncPercent: Float?,
    /** 星表示用の参考理論（offset 0..10 平均）。未解放時は 0 */
    val referenceTheoAsset: Int = 0,
    /** 自己ベスト最終資産。未記録は null */
    val bestAsset: Int? = null,
)
