package com.famas.tonz.feature_profile.presentation.screen_profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileVM @Inject constructor(): ViewModel() {

    var profileScreenState by mutableStateOf(ProfileScreenState())
        private set

    fun onEvent(event: ProfileScreenEvent) {
        when (event) {

            else -> {}
        }
    }
}