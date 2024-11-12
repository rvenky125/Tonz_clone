package com.famas.tonz.feature_home.presentation.screen_select_contacts

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.famas.tonz.core.TAG
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.util.UiContact
import com.famas.tonz.core.util.toUiContact
import contacts.async.findAsync
import contacts.core.Contacts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectContactsVM @Inject constructor(
    private val contactsApi: Contacts
) : ViewModel() {
    private val _selectContactsScreen = mutableStateOf(SelectContactsState())
    val selectContactsScreen: State<SelectContactsState> = _selectContactsScreen

    var onContactsSelectionCompleted: ((List<UiContact>) -> Unit)? = null

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()

    private var searchJob: Job? = null

    val uiEventFlow = _uiEventFlow.asSharedFlow()
        .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(500))

    fun onEvent(event: SelectContactsEvent) {
        when (event) {
            is SelectContactsEvent.OnSearchValueChange -> {
                _selectContactsScreen.value =
                    selectContactsScreen.value.copy(searchValue = event.searchValue)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500)
                    syncContacts()
                }
            }

            SelectContactsEvent.ReloadContacts -> {
                _selectContactsScreen.value = selectContactsScreen.value.copy(
                    isLoading = true
                )
                viewModelScope.launch {
                    syncContacts()
                }
                _selectContactsScreen.value = selectContactsScreen.value.copy(
                    isLoading = false
                )
            }

            is SelectContactsEvent.OnClickContact -> {
                onEvent(SelectContactsEvent.OnToggleContact(event.uiContact))
//                if (_selectContactsScreen.value.selectedContacts.isNotEmpty()) {
//                    onEvent(SelectContactsEvent.OnToggleContact(event.uiContact))
//                    return
//                }
//
//                onContactsSelectionCompleted?.invoke(listOf(event.uiContact))
            }

            is SelectContactsEvent.OnToggleContact -> {
                var contacts = _selectContactsScreen.value.selectedContacts
                val isAlreadyExists = contacts.contains(event.contact)
                contacts = if (isAlreadyExists) {
                    contacts.filter { it.contactId != event.contact.contactId }
                } else {
                    contacts.plus(event.contact)
                }
                Log.d(TAG, "$contacts $isAlreadyExists")
                _selectContactsScreen.value = selectContactsScreen.value.copy(
                    selectedContacts = contacts
                )
            }

            SelectContactsEvent.OnClickFloatingActionBtn -> {
                onContactsSelectionCompleted?.invoke(selectContactsScreen.value.selectedContacts)
            }

            is SelectContactsEvent.OnSelectAll -> {
                if (!selectContactsScreen.value.selectedContacts.containsAll(selectContactsScreen.value.contacts)) {
                    _selectContactsScreen.value = selectContactsScreen.value.copy(
                        selectedContacts = selectContactsScreen.value.contacts
                    )
                } else {
                    _selectContactsScreen.value = selectContactsScreen.value.copy(
                        selectedContacts = emptyList()
                    )
                }
            }
        }
    }

    private suspend fun syncContacts() {
        val contacts = contactsApi
            .broadQuery()
            .wherePartiallyMatches(selectContactsScreen.value.searchValue)
            .findAsync()
            .await()
            .mapNotNull { it.toUiContact() }
        _selectContactsScreen.value = selectContactsScreen.value.copy(contacts = contacts)
    }

    init {
        viewModelScope.launch {
            _selectContactsScreen.value = selectContactsScreen.value.copy(
                isLoading = true
            )
            syncContacts()
            _selectContactsScreen.value = selectContactsScreen.value.copy(
                isLoading = false
            )
        }
    }
}