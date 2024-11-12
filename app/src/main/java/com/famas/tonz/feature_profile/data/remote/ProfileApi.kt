package com.famas.tonz.feature_profile.data.remote

import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.feature_feed.data.remote.responses.User
import com.famas.tonz.feature_profile.data.remote.requests.GetUserRequest

interface ProfileApi {
    suspend fun getUser(getUserRequest: GetUserRequest): BasicResponse<User>

    companion object {
        const val GET_USER = "get_user/"
    }
}