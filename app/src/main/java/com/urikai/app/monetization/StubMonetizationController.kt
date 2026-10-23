package com.urikai.app.monetization

import android.util.Log
import com.urikai.app.platform.MonetizationController

/**
 * M1 スタブ。AdMob / Play Billing 接続までの仮実装。
 * Debug では「なしにする」で即 adsRemoved 相当を返す。
 */
class StubMonetizationController(
    private val isDebugBuild: Boolean,
) : MonetizationController {

    override fun maybeShowInterstitial(adsRemoved: Boolean, onContinue: () -> Unit) {
        if (adsRemoved) {
            onContinue()
            return
        }
        Log.d(TAG, "interstitial stub — skip (wire AdMob in M2)")
        onContinue()
    }

    override fun showRewarded(adsRemoved: Boolean, onResult: (rewarded: Boolean) -> Unit) {
        if (adsRemoved) {
            Log.d(TAG, "rewarded stub — adsRemoved, grant")
            onResult(true)
            return
        }
        if (isDebugBuild) {
            Log.d(TAG, "rewarded stub — grant in debug")
            onResult(true)
            return
        }
        Log.d(TAG, "rewarded stub — not connected")
        onResult(false)
    }

    override fun purchaseRemoveAds(onResult: (purchased: Boolean) -> Unit) {
        if (isDebugBuild) {
            Log.d(TAG, "purchaseRemoveAds stub — grant in debug")
            onResult(true)
            return
        }
        Log.d(TAG, "purchaseRemoveAds stub — not connected")
        onResult(false)
    }

    override fun restorePurchases(onResult: (adsRemoved: Boolean) -> Unit) {
        Log.d(TAG, "restorePurchases stub")
        onResult(false)
    }

    companion object {
        private const val TAG = "UriKaiMonetization"
    }
}
