package com.urikai.app.game

/**
 * 最終資産と HUMAN MAX（理論 = 目標）から星を決める。
 *
 * - ★1: 資産 &lt; ¥10,000（必ずマイナス）
 * - ★2〜★5: ¥10,000〜目標を4等分した帯
 * - ★5+: 資産 ≥ 目標（OVER）
 *
 * RETIRE は呼び出し側で ★0。
 */
object SyncRating {

    /** ★2 の床（これ未満は ★1）。 */
    const val STAR2_FLOOR_ASSET: Int = 10_000

    /** ¥10,000 換算の資産から増減率（%）。 */
    fun returnPercent(normalizedAsset: Int): Float =
        (normalizedAsset - STAR2_FLOOR_ASSET) / (STAR2_FLOOR_ASSET / 100f)

    /** 増減率（%）から ¥10,000 換算の資産。 */
    fun assetForReturn(returnPercent: Float): Int =
        kotlin.math.round(STAR2_FLOOR_ASSET * (1f + returnPercent / 100f)).toInt()

    /** 目標到達（★5+）。 */
    fun isOverSync(finalAsset: Int, theoreticalMax: Int): Boolean =
        theoreticalMax > 0 &&
            finalAsset >= STAR2_FLOOR_ASSET &&
            finalAsset >= theoreticalMax

    /**
     * ★5+ も 5 を返す（＋は [isOverSync] で判定）。
     * @param finalAsset 最終資産
     * @param theoreticalMax HUMAN MAX（その試合の目標）
     */
    fun stars(finalAsset: Int, theoreticalMax: Int): Int {
        if (finalAsset < STAR2_FLOOR_ASSET) return 1
        val t = starThresholdAssets(theoreticalMax)
        return when {
            finalAsset >= t.star5Min -> 5
            finalAsset >= t.star4Min -> 4
            finalAsset >= t.star3Min -> 3
            else -> 2
        }
    }

    fun starThresholdAssets(theoreticalMax: Int): StarThresholdAssets {
        if (theoreticalMax <= STAR2_FLOOR_ASSET) {
            return StarThresholdAssets(
                star2Min = STAR2_FLOOR_ASSET,
                star3Min = STAR2_FLOOR_ASSET,
                star4Min = STAR2_FLOOR_ASSET,
                star5Min = STAR2_FLOOR_ASSET,
                star5PlusMin = STAR2_FLOOR_ASSET,
            )
        }
        val span = theoreticalMax - STAR2_FLOOR_ASSET
        return StarThresholdAssets(
            star2Min = STAR2_FLOOR_ASSET,
            star3Min = STAR2_FLOOR_ASSET + span / 4,
            star4Min = STAR2_FLOOR_ASSET + span / 2,
            star5Min = STAR2_FLOOR_ASSET + (span * 3) / 4,
            star5PlusMin = theoreticalMax,
        )
    }

    /**
     * 例: ★★★★☆ / 目標到達は ★★★★★+
     */
    fun starsText(
        finalAsset: Int,
        theoreticalMax: Int,
    ): String {
        val filled = stars(finalAsset, theoreticalMax).coerceIn(1, 5)
        val over = isOverSync(finalAsset, theoreticalMax)
        val base = "★".repeat(filled) + "☆".repeat(5 - filled)
        return if (over) "$base+" else base
    }
}

data class StarThresholdAssets(
    val star2Min: Int,
    val star3Min: Int,
    val star4Min: Int,
    val star5Min: Int,
    val star5PlusMin: Int,
)
