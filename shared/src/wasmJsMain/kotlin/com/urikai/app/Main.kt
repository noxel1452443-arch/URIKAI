package com.urikai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeViewport
import com.urikai.app.platform.LocalStorageStore
import com.urikai.app.platform.PlatformServices
import com.urikai.app.platform.WebBgm
import com.urikai.app.platform.WebHaptics
import com.urikai.app.platform.WebSoundBackend
import com.urikai.app.ui.UriKaiApp

private fun jsShareText(text: String): Unit = js(
    """{
        if (navigator.share) {
            navigator.share({ text: text }).catch(function () {});
        } else if (navigator.clipboard) {
            navigator.clipboard.writeText(text).then(function () { alert('Copied!'); }, function () {});
        }
    }"""
)

/** 横長画面（PC）では縦長スマホ比率の枠に収める。 */
private const val MAX_WIDTH_PER_HEIGHT = 0.56f

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val services = PlatformServices(
        openStore = { name -> LocalStorageStore(name) },
        sound = WebSoundBackend,
        haptics = WebHaptics,
        bgm = WebBgm,
        shareText = { text, _ -> jsShareText(text) },
    )
    ComposeViewport("composeApp") {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().background(Color(0xFF1B2410)),
            contentAlignment = Alignment.Center
        ) {
            val frameWidth = minOf(maxWidth, maxHeight * MAX_WIDTH_PER_HEIGHT)
            Box(Modifier.width(frameWidth).fillMaxHeight()) {
                UriKaiApp(services)
            }
        }
    }
}
