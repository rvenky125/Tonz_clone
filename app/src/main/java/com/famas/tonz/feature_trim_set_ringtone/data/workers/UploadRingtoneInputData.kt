package com.famas.tonz.feature_trim_set_ringtone.data.workers

import kotlinx.serialization.Serializable

@Serializable
data class UploadRingtoneInputData(
    val filePath: String,
    val userId: String,
    val shareToPublic: Boolean
)
