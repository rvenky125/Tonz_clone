package com.famas.tonz.feature_trim_set_ringtone.presentation

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.Q
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.famas.tonz.R
import com.famas.tonz.core.TAG
import com.famas.tonz.core.ad_util.AdMobInterstitial
import com.famas.tonz.core.audio_util.AudioPlayer
import com.famas.tonz.core.core_states.UserDataState
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.core.model.LocalAudio
import com.famas.tonz.core.ui.navigation.TrimRingtoneNavArgs
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.ui.util.UiText
import com.famas.tonz.core.util.ContactIds
import com.famas.tonz.core.util.InAppReviewHelper
import com.famas.tonz.core.util.RingtoneApi
import com.famas.tonz.core.util.UiContact
import com.famas.tonz.feature_device_audio.data.AudioRepository
import com.famas.tonz.feature_trim_set_ringtone.data.workers.UploadRingtoneInputData
import com.famas.tonz.feature_trim_set_ringtone.data.workers.UploadRingtoneWorker
import com.famas.tonz.navArgs
import com.google.android.play.core.review.ReviewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TrimRingtoneScreenVM @Inject constructor(
    private val audioRepository: AudioRepository,
    private val audioPlayer: AudioPlayer,
    @ApplicationContext private val applicationContext: Context,
    private val ringtoneApi: RingtoneApi,
    private val workManager: WorkManager,
    private val preferences: Preferences,
    private val json: Json,
    private val userDataState: UserDataState,
    savedStateHandle: SavedStateHandle,
    private val inAppReviewHelper: InAppReviewHelper
) : ViewModel() {

    val navArgs = savedStateHandle.navArgs<TrimRingtoneNavArgs>()

    var interstitial: AdMobInterstitial? = null

    private val _trimRingtoneScreenState = mutableStateOf(TrimRingtoneState())
    val trimRingtoneScreenState: State<TrimRingtoneState> = _trimRingtoneScreenState

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow = _uiEventFlow.asSharedFlow()

    private var currentLocalAudio: LocalAudio? = null
    private val minimumAllowedDuration =
        currentLocalAudio?.duration?.toFloat()?.times(0.01f)?.toLong() ?: 1000L

    private val zoomValue = 0.45f

    fun onEvent(event: TrimRingtoneEvent) {
        when (event) {
            TrimRingtoneEvent.OnZoomIn -> {
                val waveFormWidthFactor =
                    if (trimRingtoneScreenState.value.waveFormWidthFactor < 2.7f)
                        trimRingtoneScreenState.value.waveFormWidthFactor + zoomValue
                    else trimRingtoneScreenState.value.waveFormWidthFactor
                _trimRingtoneScreenState.value =
                    trimRingtoneScreenState.value.copy(waveFormWidthFactor = waveFormWidthFactor)
            }

            TrimRingtoneEvent.OnZoomOut -> {
                val waveFormWidthFactor =
                    if (trimRingtoneScreenState.value.waveFormWidthFactor !in 1.0f..1.3f)
                        trimRingtoneScreenState.value.waveFormWidthFactor - zoomValue
                    else trimRingtoneScreenState.value.waveFormWidthFactor

                _trimRingtoneScreenState.value =
                    trimRingtoneScreenState.value.copy(waveFormWidthFactor = waveFormWidthFactor)
            }

            is TrimRingtoneEvent.OnDragHandle -> {
                if (event.width == null) {
                    return
                }
                val positionPercentage = event.x / event.width
                if (positionPercentage < 0f || positionPercentage > 1f) {
                    return
                }

                val durationFromEvent =
                    (currentLocalAudio?.duration?.toFloat()?.times(positionPercentage))?.toLong()

                when (event.handle) {
                    TrimHandle.Left -> {
                        val rightHandleCurrentDuration = (currentLocalAudio?.duration?.toFloat()
                            ?.times(trimRingtoneScreenState.value.trimRightHandlePosition))?.toLong()
                            ?: return
                        if (rightHandleCurrentDuration.minus(
                                durationFromEvent ?: 0L
                            ) < minimumAllowedDuration
                        ) {
                            return
                        }

                        Log.d(TAG, "$positionPercentage")
                        _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                            trimLeftHandlePosition = positionPercentage
                        )
                        updateProgress(positionPercentage)
                    }

                    TrimHandle.Right -> {
                        val leftHandleCurrentDuration = (currentLocalAudio?.duration?.toFloat()
                            ?.times(trimRingtoneScreenState.value.trimLeftHandlePosition))?.toLong()
                            ?: return
                        if ((durationFromEvent
                                ?: 0L).minus(leftHandleCurrentDuration) < minimumAllowedDuration
                        ) {
                            return
                        }
                        _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                            trimRightHandlePosition = positionPercentage
                        )
                    }
                }
            }

            is TrimRingtoneEvent.OnLeftHandleEvent -> {
                val millisecondsThreshold = 500L
                val progressFloatThreshold =
                    millisecondsThreshold.toFloat() / (currentLocalAudio?.duration
                        ?: return).toFloat()

                val leftHandleProgressToIncOrDec =
                    if (event.event == HandleEvent.Increase) progressFloatThreshold else -progressFloatThreshold

                var finalLeftHandleProgressToSet =
                    trimRingtoneScreenState.value.trimLeftHandlePosition + leftHandleProgressToIncOrDec

                if (finalLeftHandleProgressToSet < 0f) {
                    finalLeftHandleProgressToSet = 0f
                }

                val finalLeftHandleDuration = (currentLocalAudio?.duration?.toFloat()
                    ?.times(finalLeftHandleProgressToSet))?.toLong()

                val rightHandleCurrentDuration = (currentLocalAudio?.duration?.toFloat()
                    ?.times(trimRingtoneScreenState.value.trimRightHandlePosition))?.toLong()
                    ?: return
                if (rightHandleCurrentDuration.minus(
                        finalLeftHandleDuration ?: 0L
                    ) < minimumAllowedDuration
                ) {
                    return
                }

                _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                    trimLeftHandlePosition = finalLeftHandleProgressToSet
                )

                updateProgress(finalLeftHandleProgressToSet)
            }

            is TrimRingtoneEvent.OnRightHandleEvent -> {
                val millisecondsThreshold = 500L
                val progressFloatThreshold =
                    millisecondsThreshold.toFloat() / (currentLocalAudio?.duration
                        ?: return).toFloat()
                val rightHandleProgressToIncOrDec =
                    if (event.event == HandleEvent.Increase) progressFloatThreshold else -progressFloatThreshold

                var finalRightHandleProgressToSet =
                    trimRingtoneScreenState.value.trimRightHandlePosition + rightHandleProgressToIncOrDec

                if (finalRightHandleProgressToSet > 1f) {
                    finalRightHandleProgressToSet = 0f
                }

                val finalRightHandleDuration = (currentLocalAudio?.duration?.toFloat()
                    ?.times(finalRightHandleProgressToSet))?.toLong()

                val leftHandleCurrentDuration = (currentLocalAudio?.duration?.toFloat()
                    ?.times(trimRingtoneScreenState.value.trimLeftHandlePosition))?.toLong()
                    ?: return
                if ((finalRightHandleDuration
                        ?: 0L).minus(leftHandleCurrentDuration) < minimumAllowedDuration
                ) {
                    return
                }

                _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                    trimRightHandlePosition = finalRightHandleProgressToSet
                )
            }

            TrimRingtoneEvent.SkipPrevious -> {
                updateProgress(trimRingtoneScreenState.value.trimLeftHandlePosition)
            }

            TrimRingtoneEvent.OnSetRingtone -> {
                trimAndSaveRingtoneInFiles(currentLocalAudio?.uri)
            }

            TrimRingtoneEvent.OnSaveRingtone -> {
                trimAndSaveRingtoneInFiles(currentLocalAudio?.uri)
            }

            is TrimRingtoneEvent.OnToggleShare -> {
                _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                    shareRingtoneToPublic = event.value
                )
            }

            is TrimRingtoneEvent.SetShowShareRingtone -> {
                _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                    showShareRingtoneDlg = event.value
                )
            }

            TrimRingtoneEvent.DismissGoBackDialog -> {
                _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                    showGoBackAlert = false
                )
            }

            TrimRingtoneEvent.ShowGoBackDialog -> {
                _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                    showGoBackAlert = true
                )
            }
        }
    }

    init {
        audioPlayer.initialize()

        viewModelScope.launch {
            if (navArgs.contentId != null) {
                currentLocalAudio =
                    audioRepository.loadAudioByContentId(navArgs.contentId) ?: return@launch
            } else if (navArgs.songUri != null) {
                currentLocalAudio = audioRepository.loadAudioByUri(navArgs.songUri) ?: return@launch
            } else {
                _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.StringResource(R.string.please_try_again)))
            }
            loadAudio()
        }
    }

    override fun onCleared() {
        audioPlayer.clearAudio()
        audioPlayer.release()
        super.onCleared()
    }

    fun updateProgress(progress: Float) {
        val position = currentLocalAudio?.duration?.times(progress)?.toLong() ?: 0L
        audioPlayer.seekTo(position)
        _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(progress = progress)
    }

    fun updatePlaybackState() {
        when {
            trimRingtoneScreenState.value.isPlaying -> audioPlayer.pause()
            else -> audioPlayer.play()
        }
    }

    private fun loadAudio() {
        viewModelScope.launch {
            try {
                if (currentLocalAudio == null) {
                    _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.StringResource(R.string.please_try_again)))
                    return@launch
                }
                currentLocalAudio?.let(audioPlayer::setAudio)
                launch { currentLocalAudio?.let { loadAudioAmplitudes(it) } }
                launch { observePlaybackEvents() }
                _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                    audioDisplayName = currentLocalAudio?.nameWithoutExtension.orEmpty(),
                    totalDuration = currentLocalAudio?.duration,
                )

                delay(1000)
                audioPlayer.play()
                updateProgress(trimRingtoneScreenState.value.trimLeftHandlePosition)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadAudioAmplitudes(localAudio: LocalAudio) {
        try {
            val amplitudes = audioRepository.loadAudioAmplitudes(localAudio.path)
            _trimRingtoneScreenState.value =
                trimRingtoneScreenState.value.copy(amplitudes = amplitudes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun observePlaybackEvents() {
        audioPlayer.events.collectLatest {
            when (it) {
                is AudioPlayer.Event.PositionChanged -> {
                    /**This is a work around if the song is not loaded from the media files then there is not content id available.
                     * In that case we cannot get the duration for the song.
                     * So we are updating the duration here and the song is also playing from start. That's why we are updating the progress of the song**/
                    if (currentLocalAudio?.duration == 0L) {
                        currentLocalAudio = currentLocalAudio?.copy(duration = it.duration)
                        _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                            totalDuration = it.duration
                        )
                        updateProgress(_trimRingtoneScreenState.value.trimLeftHandlePosition)
                        return@collectLatest
                    }
                    updatePlaybackProgress(it.position)
                }

                is AudioPlayer.Event.PlayingChanged -> updatePlayingState(it.isPlaying)
                else -> {}
            }
        }
    }

    private fun updatePlaybackProgress(position: Long) {
        val audio = currentLocalAudio ?: return
        val progress = position.toFloat() / audio.duration

        if (progress >= trimRingtoneScreenState.value.trimRightHandlePosition) {
            updateProgress(trimRingtoneScreenState.value.trimLeftHandlePosition)
            return
        }

        _trimRingtoneScreenState.value =
            trimRingtoneScreenState.value.copy(progress = progress)
    }

    private fun updatePlayingState(isPlaying: Boolean) {
        _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(isPlaying = isPlaying)
    }

    private fun trimAndSaveRingtoneInFiles(uri: Uri?) {
        viewModelScope.launch {
            try {
                uri?.let { fileUri ->
                    val extension = currentLocalAudio?.name?.substringAfterLast(".")
                    _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                        loading = true
                    )
                    val ringtoneFile =
                        File(applicationContext.cacheDir, "${UUID.randomUUID()}.$extension")
                    ringtoneFile.createNewFile()
                    var filesToSave = File(
                        if (Build.VERSION.SDK_INT >= Q) applicationContext.filesDir else applicationContext.getExternalFilesDir(
                            Environment.DIRECTORY_RINGTONES
                        ),
                        "${currentLocalAudio?.nameWithoutExtension}.$extension"
                    )
                    var i = 1
                    while (filesToSave.exists()) {
                        filesToSave = File(
                            if (Build.VERSION.SDK_INT >= Q) applicationContext.filesDir else applicationContext.getExternalFilesDir(
                                Environment.DIRECTORY_RINGTONES
                            ),
                            "${currentLocalAudio?.nameWithoutExtension}(${i}).$extension"
                        )
                        i += 1
                    }

                    filesToSave.createNewFile()

                    applicationContext.contentResolver.openInputStream(fileUri).use { inp ->
                        ringtoneFile.outputStream().use { output ->
                            inp?.copyTo(output)
                        }
                    }

                    val trimLeftHandleDurationSeconds =
                        (trimRingtoneScreenState.value.trimLeftHandlePosition * (currentLocalAudio?.duration
                            ?: 0L)) / 1000f
                    val trimRightHandleDurationSeconds =
                        (trimRingtoneScreenState.value.trimRightHandlePosition * (currentLocalAudio?.duration
                            ?: 0L)) / 1000f
                    val totalSongLengthNeeded =
                        trimRightHandleDurationSeconds - trimLeftHandleDurationSeconds

                    val result =
                        FFmpeg.execute("-ss ${maxOf(0f, trimLeftHandleDurationSeconds - 0.25f)} -t $totalSongLengthNeeded -y -i ${ringtoneFile.absolutePath} -map 0 -c copy -acodec copy \"${filesToSave.absolutePath}\"")
                    Config.enableStatisticsCallback {
                        Log.d(TAG, "${it.time}")
                    }
                    _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                        loading = false
                    )
                    if (result == Config.RETURN_CODE_SUCCESS) {
                        if (navArgs.contactsArg.contacts.isNotEmpty()) {
                            setRingtoneToContacts(
                                file = filesToSave,
                                contacts = navArgs.contactsArg.contacts
                            )
                        } else {
                            if (Settings.System.canWrite(applicationContext)) {
                                if (ringtoneApi.setDefaultRingtone(filesToSave)) {
                                    startFileUploadWorker(filesToSave)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        if ((userDataState.userData.value?.adsCoins ?: 0) < 10) {
                                            interstitial?.showAd()
                                        } else askForReview()
                                    }
                                    _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.StringResource(R.string.ringtone_set_successfully)))
                                    _uiEventFlow.emit(UiEvent.NavigateBack)
                                    _uiEventFlow.emit(UiEvent.NavigateBack)
                                } else {
                                    _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.DynamicString("Something went wrong, Failed to set ringtone.")))
                                }
                            } else {
                                _trimRingtoneScreenState.value = trimRingtoneScreenState.value.copy(
                                    filePathToSaveAsRingtoneWithPendingPermission = filesToSave.absolutePath
                                )
                            }
                        }
                    } else {
                        Log.d(TAG, "FFMPEG fail")
                        _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.StringResource(R.string.failed_to_create_file)))
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, e.message, e)
                _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.StringResource(R.string.failed_to_create_file)))
            }
        }
    }

    private suspend fun askForReview() {
        inAppReviewHelper.askForReview()
    }

    private fun startFileUploadWorker(filesToSave: File) {
        viewModelScope.launch {
            preferences.getUserData().first()?.id?.let { id ->
                val uploadRequest =
                    OneTimeWorkRequestBuilder<UploadRingtoneWorker>()
                val uploadRingtoneInputData = UploadRingtoneInputData(
                    filePath = filesToSave.absolutePath,
                    userId = id,
                    shareToPublic = _trimRingtoneScreenState.value.shareRingtoneToPublic
                )
                uploadRequest.setInputData(
                    workDataOf(
                        UploadRingtoneWorker.UPLOAD_FILE_INPUT_DATA to json.encodeToString(
                            uploadRingtoneInputData
                        )
                    )
                )
                uploadRequest.setInitialDelay(
                    duration = 2000L,
                    timeUnit = TimeUnit.MILLISECONDS
                )
                uploadRequest.addTag(UploadRingtoneWorker.UPLOAD_FILE_WORKER_TAG)
                workManager.enqueue(uploadRequest.build())
            }
        }
    }

    private fun setRingtoneToContacts(file: File, contacts: List<UiContact>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = ringtoneApi.setCustomRingtoneToContacts(
                    file = file,
                    contacts = contacts.map { ContactIds(it.lookupKey, it.contactId) }
                )

                if (result) {
                    startFileUploadWorker(file)
                    CoroutineScope(Dispatchers.Main).launch {
                        if ((userDataState.userData.value?.adsCoins ?: 0) < 10) {
                            interstitial?.showAd()
                        } else askForReview()
                    }
                    _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.StringResource(R.string.ringtone_set_successfully)))
                    _uiEventFlow.emit(UiEvent.NavigateBack)
                    _uiEventFlow.emit(UiEvent.NavigateBack)
                } else {
                    _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.DynamicString("Something went wrong, Failed to set ringtone.")))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.StringResource(R.string.failed_to_create_file)))
            }
        }
    }
}