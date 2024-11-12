package com.famas.tonz.feature_home.presentation.screen_select_contacts

import com.famas.tonz.core.util.UiContact

sealed class SelectContactsEvent {
    data class OnClickContact(val uiContact: UiContact): SelectContactsEvent()
    data class OnSearchValueChange(val searchValue: String) : SelectContactsEvent()
    data class OnToggleContact(val contact: UiContact) : SelectContactsEvent()

    object ReloadContacts : SelectContactsEvent()
    object OnClickFloatingActionBtn : SelectContactsEvent()
    object OnSelectAll : SelectContactsEvent()
}