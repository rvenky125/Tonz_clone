package com.famas.tonz.feature_feed.data.remote.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetRingtoneRequest(
    @SerialName("post_id")
    val postId: String,
    @SerialName("user_id")
    val userId: String
)