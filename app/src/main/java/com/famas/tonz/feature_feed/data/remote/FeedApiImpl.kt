package com.famas.tonz.feature_feed.data.remote

import android.util.Log
import com.famas.tonz.core.TAG
import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.feature_feed.data.remote.requests.GetRingtoneRequest
import com.famas.tonz.feature_feed.data.remote.requests.GetRingtonesRequest
import com.famas.tonz.feature_feed.data.remote.requests.ToggleLikeRequest
import com.famas.tonz.feature_feed.data.remote.responses.Ringtone
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url

class FeedApiImpl(val httpClient: HttpClient) : FeedApi {
    override suspend fun getRingtones(
        tags: List<Int>,
        currentUid: String,
        pageNumber: Int,
        search: String,
        language: String?
    ): BasicResponse<Ringtone> {
        Log.d(TAG, "sending uid for ringtones:$currentUid")
        return httpClient.get {
            url(FeedApi.GET_RINGTONES_END_POINT)
            setBody(
                GetRingtonesRequest(
                    query = search,
                    user_id = currentUid,
                    page = pageNumber,
                    tags = tags,
                    language = language
                )
            )
        }.body()
    }

    override suspend fun toggleLike(id: String, created_by: String): BasicResponse<Unit> {
        return httpClient.post {
            url(FeedApi.TOGGLE_LIKE_END_POINT)

            setBody(
                ToggleLikeRequest(
                    id,
                    "RINGTONE_LIKE",
                    created_by
                )
            )
        }.body()
    }

    override suspend fun getRingtone(id: String, userId: String): BasicResponse<Ringtone> {
        return httpClient.get {
            url(FeedApi.GET_RINGTONE_END_POINT)
            setBody(
                GetRingtoneRequest(
                    postId = id,
                    userId = userId
                )
            )
        }.body()
    }

    override suspend fun getLanguages(): BasicResponse<String> {
        return httpClient.get {
            url(FeedApi.GET_LANGUAGES)
        }.body()
    }
}