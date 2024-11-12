package com.famas.tonz.feature_feed.domain

import com.famas.tonz.core.data.BasicResponse
import com.google.firebase.auth.AuthCredential

interface AuthRepository {
    suspend fun signInWithGoogle(authCredential: AuthCredential): Result<Boolean>
    suspend fun redeemReferral(referralCode: String): BasicResponse<Unit>
}