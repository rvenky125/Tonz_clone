package com.famas.tonz.feature_feed.data.remote.responses


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("_id")
    val id: String? = null,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    @SerialName("status")
    val status: Int? = null,
    @SerialName("uid")
    val uid: String? = null,
    @SerialName("is_just_registered")
    val isJustRegistered: Boolean = false,
    @SerialName("referral_code")
    val referralCode: String? = null,
    @SerialName("ads_coins")
    val adsCoins: Int = 0
)