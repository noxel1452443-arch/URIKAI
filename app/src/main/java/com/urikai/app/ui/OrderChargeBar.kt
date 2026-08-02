package com.urikai.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.urikai.app.game.CoolFxTier
import com.urikai.app.ui.theme.Ink
import com.urikai.app.ui.theme.LcdBackground

/**
 * チャージ中のボタン最下段バー。
 * NONE=細バー / I=きれいな二重レール / II=砂時計（上下に砂が落ちる）。
 */
@Composable
fun OrderChargeBottomBar(
    progress: Float,
    fxTier: CoolFxTier = CoolFxTier.NONE,
    modifier: Modifier = Modifier,
) {
    val ink = Ink
    val bg = LcdBackground
    Canvas(modifier = modifier) {
        val p = progress.coerceIn(0f, 1f)
        val pad = 10.dp.toPx()
        val barW = (size.width - pad * 2f).coerceAtLeast(0f)
        if (barW <= 0f) return@Canvas

        when (fxTier) {
            CoolFxTier.NONE -> {
                val barH = 4.dp.toPx()
                val y = size.height - pad - barH
                drawRect(ink.copy(alpha = 0.14f), Offset(pad, y), Size(barW, barH))
                if (p > 0f) {
                    drawRect(ink.copy(alpha = 0.82f), Offset(pad, y), Size(barW * p, barH))
                }
            }
            CoolFxTier.I -> {
                // とげ目盛なしの二重レール＋先端ハブ
                val barH = 9.dp.toPx()
                val y = size.height - pad - barH
                drawRect(ink.copy(alpha = 0.2f), Offset(pad, y), Size(barW, barH))
                drawRect(bg, Offset(pad + 2.5f, y + 2.5f), Size(barW - 5f, barH - 5f))
                drawRect(
                    ink.copy(alpha = 0.4f),
                    Offset(pad + 2.5f, y + 2.5f),
                    Size(barW - 5f, barH - 5f),
                    style = Stroke(width = 1.4f)
                )
                val innerW = (barW - 7f) * p
                if (innerW > 0f) {
                    drawRect(
                        ink.copy(alpha = 0.9f),
                        Offset(pad + 3.5f, y + 3.5f),
                        Size(innerW, barH - 7f)
                    )
                    val tipX = pad + 3.5f + innerW
                    val midY = y + barH / 2f
                    drawCircle(ink, 4f, Offset(tipX, midY))
                    drawCircle(bg, 2f, Offset(tipX, midY))
                }
            }
            CoolFxTier.II -> {
                // 砂時計：progress で上→下へ砂が落ちる
                val glassH = 36.dp.toPx()
                val glassW = 28.dp.toPx()
                val cx = size.width / 2f
                val top = size.height - pad - glassH
                val midY = top + glassH / 2f
                val halfW = glassW / 2f

                val glass = Path().apply {
                    moveTo(cx - halfW, top)
                    lineTo(cx + halfW, top)
                    lineTo(cx + 3f, midY)
                    lineTo(cx + halfW, top + glassH)
                    lineTo(cx - halfW, top + glassH)
                    lineTo(cx - 3f, midY)
                    close()
                }
                drawPath(glass, ink.copy(alpha = 0.22f))
                drawPath(glass, ink, style = Stroke(width = 2.8f))

                // 上側の残り砂（1-p）、下側の溜まり（p）
                val topSand = 1f - p
                clipPath(glass) {
                    if (topSand > 0.01f) {
                        val sandTopH = (midY - top - 4f) * topSand
                        drawRect(
                            ink.copy(alpha = 0.88f),
                            Offset(cx - halfW, midY - 4f - sandTopH),
                            Size(glassW, sandTopH)
                        )
                    }
                    if (p > 0.01f) {
                        val sandBotH = (top + glassH - midY - 4f) * p
                        drawRect(
                            ink.copy(alpha = 0.88f),
                            Offset(cx - halfW, top + glassH - 2f - sandBotH),
                            Size(glassW, sandBotH)
                        )
                    }
                    // 落ちる砂の筋
                    if (p in 0.02f..0.98f) {
                        drawLine(
                            ink.copy(alpha = 0.7f),
                            Offset(cx, midY - 6f),
                            Offset(cx, midY + 6f),
                            strokeWidth = 2.2f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                // 左右トラック：中央（砂時計側）から外側へ伸びる
                val trackH = 4.dp.toPx()
                val trackY = top + glassH + 4f
                val sideW = (size.width - pad * 2f - glassW - 16f) / 2f
                if (sideW > 8f) {
                    val fillW = sideW * p
                    // 左：右端（内側）起点 → 左へ増える
                    drawRect(ink.copy(alpha = 0.18f), Offset(pad, trackY), Size(sideW, trackH))
                    if (fillW > 0f) {
                        drawRect(ink, Offset(pad + sideW - fillW, trackY), Size(fillW, trackH))
                    }
                    // 右：左端（内側）起点 → 右へ増える
                    val rx = size.width - pad - sideW
                    drawRect(ink.copy(alpha = 0.18f), Offset(rx, trackY), Size(sideW, trackH))
                    if (fillW > 0f) {
                        drawRect(ink, Offset(rx, trackY), Size(fillW, trackH))
                    }
                }
            }
        }
    }
}
