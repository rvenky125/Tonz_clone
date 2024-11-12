package com.famas.tonz.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.WorkManager
import com.famas.tonz.BuildConfig
import com.famas.tonz.adblock.AdBlocker
import com.famas.tonz.core.audio_util.AudioPlaybackManager
import com.famas.tonz.core.audio_util.AudioPlayer
import com.famas.tonz.core.audio_util.ExoPlayerAndroid
import com.famas.tonz.core.data.preferences.DataStorePreferences
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.core.device_util.GetAdvertisingId
import com.famas.tonz.core.util.ConnectivityObserver
import com.famas.tonz.core.util.InAppReviewHelper
import com.famas.tonz.core.util.NetworkConnectivityObserver
import com.famas.tonz.core.util.RingtoneApi
import com.famas.tonz.feature_home.data.HomeScreenRepoImpl
import com.famas.tonz.feature_home.domain.HomeScreenRepository
import com.famas.tonz.feature_music.data.remote.MusicApi
import com.famas.tonz.feature_music.data.remote.MusicApiImpl
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import contacts.core.Contacts
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import linc.com.amplituda.Amplituda
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    fun provideAmplituda(@ApplicationContext context: Context): Amplituda = Amplituda(context)

    @Provides
    fun provideAudioPlaybackManager(@ApplicationContext context: Context): AudioPlaybackManager =
        AudioPlaybackManager(context)

    @Provides
    fun profideRingtoneApi(@ApplicationContext context: Context): RingtoneApi = RingtoneApi(context)

    @Provides
    @Singleton
    fun provideCoroutineContext(): CoroutineDispatcher = Dispatchers.IO

    @SuppressLint("UnsafeOptInUsageError")
    @Provides
    fun provideAudioPlayer(
        @ApplicationContext context: Context,
    ): AudioPlayer {
        return ExoPlayerAndroid(context)
//        return AndroidAudioPlayer(context)
    }

    @Provides
    fun provideContactsApi(
        @ApplicationContext context: Context
    ): Contacts = Contacts(context)

    @Provides
    fun provideHomeScreenRepo(
        contactsApi: Contacts
    ): HomeScreenRepository = HomeScreenRepoImpl(contactsApi)

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(HttpTimeout) {
                socketTimeoutMillis = 1 * 60 * 1000L
                requestTimeoutMillis = 1 * 60 * 1000L
                connectTimeoutMillis = 1 * 60 * 1000L
            }
            install(ContentNegotiation) {
                json(json = Json {
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                url(BuildConfig.BASE_URL)
                header("Authorization", BuildConfig.TONZ_API_TOKEN)
                header("VERSION_CODE", "${BuildConfig.VERSION_CODE}")
                header("Content-Type", "application/json")
            }
        }
    }

    @Provides
    fun provideMusicApi(
        httpClient: HttpClient,
    ): MusicApi = MusicApiImpl(httpClient)

    @Provides
    @Singleton
    fun provideWorkManger(
        @ApplicationContext applicationContext: Context
    ): WorkManager = WorkManager.getInstance(applicationContext)

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext applicationContext: Context,
        json: Json
    ): Preferences = DataStorePreferences(applicationContext, json = json)

    @Provides
    @Singleton
    fun provideJson(): Json = Json

    @Provides
    fun provideGetAdvertisingIdUsecase(
        @ApplicationContext context: Context
    ): GetAdvertisingId = GetAdvertisingId(context)


    @Provides
    @Singleton
    fun provideConnectivityObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver = NetworkConnectivityObserver(context)

    @Provides
    @Singleton
    fun provideAdBlocker(
        httpClient: HttpClient,
        @ApplicationContext context: Context
    ): AdBlocker = AdBlocker(httpClient, context)

    @Provides
    fun provideReviewManager(
        @ApplicationContext context: Context
    ): ReviewManager = ReviewManagerFactory.create(context)

    @Provides
    @Singleton
    fun provideInAppReviewHelper(
        reviewManager: ReviewManager,
        dataStorePreferences: Preferences
    ): InAppReviewHelper {
        return InAppReviewHelper(reviewManager, dataStorePreferences)
    }
}