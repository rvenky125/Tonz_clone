package com.famas.tonz.feature_profile.domain

import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.feature_feed.data.remote.responses.User

interface ProfileRepository {
    suspend fun getCurrentUser(): BasicResponse<User>
}