package com.famas.tonz.feature_home.presentation.screen_home

import android.content.Context
import com.famas.tonz.core.util.UiContact

sealed class HomeScreenEvent {
    data class ToggleClickContactCard(val contact: UiContact) : HomeScreenEvent()
    data class OnSearchValueChange(val searchValue: String) : HomeScreenEvent()
    data class OnProgressChange(val value: Float) : HomeScreenEvent()
    object TogglePlay : HomeScreenEvent()
    object TogglePlayDefault : HomeScreenEvent()
    object PauseAudio : HomeScreenEvent()
    data class ReloadContacts(val context: Context) : HomeScreenEvent()
    object OnClickEditDefaultRingtone : HomeScreenEvent()
    object ClearAudio : HomeScreenEvent()
    object ToggleMoreVert : HomeScreenEvent()
    data class OnToggleSelectContact(val contact: UiContact): HomeScreenEvent()
    object DiscardContactSelection: HomeScreenEvent()
}