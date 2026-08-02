package com.urikai.app.game

/**
 * リザルト peek 用の転換帯（charts 絶対秒）。
 * keyframes 生成（rewrite_keyframes.py）の骨格に合わせ、macro_shape ごとに帯を決める。
 */
object StageAnchor {

    data class ReviewBand(
        val startSec: Int,
        val endSec: Int,
        /** 帯内の主転換点（ラベル位置の目安） */
        val focalSec: Int,
    )

    fun reviewBand(macroShape: String, pivotTiming: String, stageId: String): ReviewBand {
        stageOverride(stageId)?.let { return it }

        val idx = stageIndex(stageId)
        val pivot = pivotBand(pivotTiming)
        return when (macroShape) {
            "V_RECOVER", "W_DOUBLE" -> ReviewBand(
                startSec = pivot.dropEnd,
                endSec = pivot.rally,
                focalSec = pivot.bottom,
            )
            "EARLY_SHOCK" -> {
                val shock = 6 + (idx % 3)
                ReviewBand(
                    startSec = shock,
                    endSec = 20,
                    focalSec = shock + 3,
                )
            }
            "LATE_SPIKE" -> ReviewBand(
                startSec = 55,
                endSec = 63,
                focalSec = 59,
            )
            "RANGE" -> {
                val (start, end) = when (pivotTiming) {
                    "EARLY" -> 8 to 28
                    "LATE" -> 38 to 58
                    else -> 20 to 44
                }
                ReviewBand(startSec = start, endSec = end, focalSec = (start + end) / 2)
            }
            "N_RALLY" -> {
                val rally = pivot.rally
                val crashStart = minOf(58, rally + 24)
                ReviewBand(
                    startSec = maxOf(rally, crashStart - 8),
                    endSec = minOf(62, rally + 26),
                    focalSec = minOf(60, rally + 25),
                )
            }
            "INV_N" -> ReviewBand(
                startSec = maxOf(6, pivot.bottom - 4),
                endSec = pivot.rally + 8,
                focalSec = pivot.rally,
            )
            "BUBBLE" -> {
                val peakSec = when (pivotTiming) {
                    "EARLY" -> 26 + (idx % 3)
                    "LATE" -> 42 + (idx % 4)
                    else -> 34 + (idx % 3)
                }
                ReviewBand(
                    startSec = peakSec - 2,
                    endSec = peakSec + 10,
                    focalSec = peakSec,
                )
            }
            "INVERSE_V" -> {
                val peakSec = when (pivotTiming) {
                    "EARLY" -> 16 + (idx % 3)
                    "LATE" -> 38 + (idx % 3)
                    else -> 28 + (idx % 3)
                }
                ReviewBand(
                    startSec = peakSec - 2,
                    endSec = peakSec + 14,
                    focalSec = peakSec,
                )
            }
            "GRIND_UP" -> {
                val turn = when (pivotTiming) {
                    "EARLY" -> 28
                    "LATE" -> 48
                    else -> 38
                }
                ReviewBand(
                    startSec = turn - 4,
                    endSec = minOf(turn + 14, 64),
                    focalSec = turn,
                )
            }
            "GRIND_DOWN" -> {
                val accel = when (pivotTiming) {
                    "EARLY" -> 22
                    "LATE" -> 46
                    else -> 34
                }
                ReviewBand(
                    startSec = accel,
                    endSec = minOf(accel + 22, 69),
                    focalSec = accel + 6,
                )
            }
            else -> ReviewBand(
                startSec = pivot.dropEnd,
                endSec = pivot.rally,
                focalSec = pivot.bottom,
            )
        }
    }

    private data class PivotBand(
        val dropEnd: Int,
        val bottom: Int,
        val rally: Int,
    )

    private fun pivotBand(pivotTiming: String): PivotBand = when (pivotTiming) {
        "EARLY" -> PivotBand(dropEnd = 14, bottom = 22, rally = 26)
        "LATE" -> PivotBand(dropEnd = 36, bottom = 44, rally = 48)
        else -> PivotBand(dropEnd = 22, bottom = 30, rally = 36)
    }

    private fun stageOverride(stageId: String): ReviewBand? = when (stageId) {
        // Extreme: rewrite_keyframes.py の CLIFF / SURGE / 手動 keyframes に合わせる
        "e01", "e02" -> band(52, 62, 57)
        "e03", "e05", "e08" -> band(52, 64, 57)
        "e04" -> band(50, 64, 56)
        "e06" -> band(52, 64, 57)
        "e07" -> band(52, 62, 56)
        "e09" -> band(50, 62, 55)
        "e10" -> band(32, 52, 35)
        "e11" -> band(44, 58, 48)
        "e12" -> band(50, 68, 58)
        "e13" -> band(46, 58, 53)
        "e14" -> band(44, 58, 48)
        "e15" -> band(46, 58, 50)
        else -> null
    }

    private fun band(startSec: Int, endSec: Int, focalSec: Int) =
        ReviewBand(startSec = startSec, endSec = endSec, focalSec = focalSec)

    private fun stageIndex(stageId: String): Int =
        stageId.drop(1).toIntOrNull() ?: 0
}
