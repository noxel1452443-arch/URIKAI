package com.urikai.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.urikai.app.game.LcdThemeMode
import com.urikai.app.game.PanelContrast
import com.urikai.app.game.SurfaceTone

data class LcdColors(
    val background: Color,
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val inkHatch: Color,
    val invertedText: Color,
    val buttonFill: Color,
    val buttonLabel: Color
)

val LocalLcdColors = staticCompositionLocalOf {
    paletteFor(LcdThemeMode.GAME_BOY, PanelContrast.NORMAL, SurfaceTone.NORMAL)
}

private fun paperBackground(tone: SurfaceTone): Color = when (tone) {
    SurfaceTone.DIM -> Color(0xFFA8A8A8)
    SurfaceTone.NORMAL -> Color(0xFFC8C8C8)
    SurfaceTone.BRIGHT -> Color(0xFFF2F2F2)
}

private fun gameBoyBackground(tone: SurfaceTone): Color = when (tone) {
    SurfaceTone.DIM -> Color(0xFFADB58E)
    SurfaceTone.NORMAL -> Color(0xFFC3CBA0)
    SurfaceTone.BRIGHT -> Color(0xFFD8E0B8)
}

private fun darkInk(tone: SurfaceTone): Color = when (tone) {
    SurfaceTone.DIM -> Color(0xFF989898)
    SurfaceTone.NORMAL -> Color(0xFFC0C0C0)
    SurfaceTone.BRIGHT -> Color(0xFFF0F0F0)
}

private fun darkButtonFill(tone: SurfaceTone): Color = when (tone) {
    SurfaceTone.DIM -> Color(0xFF787878)
    SurfaceTone.NORMAL -> Color(0xFF989898)
    SurfaceTone.BRIGHT -> Color(0xFFB8B8B8)
}

fun paletteFor(
    mode: LcdThemeMode,
    contrast: PanelContrast,
    surfaceTone: SurfaceTone
): LcdColors {
    val (mutedAlpha, faintAlpha, hatchAlpha) = when (contrast) {
        PanelContrast.SOFT -> Triple(0.42f, 0.22f, 0.08f)
        PanelContrast.NORMAL -> Triple(0.68f, 0.40f, 0.20f)
        PanelContrast.HARD -> Triple(0.92f, 0.68f, 0.42f)
    }

    return when (mode) {
        LcdThemeMode.GAME_BOY -> {
            val ink = Color(0xFF1F241A)
            val bg = gameBoyBackground(surfaceTone)
            LcdColors(
                background = bg,
                ink = ink,
                inkMuted = ink.copy(alpha = mutedAlpha),
                inkFaint = ink.copy(alpha = faintAlpha),
                inkHatch = ink.copy(alpha = hatchAlpha),
                invertedText = bg,
                buttonFill = ink,
                buttonLabel = bg
            )
        }
        LcdThemeMode.E_PAPER -> {
            val ink = Color(0xFF1F241A)
            val bg = paperBackground(surfaceTone)
            LcdColors(
                background = bg,
                ink = ink,
                inkMuted = ink.copy(alpha = mutedAlpha),
                inkFaint = ink.copy(alpha = faintAlpha),
                inkHatch = ink.copy(alpha = hatchAlpha),
                invertedText = bg,
                buttonFill = ink,
                buttonLabel = bg
            )
        }
        LcdThemeMode.E_PAPER_INVERT -> {
            val ink = darkInk(surfaceTone)
            val bg = Color(0xFF1F241A)
            LcdColors(
                background = bg,
                ink = ink,
                inkMuted = ink.copy(alpha = mutedAlpha),
                inkFaint = ink.copy(alpha = faintAlpha),
                inkHatch = ink.copy(alpha = hatchAlpha),
                invertedText = bg,
                buttonFill = darkButtonFill(surfaceTone),
                buttonLabel = Color(0xFF1F241A)
            )
        }
    }
}

@Composable
fun lcdColors(): LcdColors = LocalLcdColors.current

val Ink: Color @Composable get() = LocalLcdColors.current.ink
val InkMuted: Color @Composable get() = LocalLcdColors.current.inkMuted
val InkFaint: Color @Composable get() = LocalLcdColors.current.inkFaint
val InkHatch: Color @Composable get() = LocalLcdColors.current.inkHatch
val LcdBackground: Color @Composable get() = LocalLcdColors.current.background
val LcdInk: Color @Composable get() = LocalLcdColors.current.ink
val LcdInvertedText: Color @Composable get() = LocalLcdColors.current.invertedText
val LcdButtonFill: Color @Composable get() = LocalLcdColors.current.buttonFill
val LcdButtonLabel: Color @Composable get() = LocalLcdColors.current.buttonLabel
val ScreenBackground: Color @Composable get() = LocalLcdColors.current.background
