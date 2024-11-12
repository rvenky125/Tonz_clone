package com.famas.tonz.feature_music.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.famas.tonz.R
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.ui.util.UiText
import com.famas.tonz.core.util.Constants
import com.famas.tonz.feature_feed.domain.AuthRepository
import com.famas.tonz.feature_music.data.remote.MusicApi
import com.famas.tonz.feature_music.data.workers.DownloadAudioFileInputData
import com.famas.tonz.feature_music.data.workers.DownloadAudioFileWorker
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MusicScreenVM @Inject constructor(
    private val workManager: WorkManager,
    private val musicApi: MusicApi,
    private val authRepository: AuthRepository
) : ViewModel() {

    var onAddWorkId: ((id: UUID) -> Unit)? = null

    var musicScreenState by mutableStateOf(MusicScreenState())
        private set

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow: SharedFlow<UiEvent> = _uiEventFlow.asSharedFlow()

    private var searchValueChangeJob: Job? = null

    fun onEvent(event: MusicScreenEvent) {
        when (event) {
            is MusicScreenEvent.OnGetDownloadFileUrl -> {
                val downloadFileInputData = DownloadAudioFileInputData(
                    url = event.url,
                    headers = event.headers,
                    method = event.method,
                    fileName = event.fileName
                )
                println(downloadFileInputData)

                val downloadAudioFileRequest = OneTimeWorkRequestBuilder<DownloadAudioFileWorker>()
                    .setInputData(
                        workDataOf(
                            Constants.DOWNLOAD_AUDIO_FILE_DATA to Json.encodeToString(
                                downloadFileInputData
                            )
                        )
                    )
                    .addTag(Constants.DOWLOAD_AUDIO_FILE_WORKER)
                    .build()

                workManager.enqueue(downloadAudioFileRequest)
                onAddWorkId?.invoke(downloadAudioFileRequest.id)
            }

            is MusicScreenEvent.OnSelectMusicWebPage -> {
                musicScreenState = musicScreenState.copy(
                    selectedWebPage = event.webPage
                )
            }

            is MusicScreenEvent.OnClickTab -> {
                musicScreenState = musicScreenState.copy(
                    selectedTabIndex = event.tabIndex
                )
            }

            is MusicScreenEvent.OnLogin -> {
                val credentials = GoogleAuthProvider.getCredential(event.credential, null)
                signInWithGoogle(credentials)
            }

            is MusicScreenEvent.SetLoginLoading -> {
                musicScreenState = musicScreenState.copy(
                    loginLoading = event.value
                )
            }

            is MusicScreenEvent.OnSearchValueChange -> {
                musicScreenState = musicScreenState.copy(
                    searchQuery = event.text
                )

                searchValueChangeJob?.cancel()
                searchValueChangeJob = viewModelScope.launch {
                    delay(500)
                    syncSearchWebUrl()
                }
            }

            is MusicScreenEvent.CloseSearchEngine -> {
                println("Close Search Engine")
                musicScreenState = musicScreenState.copy(
                    searchEngineUrl = null
                )
            }

            is MusicScreenEvent.OnToggleSearchOnlyInSuggested -> {
                musicScreenState = musicScreenState.copy(
                    searchOnlyInSuggested = !musicScreenState.searchOnlyInSuggested
                )
                syncSearchWebUrl()
            }
        }
    }

    private fun syncSearchWebUrl() {
        if (musicScreenState.searchQuery.isBlank()) {
            musicScreenState = musicScreenState.copy(
                searchEngineUrl = null
            )
            return
        }
        musicScreenState = musicScreenState.copy(
            searchEngineUrl = buildSearchUrl(
                musicScreenState.webpages.mapNotNull { it.url },
                musicScreenState.searchQuery
            )
        )
    }

    private fun buildSearchUrl(sites: List<String>, searchString: String): String {
        val baseUrl = "https://www.google.com/search?q="
        val searchStringMod = if (searchString.contains("download")) searchString else "$searchString download"

        if (!musicScreenState.searchOnlyInSuggested) {
            return "$baseUrl$searchStringMod"
        }

        val siteQueries = sites.joinToString(" OR ") { "site:$it" }
        val fullQuery = "$siteQueries $searchStringMod"
        val encodedSearchString = URLEncoder.encode(fullQuery, "UTF-8")
        return "$baseUrl$encodedSearchString"
    }

    private fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            musicScreenState = musicScreenState.copy(
                loading = true,
                loginLoading = true
            )
            authRepository.signInWithGoogle(credential).onSuccess {
                musicScreenState = musicScreenState.copy(
                    loading = false,
                    loginLoading = false
                )
            }.onFailure { throwable ->
                _uiEventFlow.emit(UiEvent.ShowSnackBar(throwable.message?.let {
                    UiText.DynamicString(
                        it
                    )
                } ?: UiText.StringResource(
                    R.string.something_went_wrong
                )))
                musicScreenState = musicScreenState.copy(
                    loading = false,
                    loginLoading = false
                )
            }
        }
    }

    fun refreshWebpages() {
        viewModelScope.launch {
            musicScreenState = musicScreenState.copy(
                loadingWebPages = true
            )
            val webpagesResult = musicApi.getMusicWebPages()
            musicScreenState = musicScreenState.copy(
                loadingWebPages = false
            )
            if (!webpagesResult.successful) {
                _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.DynamicString(webpagesResult.msg)))
                return@launch
            }
            musicScreenState = musicScreenState.copy(
                webpages = webpagesResult.data
            )
            syncSearchWebUrl()
        }
    }


    init {
        refreshWebpages()
    }
}