package com.urikai.app.data

import com.urikai.app.platform.KeyValueStore
import com.urikai.app.game.SyncCalculator
import com.urikai.app.game.DisplayLanguage

/**
 * クリア済み銘柄と自己ベスト（最終資産＝増減率）の永続化（SharedPreferences）。
 */
class ScrapbookRepository(openStore: (String) -> KeyValueStore) {

    private val prefs = openStore(PREFS_NAME)

    fun isUnlocked(stageId: String): Boolean =
        prefs.getBoolean(unlockKey(stageId), false)

    fun unlockedStageIds(stages: List<StageChart>): Set<String> =
        stages.map { it.stageId }.filter { isUnlocked(it) }.toSet()

    fun buildEntries(stages: List<StageChart>, lang: DisplayLanguage): List<ScrapbookEntry> {
        val unlockedIds = unlockedStageIds(stages)
        val totalsByInstrument = stages.groupingBy { it.instrumentName }.eachCount()
        val prevByStage = stages.associate { stage ->
            val prev = stages.find {
                it.instrumentName == stage.instrumentName && it.sagaEp == stage.sagaEp - 1
            }
            stage.stageId to prev?.stageId
        }
        return stages.map { stage ->
            val bestAssetRaw = prefs.getInt(bestAssetKey(stage.stageId), -1)
            val unlocked = stage.stageId in unlockedIds
            val prevId = prevByStage[stage.stageId]
            val awaitingPrevious = !unlocked && stage.sagaEp > 1 &&
                (prevId == null || prevId !in unlockedIds)
            ScrapbookEntry(
                stageId = stage.stageId,
                brandName = stage.brandName(lang),
                calendarStart = stage.calendarStart(lang),
                calendarEnd = stage.calendarEnd(lang),
                instrumentName = stage.instrumentName(lang),
                instrumentKey = stage.instrumentName,
                sagaEp = stage.sagaEp,
                sagaTotal = totalsByInstrument[stage.instrumentName] ?: 1,
                awaitingPrevious = awaitingPrevious,
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
            .putBoolean(KEY_HAD_ANY_UNLOCK, true)
            .apply {
                if (isNewBest) putInt(bestAssetKey(stageId), finalAsset)
            }
            .apply()

        return ClearRecord(
            firstUnlock = !unlocked,
            newBest = isNewBest
        )
    }

    /** 1銘柄でもクリア解放済みか（コールドスタート時の図鑑ガイド復旧用）。 */
    fun hasAnyUnlocked(): Boolean = prefs.getBoolean(KEY_HAD_ANY_UNLOCK, false)

    private fun unlockKey(stageId: String) = "unlock_$stageId"
    private fun bestAssetKey(stageId: String) = "best_asset_$stageId"

    companion object {
        private const val PREFS_NAME = "urikai_scrapbook"
        private const val KEY_HAD_ANY_UNLOCK = "had_any_unlock"
    }
}

data class ClearRecord(
    val firstUnlock: Boolean,
    val newBest: Boolean,
)
