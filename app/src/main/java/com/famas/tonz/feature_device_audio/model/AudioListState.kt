package com.famas.tonz.feature_device_audio.model

import com.famas.tonz.core.model.PermissionsState

data class AudioListState(
    val permissionsState: PermissionsState = PermissionsState.Unknown,
    val searchQuery: String = "",
    val audioFiles: List<LocalAudioItemState> = emptyList(),
    val isLoadingAudios: Boolean = false,
)