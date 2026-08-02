package com.urikai.app.game

/**
 * リザルト資産行の吹き出し（STAR / BEST のどちらか1つ）。
 * RETIRE・マイナス（★1床未満）・OVER は出さない。
 */
sealed class ResultChaseHint {
    data class StarGap(val nextStars: Int, val yenLeft: Int) : ResultChaseHint()
    data class BestGap(val yenLeft: Int) : ResultChaseHint()
}

object ResultChaseHints {

    fun resolve(
        finalAsset: Int,
        theoreticalMax: Int,
        isRetired: Boolean,
        priorBestAsset: Int?,
    ): ResultChaseHint? {
        if (isRetired) return null
        if (finalAsset < SyncRating.STAR2_FLOOR_ASSET) return null
        if (SyncRating.isOverSync(finalAsset, theoreticalMax)) return null

        val stars = SyncRating.stars(finalAsset, theoreticalMax)
        if (stars in 2..4) {
            val nextMin = nextStarMinAsset(stars, theoreticalMax) ?: return null
            val left = nextMin - finalAsset
            if (left >= 1) {
                return ResultChaseHint.StarGap(nextStars = stars + 1, yenLeft = left)
            }
        }

        val best = priorBestAsset ?: return null
        val bestLeft = best - finalAsset
        if (bestLeft >= 1) {
            return ResultChaseHint.BestGap(yenLeft = bestLeft)
        }
        return null
    }

    private fun nextStarMinAsset(currentStars: Int, theoreticalMax: Int): Int? {
        val t = SyncRating.starThresholdAssets(theoreticalMax)
        return when (currentStars) {
            2 -> t.star3Min
            3 -> t.star4Min
            4 -> t.star5Min
            else -> null
        }
    }
}
