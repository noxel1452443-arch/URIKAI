package com.urikai.app.data

import com.urikai.app.platform.KeyValueStore
import com.urikai.app.game.SyncCalculator
import com.urikai.app.game.DisplayLanguage

/**
 * クリア済み銘柄と自己ベスト（最終資産＝増減率）の永続化（SharedPreferences）。
 */
class ScrapbookRepository(openStore: (String) -> KeyValueStore) {

    private val prefs = openStore(PREFS_NAME)

    fun buildEntries(stages: List<StageChart>, lang: DisplayLanguage): List<ScrapbookEntry> {
        return stages.map { stage ->
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
                referenceTheoAsset = SyncCalculator.stageTargetAsset(stage.priceRatios),
                bestAsset = bestAssetRaw.takeIf { it > 0 },
                flavorText = stage.flavorText(lang),
                anchorWhen = stage.anchorWhen(lang),
                anchorBandStart = stage.anchorBandStart,
                anchorBandEnd = stage.anchorBandEnd,
                priceRatios = stage.priceRatios,
            )
        }
    }

    fun getBestAsset(stageId: String): Int? {
        val v = prefs.getInt(bestAssetKey(stageId), -1)
        return v.takeIf { it > 0 }
    }

    /**
     * クリア時に解放し、自己ベスト（最終資産）を更新する。
     * @return firstUnlock / newBest
     */
    fun recordClear(stageId: String, finalAsset: Int): ClearRecord {
        val unlocked = prefs.getBoolean(unlockKey(stageId), false)
        val prevAsset = prefs.getInt(bestAssetKey(stageId), -1)
        val isNewBest = finalAsset > prevAsset

        prefs.edit()
            .putBoolean(unlockKey(stageId), true)
            .apply {
                if (isNewBest) putInt(bestAssetKey(stageId), finalAsset)
            }
            .apply()

        return ClearRecord(
            firstUnlock = !unlocked,
            newBest = isNewBest
        )
    }

    private fun unlockKey(stageId: String) = "unlock_$stageId"
    private fun bestAssetKey(stageId: String) = "best_asset_$stageId"

    companion object {
        private const val PREFS_NAME = "urikai_scrapbook"
    }
}

data class ClearRecord(
    val firstUnlock: Boolean,
    val newBest: Boolean,
)
