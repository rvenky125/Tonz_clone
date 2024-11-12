package com.famas.tonz.feature_feed.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikeDto(
    @SerialName("_id")
    val id: String,
    @SerialName("to_id")
    val to_id: String,
    @SerialName("type")
    val type: String,
    @SerialName("created_by")
    val created_by: String,
    @SerialName("timestamp")
    val timestamp: Long
) {
    companion object {
        const val RINGTONE_LIKE = "RINGTONE_LIKE"
    }
}
