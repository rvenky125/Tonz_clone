package com.famas.tonz.di

import android.content.Context
import com.famas.tonz.core.core_states.UserDataState
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.feature_feed.data.FeedRepositoryImpl
import com.famas.tonz.feature_feed.data.remote.FeedApi
import com.famas.tonz.feature_feed.data.remote.FeedApiImpl
import com.famas.tonz.feature_feed.domain.FeedRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {

    @Provides
    fun provideFeedApi(httpClient: HttpClient): FeedApi = FeedApiImpl(httpClient)

    @Provides
    fun provideFeedRepository(
        feedApi: FeedApi,
        userDataState: UserDataState,
        preferences: Preferences,
        @ApplicationContext context: Context
    ): FeedRepository = FeedRepositoryImpl(
        feedApi = feedApi,
        userDataState = userDataState,
        context = context,
        preferences = preferences
    )
}