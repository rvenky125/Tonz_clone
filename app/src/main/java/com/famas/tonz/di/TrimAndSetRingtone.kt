package com.famas.tonz.di

import com.famas.tonz.feature_trim_set_ringtone.data.remote.TrimSetRingtoneApi
import com.famas.tonz.feature_trim_set_ringtone.data.remote.TrimSetRingtoneApiImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient

@Module
@InstallIn(SingletonComponent::class)
object TrimAndSetRingtone {
    @Provides
    fun provideSetRingtoneApi(
        httpClient: HttpClient
    ): TrimSetRingtoneApi = TrimSetRingtoneApiImpl(httpClient)
}