package com.famas.tonz.feature_feed.domain

import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.feature_feed.data.remote.responses.Ringtone
import com.famas.tonz.feature_feed.presentation.feed_screen.FeedListTag

interface FeedRepository {
    suspend fun getRingtones(
        tags: List<FeedListTag> = emptyList(),
        pageNumber: Int = 1,
        searchValue: String = "",
        language: String? = null
    ): BasicResponse<Ringtone>

    suspend fun toggleLike(id: String): BasicResponse<Unit>
    suspend fun getRingtone(ringtoneId: String): BasicResponse<Ringtone>
    suspend fun getLanguages(): BasicResponse<String>
}