package com.urikai.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

/**
 * 16px系ビットマップ由来のドットゴシック。日本語・英数字を統一。
 * Web ではフォント読み込みが非同期なので、ルートで読み込み完了後に [installPixelFont] してから画面を組む。
 */
var PixelFont: FontFamily = FontFamily.Monospace
    private set

var LcdTypography: Typography = buildTypography()
    private set

fun installPixelFont(family: FontFamily) {
    PixelFont = family
    LcdTypography = buildTypography()
}

private fun buildTypography() = Typography(
    displayLarge = TextStyle(fontFamily = PixelFont, fontSize = 36.sp),
    displayMedium = TextStyle(fontFamily = PixelFont, fontSize = 28.sp),
    headlineLarge = TextStyle(fontFamily = PixelFont, fontSize = 24.sp),
    headlineMedium = TextStyle(fontFamily = PixelFont, fontSize = 20.sp),
    titleLarge = TextStyle(fontFamily = PixelFont, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = PixelFont, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = PixelFont, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = PixelFont, fontSize = 13.sp),
    bodySmall = TextStyle(fontFamily = PixelFont, fontSize = 11.sp),
    labelLarge = TextStyle(fontFamily = PixelFont, fontSize = 12.sp)
)
