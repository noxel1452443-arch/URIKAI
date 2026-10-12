package com.urikai.app.data

/**
 * 図鑑の1行（一覧）と詳細画面の表示データ。
 */
data class ScrapbookEntry(
    val stageId: String,
    val brandName: String,
    val calendarStart: String,
    val calendarEnd: String,
    val instrumentName: String,
    val scenarioType: String,
    /** 教訓（物語の締めのひとこと） */
    val historyComment: String,
    val macroShape: String,
    val unlocked: Boolean,
    /** 星表示用の参考理論（offset 0..10 平均）。未解放時は 0 */
    val referenceTheoAsset: Int = 0,
    /** 自己ベスト最終資産。未記録は null */
    val bestAsset: Int? = null,
    /** 出来事の掘り下げ（詳細画面） */
    val flavorText: String = "",
    /** 転換点の出来事（詳細チャートの印） */
    val anchorWhen: String = "",
    val anchorBandStart: Int = 0,
    val anchorBandEnd: Int = 0,
    /** 詳細チャート用（70点） */
    val priceRatios: FloatArray = FloatArray(0),
)
