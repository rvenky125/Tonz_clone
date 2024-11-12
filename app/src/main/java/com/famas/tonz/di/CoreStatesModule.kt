package com.famas.tonz.di

import com.famas.tonz.core.core_states.UserDataState
import com.famas.tonz.core.data.preferences.Preferences
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreStatesModule {

    @Provides
    @Singleton
    fun provideUserDataState(
        preferences: Preferences,
        coroutineDispatcher: CoroutineDispatcher,
        firebaseAuth: FirebaseAuth
    ): UserDataState = UserDataState(preferences, firebaseAuth)
}