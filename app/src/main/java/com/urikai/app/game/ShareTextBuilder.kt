package com.urikai.app.game

object ShareTextBuilder {

    fun build(result: ResultInfo, language: DisplayLanguage): String {
        return buildString {
            appendLine("URIKAI")
            if (result.isRetired) {
                appendLine(UiStrings.retireBanner())
                appendLine(UiStrings.retireFlavorLine1(result.retireFlavorIndex, language))
                appendLine(UiStrings.retireFlavorLine2(result.retireFlavorIndex, language))
                appendLine(UiStrings.shareSyncLine(0f, language))
                appendLine("☆☆☆☆☆")
            } else {
                appendLine(result.brandName)
                appendLine(
                    UiStrings.instrumentWithCalendarRange(
                        result.instrumentName,
                        result.calendarStart,
                        result.calendarEnd,
                        language
                    )
                )
                appendLine("${UiStrings.assetLabel(language)} ¥${"%,d".format(result.finalAsset)}")
                if (result.totalInterest > 0 || result.filledTradeCount > 0) {
                    val detailParts = buildList {
                        if (result.filledTradeCount > 0) {
                            add(UiStrings.formatTradeGainLoss(result.tradeGainLoss, language))
                        }
                        if (result.totalInterest > 0) {
                            add("${UiStrings.interestLabel(language)} +¥${"%,d".format(result.totalInterest)}")
                        }
                    }
                    appendLine(detailParts.joinToString("  "))
                }
                appendLine(UiStrings.shareSyncLine(result.syncPercent, language))
                if (result.isGhostRun) {
                    appendLine(UiStrings.resultFlavorGhost(language))
                } else {
                    appendLine(
                        SyncRating.starsText(
                            result.finalAsset,
                            result.theoreticalMaxAsset,
                            halfMark = result.isHalfSync,
                        )
                    )
                }
            }
            append("#URIKAI")
            appendLine()
            append(PLAY_STORE_URL)
        }
    }

    /** 未公開でも package 固定のストア URL（公開後に同じリンクが有効になる）。 */
    const val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.urikai.app"
}
