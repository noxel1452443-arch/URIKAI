package com.urikai.app.monetization

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.urikai.app.BuildConfig
import com.urikai.app.platform.MonetizationController

/**
 * Play Billing（広告除去・買い切り `urikai_remove_ads`）＋ AdMob インタースティシャル／リワード。
 */
class PlayBillingController(
    private val activity: Activity,
    private val productId: String = BuildConfig.REMOVE_ADS_PRODUCT_ID,
) : MonetizationController, PurchasesUpdatedListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var purchaseCallback: ((Boolean) -> Unit)? = null
    private var productDetails: ProductDetails? = null
    private val interstitialGate = InterstitialAdGate(activity)
    private val rewardedGate = RewardedAdGate(activity)

    private val billingClient: BillingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    init {
        // 広告プリロードは warmUpAds()（MobileAds 初期化後・UI 表示後）で行う。
        startConnection()
    }

    /**
     * 起動直後の同時ロードを避け、インタースティシャル→（遅延）リワードの順で温める。
     * エミュレータ／低メモリ端末での初回起動ハング・OOM を抑える。
     */
    override fun warmUpAds() {
        interstitialGate.preload()
        mainHandler.postDelayed({ rewardedGate.preload() }, REWARDED_PRELOAD_DELAY_MS)
    }

    private fun startConnection() {
        if (billingClient.isReady) return
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "billing setup failed: ${result.debugMessage}")
                    return
                }
                Log.d(TAG, "billing ready; productId=$productId")
                prefetchProductDetails()
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "billing disconnected")
            }
        })
    }

    private fun prefetchProductDetails(onDone: (() -> Unit)? = null) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        billingClient.queryProductDetailsAsync(params) { result, detailsResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "queryProductDetails: ${result.debugMessage}")
                onDone?.let { mainHandler.post(it) }
                return@queryProductDetailsAsync
            }
            productDetails = detailsResult.productDetailsList.firstOrNull()
            if (productDetails == null) {
                Log.w(TAG, "product not found: $productId")
            }
            onDone?.let { mainHandler.post(it) }
        }
    }

    /** 購入オプション（buy）の offerToken。無いと launch が失敗しやすい。 */
    private fun offerTokenFor(details: ProductDetails): String? {
        val list = details.oneTimePurchaseOfferDetailsList
        if (!list.isNullOrEmpty()) {
            return list.first().offerToken
        }
        @Suppress("DEPRECATION")
        return details.oneTimePurchaseOfferDetails?.offerToken
    }

    private fun formattedPriceFor(details: ProductDetails): String? {
        val list = details.oneTimePurchaseOfferDetailsList
        if (!list.isNullOrEmpty()) {
            return list.first().formattedPrice
        }
        @Suppress("DEPRECATION")
        return details.oneTimePurchaseOfferDetails?.formattedPrice
    }

    override fun removeAdsPriceLabel(): String =
        productDetails?.let { formattedPriceFor(it) } ?: "¥290"

    override fun maybeShowInterstitial(adsRemoved: Boolean, onContinue: () -> Unit) {
        interstitialGate.maybeShow(adsRemoved, onContinue)
    }

    override fun showRewarded(adsRemoved: Boolean, onResult: (rewarded: Boolean) -> Unit) {
        if (adsRemoved) {
            mainHandler.post { onResult(true) }
            return
        }
        mainHandler.post {
            rewardedGate.show(onResult)
        }
    }

    override fun purchaseRemoveAds(onResult: (purchased: Boolean) -> Unit) {
        mainHandler.post {
            if (!billingClient.isReady) {
                startConnection()
                onResult(false)
                return@post
            }
            fun launch() {
                val details = productDetails
                if (details == null) {
                    Log.w(TAG, "purchase aborted: no product details")
                    onResult(false)
                    return
                }
                val offerToken = offerTokenFor(details)
                if (offerToken.isNullOrBlank()) {
                    Log.w(TAG, "purchase aborted: no offerToken (activate purchase option in Console)")
                    onResult(false)
                    return
                }
                purchaseCallback = onResult
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details)
                                .setOfferToken(offerToken)
                                .build()
                        )
                    )
                    .build()
                val launchResult = billingClient.launchBillingFlow(activity, flowParams)
                if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "launchBillingFlow: ${launchResult.debugMessage}")
                    purchaseCallback = null
                    onResult(false)
                }
            }
            if (productDetails == null || offerTokenFor(productDetails!!) == null) {
                prefetchProductDetails { launch() }
            } else {
                launch()
            }
        }
    }

    override fun restorePurchases(onResult: (adsRemoved: Boolean) -> Unit) {
        mainHandler.post {
            if (!billingClient.isReady) {
                startConnection()
                onResult(false)
                return@post
            }
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            billingClient.queryPurchasesAsync(params) { result, purchases ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "restore: ${result.debugMessage}")
                    mainHandler.post { onResult(false) }
                    return@queryPurchasesAsync
                }
                val owned = purchases.any { purchase ->
                    purchase.products.contains(productId) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                purchases.filter {
                    it.products.contains(productId) &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        !it.isAcknowledged
                }.forEach { acknowledge(it) }
                mainHandler.post { onResult(owned) }
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val cb = purchaseCallback
        purchaseCallback = null
        if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            mainHandler.post { cb?.invoke(false) }
            return
        }
        if (result.responseCode != BillingClient.BillingResponseCode.OK || purchases.isNullOrEmpty()) {
            Log.w(TAG, "onPurchasesUpdated: ${result.debugMessage}")
            mainHandler.post { cb?.invoke(false) }
            return
        }
        var granted = false
        for (purchase in purchases) {
            if (!purchase.products.contains(productId)) continue
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) continue
            granted = true
            if (!purchase.isAcknowledged) acknowledge(purchase)
        }
        mainHandler.post { cb?.invoke(granted) }
    }

    private fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "acknowledge: ${result.debugMessage}")
            }
        }
    }

    override fun release() {
        purchaseCallback = null
        interstitialGate.release()
        rewardedGate.release()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    companion object {
        private const val TAG = "UriKaiBilling"
        private const val REWARDED_PRELOAD_DELAY_MS = 8_000L
    }
}
