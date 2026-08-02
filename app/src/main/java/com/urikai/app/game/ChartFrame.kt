package com.urikai.app.game

/**
 * チャート描画用フレーム。GameUiState は拡張せず、こちらで描画に必要な情報を渡す。
 */
data class ChartFrame(
    val priceRatios: FloatArray = FloatArray(0),
    val startOffsetSeconds: Int = 0,
    val elapsedMs: Long = 0L,
    val calendarStart: String = "",
    val calendarEnd: String = "",
    val marketKind: String = "",
    val anchorWhen: String = "",
    val anchorLabel: String = "",
    /** 転換帯（charts.csv 絶対秒） */
    val pivotBandStart: Int = 0,
    val pivotBandEnd: Int = 0,
    val isRunning: Boolean = false,
    val markers: List<TradeMarker> = emptyList(),
    val hasStock: Boolean = false,
    val resultInfo: ResultInfo? = null,
    /** TIME 60 の FINISH! 表示中 */
    val showFinishBanner: Boolean = false,
    /** 直近の利子獲得演出（0 なら非表示） */
    val interestTickAmount: Int = 0,
    val interestTickUntilEpochMs: Long = 0L,
    /** 利子ティック表示用の倍率（1f 超ならバッジ表示） */
    val interestTickMult: Float = 1f,
    /** このフレームでの実効注文ラグ（pending タイマー用） */
    val activeOrderLagMs: Long = 0L,
    /** 次の注文が可能になる時刻（0 = 即時可） */
    val orderReadyAtEpochMs: Long = 0L,
    /** チャージ開始時刻 */
    val orderCooldownStartedAtEpochMs: Long = 0L,
    /** 注文中連打でタイマー微震（epoch ms。新しいほど強い） */
    val pendingShakeEpochMs: Long = 0L,
    /** 音OFF約定時の縁フラッシュ終了時刻 */
    val edgeFlashUntilEpochMs: Long = 0L,
    /** 中央判定スタンプ（売りのみ） */
    val fillJudgmentUntilEpochMs: Long = 0L,
    val fillJudgmentQuality: FillQuality? = null,
    /** 連続 GREAT/PERFECT/OK（売り判定）。MISS で切れ。0 で非表示。 */
    val fillCombo: Int = 0,
    /** COMBO が切れる時刻。増加で延長。 */
    val fillComboExpiresAtEpochMs: Long = 0L,
    /** COMBO 増加時のバウンス用トークン。 */
    val fillComboBounceEpochMs: Long = 0L,
    /** リザルトで受け取り操作済み */
    val resultPayoutClaimed: Boolean = false,
    /** ナノテック風体力。PLAYING 中に表示 */
    val healthPoints: Int = 4,
    /** この試合の最大 LIFE（MISSガードで 5） */
    val maxHealthPoints: Int = 4,
    /**
     * MISS で LIFE が減った演出トークン（epoch ms）。
     * 判定スタンプの直後に球が消えるよう UI 側で遅延再生する。
     */
    val lifeLossTokenEpochMs: Long = 0L,
    /** 消えた球の index（0始まり）。未演出は -1 */
    val lifeLossOrbIndex: Int = -1,
    /** リタイア演出段階 */
    val retirePhase: RetirePhase = RetirePhase.NONE,
) {
    val elapsedSeconds: Float
        get() = elapsedMs / 1_000f

    val retireLocked: Boolean
        get() = retirePhase == RetirePhase.SHOCK || retirePhase == RetirePhase.BANNER

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChartFrame) return false
        return startOffsetSeconds == other.startOffsetSeconds &&
            elapsedMs == other.elapsedMs &&
            calendarStart == other.calendarStart &&
            calendarEnd == other.calendarEnd &&
            marketKind == other.marketKind &&
            anchorWhen == other.anchorWhen &&
            anchorLabel == other.anchorLabel &&
            pivotBandStart == other.pivotBandStart &&
            pivotBandEnd == other.pivotBandEnd &&
            isRunning == other.isRunning &&
            hasStock == other.hasStock &&
            markers == other.markers &&
            resultInfo == other.resultInfo &&
            showFinishBanner == other.showFinishBanner &&
            interestTickAmount == other.interestTickAmount &&
            interestTickUntilEpochMs == other.interestTickUntilEpochMs &&
            interestTickMult == other.interestTickMult &&
            activeOrderLagMs == other.activeOrderLagMs &&
            orderReadyAtEpochMs == other.orderReadyAtEpochMs &&
            orderCooldownStartedAtEpochMs == other.orderCooldownStartedAtEpochMs &&
            pendingShakeEpochMs == other.pendingShakeEpochMs &&
            edgeFlashUntilEpochMs == other.edgeFlashUntilEpochMs &&
            fillJudgmentUntilEpochMs == other.fillJudgmentUntilEpochMs &&
            fillJudgmentQuality == other.fillJudgmentQuality &&
            fillCombo == other.fillCombo &&
            fillComboExpiresAtEpochMs == other.fillComboExpiresAtEpochMs &&
            fillComboBounceEpochMs == other.fillComboBounceEpochMs &&
            resultPayoutClaimed == other.resultPayoutClaimed &&
            healthPoints == other.healthPoints &&
            maxHealthPoints == other.maxHealthPoints &&
            lifeLossTokenEpochMs == other.lifeLossTokenEpochMs &&
            lifeLossOrbIndex == other.lifeLossOrbIndex &&
            retirePhase == other.retirePhase &&
            priceRatios.contentEquals(other.priceRatios)
    }

    override fun hashCode(): Int {
        var result = priceRatios.contentHashCode()
        result = 31 * result + startOffsetSeconds
        result = 31 * result + elapsedMs.hashCode()
        result = 31 * result + calendarStart.hashCode()
        result = 31 * result + calendarEnd.hashCode()
        result = 31 * result + marketKind.hashCode()
        result = 31 * result + anchorWhen.hashCode()
        result = 31 * result + anchorLabel.hashCode()
        result = 31 * result + pivotBandStart
        result = 31 * result + pivotBandEnd
        result = 31 * result + isRunning.hashCode()
        result = 31 * result + hasStock.hashCode()
        result = 31 * result + markers.hashCode()
        result = 31 * result + (resultInfo?.hashCode() ?: 0)
        result = 31 * result + showFinishBanner.hashCode()
        result = 31 * result + interestTickAmount
        result = 31 * result + interestTickUntilEpochMs.hashCode()
        result = 31 * result + interestTickMult.hashCode()
        result = 31 * result + activeOrderLagMs.hashCode()
        result = 31 * result + orderReadyAtEpochMs.hashCode()
        result = 31 * result + orderCooldownStartedAtEpochMs.hashCode()
        result = 31 * result + pendingShakeEpochMs.hashCode()
        result = 31 * result + edgeFlashUntilEpochMs.hashCode()
        result = 31 * result + fillJudgmentUntilEpochMs.hashCode()
        result = 31 * result + (fillJudgmentQuality?.hashCode() ?: 0)
        result = 31 * result + fillCombo
        result = 31 * result + fillComboExpiresAtEpochMs.hashCode()
        result = 31 * result + fillComboBounceEpochMs.hashCode()
        result = 31 * result + resultPayoutClaimed.hashCode()
        result = 31 * result + healthPoints
        result = 31 * result + maxHealthPoints
        result = 31 * result + lifeLossTokenEpochMs.hashCode()
        result = 31 * result + lifeLossOrbIndex
        result = 31 * result + retirePhase.hashCode()
        return result
    }
}
