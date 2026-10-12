package com.urikai.app.game

/**
 * 口座残高の運用プラン。日付が1日進むごとに残高へ掛かる。
 * 変動ありのプランは長期の期待値がプラスになるよう、平均 − 変動²/2 &gt; 0 にしている。
 *
 * @param dailyMeanPercent 1日あたりの平均増減（%）
 * @param dailyVolPercent 1日あたりのぶれ（標準偏差, %）。0 なら確定
 * @param unlockPeakBalance 最高残高がこの額以上で選べる
 */
enum class InvestPlan(
    val nameJp: String,
    val nameEn: String,
    val dailyMeanPercent: Float,
    val dailyVolPercent: Float,
    val unlockPeakBalance: Long,
) {
    DEPOSIT("銀行預金", "Bank Deposit", 0.3f, 0f, 0L),
    BALANCED("バランス投信", "Balanced Fund", 0.5f, 2f, 0L),
    GROWTH("グロース株ファンド", "Growth Fund", 0.8f, 6f, 0L),
    EMERGING("新興国ファンド", "Emerging Fund", 1.0f, 9f, 20_000L),
    ACORN("ドングリコイン積立", "Acorn Coin Plan", 2.0f, 14f, 50_000L);

    val isGuaranteed: Boolean get() = dailyVolPercent <= 0f

    fun displayName(lang: DisplayLanguage): String = when (lang) {
        DisplayLanguage.EN -> nameEn
        DisplayLanguage.JP -> nameJp
    }

    fun isUnlocked(peakBalance: Long): Boolean = peakBalance >= unlockPeakBalance

    companion object {
        val DEFAULT = DEPOSIT

        fun fromPersisted(name: String?): InvestPlan =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
