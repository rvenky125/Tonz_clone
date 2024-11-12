package com.famas.tonz.feature_home.presentation.screen_home

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.famas.tonz.R
import com.famas.tonz.core.audio_util.AudioPlayer
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.ui.util.UiText
import com.famas.tonz.core.util.toUiContact
import com.famas.tonz.feature_feed.domain.AuthRepository
import com.google.firebase.auth.AuthCredential
import contacts.async.findAsync
import contacts.core.Contacts
import contacts.core.Fields
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenVM @Inject constructor(
    private val audioPlayer: AudioPlayer,
    private val contactsApi: Contacts,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _homeScreenState = mutableStateOf(HomeScreenState())
    val homeScreenState: State<HomeScreenState> = _homeScreenState

    var contactsLoadedCallback: (() -> Unit)? = null

    var launchSystemSettingsCallback: (() -> Unit)? = null

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()

    private var searchJob: Job? = null

    val uiEventFlow = _uiEventFlow.asSharedFlow()
        .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(500))

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnSearchValueChange -> {
                _homeScreenState.value =
                    homeScreenState.value.copy(searchValue = event.searchValue)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500)
                    syncContacts()
                }
            }

            is HomeScreenEvent.ToggleClickContactCard -> {
                if (homeScreenState.value.playingDefault) {
                    _homeScreenState.value =
                        homeScreenState.value.copy(playingDefault = false, loadingDefault = false)
                }

                if (homeScreenState.value.expandedCardId == event.contact.contactId) {
                    _homeScreenState.value =
                        homeScreenState.value.copy(expandedCardId = null, isPlaying = false)
                    audioPlayer.pause()
                    audioPlayer.clearAudio()
                    return
                }

                _homeScreenState.value =
                    homeScreenState.value.copy(
                        expandedCardId = event.contact.contactId,
                        isPlaying = false
                    )

                event.contact.currentRingtoneUri?.let {
                    audioPlayer.pause()
                    audioPlayer.clearAudio()
                    audioPlayer.setAudio(it.toUri())
                }
            }

            HomeScreenEvent.TogglePlay -> {
                if (homeScreenState.value.playingDefault) {
                    _homeScreenState.value = homeScreenState.value.copy(playingDefault = false)
                    homeScreenState.value.contacts.firstOrNull { it.contactId == homeScreenState.value.expandedCardId }
                        ?.let {
                            it.currentRingtoneUri?.toUri()?.let { it1 -> audioPlayer.setAudio(it1) }
                        }
                }

                if (!homeScreenState.value.isPlaying) {
                    audioPlayer.play()
                } else {
                    audioPlayer.pause()
                }
            }

            is HomeScreenEvent.OnProgressChange -> {
                updateProgress(event.value)
            }

            HomeScreenEvent.PauseAudio -> {
                if (homeScreenState.value.isPlaying) {
                    audioPlayer.pause()
                }
            }

            is HomeScreenEvent.ReloadContacts -> {
                viewModelScope.launch {
                    _homeScreenState.value = homeScreenState.value.copy(isLoading = true)
                    audioPlayer.clearAudio()
                    syncContacts(event.context)
                    _homeScreenState.value = homeScreenState.value.copy(isLoading = false)
                }
            }

            HomeScreenEvent.TogglePlayDefault -> {
                viewModelScope.launch {
                    if (!homeScreenState.value.playingDefault) {
                        audioPlayer.pause()
                        _homeScreenState.value =
                            homeScreenState.value.copy(
                                playingDefault = true,
                                expandedCardId = null,
                                loadingDefault = true
                            )
                        homeScreenState.value.defaultRingtoneUri?.let {
                            audioPlayer.setAudio(Uri.parse(it))
                        }
                        delay(800)
                    }
                    _homeScreenState.value = homeScreenState.value.copy(loadingDefault = false)
                    if (!homeScreenState.value.isPlaying) {
                        audioPlayer.play()
                    } else {
                        audioPlayer.pause()
                    }
                }
            }

            HomeScreenEvent.OnClickEditDefaultRingtone -> {
                //This block temporarily unusable
            }

            HomeScreenEvent.ClearAudio -> {
                _homeScreenState.value =
                    homeScreenState.value.copy(
                        playingDefault = false,
                        expandedCardId = null,
                        loadingDefault = false
                    )
                audioPlayer.clearAudio()
            }

            HomeScreenEvent.ToggleMoreVert -> {
                _homeScreenState.value = homeScreenState.value.copy(
                    isMenuVisible = !homeScreenState.value.isMenuVisible
                )
            }

            is HomeScreenEvent.OnToggleSelectContact -> {
                if (homeScreenState.value.selectedContacts.contains(event.contact)) {
                    _homeScreenState.value = homeScreenState.value.copy(
                        selectedContacts = homeScreenState.value.selectedContacts.filter { it.contactId != event.contact.contactId }
                    )
                } else {
                    _homeScreenState.value = homeScreenState.value.copy(
                        selectedContacts = homeScreenState.value.selectedContacts.plus(event.contact)
                    )
                }
            }

            HomeScreenEvent.DiscardContactSelection -> {
                _homeScreenState.value = homeScreenState.value.copy(
                    selectedContacts = listOf()
                )
            }
        }
    }

    private fun updateProgress(progress: Float) {
        val position = homeScreenState.value.totalDuration?.times(progress)?.toLong() ?: 0L
        audioPlayer.seekTo(position)
        _homeScreenState.value = homeScreenState.value.copy(progress = progress)
    }

    private suspend fun syncContacts(context: Context? = null) {
        context?.let { initializeDefaultRingtone(it) }
        val contacts = contactsApi
            .broadQuery()
            .wherePartiallyMatches(homeScreenState.value.searchValue)
            .include(
                Fields.Contact.DisplayNamePrimary,
                Fields.Contact.Id,
                Fields.Contact.LookupKey,
                Fields.Contact.PhotoUri,
                Fields.Contact.Options.CustomRingtone,
                Fields.Phone.Number
            )
            .findAsync()
            .await()
            .mapNotNull { it.toUiContact() }
        _homeScreenState.value = homeScreenState.value.copy(contacts = contacts)
    }

    fun initializeDefaultRingtone(context: Context) {
        if (!Settings.System.canWrite(context)) {
            viewModelScope.launch {
                delay(1000)
                launchSystemSettingsCallback?.invoke()
            }
            return
        }

        val uri = RingtoneManager.getActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_RINGTONE
        )
        _homeScreenState.value = homeScreenState.value.copy(
            defaultRingtoneUri = uri?.toString()
        )
    }

    init {
        audioPlayer.initialize()

        audioPlayer.events.onEach {
            when (it) {
                is AudioPlayer.Event.PlayingChanged -> {
                    _homeScreenState.value = homeScreenState.value.copy(
                        isPlaying = it.isPlaying
                    )
                }

                is AudioPlayer.Event.PositionChanged -> {
                    val duration = if (it.duration > 0) it.duration else 1L
                    var homeScreenStateToSet = homeScreenState.value.copy(
                        progress = it.position.toFloat().div(duration.toFloat())
                    )

                    if (duration != homeScreenStateToSet.totalDuration) {
                        homeScreenStateToSet = homeScreenStateToSet.copy(
                            totalDuration = duration
                        )
                    }
                    _homeScreenState.value = homeScreenStateToSet
                }

                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _homeScreenState.value = homeScreenState.value.copy(
                loading = true
            )
            authRepository.signInWithGoogle(credential).onSuccess {
                _homeScreenState.value = homeScreenState.value.copy(
                    loading = false
                )
            }.onFailure { throwable ->
                _uiEventFlow.emit(UiEvent.ShowSnackBar(throwable.message?.let {
                    UiText.DynamicString(
                        it
                    )
                } ?: UiText.StringResource(
                    R.string.something_went_wrong
                )))
                _homeScreenState.value = homeScreenState.value.copy(
                    loading = false
                )
            }
        }
    }

    override fun onCleared() {
        try {
            audioPlayer.clearAudio()
            audioPlayer.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onCleared()
    }
}