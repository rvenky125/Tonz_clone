package com.famas.tonz.di

import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.feature_profile.data.remote.ProfileApi
import com.famas.tonz.feature_profile.data.remote.ProfileApiImpl
import com.famas.tonz.feature_profile.data.repository.ProfileRepositoryImpl
import com.famas.tonz.feature_profile.domain.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides
    fun provideProfileApi(httpClient: HttpClient): ProfileApi = ProfileApiImpl(httpClient)

    @Provides
    fun provideProfileRepository(preferences: Preferences, profileApi: ProfileApi): ProfileRepository =
        ProfileRepositoryImpl(preferences, profileApi)
}