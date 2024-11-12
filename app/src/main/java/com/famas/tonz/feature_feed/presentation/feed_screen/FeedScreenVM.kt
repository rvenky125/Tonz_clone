package com.famas.tonz.feature_feed.presentation.feed_screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.famas.tonz.BuildConfig
import com.famas.tonz.R
import com.famas.tonz.core.TAG
import com.famas.tonz.core.audio_util.AudioPlayer
import com.famas.tonz.core.core_states.UserDataState
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.ui.util.UiText
import com.famas.tonz.core.util.Constants
import com.famas.tonz.feature_feed.data.remote.responses.toRingtonePostModel
import com.famas.tonz.feature_feed.domain.AuthRepository
import com.famas.tonz.feature_feed.domain.FeedRepository
import com.famas.tonz.feature_music.data.workers.DownloadAudioFileInputData
import com.famas.tonz.feature_music.data.workers.DownloadAudioFileWorker
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider.getCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FeedScreenVM @Inject constructor(
    private val authRepository: AuthRepository,
    private val feedRepository: FeedRepository,
    private val audioPlayer: AudioPlayer,
    private val workManager: WorkManager,
    private val userDataState: UserDataState
) : ViewModel() {
    var onAddWorkId: ((id: UUID, showProgressOnly: Boolean) -> Unit)? = null
    var onGetFileUriToSetRingtone: ((fileUri: String, trim: Boolean) -> Unit)? = null

    private val _feedScreenState = mutableStateOf(FeedScreenState())
    val feedScreenState: State<FeedScreenState> = _feedScreenState

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow = _uiEventFlow.asSharedFlow()

    private var searchValueChangeJob: Job? = null

    fun onEvent(event: FeedScreenEvent) {
        try {
            when (event) {
                is FeedScreenEvent.OnLogin -> {
                    val credentials = getCredential(event.tokenId, null)
                    signInWithGoogle(credentials)
                }

                is FeedScreenEvent.OnSelectTag -> {
                    val alreadyExists = feedScreenState.value.feedListTags.contains(event.tag)
                    _feedScreenState.value =
                        feedScreenState.value.copy(feedListTags = if (alreadyExists) _feedScreenState.value.feedListTags.filter { it.ordinal != event.tag.ordinal } else feedScreenState.value.feedListTags + event.tag,
                            currentPage = 1,
                            isEndReached = false,
                            errMessage = null)
                    syncRingtones()
                }

                is FeedScreenEvent.OnClickGridItem -> {
                    if (event.index !in (0 until _feedScreenState.value.ringtonePosts.size)) {
                        return
                    }
                    audioPlayer.clearAudio()
                    val postItem = _feedScreenState.value.ringtonePosts[event.index]
                    _feedScreenState.value =
                        feedScreenState.value.copy(selectedRingtonePostModelIndex = event.index)

                    loadAudioFileFromFirebaseUrl(postItem.fileUrl)
                }

                FeedScreenEvent.OnDismissSelectedRingtonePost -> {
                    _feedScreenState.value =
                        feedScreenState.value.copy(selectedRingtonePostModelIndex = null)
                    audioPlayer.clearAudio()
                }

                is FeedScreenEvent.OnSearchValueChange -> {
                    _feedScreenState.value = feedScreenState.value.copy(
                        searchValue = event.value,
                        currentPage = 1,
                        errMessage = null,
                        isEndReached = false
                    )

                    searchValueChangeJob?.cancel()
                    searchValueChangeJob = viewModelScope.launch {
                        delay(500)
                        syncRingtones()
                    }
                }

                FeedScreenEvent.TogglePlay -> {
                    if (!feedScreenState.value.isPlaying) {
                        audioPlayer.play()
                    } else {
                        audioPlayer.pause()
                    }
                }

                is FeedScreenEvent.OnProgressChange -> {
                    updateProgress(event.it)
                }

                FeedScreenEvent.PauseAudio -> {
                    if (feedScreenState.value.isPlaying) {
                        audioPlayer.pause()
                    }
                }

                is FeedScreenEvent.OnClickSetRingtonFromDlg -> {
                    if (_feedScreenState.value.isPlaying) onEvent(FeedScreenEvent.TogglePlay)
                    viewModelScope.launch {
                        try {
                            val ringtonePost = event.ringtonePost

                            val url = ringtonePost.fileUrl

                            val downloadFileInputData = DownloadAudioFileInputData(
                                url = url,
                                headers = mapOf(),
                                method = "GET",
                                fileName = ringtonePost.ringtoneName.replace(" ", "_")
                            )

                            val downloadAudioFileRequest =
                                OneTimeWorkRequestBuilder<DownloadAudioFileWorker>().setInputData(
                                    workDataOf(
                                        Constants.DOWNLOAD_AUDIO_FILE_DATA to Json.encodeToString(
                                            downloadFileInputData
                                        )
                                    )
                                ).addTag(Constants.DOWLOAD_AUDIO_FILE_WORKER).build()

                            workManager.enqueue(downloadAudioFileRequest)
                            onAddWorkId?.invoke(downloadAudioFileRequest.id, true)
                            onEvent(FeedScreenEvent.OnDismissOptionsSheet)

                            workManager.getWorkInfoByIdLiveData(downloadAudioFileRequest.id)
                                .asFlow().onEach {
                                    if (!it.state.isFinished) return@onEach
                                    val outputFileUri =
                                        it.outputData.getString(DownloadAudioFileWorker.FILE_URI)
                                            ?: kotlin.run {
                                                _uiEventFlow.emit(
                                                    UiEvent.ShowSnackBar(
                                                        UiText.DynamicString(
                                                            "Failed to download, try again"
                                                        )
                                                    )
                                                )
                                                return@onEach
                                            }
                                    onGetFileUriToSetRingtone?.invoke(outputFileUri, false)
                                }.launchIn(viewModelScope)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                is FeedScreenEvent.OnSelectOptionFromBottomSheet -> {
                    viewModelScope.launch {
                        try {
                            onEvent(FeedScreenEvent.TogglePlay)
                            val ringtonePost =
                                _feedScreenState.value.ringtoneForBottomSheet ?: return@launch
                            val url = ringtonePost.fileUrl

                            val downloadFileInputData = DownloadAudioFileInputData(
                                url = url,
                                headers = mapOf(),
                                method = "GET",
                                fileName = ringtonePost.ringtoneName.replace(" ", "_")
                            )

                            val downloadAudioFileRequest =
                                OneTimeWorkRequestBuilder<DownloadAudioFileWorker>().setInputData(
                                    workDataOf(
                                        Constants.DOWNLOAD_AUDIO_FILE_DATA to Json.encodeToString(
                                            downloadFileInputData
                                        )
                                    )
                                ).addTag(Constants.DOWLOAD_AUDIO_FILE_WORKER).build()

                            workManager.enqueue(downloadAudioFileRequest)
                            onAddWorkId?.invoke(downloadAudioFileRequest.id, true)
                            onEvent(FeedScreenEvent.OnDismissOptionsSheet)

                            if (event.downloadOnly) {
                                return@launch
                            }

                            workManager.getWorkInfoByIdLiveData(downloadAudioFileRequest.id)
                                .asFlow().onEach {
                                    if (!it.state.isFinished) return@onEach
                                    val outputFileUri =
                                        it.outputData.getString(DownloadAudioFileWorker.FILE_URI)
                                            ?: kotlin.run {
                                                _uiEventFlow.emit(
                                                    UiEvent.ShowSnackBar(
                                                        UiText.DynamicString(
                                                            "Failed to download, try again"
                                                        )
                                                    )
                                                )
                                                return@onEach
                                            }
                                    onGetFileUriToSetRingtone?.invoke(
                                        outputFileUri, event.trimRingtone
                                    )
                                }.launchIn(viewModelScope)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                is FeedScreenEvent.OnToggleLikeRingtone -> {
                    val currentRingtonePost = event.ringtonePost
                    viewModelScope.launch {
                        try {
                            //TODO need to update like count also
                            val withIncrementLike =
                                currentRingtonePost.copy(isPresentUserLiked = !currentRingtonePost.isPresentUserLiked,
                                    likeCount = currentRingtonePost.likeCount.toIntOrNull()
                                        ?.let { (it + (if (!currentRingtonePost.isPresentUserLiked) 1 else -1)).toString() }
                                        ?: currentRingtonePost.likeCount)
                            val posts = _feedScreenState.value.ringtonePosts.toMutableList()
                            val index = posts.indexOf(currentRingtonePost)
                            posts[index] = withIncrementLike
                            _feedScreenState.value = feedScreenState.value.copy(
                                ringtonePosts = posts.toList()
                            )

                            val result = feedRepository.toggleLike(currentRingtonePost.ringtoneId)
                            if (!result.successful) {
                                _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.DynamicString(result.msg)))
                                //TODO: Something went wrong
                                return@launch
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                FeedScreenEvent.IncrementPage -> {
                    _feedScreenState.value = feedScreenState.value.copy(
                        currentPage = feedScreenState.value.currentPage + 1
                    )
                    syncRingtones()
                }

                FeedScreenEvent.ClearAudio -> {
                    _feedScreenState.value =
                        feedScreenState.value.copy(selectedRingtonePostModelIndex = null)
                    audioPlayer.clearAudio()
                }

                is FeedScreenEvent.OnClickRingtoneForOptions -> {
                    _feedScreenState.value =
                        feedScreenState.value.copy(ringtoneForBottomSheet = event.ringtonePost)
                }

                FeedScreenEvent.OnDismissOptionsSheet -> {
                    _feedScreenState.value =
                        feedScreenState.value.copy(ringtoneForBottomSheet = null)
                }

                FeedScreenEvent.Refresh -> {
                    viewModelScope.launch {
                        _feedScreenState.value = feedScreenState.value.copy(
                            currentPage = 1, isEndReached = false, errMessage = null
                        )
                        delay(100)
                        syncRingtones()
                    }
                }

                is FeedScreenEvent.OnChangeReferralCode -> {
                    _feedScreenState.value = feedScreenState.value.copy(
                        referralCode = event.value
                    )
                }

                is FeedScreenEvent.SetLoginLoading -> {
                    _feedScreenState.value = feedScreenState.value.copy(
                        loginLoading = event.loading
                    )
                }

                is FeedScreenEvent.OnShareRingtoneToOtherApps -> {
                    shareRingtone(event)
                }

                is FeedScreenEvent.LoadSongWithId -> {
                    getRingtone(event.ringtoneId)
                }

                FeedScreenEvent.OnDismissExplicitRingtonePost -> {
                    _feedScreenState.value = feedScreenState.value.copy(
                        ringtonePostToShowExplicitly = null
                    )
                }

                is FeedScreenEvent.OnSelectLanguage -> {
                    _feedScreenState.value = feedScreenState.value.copy(
                        selectedLanguage = event.language, showLanguagesSheet = false
                    )

                    syncRingtones()
                }

                FeedScreenEvent.ToggleLanguageSheet -> {
                    _feedScreenState.value = feedScreenState.value.copy(
                        showLanguagesSheet = !feedScreenState.value.showLanguagesSheet,
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareRingtone(event: FeedScreenEvent.OnShareRingtoneToOtherApps) {
        viewModelScope.launch(Dispatchers.IO) {
            var fileUriToSend: Uri? = null
            try {
                _feedScreenState.value = feedScreenState.value.copy(
                    loading = true
                )
                val connection = URL(event.ringtone.thumbnailPictureUrl).openConnection()
                val file = File(event.context.cacheDir, "${event.ringtone.ringtoneName}.jpg")
                connection.getInputStream().use { inp ->
                    file.outputStream().use { outp ->
                        inp.copyTo(outp)
                    }
                }
                fileUriToSend = FileProvider.getUriForFile(
                    event.context, "${BuildConfig.APPLICATION_ID}.fileprovider", file
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _feedScreenState.value = feedScreenState.value.copy(
                    loading = false
                )
            }


            val textMessage =
                "Hey, Check out this ringtone https://tonz.co.in/ringtone?id=${event.ringtone.ringtoneId}&code=${userDataState.userData.value?.referralCode}.\nDownload Tonz app for ringtones like this and use my code ${userDataState.userData.value?.referralCode} to get 100 bonus coins. Download Tonz here: https://play.google.com/store/apps/details?id=${event.context.packageName}&referrer=${userDataState.userData.value?.referralCode}."

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textMessage)
                type = "text/plain"
                if (fileUriToSend != null) {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, fileUriToSend)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            val shareIntent = Intent.createChooser(
                intent, "Share ringtone through"
            )
            event.context.startActivity(shareIntent)
        }
    }

    private fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            try {
                _feedScreenState.value = feedScreenState.value.copy(
                    loading = true, loginLoading = true
                )
                authRepository.signInWithGoogle(credential).onSuccess {
                    _feedScreenState.value = feedScreenState.value.copy(
                        loading = false,
                        loginLoading = false,
                        searchValue = "",
                        feedListTags = listOf(FeedListTag.RecentUploads)
                    )
                    syncRingtones()
                    delay(100)
                }.onFailure { throwable ->
                    _uiEventFlow.emit(UiEvent.ShowSnackBar(throwable.message?.let {
                        UiText.DynamicString(
                            it
                        )
                    } ?: UiText.StringResource(
                        R.string.something_went_wrong
                    )))
                    _feedScreenState.value = feedScreenState.value.copy(
                        loading = false,
                        loginLoading = false,
                    )
                }
            } catch (e: Exception) {
                _feedScreenState.value = feedScreenState.value.copy(
                    loading = false, loginLoading = false
                )
            }
        }
    }

    private fun syncRingtones() {
        Log.d(TAG, "Called sync ringtones")
        viewModelScope.launch {
            try {
                if (userDataState.userData.value == null) {
                    _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.DynamicString("Something went wrong, please reopen the app")))
                    return@launch
                }

                _feedScreenState.value = if (feedScreenState.value.currentPage == 1) {
                    feedScreenState.value.copy(
                        loading = true
                    )
                } else {
                    feedScreenState.value.copy(
                        loadingExtra = true
                    )
                }

                val result = feedRepository.getRingtones(
                    tags = feedScreenState.value.feedListTags,
                    searchValue = feedScreenState.value.searchValue,
                    pageNumber = feedScreenState.value.currentPage,
                    language = feedScreenState.value.selectedLanguage
                )

                if (result.successful) {
                    val ringtonePosts = result.data.mapNotNull { it.toRingtonePostModel() }
                    _feedScreenState.value = feedScreenState.value.copy(
                        ringtonePosts = if (feedScreenState.value.currentPage == 1) ringtonePosts else feedScreenState.value.ringtonePosts.plus(
                            ringtonePosts
                        ), errMessage = null, isEndReached = ringtonePosts.isEmpty()
                    )
                }

                if (!result.successful) {
                    Log.d(TAG, "$result")
                    _feedScreenState.value = feedScreenState.value.copy(
                        errMessage = result.msg
                    )
//                _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.DynamicString(result.msg)))
                }

                _feedScreenState.value = if (feedScreenState.value.currentPage == 1) {
                    feedScreenState.value.copy(
                        loading = false
                    )
                } else {
                    feedScreenState.value.copy(
                        loadingExtra = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getRingtone(ringtoneId: String) {
        viewModelScope.launch {
            try {
                if (userDataState.userData.value == null) {
                    _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.DynamicString("Something went wrong, please reopen the app")))
                    return@launch
                }

                _feedScreenState.value = feedScreenState.value.copy(
                    loading = true
                )

                val result = feedRepository.getRingtone(
                    ringtoneId
                )

                if (result.successful) {
                    result.data.firstNotNullOfOrNull { it.toRingtonePostModel() }?.let {
                        _feedScreenState.value = feedScreenState.value.copy(
                            ringtonePostToShowExplicitly = it
                        )
                        loadAudioFileFromFirebaseUrl(it.fileUrl)
                    }
                }

                _feedScreenState.value = feedScreenState.value.copy(
                    loading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateProgress(progress: Float) {
        try {
            val position = feedScreenState.value.totalDuration?.times(progress)?.toLong() ?: 0L
            audioPlayer.seekTo(position)
            _feedScreenState.value = feedScreenState.value.copy(progress = progress)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun syncLanguages() {
        viewModelScope.launch {
            val response = feedRepository.getLanguages()

            if (response.successful) {
                _feedScreenState.value = feedScreenState.value.copy(
                    languages = response.data
                )
            }
        }
    }

    init {
        syncRingtones()
        syncLanguages()

        audioPlayer.initialize()

        audioPlayer.events.onEach {
            when (it) {
                is AudioPlayer.Event.PlayingChanged -> {
                    _feedScreenState.value = feedScreenState.value.copy(
                        isPlaying = it.isPlaying
                    )
                }

                is AudioPlayer.Event.PositionChanged -> {
//                    try {
//                        val duration = if (it.duration > 0) it.duration else 1L
//                        var feedScreenStateToSet = feedScreenState.value.copy(
//                            progress = it.position.toFloat().div(duration.toFloat())
//                        )
//
//                        if (duration != feedScreenStateToSet.totalDuration) {
//                            feedScreenStateToSet = feedScreenStateToSet.copy(
//                                totalDuration = duration
//                            )
//                        }
//                        _feedScreenState.value = feedScreenStateToSet
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
                }

                is AudioPlayer.Event.LoadingChanged -> {
                    _feedScreenState.value = feedScreenState.value.copy(
                        isPlayerLoading = it.isLoading
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadAudioFileFromFirebaseUrl(url: String) {
        audioPlayer.streamAudio(url)
        audioPlayer.play()
    }

    override fun onCleared() {
        audioPlayer.clearAudio()
        audioPlayer.release()
        super.onCleared()
    }
}