package com.famas.tonz.feature_feed.data

import com.google.firebase.auth.FirebaseUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseUserModel(
    @SerialName("display_name")
    val display_name: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("photo_url")
    val photo_url: String? = null,
    @SerialName("uid")
    val uid: String? = null,
    @SerialName("status")
    val status: Int? = null
)

fun FirebaseUser.toUser(): FirebaseUserModel? {
    return FirebaseUserModel(
        display_name = displayName ?: return null,
        email = email ?: return null,
        photo_url = photoUrl?.toString() ?: "",
        uid = "$uid",
        status = 1
    )
}
