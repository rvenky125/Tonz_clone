package com.famas.tonz.di

import android.content.Context
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.core.device_util.GetAdvertisingId
import com.famas.tonz.feature_feed.data.AuthRepositoryImpl
import com.famas.tonz.feature_feed.data.remote.AuthApi
import com.famas.tonz.feature_feed.data.remote.AuthApiImpl
import com.famas.tonz.feature_feed.domain.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun provideAuthApi(httpClient: HttpClient): AuthApi = AuthApiImpl(httpClient)

    @Provides
    fun provideAuthRepository(
        auth: FirebaseAuth,
        preferences: Preferences,
        @ApplicationContext context: Context,
        authApi: AuthApi,
        getAdvertisingId: GetAdvertisingId
    ): AuthRepository {
        return AuthRepositoryImpl(
            auth = auth,
            preferences = preferences,
            context = context,
            authApi = authApi,
            getAdvertisingId = getAdvertisingId
        )
    }
}