package com.famas.tonz.feature_feed.data.remote.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RedeemReferralRequest(
    @SerialName("referral_code")
    val referralCode: String,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("device_id")
    val deviceId: String? = null
)
