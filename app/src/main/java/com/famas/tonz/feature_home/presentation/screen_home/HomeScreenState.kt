package com.famas.tonz.feature_home.presentation.screen_home

import com.famas.tonz.core.util.UiContact

data class HomeScreenState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val contacts: List<UiContact> = emptyList(),
    val searchValue: String = "",
    val expandedCardId: Long? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0F,
    val totalDuration: Long? = null,
    val defaultRingtoneUri: String? = null,
    val playingDefault: Boolean = false,
    val loadingMusic: Boolean = false,
    val loadingDefault: Boolean = false,
    val isMenuVisible: Boolean = false,
    val selectedContacts: List<UiContact> = emptyList(),
    val loading: Boolean = false
)