package com.famas.tonz.feature_device_audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.famas.tonz.core.model.PermissionsState
import com.famas.tonz.feature_device_audio.data.AudioRepository
import com.famas.tonz.feature_device_audio.model.AudioListState
import com.famas.tonz.feature_device_audio.model.toLocalAudioItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioListViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {
    var audioListState: AudioListState by mutableStateOf(AudioListState())
        private set

    private var loadAudioJob: Job? = null

    fun updateSearchQuery(query: String) {
        audioListState = audioListState.copy(searchQuery = query)
        loadAudioFiles(query)
    }

    fun updatePermissionsState(granted: Boolean) {
        audioListState = audioListState.copy(
            permissionsState = when {
                granted -> PermissionsState.Granted
                else -> PermissionsState.Denied
            }
        )
        if (granted) {
            loadAudioFiles()
        }
    }

    fun loadAudioFiles(query: String? = null) {
        loadAudioJob?.cancel()
        loadAudioJob = viewModelScope.launch {
            try {
                audioListState = audioListState.copy(isLoadingAudios = true)
                val audioFiles = audioRepository.loadAudioFiles(query ?: audioListState.searchQuery)
                    .mapNotNull { it.toLocalAudioItemState() }
                audioListState = audioListState.copy(audioFiles = audioFiles)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                audioListState = audioListState.copy(isLoadingAudios = false)
            }
        }
    }
}