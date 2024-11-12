package com.famas.tonz.feature_feed.data.remote.responses


import com.famas.tonz.feature_feed.domain.models.RingtonePostModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ringtone(
    @SerialName("album")
    val album: String? = null,
    @SerialName("artist")
    val artist: String? = null,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("file_name")
    val fileName: String? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("_id")
    val id: String? = null,
    @SerialName("like_count")
    val likeCount: Int? = null,
    @SerialName("share_to_public")
    val shareToPublic: Boolean? = null,
    @SerialName("thumbnail_image_url")
    val thumbnailImageUrl: String? = null,
    @SerialName("timestamp")
    val timestamp: Long? = null,
    @SerialName("verified")
    val verified: Boolean? = null,
    @SerialName("current_user_liked")
    val currentUserLiked: Boolean? = null,
    @SerialName("created_by_name")
    val createdByName: String? = null,
    @SerialName("created_by_profile_pic")
    val createdByProfilePic: String? = null,
    @SerialName("show_ad")
    val showAd: Boolean = false
)


fun Ringtone.toRingtonePostModel(): RingtonePostModel? {
    return if (showAd) RingtonePostModel(
        ringtoneName = "",
        thumbnailPictureUrl = "",
        albumName = "",
        artist = "",
        fileUrl = "",
        likeCount = "",
        ringtoneId = "",
        uploadedByName = "",
        uploadedByProfilePic = "",
        isPresentUserLiked = false,
        isAd = true
    ) else RingtonePostModel(
        ringtoneName = fileName ?: return null,
        thumbnailPictureUrl = thumbnailImageUrl,
        albumName = album,
        artist = artist,
        fileUrl = fileUrl ?: return null,
        likeCount = likeCount?.run {
            if (this > 1000) "${
                String.format(
                    "%.1f",
                    div(1000f)
                )
            }K" else toString()
        } ?: return null,
        ringtoneId = id ?: return null,
        uploadedByName = createdByName ?: return null,
        uploadedByProfilePic = createdByProfilePic,
        isPresentUserLiked = currentUserLiked ?: return null,
    )
}