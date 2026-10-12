package com.urikai.app.game

/**
 * RESULT 振り返り用。約定タイミングがその場の底/天井にどれだけ近かったか。
 */
enum class TradeTimingGrade {
    /** 表示しない（中間） */
    NONE,
    /** 底/天井付近の好timing */
    NICE,
    /** 高値買い・安値売りなど */
    MISS,
}
