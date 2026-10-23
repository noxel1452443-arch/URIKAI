package com.urikai.app.monetization

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.urikai.app.BuildConfig

/**
 * 任意リワード（給料2倍・レベルアップ半額）。
 * 未準備時は1回ロードしてから表示を試す。失敗時は false。
 */
class RewardedAdGate(
    private val activity: Activity,
    private val unitId: String = BuildConfig.ADMOB_REWARDED_UNIT_ID,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var rewarded: RewardedAd? = null
    private var loading = false
    private var pendingShow: ((Boolean) -> Unit)? = null

    fun preload() {
        if (loading || rewarded != null) return
        if (unitId.isBlank()) return
        loading = true
        RewardedAd.load(
            activity,
            unitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loading = false
                    rewarded = ad
                    Log.d(TAG, "rewarded loaded")
                    val pending = pendingShow
                    pendingShow = null
                    if (pending != null) {
                        showLoaded(ad, pending)
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    rewarded = null
                    Log.w(TAG, "rewarded load failed: ${error.message}")
                    val pending = pendingShow
                    pendingShow = null
                    if (pending != null) {
                        mainHandler.post { pending(false) }
                    }
                }
            }
        )
    }

    fun show(onResult: (rewarded: Boolean) -> Unit) {
        val ad = rewarded
        if (ad != null) {
            showLoaded(ad, onResult)
            return
        }
        if (unitId.isBlank()) {
            onResult(false)
            return
        }
        // 未ロードなら1回だけ待って表示
        pendingShow = onResult
        preload()
    }

    private fun showLoaded(ad: RewardedAd, onResult: (Boolean) -> Unit) {
        rewarded = null
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload()
                mainHandler.post { onResult(earned) }
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "rewarded show failed: ${error.message}")
                preload()
                mainHandler.post { onResult(false) }
            }
        }
        ad.show(
            activity,
            OnUserEarnedRewardListener {
                earned = true
                Log.d(TAG, "reward earned")
            }
        )
    }

    fun release() {
        pendingShow = null
        rewarded = null
        loading = false
    }

    companion object {
        private const val TAG = "UriKaiAds"
    }
}
