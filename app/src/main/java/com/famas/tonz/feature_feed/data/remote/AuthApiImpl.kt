package com.famas.tonz.feature_feed.data.remote

import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.feature_feed.data.FirebaseUserModel
import com.famas.tonz.feature_feed.data.remote.requests.RedeemReferralRequest
import com.famas.tonz.feature_feed.data.remote.requests.SignInOrUpUser
import com.famas.tonz.feature_feed.data.remote.responses.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url

class AuthApiImpl(private val httpClient: HttpClient) : AuthApi {
    override suspend fun signInOrUpUser(userModel: FirebaseUserModel, advertisingId: String?, fcmToken: String?): BasicResponse<User> {
        return httpClient.post {
            url(AuthApi.SING_IN_UP_END_PATH)
            setBody(
                SignInOrUpUser(
                    display_name = userModel.display_name,
                    email = userModel.email!!,
                    photo_url = userModel.photo_url,
                    uid = userModel.uid!!,
                    deviceId = advertisingId,
                    fcmToken = fcmToken
                )
            )
        }.body()
    }

    override suspend fun redeemReferral(referralRequest: RedeemReferralRequest): BasicResponse<Unit> {
        return httpClient.post {
            url(AuthApi.REDEEM_REFERRAL_END_PATH)
            setBody(referralRequest)
        }.body()
    }
}