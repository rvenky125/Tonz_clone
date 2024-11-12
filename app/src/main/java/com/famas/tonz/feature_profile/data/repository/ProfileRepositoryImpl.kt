package com.famas.tonz.feature_profile.data.repository

import android.util.Log
import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.feature_feed.data.remote.responses.User
import com.famas.tonz.feature_profile.data.remote.ProfileApi
import com.famas.tonz.feature_profile.data.remote.requests.GetUserRequest
import com.famas.tonz.feature_profile.domain.ProfileRepository
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

class ProfileRepositoryImpl(
    private val preferences: Preferences,
    private val profileApi: ProfileApi
) : ProfileRepository {
    override suspend fun getCurrentUser(): BasicResponse<User> {
        return try {
            profileApi.getUser(
                GetUserRequest(
                    uid = preferences.getUserData().firstOrNull()?.uid ?: return BasicResponse(
                        successful = false,
                        msg = "Please login first"
                    )
                )
            )
        }  catch (e: IOException) {
            Log.d("myTag", e.localizedMessage, e)
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: UnresolvedAddressException) {
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BasicResponse(msg = e.localizedMessage ?: "Something went wrong", successful = false)
        }
    }
}