package com.famas.tonz.feature_feed.data.remote

import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.feature_feed.data.FirebaseUserModel
import com.famas.tonz.feature_feed.data.remote.requests.RedeemReferralRequest
import com.famas.tonz.feature_feed.data.remote.responses.User

interface AuthApi {
    suspend fun signInOrUpUser(userModel: FirebaseUserModel, advertisingId: String?, fcmToken: String?): BasicResponse<User>

    suspend fun redeemReferral(referralRequest: RedeemReferralRequest): BasicResponse<Unit>

    companion object {
        const val SING_IN_UP_END_PATH = "sign_in_up_user/"
        const val REDEEM_REFERRAL_END_PATH = "redeem_referral/"
    }
}