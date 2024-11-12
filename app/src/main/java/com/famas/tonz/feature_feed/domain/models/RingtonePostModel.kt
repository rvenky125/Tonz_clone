package com.famas.tonz.feature_feed.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class RingtonePostModel(
    val ringtoneName: String,
    val thumbnailPictureUrl: String?,
    val albumName: String?,
    val artist: String?,
    val fileUrl: String,
    val likeCount: String,
    val ringtoneId: String,
    val uploadedByName: String,
    val uploadedByProfilePic: String?,
    val isPresentUserLiked: Boolean,
    val isAd: Boolean = false
)