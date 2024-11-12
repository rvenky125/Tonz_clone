package com.famas.tonz.core.data.preferences

import com.famas.tonz.feature_feed.data.remote.responses.User
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Preferences {
    fun getUserData(): Flow<User?>
    suspend fun setUserData(user: User)
    suspend fun clearUserData()
    fun getAdsPromotionCount(): Flow<Int>
    suspend fun setAdsPromotionCount(count: Int)
    suspend fun clearAdsPromotionCount()
    suspend fun setReferralData(referralData: ReferralData)
    suspend fun getReferralData(): ReferralData?
    suspend fun observeReferralData(): Flow<ReferralData?>
    suspend fun incrementUpdateDialogToShowCount()
    fun getUpdateDialogToShowCount(): Flow<Int>
    suspend fun disableUpdateDialogToShow()
}


@Serializable
data class ReferralData(
    @SerialName("code")
    val referralCode: String? = null,
    @SerialName("ringtone_id")
    val ringtoneId: String? = null
)