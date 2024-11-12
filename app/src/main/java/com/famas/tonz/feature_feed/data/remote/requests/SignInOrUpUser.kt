package com.famas.tonz.feature_feed.data.remote.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInOrUpUser(
    @SerialName("display_name")
    val display_name: String?,
    @SerialName("email")
    val email: String,
    @SerialName("photo_url")
    val photo_url: String?,
    @SerialName("uid")
    val uid: String,
    @SerialName("device_id")
    val deviceId: String? = null,
    @SerialName("fcm_token")
    val fcmToken: String? = null
)