package com.urikai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.urikai.app.game.ChartLineStyle
import com.urikai.app.game.DisplayLanguage
import com.urikai.app.game.LcdThemeMode
import com.urikai.app.game.PanelContrast
import com.urikai.app.game.ScanlineStrength
import com.urikai.app.game.SurfaceTone

val LocalDisplayLanguage = staticCompositionLocalOf { DisplayLanguage.JP }
val LocalChartLineStyle = staticCompositionLocalOf { ChartLineStyle.STEP }
val LocalScanlineStrength = staticCompositionLocalOf { ScanlineStrength.LOW }
val LocalPanelContrast = staticCompositionLocalOf { PanelContrast.NORMAL }

@Composable
fun UriKaiTheme(
    lcdTheme: LcdThemeMode = LcdThemeMode.GAME_BOY,
    displayLanguage: DisplayLanguage = DisplayLanguage.JP,
    chartLineStyle: ChartLineStyle = ChartLineStyle.STEP,
    scanlineStrength: ScanlineStrength = ScanlineStrength.LOW,
    panelContrast: PanelContrast = PanelContrast.NORMAL,
    surfaceTone: SurfaceTone = SurfaceTone.NORMAL,
    content: @Composable () -> Unit
) {
    val palette = paletteFor(lcdTheme, panelContrast, surfaceTone)
    val colorScheme = lightColorScheme(
        primary = palette.ink,
        secondary = palette.ink,
        background = palette.background,
        surface = palette.background,
        onPrimary = palette.background,
        onSecondary = palette.background,
        onBackground = palette.ink,
        onSurface = palette.ink
    )
    CompositionLocalProvider(
        LocalLcdColors provides palette,
        LocalDisplayLanguage provides displayLanguage,
        LocalChartLineStyle provides chartLineStyle,
        LocalScanlineStrength provides scanlineStrength,
        LocalPanelContrast provides panelContrast
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LcdTypography,
            content = content
        )
    }
}
