package com.famas.tonz.feature_profile.presentation.screen_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import com.famas.tonz.core.MainActivityVM
import com.famas.tonz.feature_feed.presentation.components.UserNotLoggedInLt
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Destination
@Composable
fun ProfileScreen(
    mainVm: MainActivityVM,
    snackbarHostState: SnackbarHostState,
    profileVM: ProfileVM = hiltViewModel()
) {
    val state = profileVM.profileScreenState
    val currentUser = mainVm.userData.value
    val coroutineScope = rememberCoroutineScope()

    if (currentUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            UserNotLoggedInLt(
                { profileVM.onEvent(ProfileScreenEvent.OnLogin(it)) },
                onStartLogin = { profileVM.onEvent(ProfileScreenEvent.SetLoginLoading(true)) },
                loading = state.loginLoading,
                onCancelLogin = {
                    profileVM.onEvent(ProfileScreenEvent.SetLoginLoading(false))
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(it)
                    }
                },
            )
        }
    }
}