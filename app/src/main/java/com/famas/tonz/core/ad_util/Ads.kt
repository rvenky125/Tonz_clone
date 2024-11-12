package com.famas.tonz.core.ad_util

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.famas.tonz.core.util.Constants
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

@Composable
fun AdmobBanner(modifier: Modifier = Modifier, width: Int? = null) {
    val currentWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current

    AndroidView(factory = {
        AdView(it).apply {
            setAdSize(
                AdSize.getPortraitAnchoredAdaptiveBannerAdSize(
                    context,
                    width ?: currentWidth
                )
            )
            adUnitId = Constants.MAIN_BANNER_AD_ID
            loadAd(AdRequest.Builder().build())
        }
    }, modifier)
}

class AdMobInterstitial(
    private val context: Context,
    private val activity: Activity,
    private val onClose: () -> Unit
) {
    var mInterstitialAd: InterstitialAd? = null
    fun loadAd(showOnLoaded: Boolean = true) {
        try {
            val adUnitId = Constants.MAIN_INTERSTITIAL_AD_ID
            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        mInterstitialAd = null
                    }

                    override fun onAdLoaded(ad: InterstitialAd) {
                        mInterstitialAd = ad
                        if (showOnLoaded) {
                            showAd()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    onClose()
                    mInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                }
            }

            mInterstitialAd?.show(activity)
        } else {
            loadAd(true)
        }
    }
}