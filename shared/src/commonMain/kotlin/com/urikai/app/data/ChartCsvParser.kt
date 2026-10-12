package com.urikai.app.data

/**
 * 縦持ち CSV をパースする。引用符付きフィールド（コメント内のカンマ）に対応。
 */
object ChartCsvParser {

    private val headerColumns = listOf(
        "stage_id",
        "brand_name",
        "brand_name_en",
        "period",
        "period_en",
        "market_kind",
        "instrument_name",
        "instrument_name_en",
        "calendar_start",
        "calendar_start_en",
        "calendar_end",
        "calendar_end_en",
        "anchor_when",
        "anchor_when_en",
        "anchor_band_start",
        "anchor_band_end",
        "scenario_type",
        "flavor_text",
        "flavor_text_en",
        "history_comment",
        "history_comment_en",
        "anchor_label",
        "anchor_label_en",
        "pivot_timing",
        "macro_shape",
        "base_price",
        "second",
        "price_ratio"
    )

    fun parse(text: String): List<StageChart> {
        val lines = text.removePrefix("\uFEFF").lines().filter { it.isNotBlank() }
        require(lines.isNotEmpty()) { "CSV is empty" }

        val header = splitCsvLine(lines.first())
        require(header == headerColumns) {
            "Unexpected CSV header: $header"
        }

        val rowsByStage = linkedMapOf<String, MutableList<CsvRow>>()
        for (line in lines.drop(1)) {
            val cols = splitCsvLine(line)
            require(cols.size == headerColumns.size) { "Bad row: $line" }
            val row = CsvRow(
                stageId = cols[0],
                brandName = cols[1],
                brandNameEn = cols[2],
                period = cols[3],
                periodEn = cols[4],
                marketKind = cols[5],
                instrumentName = cols[6],
                instrumentNameEn = cols[7],
                calendarStart = cols[8],
                calendarStartEn = cols[9],
                calendarEnd = cols[10],
                calendarEndEn = cols[11],
                anchorWhen = cols[12],
                anchorWhenEn = cols[13],
                anchorBandStart = cols[14].toInt(),
                anchorBandEnd = cols[15].toInt(),
                scenarioType = cols[16],
                flavorText = cols[17],
                flavorTextEn = cols[18],
                historyComment = cols[19],
                historyCommentEn = cols[20],
                anchorLabel = cols[21],
                anchorLabelEn = cols[22],
                pivotTiming = cols[23],
                macroShape = cols[24],
                basePrice = cols[25].toInt(),
                second = cols[26].toInt(),
                priceRatio = cols[27].toFloat()
            )
            rowsByStage.getOrPut(row.stageId) { mutableListOf() }.add(row)
        }

        return rowsByStage.map { (_, rows) ->
            val sorted = rows.sortedBy { it.second }
            require(sorted.map { it.second } == (0 until StageChart.POINT_COUNT).toList()) {
                "Stage ${sorted.first().stageId} must contain seconds 0..69 exactly"
            }
            val meta = sorted.first()
            StageChart(
                stageId = meta.stageId,
                brandName = meta.brandName,
                brandNameEn = meta.brandNameEn,
                period = meta.period,
                periodEn = meta.periodEn,
                marketKind = meta.marketKind,
                instrumentName = meta.instrumentName,
                instrumentNameEn = meta.instrumentNameEn,
                calendarStart = meta.calendarStart,
                calendarStartEn = meta.calendarStartEn,
                calendarEnd = meta.calendarEnd,
                calendarEndEn = meta.calendarEndEn,
                anchorWhen = meta.anchorWhen,
                anchorWhenEn = meta.anchorWhenEn,
                anchorBandStart = meta.anchorBandStart,
                anchorBandEnd = meta.anchorBandEnd,
                scenarioType = meta.scenarioType,
                flavorText = meta.flavorText,
                flavorTextEn = meta.flavorTextEn,
                historyComment = meta.historyComment,
                historyCommentEn = meta.historyCommentEn,
                anchorLabel = meta.anchorLabel,
                anchorLabelEn = meta.anchorLabelEn,
                pivotTiming = meta.pivotTiming,
                macroShape = meta.macroShape,
                basePrice = meta.basePrice,
                priceRatios = FloatArray(StageChart.POINT_COUNT) { i -> sorted[i].priceRatio }
            )
        }
    }

    /**
     * RFC4180 風の簡易分割。ダブルクォートで囲まれたカンマを許容する。
     */
    fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    private data class CsvRow(
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
        val flavorText: String,
        val flavorTextEn: String,
        val historyComment: String,
        val historyCommentEn: String,
        val anchorLabel: String,
        val anchorLabelEn: String,
        val pivotTiming: String,
        val macroShape: String,
        val basePrice: Int,
        val second: Int,
        val priceRatio: Float
    )
}