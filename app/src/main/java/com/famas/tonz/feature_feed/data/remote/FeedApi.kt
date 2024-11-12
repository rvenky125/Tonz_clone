package com.famas.tonz.feature_feed.data.remote

import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.feature_feed.data.remote.responses.Ringtone

interface FeedApi {
    suspend fun getRingtones(
        tags: List<Int>,
        currentUid: String,
        pageNumber: Int,
        search: String,
        language: String?
    ): BasicResponse<Ringtone>
    suspend fun toggleLike(id: String, created_by: String): BasicResponse<Unit>
    suspend fun getRingtone(id: String, userId: String): BasicResponse<Ringtone>
    suspend fun getLanguages(): BasicResponse<String>

    companion object {
        const val PAGE_SIZE = 5L

        const val GET_RINGTONES_END_POINT = "get_posts/"
        const val TOGGLE_LIKE_END_POINT = "toggle_like/"
        const val GET_RINGTONE_END_POINT = "get_post/"

        const val GET_LANGUAGES = "languages/"
    }
}