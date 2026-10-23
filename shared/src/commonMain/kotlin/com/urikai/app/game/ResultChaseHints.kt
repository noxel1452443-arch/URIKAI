package com.urikai.app.game

/**
 * リザルト資産行の吹き出し（次の★まで）。
 * RETIRE・マイナス（★1床未満）・OVER は出さない。
 * 銘柄ベスト比較は出さない（目標額の予告感を避ける）。
 */
sealed class ResultChaseHint {
    data class StarGap(val nextStars: Int, val yenLeft: Int) : ResultChaseHint()
}

object ResultChaseHints {

    fun resolve(
        finalAsset: Int,
        theoreticalMax: Int,
        isRetired: Boolean,
        @Suppress("UNUSED_PARAMETER") priorBestAsset: Int?,
    ): ResultChaseHint? {
        if (isRetired) return null
        if (finalAsset < SyncRating.STAR2_FLOOR_ASSET) return null
        if (SyncRating.isOverSync(finalAsset, theoreticalMax)) return null

        val stars = SyncRating.stars(finalAsset, theoreticalMax)
        if (stars in 2..5) {
            val nextMin = nextStarMinAsset(stars, theoreticalMax) ?: return null
            val left = nextMin - finalAsset
            if (left >= 1) {
                return ResultChaseHint.StarGap(nextStars = stars + 1, yenLeft = left)
            }
        }
        return null
    }

    private fun nextStarMinAsset(currentStars: Int, theoreticalMax: Int): Int? {
        val t = SyncRating.starThresholdAssets(theoreticalMax)
        return when (currentStars) {
            2 -> t.star3Min
            3 -> t.star4Min
            4 -> t.star5Min
            5 -> t.star5PlusMin
            else -> null
        }
    }
}
