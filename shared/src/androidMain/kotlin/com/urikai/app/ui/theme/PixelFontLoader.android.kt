package com.urikai.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.urikai.app.res.Res
import com.urikai.app.res.dotgothic16
import org.jetbrains.compose.resources.Font

@Composable
actual fun rememberPixelFontFamily(): FontFamily? = FontFamily(Font(Res.font.dotgothic16))
