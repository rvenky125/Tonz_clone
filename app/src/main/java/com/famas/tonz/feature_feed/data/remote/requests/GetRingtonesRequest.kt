package com.famas.tonz.feature_feed.data.remote.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetRingtonesRequest(
    @SerialName("query")
    val query: String,
    @SerialName("user_id")
    val user_id: String,
    @SerialName("page")
    val page: Int,
    @SerialName("tags")
    val tags: List<Int>,
    val language: String?
)
