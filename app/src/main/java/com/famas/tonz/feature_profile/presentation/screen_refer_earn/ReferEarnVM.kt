package com.famas.tonz.feature_profile.presentation.screen_refer_earn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.famas.tonz.R
import com.famas.tonz.core.core_states.UserDataState
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.ui.util.UiText
import com.famas.tonz.feature_feed.domain.AuthRepository
import com.famas.tonz.feature_profile.domain.ProfileRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider.getCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferEarnVM @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val preferences: Preferences,
    private val authRepository: AuthRepository,
    private val userDataState: UserDataState
) : ViewModel() {
    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow = _uiEventFlow.asSharedFlow()

    var state by mutableStateOf(ReferEarnScreenState())
        private set

    fun onEvent(event: ReferEarnEvent) {
        when (event) {
            is ReferEarnEvent.OnLogin -> {
                val credentials = getCredential(event.tokenId, null)
                signInWithGoogle(credentials)
            }
            is ReferEarnEvent.SetLoginLoading -> {
                state = state.copy(loginLoading = event.value)
            }
        }
    }

    private fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            state = state.copy(
                loading = true,
                loginLoading = true
            )
            authRepository.signInWithGoogle(credential).onSuccess {
                state = state.copy(
                    loading = false,
                    loginLoading = false,
                )
            }.onFailure { throwable ->
                _uiEventFlow.emit(UiEvent.ShowSnackBar(throwable.message?.let {
                    UiText.DynamicString(
                        it
                    )
                } ?: UiText.StringResource(
                    R.string.something_went_wrong
                )))
                state = state.copy(
                    loading = false,
                    loginLoading = false,
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            if (userDataState.userData.value?.isJustRegistered == true) return@launch

            val result = profileRepository.getCurrentUser()
            if (result.successful) {
                result.data.firstOrNull()?.let {
                    preferences.setUserData(it)
                }
            }

            if (result.msg.isNotBlank()) {
                _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.DynamicString(result.msg)))
            }
        }
    }
}