package com.urikai.app.game

import kotlin.math.abs

/**
 * ラウンドトリップ（売り）の損得判定。買い約定には付けない。
 */
enum class FillQuality {
    FANTASTIC,
    PERFECT,
    GREAT,
    OK,
    MISS,
}

object FillEvaluator {

    /** 建値に対する損益％の閾値 */
    private const val FLAT_PCT = 0.005f
    private const val GREAT_PCT = 0.01f
    private const val PERFECT_PCT = 0.02f
    private const val FANTASTIC_PCT = 0.08f

    /**
     * 売り約定のみ。entry=建値（直近の買い約定）、sell=売りの約定価格。
     * （売値 − 建値）/ 建値 で段階化。
     */
    fun evaluateSell(entryPrice: Int, sellPrice: Int): FillQuality {
        if (entryPrice <= 0) return FillQuality.OK
        val pnlPct = (sellPrice - entryPrice).toFloat() / entryPrice.toFloat()
        val absPct = abs(pnlPct)
        if (absPct < FLAT_PCT) return FillQuality.OK
        if (pnlPct < 0f) return FillQuality.MISS
        return when {
            pnlPct >= FANTASTIC_PCT -> FillQuality.FANTASTIC
            pnlPct >= PERFECT_PCT -> FillQuality.PERFECT
            pnlPct >= GREAT_PCT -> FillQuality.GREAT
            else -> FillQuality.OK
        }
    }

    /**
     * ラグ帰属用（矢印・買いSEのピッチ）。判定ポップには使わない。
     * FANTASTIC は出さない（最大 PERFECT）。
     */
    fun evaluateLag(isBuy: Boolean, submitPrice: Int, fillPrice: Int): FillQuality {
        if (submitPrice <= 0) return FillQuality.OK
        val delta = fillPrice - submitPrice
        val absPct = abs(delta).toFloat() / submitPrice.toFloat()
        if (absPct < FLAT_PCT) return FillQuality.OK
        val favorable = if (isBuy) delta < 0 else delta > 0
        if (!favorable) return FillQuality.MISS
        return when {
            absPct >= PERFECT_PCT -> FillQuality.PERFECT
            absPct >= GREAT_PCT -> FillQuality.GREAT
            else -> FillQuality.OK
        }
    }

    fun isPositive(quality: FillQuality): Boolean =
        quality == FillQuality.FANTASTIC ||
            quality == FillQuality.PERFECT ||
            quality == FillQuality.GREAT
}

/** 判定スタンプの表示時間。FANTASTIC は特に長く。 */
fun fillJudgmentDurationMs(quality: FillQuality): Long = when (quality) {
    FillQuality.FANTASTIC -> 1_680L
    FillQuality.PERFECT -> 980L
    FillQuality.GREAT -> 720L
    FillQuality.OK -> 420L
    FillQuality.MISS -> 860L
}
