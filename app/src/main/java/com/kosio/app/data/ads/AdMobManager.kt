package com.kosio.app.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {

    private const val TAG = "AdMobManager"

    // Official Google AdMob Test Ad Unit IDs (Safe for Development & Testing)
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    // Frequency capping: 10 minutes (in milliseconds)
    private const val INTERSTITIAL_MIN_INTERVAL_MS = 10 * 60 * 1000L
    private var lastInterstitialShownTimestamp: Long = 0L

    private var rewardedAd: RewardedAd? = null
    private var isRewardedAdLoading = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialAdLoading = false

    fun initialize(context: Context) {
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "AdMob MobileAds initialized: $status")
            preloadAds(context)
        }
    }

    fun preloadAds(context: Context) {
        loadRewardedAd(context)
        loadInterstitialAd(context)
    }

    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isRewardedAdLoading) return
        isRewardedAdLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded Ad loaded successfully")
                    rewardedAd = ad
                    isRewardedAdLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded Ad failed to load: ${error.message}")
                    rewardedAd = null
                    isRewardedAdLoading = false
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var earnedReward = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad dismissed")
                    rewardedAd = null
                    loadRewardedAd(activity)
                    if (earnedReward) {
                        onUserEarnedReward()
                    }
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(TAG, "Rewarded Ad failed to show: ${error.message}")
                    rewardedAd = null
                    loadRewardedAd(activity)
                    // Fallback to giving reward if ad presentation failed
                    onUserEarnedReward()
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad showed fullscreen content")
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                earnedReward = true
            }
        } else {
            Log.d(TAG, "Rewarded Ad not ready, loading new one and granting fallback reward")
            loadRewardedAd(activity)
            onUserEarnedReward()
            onAdDismissed()
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialAdLoading) return
        isInterstitialAdLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad loaded successfully")
                    interstitialAd = ad
                    isInterstitialAdLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial Ad failed to load: ${error.message}")
                    interstitialAd = null
                    isInterstitialAdLoading = false
                }
            }
        )
    }

    fun isInterstitialEligible(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastInterstitialShownTimestamp) >= INTERSTITIAL_MIN_INTERVAL_MS
    }

    fun showInterstitialIfEligible(
        activity: Activity,
        onAdDismissed: () -> Unit = {}
    ) {
        if (!isInterstitialEligible()) {
            Log.d(TAG, "Interstitial Ad skipped due to frequency capping")
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed")
                    interstitialAd = null
                    lastInterstitialShownTimestamp = System.currentTimeMillis()
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(TAG, "Interstitial Ad failed to show: ${error.message}")
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad showed fullscreen content")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial Ad not loaded yet")
            loadInterstitialAd(activity)
            onAdDismissed()
        }
    }
}
