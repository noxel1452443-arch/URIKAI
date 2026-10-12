package com.urikai.app.game

import kotlin.math.PI
import kotlin.math.round
import kotlin.math.sin
import kotlin.random.Random

/**
 * マクロ price 列の上に、プレイごとのミクロ swing を重ねる。
 * 実力軸: QUIET/RIPPLE を厚くし、CHOP/COIL_SNAP の強さと出現を抑える。
 */
object SessionMicroOverlay {

    private enum class LegKind { UP, DOWN, FLAT }

    private enum class MicroStyle {
        /** ほぼ無音 — マクロが読める */
        QUIET,
        /** 1〜4秒周期の通常 ripple */
        RIPPLE,
        /** 1〜2秒の速い chop + スパイク */
        CHOP,
        /** 緩やかに見えてから急変（コイル→スナップ） */
        COIL_SNAP,
    }

    private data class Leg(val start: Int, val end: Int, val kind: LegKind)

    fun apply(macro: FloatArray, seed: Long): FloatArray {
        if (macro.size < 4) return macro.copyOf()

        val rng = Random(seed)
        val out = macro.copyOf()
        val globalChaos = rng.nextFloat()
        val legs = splitLegs(macro)

        for (leg in legs) {
            if (leg.end <= leg.start) continue
            applyLegMicro(out, macro, leg, globalChaos, rng)
        }

        return reanchor(out, macro[0])
    }

    private fun applyLegMicro(
        out: FloatArray,
        macro: FloatArray,
        leg: Leg,
        globalChaos: Float,
        rng: Random
    ) {
        val style = pickStyle(rng, globalChaos)
        val periodSec = rng.nextInt(1, 5)
        // 振幅を全体に抑えてマクロの転換を残す
        val baseAmp = when (style) {
            MicroStyle.QUIET -> rng.nextDouble(0.3, 0.9).toFloat()
            MicroStyle.RIPPLE -> rng.nextDouble(0.8, 2.4).toFloat()
            MicroStyle.CHOP -> rng.nextDouble(1.2, 3.2).toFloat()
            MicroStyle.COIL_SNAP -> rng.nextDouble(1.0, 2.8).toFloat()
        }
        var phase = rng.nextDouble(0.0, 2 * PI).toFloat()
        val phaseStep = (2.0 * PI / periodSec.coerceAtLeast(1)).toFloat()

        when (style) {
            MicroStyle.QUIET -> {
                for (sec in leg.start..leg.end) {
                    phase += phaseStep * 0.7f
                    out[sec] = (out[sec] + sin(phase) * baseAmp * 0.35f).coerceAtLeast(1f)
                }
            }

            MicroStyle.RIPPLE -> {
                for (sec in leg.start..leg.end) {
                    phase += phaseStep
                    var micro = sin(phase) * baseAmp
                    if (rng.nextFloat() < 0.03f) {
                        micro += spikeSign(rng) * baseAmp * rng.nextDouble(0.3, 0.7).toFloat()
                    }
                    out[sec] = (out[sec] + micro).coerceAtLeast(1f)
                }
            }

            MicroStyle.CHOP -> {
                val fastPeriod = rng.nextInt(1, 3)
                val fastStep = (2.0 * PI / fastPeriod.coerceAtLeast(1)).toFloat()
                for (sec in leg.start..leg.end) {
                    phase += fastStep
                    var micro = sin(phase) * baseAmp
                    if (rng.nextFloat() < 0.08f) {
                        micro += spikeSign(rng) * baseAmp * rng.nextDouble(0.4, 0.9).toFloat()
                    }
                    out[sec] = (out[sec] + micro).coerceAtLeast(1f)
                }
            }

            MicroStyle.COIL_SNAP -> {
                val legLen = leg.end - leg.start
                val snapSec = if (legLen >= 3) {
                    leg.start + rng.nextInt(2, legLen)
                } else {
                    leg.end
                }
                val coilStrength = rng.nextDouble(0.35, 0.65).toFloat()
                val snapDelta = spikeSign(rng) * rng.nextDouble(1.8, 4.5).toFloat()

                for (sec in leg.start..leg.end) {
                    var micro = 0f
                    when {
                        sec < snapSec -> {
                            if (sec > leg.start) {
                                val macroStep = macro[sec] - macro[sec - 1]
                                micro -= macroStep * coilStrength
                            }
                            phase += phaseStep * 0.5f
                            micro += sin(phase) * baseAmp * 0.35f
                        }
                        sec == snapSec -> {
                            micro = snapDelta
                        }
                        else -> {
                            phase += phaseStep
                            micro = sin(phase) * baseAmp * 0.45f
                        }
                    }
                    out[sec] = (out[sec] + micro).coerceAtLeast(1f)
                }
            }
        }
    }

    private fun pickStyle(rng: Random, globalChaos: Float): MicroStyle {
        val roll = rng.nextFloat()
        // 低カオス寄りに寄せ、読めない脚の割合を下げる
        return when {
            globalChaos < 0.45f -> when {
                roll < 0.48f -> MicroStyle.QUIET
                roll < 0.88f -> MicroStyle.RIPPLE
                roll < 0.96f -> MicroStyle.CHOP
                else -> MicroStyle.COIL_SNAP
            }
            globalChaos < 0.75f -> when {
                roll < 0.32f -> MicroStyle.QUIET
                roll < 0.72f -> MicroStyle.RIPPLE
                roll < 0.90f -> MicroStyle.CHOP
                else -> MicroStyle.COIL_SNAP
            }
            else -> when {
                roll < 0.20f -> MicroStyle.QUIET
                roll < 0.55f -> MicroStyle.RIPPLE
                roll < 0.82f -> MicroStyle.CHOP
                else -> MicroStyle.COIL_SNAP
            }
        }
    }

    private fun spikeSign(rng: Random): Float = if (rng.nextBoolean()) 1f else -1f

    private fun splitLegs(macro: FloatArray): List<Leg> {
        val n = macro.size
        val slope = FloatArray(n)
        for (i in 1 until n) {
            val lookback = (i - 3).coerceAtLeast(0)
            val span = (i - lookback).coerceAtLeast(1)
            slope[i] = (macro[i] - macro[lookback]) / span
        }

        val legs = mutableListOf<Leg>()
        var start = 0
        var kind = classifySlope(slope[1])

        for (i in 2 until n) {
            val nextKind = classifySlope(slope[i])
            if (nextKind != kind && i - start >= 4) {
                legs.add(Leg(start, i - 1, kind))
                start = i - 1
                kind = nextKind
            } else if (nextKind != kind) {
                kind = nextKind
            }
        }
        legs.add(Leg(start, n - 1, kind))
        return legs
    }

    private fun classifySlope(slope: Float): LegKind = when {
        slope > 0.07f -> LegKind.UP
        slope < -0.07f -> LegKind.DOWN
        else -> LegKind.FLAT
    }

    private fun reanchor(series: FloatArray, anchor: Float): FloatArray {
        if (series.isEmpty()) return series
        val scale = anchor / series[0]
        return FloatArray(series.size) { i ->
            round(series[i] * scale * 100f) / 100f
        }
    }
}
