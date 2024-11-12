package com.famas.tonz.feature_profile.data.remote

import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.feature_feed.data.remote.responses.User
import com.famas.tonz.feature_profile.data.remote.requests.GetUserRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.setBody

class ProfileApiImpl(
    val client: HttpClient
): ProfileApi {
    override suspend fun getUser(getUserRequest: GetUserRequest): BasicResponse<User> {
        return client.get(ProfileApi.GET_USER) {
            setBody(getUserRequest)
        }.body()
    }
}