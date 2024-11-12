package com.famas.tonz.feature_profile.presentation.screen_profile

sealed class ProfileScreenEvent {
    class OnLogin(it: String) : ProfileScreenEvent() {

    }

    class SetLoginLoading(b: Boolean) : ProfileScreenEvent() {

    }
}