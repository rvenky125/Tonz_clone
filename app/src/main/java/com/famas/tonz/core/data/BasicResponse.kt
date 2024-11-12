package com.famas.tonz.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasicResponse<T>(
    @SerialName("data")
    val data: List<T> = emptyList(),
    @SerialName("msg")
    val msg: String,
    @SerialName("successful")
    val successful: Boolean
)