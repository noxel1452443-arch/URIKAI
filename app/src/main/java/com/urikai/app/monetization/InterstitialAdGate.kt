package com.urikai.app.monetization

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.urikai.app.BuildConfig

/**
 * リザルト→ホーム前のインタースティシャル。
 * 未準備／失敗／除去済みはすぐ [onContinue]（ブロックしない）。
 */
class InterstitialAdGate(
    private val activity: Activity,
    private val unitId: String = BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var interstitial: InterstitialAd? = null
    private var loading = false

    fun preload() {
        if (loading || interstitial != null) return
        if (unitId.isBlank()) return
        loading = true
        InterstitialAd.load(
            activity,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    interstitial = ad
                    Log.d(TAG, "interstitial loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    interstitial = null
                    Log.w(TAG, "interstitial load failed: ${error.message}")
                }
            }
        )
    }

    fun maybeShow(adsRemoved: Boolean, onContinue: () -> Unit) {
        if (adsRemoved) {
            onContinue()
            return
        }
        val ad = interstitial
        if (ad == null) {
            Log.d(TAG, "interstitial not ready — skip")
            preload()
            onContinue()
            return
        }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload()
                mainHandler.post(onContinue)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "interstitial show failed: ${error.message}")
                preload()
                mainHandler.post(onContinue)
            }
        }
        ad.show(activity)
    }

    fun release() {
        interstitial = null
        loading = false
    }

    companion object {
        private const val TAG = "UriKaiAds"
    }
}
