package com.urikai.app.game

enum class ResultRegretKind {
    MISS,
    NEAR_MISS,
    BEST_NICE,
}

/** ハイライト文言の比較軸（複数同時マッチ可） */
enum class RegretTradeContextFlag {
    /** 前後±3秒の局所極値 */
    LOCAL_WINDOW,
    /** 買い：直前の売りより高い */
    VS_PRIOR_EXIT,
    /** 売り：直前の買いより低い */
    VS_PRIOR_ENTRY,
}

data class ResultRegretMoment(
    val gameTimeSec: Float,
    val isBuy: Boolean,
    val kind: ResultRegretKind,
    val tradeContexts: Set<RegretTradeContextFlag> = setOf(RegretTradeContextFlag.LOCAL_WINDOW),
)
