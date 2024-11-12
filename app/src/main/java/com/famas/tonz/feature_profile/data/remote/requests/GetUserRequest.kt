package com.famas.tonz.feature_profile.data.remote.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserRequest(
    @SerialName("id")
    val id: String? = null,
    @SerialName("uid")
    val uid: String? = null
)
