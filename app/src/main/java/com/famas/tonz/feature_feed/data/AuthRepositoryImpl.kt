package com.famas.tonz.feature_feed.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.famas.tonz.R
import com.famas.tonz.core.TAG
import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.core.device_util.GetAdvertisingId
import com.famas.tonz.feature_feed.data.remote.AuthApi
import com.famas.tonz.feature_feed.data.remote.requests.RedeemReferralRequest
import com.famas.tonz.feature_feed.domain.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val preferences: Preferences,
    private val authApi: AuthApi,
    private val context: Context,
    private val getAdvertisingId: GetAdvertisingId,
) : AuthRepository {
    override suspend fun signInWithGoogle(authCredential: AuthCredential): Result<Boolean> {
        return try {
            auth.signInWithCredential(authCredential).await()
            loginUser()
            Result.success(true)
        } catch (e: IOException) {
            Log.d("myTag", e.localizedMessage, e)
            Result.failure(java.lang.Exception("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            Result.failure(java.lang.Exception("Something went wrong, try again"))
        } finally {
            auth.signOut()
        }
    }

    override suspend fun redeemReferral(referralCode: String): BasicResponse<Unit> {
        return try {
            val advertisingId = getAdvertisingId()
            Log.d(TAG, "advertisingId: $advertisingId")
            val user = preferences.getUserData().firstOrNull()
            return authApi.redeemReferral(
                referralRequest = RedeemReferralRequest(
                    referralCode, createdBy = user?.id ?: return BasicResponse(
                        msg = "Failed to get login info, please logout and login again",
                        successful = false
                    ), deviceId = advertisingId
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
            BasicResponse(
                msg = context.getString(R.string.something_went_wrong), successful = false
            )
        }
    }

    private suspend fun loginUser() {
        auth.currentUser?.let {
            val user = it.toUser()
            val advertisingId = getAdvertisingId()
            val fcmToken = FirebaseMessaging.getInstance().token.await()

            val loginResponse =
                authApi.signInOrUpUser(user ?: throw java.lang.Exception(), advertisingId, fcmToken)

            if (loginResponse.successful) {
                loginResponse.data.firstOrNull()?.let { it1 -> preferences.setUserData(it1) }
                    ?: throw java.lang.Exception("Failed to authenticate, please try again")
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, loginResponse.msg, Toast.LENGTH_LONG).show()
                }
                auth.signOut()
            }
        }
    }
}