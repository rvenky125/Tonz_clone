package com.famas.tonz.core

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.famas.tonz.R
import com.famas.tonz.core.ad_util.AdMobInterstitial
import com.famas.tonz.core.core_states.UserDataState
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.ui.util.UiText
import com.famas.tonz.core.util.ConnectivityObserver
import com.famas.tonz.core.util.Constants
import com.famas.tonz.core.util.ContactIds
import com.famas.tonz.core.util.InAppReviewHelper
import com.famas.tonz.core.util.RingtoneApi
import com.famas.tonz.core.util.UiContact
import com.famas.tonz.feature_feed.domain.AuthRepository
import com.famas.tonz.feature_profile.domain.ProfileRepository
import com.famas.tonz.feature_trim_set_ringtone.data.workers.UploadRingtoneInputData
import com.famas.tonz.feature_trim_set_ringtone.data.workers.UploadRingtoneWorker
import com.google.android.play.core.review.ReviewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainActivityVM @Inject constructor(
    private val workManager: WorkManager,
    private val userDataState: UserDataState,
    private val preferences: Preferences,
    private val ringtoneApi: RingtoneApi,
    private val json: Json,
    private val connectivityObserver: ConnectivityObserver,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val inAppReviewHelper: InAppReviewHelper,
) : ViewModel() {
    val workInfos = workManager.getWorkInfosByTagLiveData(Constants.DOWLOAD_AUDIO_FILE_WORKER)
    var interstitial: AdMobInterstitial? = null

    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow = _uiEventFlow.asSharedFlow()

    var referralCode by mutableStateOf("")
    var referralCodeErrMessage by mutableStateOf<String?>(null)
    var submittingReferralCode by mutableStateOf(false)

    var showSurpriseBox by mutableStateOf(false)
        private set

    var workIdToShowDialog by mutableStateOf<UUID?>(null)
        private set

    var workIdToShowDialogLaterDownload by mutableStateOf<UUID?>(null)
        private set

    var selectedFileUriToSetRingtone by mutableStateOf<String?>(null)
        private set

    var showAdsCoins by mutableStateOf(false)

    val isContactsLoaded = mutableStateOf(false)

    var filePathToSaveAsRingtoneWithPendingPermission by mutableStateOf<String?>(null)
    var trimRingtone = true

    val userData = userDataState.userData

    var showOnlyProgressOfWorker = false

    var currentAdsCount by mutableIntStateOf(0)

    private val connectivityFlow = connectivityObserver.observe()

    fun onEvent(event: MainActivityEvent) {
        when (event) {
            is MainActivityEvent.SetWorkIdToShowDialog -> {
                workIdToShowDialog = event.id
                showOnlyProgressOfWorker = event.showOnlyProgressOfWorker
            }

            is MainActivityEvent.SetWorkIdToShowLaterDialog -> {
                workIdToShowDialogLaterDownload = event.id
            }

            is MainActivityEvent.OnSelectFileUriToSetRingtone -> {
                trimRingtone = event.trimRingtone
                selectedFileUriToSetRingtone = event.uri
            }

            is MainActivityEvent.SetRingtoneToContacts -> {
                if (event.selectedFileUriToSetRingtone == null || event.selectedUiContacts == null) {
                    return
                }

                if (event.selectedUiContacts.isEmpty()) {
                    filePathToSaveAsRingtoneWithPendingPermission =
                        event.selectedFileUriToSetRingtone
                    return
                }

                setRingtoneToContacts(
                    Uri.parse(event.selectedFileUriToSetRingtone).toFile(),
                    event.selectedUiContacts
                )
            }

            MainActivityEvent.OnToggleSurpriseBox -> {
                showSurpriseBox = !showSurpriseBox
            }

            MainActivityEvent.InterstitialAdClosed -> {
                viewModelScope.launch {
                    viewModelScope.launch {
                        preferences.setAdsPromotionCount(currentAdsCount + 1)
                    }
                }
            }

            MainActivityEvent.OnConfirm -> {
                if (submittingReferralCode) {
                    return
                }
                redeemReferral()
            }

            MainActivityEvent.OnDismissReferral -> {
                if (submittingReferralCode) {
                    return
                }
                viewModelScope.launch {
                    val user = preferences.getUserData().firstOrNull() ?: return@launch
                    preferences.setUserData(user.copy(isJustRegistered = false))
                }
            }

        }
    }

    suspend fun askForReview() {
        inAppReviewHelper.askForReview()
    }

    private fun redeemReferral() {
        viewModelScope.launch {
            submittingReferralCode = true
            val response = authRepository.redeemReferral(referralCode)
            submittingReferralCode = false

            if (response.successful) {
                val user = preferences.getUserData().firstOrNull() ?: return@launch
                preferences.setUserData(user.copy(isJustRegistered = false))
                _uiEventFlow.emit(UiEvent.ShowSnackBar(UiText.DynamicString(response.msg)))
                delay(1000)
                syncUserData()
                repeat(2) {
                    delay(2500)
                    syncUserData()
                }
            } else {
                referralCodeErrMessage = response.msg
            }
        }
    }

    fun startFileUploadWorker(filesToSave: File) {
        viewModelScope.launch {
            preferences.getUserData().first()?.id?.let { id ->
                val uploadRequest =
                    OneTimeWorkRequestBuilder<UploadRingtoneWorker>()
                val uploadRingtoneInputData = UploadRingtoneInputData(
                    filePath = filesToSave.absolutePath,
                    userId = id,
                    shareToPublic = true
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
                    if ((userDataState.userData.value?.adsCoins ?: 0) < 10) {
                        interstitial?.showAd()
                    } else askForReview()

                    startFileUploadWorker(file)
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

    fun syncUserData() {
        if (userDataState.userData.value?.isJustRegistered == true) return
        viewModelScope.launch {
            val getUserResponse = profileRepository.getCurrentUser()

            if (getUserResponse.successful) {
                getUserResponse.data.firstOrNull()
                    ?.let { user ->
                        preferences.setUserData(user)
                        userDataState.changeUserData(user)
                    }
            }
        }
    }

    init {
        connectivityFlow.onEach {
            when (it) {
                ConnectivityObserver.Status.Available -> {
                    if (userDataState.userData.value?.isJustRegistered == true) return@onEach

                    val getUserResponse = profileRepository.getCurrentUser()
                    if (getUserResponse.successful) {
                        getUserResponse.data.firstOrNull()
                            ?.let { user -> preferences.setUserData(user) }
                    }
                }

                else -> {}
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            preferences.getAdsPromotionCount().collectLatest {
                currentAdsCount = it
            }
        }

//        viewModelScope.launch {
//            delay(10000)
//            userDataState.changeUserData(userDataState.userData.value?.copy(isJustRegistered = true) ?: return@launch)
//        }
    }
}