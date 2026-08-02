package com.urikai.app.data

import android.content.Context
import com.urikai.app.game.SyncCalculator
import com.urikai.app.game.DisplayLanguage
import kotlin.math.roundToInt

/**
 * クリア済み銘柄と自己ベスト（到達率・資産）の永続化（SharedPreferences）。
 */
class ScrapbookRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun buildEntries(stages: List<StageChart>, lang: DisplayLanguage): List<ScrapbookEntry> {
        return stages.map { stage ->
            val best = prefs.getFloat(bestKey(stage.stageId), Float.NaN)
            val bestAssetRaw = prefs.getInt(bestAssetKey(stage.stageId), -1)
            val unlocked = prefs.getBoolean(unlockKey(stage.stageId), false)
            ScrapbookEntry(
                stageId = stage.stageId,
                brandName = stage.brandName(lang),
                calendarStart = stage.calendarStart(lang),
                calendarEnd = stage.calendarEnd(lang),
                instrumentName = stage.instrumentName(lang),
                scenarioType = stage.scenarioType,
                historyComment = stage.historyComment(lang),
                macroShape = stage.macroShape,
                unlocked = unlocked,
                bestSyncPercent = if (best.isNaN()) null else best,
                referenceTheoAsset = meanTheoreticalMax(stage),
                bestAsset = bestAssetRaw.takeIf { it > 0 },
            )
        }
    }

    fun getBestAsset(stageId: String): Int? {
        val v = prefs.getInt(bestAssetKey(stageId), -1)
        return v.takeIf { it > 0 }
    }

    /**
     * クリア時に解放し、自己ベストを更新する。
     * @return firstUnlock / newBest（到達率の自己ベスト更新）
     */
    fun recordClear(stageId: String, syncPercent: Float, finalAsset: Int): ClearRecord {
        val unlocked = prefs.getBoolean(unlockKey(stageId), false)
        val prev = prefs.getFloat(bestKey(stageId), Float.NaN)
        val isNewBest = prev.isNaN() || syncPercent > prev
        val prevAsset = prefs.getInt(bestAssetKey(stageId), -1)
        val isNewBestAsset = finalAsset > prevAsset

        prefs.edit()
            .putBoolean(unlockKey(stageId), true)
            .apply {
                if (isNewBest) putFloat(bestKey(stageId), syncPercent)
                if (isNewBestAsset) putInt(bestAssetKey(stageId), finalAsset)
            }
            .apply()

        return ClearRecord(
            firstUnlock = !unlocked,
            newBest = isNewBest
        )
    }

    private fun meanTheoreticalMax(stage: StageChart): Int {
        var sum = 0
        for (off in 0..10) {
            sum += SyncCalculator.theoreticalMaxAsset(stage.priceRatios, off)
        }
        return (sum / 11f).roundToInt()
    }

    private fun unlockKey(stageId: String) = "unlock_$stageId"
    private fun bestKey(stageId: String) = "best_$stageId"
    private fun bestAssetKey(stageId: String) = "best_asset_$stageId"

    companion object {
        private const val PREFS_NAME = "urikai_scrapbook"
    }
}

data class ClearRecord(
    val firstUnlock: Boolean,
    val newBest: Boolean,
)
