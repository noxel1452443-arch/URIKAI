package com.urikai.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/** DotGothic16 を読み込む。読み込み中は null（Web は非同期）。 */
@Composable
expect fun rememberPixelFontFamily(): FontFamily?
