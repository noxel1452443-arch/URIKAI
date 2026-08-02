package com.urikai.app.data

import com.urikai.app.game.DisplayLanguage

/**
 * 1ステージ分のチャート（70秒 = second 0..69）。
 */
data class StageChart(
    val stageId: String,
    val brandName: String,
    val brandNameEn: String,
    val period: String,
    val periodEn: String,
    val marketKind: String,
    val instrumentName: String,
    val instrumentNameEn: String,
    val calendarStart: String,
    val calendarStartEn: String,
    val calendarEnd: String,
    val calendarEndEn: String,
    val anchorWhen: String,
    val anchorWhenEn: String,
    val anchorBandStart: Int,
    val anchorBandEnd: Int,
    val scenarioType: String,
    val hintText: String,
    val hintTextEn: String,
    val historyComment: String,
    val historyCommentEn: String,
    val anchorLabel: String,
    val anchorLabelEn: String,
    val pivotTiming: String,
    val macroShape: String,
    val priceRatios: FloatArray
) {

    init {
        require(priceRatios.size == POINT_COUNT) {
            "Stage $stageId must have $POINT_COUNT points, was ${priceRatios.size}"
        }
    }

    fun brandName(lang: DisplayLanguage): String =
        pick(lang, brandName, brandNameEn)

    fun instrumentName(lang: DisplayLanguage): String =
        pick(lang, instrumentName, instrumentNameEn)

    fun period(lang: DisplayLanguage): String =
        pick(lang, period, periodEn)

    fun calendarStart(lang: DisplayLanguage): String =
        pick(lang, calendarStart, calendarStartEn)

    fun calendarEnd(lang: DisplayLanguage): String =
        pick(lang, calendarEnd, calendarEndEn)

    fun anchorWhen(lang: DisplayLanguage): String =
        pick(lang, anchorWhen, anchorWhenEn)

    fun hintText(lang: DisplayLanguage): String =
        pick(lang, hintText, hintTextEn)

    fun historyComment(lang: DisplayLanguage): String =
        pick(lang, historyComment, historyCommentEn)

    fun anchorLabel(lang: DisplayLanguage): String =
        pick(lang, anchorLabel, anchorLabelEn)

    private fun pick(lang: DisplayLanguage, jp: String, en: String): String =
        if (lang == DisplayLanguage.EN && en.isNotBlank()) en else jp

    companion object {
        const val POINT_COUNT = 70
    }
}