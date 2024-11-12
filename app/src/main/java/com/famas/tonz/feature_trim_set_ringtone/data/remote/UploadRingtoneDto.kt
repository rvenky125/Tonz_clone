package com.famas.tonz.feature_trim_set_ringtone.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadRingtoneDto(
    @SerialName("file_name")
    val fileName: String? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("share_to_public")
    val shareToPublic: Boolean = false,
    @SerialName("verified")
    val verified: Boolean? = null,
    @SerialName("thumbnail_image_url")
    val thumbnailImageUrl: String? = null,
    @SerialName("timestamp")
    val timestamp: Long? = null,
    @SerialName("album")
    val album: String? = null,
    @SerialName("artist")
    val artist: String? = null,
    @SerialName("likes_count")
    val likes_count: String = ""
)
