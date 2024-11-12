package com.famas.tonz.feature_music.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MusicWebPage(
    @SerialName("name")
    val name: String? = null,
    @SerialName("label")
    val label: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("picture_url")
    val pictureUrl: String? = null,
    @SerialName("download_hint")
    val downloadHint: String? = null,
    @SerialName("enable_js")
    val enableJavascript: Boolean? = null,
    @SerialName("card_color")
    val cardColor: String? = null,
)