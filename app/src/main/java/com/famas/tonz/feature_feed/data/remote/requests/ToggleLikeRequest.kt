package com.famas.tonz.feature_feed.data.remote.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToggleLikeRequest(
    @SerialName("to_id")
    val to_id: String,
    @SerialName("type")
    val type: String,
    @SerialName("created_by")
    val created_by: String
)
