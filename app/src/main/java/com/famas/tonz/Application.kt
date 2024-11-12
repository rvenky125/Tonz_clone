package com.famas.tonz

import android.app.Application
import android.net.Uri
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerClient.newBuilder
import com.android.installreferrer.api.InstallReferrerStateListener
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.core.data.preferences.ReferralData
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class Application() : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var preferences: Preferences

    private lateinit var referrerClient: InstallReferrerClient

    override fun onCreate() {
        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this)

        referrerClient = newBuilder(this).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerResponse.OK -> {
                        try {
                            val uri = Uri.parse(referrerClient.installReferrer.installReferrer)
                            val referrerCode = uri.getQueryParameter("referrer")
                            val ringtoneId = uri.getQueryParameter("ringtone_id")
                            CoroutineScope(Dispatchers.IO).launch {
                                preferences.setReferralData(
                                    ReferralData(
                                        referralCode = referrerCode,
                                        ringtoneId = ringtoneId
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app.
                    }

                    InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

        super.onCreate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onTerminate() {
        referrerClient.endConnection()
        super.onTerminate()
    }
}
