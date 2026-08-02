package com.urikai.app.game

/**
 * 最終資産と HUMAN MAX（理論）から星を決める。
 *
 * - ★1: 資産 &lt; ¥10,000（必ずマイナス）
 * - ★2床: ¥10,000
 * - ★3・★4: ¥10,000〜理論値を等分した境界
 * - ★5: 資産 ≥ 理論値
 * - ★5+: 資産 &gt; 理論値（OVER SYNC）
 *
 * RETIRE は呼び出し側で ★0。
 */
object SyncRating {

    /** ★2 の床（これ未満は ★1）。 */
    const val STAR2_FLOOR_ASSET: Int = 10_000

    fun isOverSync(finalAsset: Int, theoreticalMax: Int): Boolean =
        theoreticalMax > 0 && finalAsset > theoreticalMax

    /**
     * @param finalAsset 最終資産
     * @param theoreticalMax HUMAN MAX（その試合の分母）
     */
    fun stars(finalAsset: Int, theoreticalMax: Int): Int {
        if (finalAsset < STAR2_FLOOR_ASSET) return 1
        if (theoreticalMax <= STAR2_FLOOR_ASSET) {
            return if (finalAsset >= theoreticalMax) 5 else 2
        }
        val span = theoreticalMax - STAR2_FLOOR_ASSET
        val star3Min = STAR2_FLOOR_ASSET + span / 3
        val star4Min = STAR2_FLOOR_ASSET + (span * 2) / 3
        return when {
            finalAsset >= theoreticalMax -> 5
            finalAsset >= star4Min -> 4
            finalAsset >= star3Min -> 3
            else -> 2
        }
    }

    /** アニメ用: SYNC% を理論に対する資産相当へ戻して星を求める。 */
    fun starsForSync(syncPercent: Float, theoreticalMax: Int): Int {
        if (theoreticalMax <= 0) return 1
        val asset = kotlin.math.round(syncPercent / 100f * theoreticalMax).toInt()
        return stars(asset, theoreticalMax)
    }

    fun starThresholdAssets(theoreticalMax: Int): StarThresholdAssets {
        if (theoreticalMax <= STAR2_FLOOR_ASSET) {
            return StarThresholdAssets(
                star2Min = STAR2_FLOOR_ASSET,
                star3Min = STAR2_FLOOR_ASSET,
                star4Min = STAR2_FLOOR_ASSET,
                star5Min = theoreticalMax.coerceAtLeast(STAR2_FLOOR_ASSET),
            )
        }
        val span = theoreticalMax - STAR2_FLOOR_ASSET
        return StarThresholdAssets(
            star2Min = STAR2_FLOOR_ASSET,
            star3Min = STAR2_FLOOR_ASSET + span / 3,
            star4Min = STAR2_FLOOR_ASSET + (span * 2) / 3,
            star5Min = theoreticalMax,
        )
    }

    /**
     * 例: ★★★★☆ / 100%超は ★★★★★+
     */
    fun starsText(
        finalAsset: Int,
        theoreticalMax: Int,
        halfMark: Boolean = false,
    ): String {
        val filled = stars(finalAsset, theoreticalMax).coerceIn(1, 5)
        val over = isOverSync(finalAsset, theoreticalMax)
        return formatStars(filled, over, halfMark)
    }

    fun starsTextForSync(
        syncPercent: Float,
        theoreticalMax: Int,
        halfMark: Boolean = false,
    ): String {
        if (theoreticalMax <= 0) return formatStars(1, over = false, halfMark = halfMark)
        val asset = kotlin.math.round(syncPercent / 100f * theoreticalMax).toInt()
        return starsText(asset, theoreticalMax, halfMark)
    }

    private fun formatStars(filled: Int, over: Boolean, halfMark: Boolean): String {
        if (!halfMark) {
            val base = "★".repeat(filled) + "☆".repeat(5 - filled)
            return if (over) "$base+" else base
        }
        val withDot = when (filled) {
            1 -> "★·☆☆☆☆"
            2 -> "★★·☆☆☆"
            3 -> "★★·★☆☆"
            4 -> "★★★·★☆"
            else -> "★★★·★★"
        }
        return if (over) "$withDot+" else withDot
    }
}

data class StarThresholdAssets(
    val star2Min: Int,
    val star3Min: Int,
    val star4Min: Int,
    val star5Min: Int,
)
