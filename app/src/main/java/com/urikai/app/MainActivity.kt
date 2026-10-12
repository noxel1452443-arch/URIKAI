package com.urikai.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.urikai.app.audio.BgmPlayer
import com.urikai.app.audio.SoundPoolBackend
import com.urikai.app.data.SharedPreferencesStore
import com.urikai.app.feedback.VibratorHapticBackend
import com.urikai.app.platform.PlatformServices
import com.urikai.app.ui.UriKaiApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = applicationContext
        val services = PlatformServices(
            openStore = { name -> SharedPreferencesStore(app, name) },
            sound = SoundPoolBackend(app),
            haptics = VibratorHapticBackend(app),
            bgm = BgmPlayer(app),
            isDebugBuild = BuildConfig.DEBUG,
            shareText = ::shareText,
        )
        setContent {
            UriKaiApp(services)
        }
    }

    private fun shareText(text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, title))
    }
}
