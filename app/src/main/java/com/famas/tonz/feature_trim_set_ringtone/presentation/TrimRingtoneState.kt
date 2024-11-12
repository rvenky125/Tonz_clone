package com.famas.tonz.feature_trim_set_ringtone.presentation

data class TrimRingtoneState(
    val loading: Boolean = false,
    val audioDisplayName: String = "",
    val amplitudes: List<Int> = emptyList(),
    val isPlaying: Boolean = false,
    val progress: Float = 0F,
    val waveFormWidthFactor: Float = 1f,
    val trimLeftHandlePosition: Float = 0.3f,
    val trimRightHandlePosition: Float = 0.6f,
    val totalDuration: Long? = null,
    val showShareRingtoneDlg: Boolean = false,
    val shareRingtoneToPublic: Boolean = true,
    val filePathToSaveAsRingtoneWithPendingPermission: String? = null,
    val showGoBackAlert: Boolean = false
)