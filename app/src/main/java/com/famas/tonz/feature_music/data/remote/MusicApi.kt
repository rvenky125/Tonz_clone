package com.famas.tonz.feature_music.data.remote

import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.feature_music.data.MusicWebPage
import com.famas.tonz.feature_music.domain.models.DownloadStatus
import kotlinx.coroutines.flow.Flow

interface MusicApi {
    fun getStreamOfAudioFile(
        url: String, headers: Map<String, String>,
        suggestedFileName: String
    ): Flow<DownloadStatus>

    suspend fun getMusicWebPages(): BasicResponse<MusicWebPage>

    companion object {
        const val MUSIC_WEBPAGES_END_POINT = "music_webpages/"
    }
}