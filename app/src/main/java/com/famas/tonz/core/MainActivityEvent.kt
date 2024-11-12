package com.famas.tonz.core

import com.famas.tonz.core.util.UiContact
import java.util.UUID

sealed class MainActivityEvent {
    object OnToggleSurpriseBox : MainActivityEvent()
    object InterstitialAdClosed : MainActivityEvent()
    data class SetWorkIdToShowDialog(val id: UUID?, val showOnlyProgressOfWorker: Boolean = false): MainActivityEvent()
    data class SetWorkIdToShowLaterDialog(val id: UUID?): MainActivityEvent()
    data class OnSelectFileUriToSetRingtone(val uri: String?, val trimRingtone: Boolean = true): MainActivityEvent()
    data class SetRingtoneToContacts(
        val selectedFileUriToSetRingtone: String?,
        val selectedUiContacts: List<UiContact>?
    ) : MainActivityEvent()
    object OnDismissReferral : MainActivityEvent()
    object OnConfirm : MainActivityEvent()
}
