package com.urikai.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.urikai.app.ui.theme.InkFaint
import com.urikai.app.ui.theme.InkMuted

/**
 * macro_shape タグ用の抽象シルエット（数値・時間軸なし）。
 */
@Composable
fun MacroShapeSilhouette(
    macroShape: String,
    modifier: Modifier = Modifier,
    color: Color = InkMuted,
    strokeWidth: Float = 2f
) {
    Canvas(modifier = modifier.size(52.dp, 36.dp)) {
        val w = size.width
        val h = size.height
        val padX = w * 0.08f
        val padY = h * 0.12f
        val left = padX
        val right = w - padX
        val top = padY
        val bottom = h - padY
        val midX = w * 0.5f

        fun linePath(block: Path.() -> Unit): Path = Path().apply(block)

        val path = when (macroShape) {
            "V_RECOVER" -> linePath {
                moveTo(left, top + h * 0.08f)
                lineTo(midX - w * 0.04f, bottom)
                lineTo(midX + w * 0.04f, bottom)
                lineTo(right, top + h * 0.22f)
            }
            "W_DOUBLE" -> linePath {
                moveTo(left, top + h * 0.15f)
                lineTo(w * 0.33f, bottom)
                lineTo(midX, top + h * 0.35f)
                lineTo(w * 0.67f, bottom)
                lineTo(right, top + h * 0.2f)
            }
            "EARLY_SHOCK" -> linePath {
                moveTo(left, top + h * 0.55f)
                lineTo(left + w * 0.12f, bottom)
                lineTo(right, bottom - h * 0.05f)
            }
            "LATE_SPIKE" -> linePath {
                moveTo(left, bottom - h * 0.05f)
                lineTo(right - w * 0.18f, bottom - h * 0.05f)
                lineTo(right - w * 0.08f, top)
                lineTo(right, top + h * 0.18f)
            }
            "RANGE" -> linePath {
                moveTo(left, top + h * 0.45f)
                lineTo(w * 0.25f, top + h * 0.2f)
                lineTo(w * 0.45f, bottom - h * 0.15f)
                lineTo(w * 0.65f, top + h * 0.25f)
                lineTo(right, top + h * 0.45f)
            }
            "N_RALLY" -> linePath {
                moveTo(left, bottom)
                lineTo(w * 0.35f, top + h * 0.15f)
                lineTo(w * 0.55f, bottom - h * 0.1f)
                lineTo(right, top + h * 0.12f)
            }
            "INV_N" -> linePath {
                moveTo(left, top + h * 0.15f)
                lineTo(w * 0.35f, bottom - h * 0.1f)
                lineTo(w * 0.55f, top + h * 0.2f)
                lineTo(right, bottom)
            }
            "BUBBLE" -> linePath {
                moveTo(left, bottom - h * 0.05f)
                quadraticTo(midX, top - h * 0.05f, right - w * 0.05f, top + h * 0.18f)
                lineTo(right, bottom)
            }
            "INVERSE_V" -> linePath {
                moveTo(left, bottom - h * 0.05f)
                lineTo(midX - w * 0.04f, top + h * 0.08f)
                lineTo(midX + w * 0.04f, top + h * 0.08f)
                lineTo(right, bottom - h * 0.05f)
            }
            "GRIND_UP" -> linePath {
                moveTo(left, bottom - h * 0.08f)
                lineTo(right - w * 0.12f, top + h * 0.18f)
                lineTo(right, top + h * 0.28f)
            }
            "GRIND_DOWN" -> linePath {
                moveTo(left, top + h * 0.18f)
                lineTo(right - w * 0.12f, bottom - h * 0.08f)
                lineTo(right, bottom - h * 0.18f)
            }
            else -> linePath {
                moveTo(left, top + h * 0.45f)
                lineTo(right, top + h * 0.45f)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun MacroShapeSilhouetteLocked(
    modifier: Modifier = Modifier,
    peekMacroShape: String = "",
    reveal: Float = 0f,
) {
    val amount = reveal.coerceIn(0f, 1f)
    if (amount > 0.05f && peekMacroShape.isNotBlank()) {
        MacroShapeSilhouette(
            macroShape = peekMacroShape,
            modifier = modifier,
            color = InkFaint.copy(alpha = 0.35f + amount * 0.55f),
            strokeWidth = 1.5f + amount
        )
    } else {
        MacroShapeSilhouette(
            macroShape = "",
            modifier = modifier,
            color = InkFaint,
            strokeWidth = 1.5f
        )
    }
}
