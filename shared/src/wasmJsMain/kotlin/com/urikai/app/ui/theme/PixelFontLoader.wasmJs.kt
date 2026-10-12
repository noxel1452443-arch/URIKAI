package com.urikai.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import com.urikai.app.res.Res
import com.urikai.app.res.dotgothic16
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberPixelFontFamily(): FontFamily? {
    val font by preloadFont(Res.font.dotgothic16)
    val loaded = font ?: return null
    return remember(loaded) { FontFamily(loaded) }
}
