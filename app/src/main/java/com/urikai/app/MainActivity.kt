package com.urikai.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.ads.MobileAds
import com.urikai.app.audio.BgmPlayer
import com.urikai.app.audio.SoundPoolBackend
import com.urikai.app.data.SharedPreferencesStore
import com.urikai.app.feedback.VibratorHapticBackend
import com.urikai.app.monetization.PlayBillingController
import com.urikai.app.platform.MonetizationController
import com.urikai.app.platform.PlatformServices
import com.urikai.app.ui.UriKaiApp

class MainActivity : ComponentActivity() {

    private var monetization: MonetizationController? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = applicationContext
        val billing = PlayBillingController(activity = this)
        monetization = billing
        val openStore = { name: String -> SharedPreferencesStore(app, name) }
        // ViewModel 生成前に: 進捗エポック上昇時のみセーブを初回状態へ
        com.urikai.app.data.ProgressFreshStart.applyIfNeeded(openStore)
        val services = PlatformServices(
            openStore = openStore,
            sound = SoundPoolBackend(app),
            haptics = VibratorHapticBackend(app),
            bgm = BgmPlayer(app),
            monetization = billing,
            isDebugBuild = BuildConfig.DEBUG,
            appVersionCode = BuildConfig.VERSION_CODE,
            shareText = ::shareText,
            restartApp = ::restartAppProcess,
            openStoreListing = ::openStoreListing,
        )
        setContent {
            UriKaiApp(services)
        }
        // チャート／SE／Compose 初回描画を優先。AdMob は UI 表示後に遅延初期化。
        window.decorView.post {
            mainHandler.postDelayed({ warmUpMobileAds(billing) }, ADS_WARMUP_DELAY_MS)
        }
    }

    private fun warmUpMobileAds(billing: PlayBillingController) {
        if (isDestroyed) return
        runCatching {
            MobileAds.initialize(this) {
                Log.d("UriKaiAds", "MobileAds ready")
                if (!isDestroyed) {
                    billing.warmUpAds()
                }
            }
        }.onFailure { e ->
            Log.w("UriKaiAds", "MobileAds.initialize failed: ${e.message}")
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        monetization?.release()
        monetization = null
        super.onDestroy()
    }

    private fun shareText(text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, title))
    }

    /** デバッグ用ワイプ後にコールドスタート相当へ戻す。 */
    private fun restartAppProcess() {
        val launch = packageManager.getLaunchIntentForPackage(packageName) ?: return
        launch.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
        startActivity(launch)
        finishAffinity()
        // apply 済み prefs を確実に読ませるためプロセスを落とす
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    /**
     * Play ストアのこのアプリ（applicationId）詳細へ。
     * レビュー投稿 UI はストア側。未公開ビルドではページが無い場合あり。
     */
    private fun openStoreListing() {
        val id = packageName // = applicationId (com.urikai.app)
        val market = Uri.parse("market://details?id=$id")
        val web = Uri.parse("https://play.google.com/store/apps/details?id=$id")
        val marketIntent = Intent(Intent.ACTION_VIEW, market).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resolved = marketIntent.resolveActivity(packageManager)
        if (resolved != null) {
            startActivity(marketIntent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, web).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    companion object {
        /** 初回フレーム後、さらに少し空けてから AdMob を起こす。 */
        private const val ADS_WARMUP_DELAY_MS = 1_500L
    }
}
