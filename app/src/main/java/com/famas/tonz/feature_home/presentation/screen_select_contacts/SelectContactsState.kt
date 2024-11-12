package com.famas.tonz.feature_home.presentation.screen_select_contacts

import com.famas.tonz.core.util.UiContact

data class SelectContactsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val contacts: List<UiContact> = emptyList(),
    val searchValue: String = "",
    val selectedContacts: List<UiContact> = emptyList(),
)