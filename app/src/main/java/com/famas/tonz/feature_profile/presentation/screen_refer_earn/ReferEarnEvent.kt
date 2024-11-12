package com.famas.tonz.feature_profile.presentation.screen_refer_earn

sealed class ReferEarnEvent {
    data class OnLogin(val tokenId: String): ReferEarnEvent()
    data class SetLoginLoading(val value: Boolean): ReferEarnEvent()
}
