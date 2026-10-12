package com.urikai.app.game

/**
 * チャートが何の値か（content/stages_meta.csv の market_kind）。
 */
enum class MarketKind(val code: String) {
    INDEX("INDEX"),
    FX("FX"),
    CRYPTO("CRYPTO"),
    OIL("OIL"),
    STOCK("STOCK"),
    ;

    companion object {
        fun fromCode(raw: String): MarketKind {
            val code = raw.trim().uppercase()
            return entries.firstOrNull { it.code == code }
                ?: throw IllegalArgumentException("Unknown market_kind: $raw")
        }
    }
}
