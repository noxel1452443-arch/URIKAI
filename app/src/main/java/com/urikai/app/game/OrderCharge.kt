package com.urikai.app.game

/**
 * 約定後クールダウン（ボタン再押下までの待機）。
 */
object OrderCharge {

    fun progress(nowEpochMs: Long, startedAtEpochMs: Long, readyAtEpochMs: Long): Float {
        if (readyAtEpochMs <= 0L || nowEpochMs >= readyAtEpochMs) return 1f
        if (startedAtEpochMs <= 0L || startedAtEpochMs >= readyAtEpochMs) return 0f
        return ((nowEpochMs - startedAtEpochMs).toFloat() /
            (readyAtEpochMs - startedAtEpochMs).toFloat()).coerceIn(0f, 1f)
    }

    fun isReady(nowEpochMs: Long, readyAtEpochMs: Long): Boolean =
        readyAtEpochMs <= 0L || nowEpochMs >= readyAtEpochMs
}
