package com.famas.tonz.feature_music.data.workers

import android.util.Log
import android.webkit.URLUtil
import com.famas.tonz.core.TAG
import kotlinx.serialization.Serializable
import java.net.URLDecoder

@Serializable
data class DownloadAudioFileInputData(
    val url: String,
    val headers: Map<String, String>,
    val method: String,
    val fileName: String = URLUtil.guessFileName(url, null, null) ?: generateFileNameFromUrl(url)
)

fun generateFileNameFromUrl(url: String): String {
    val decodedUrl = URLDecoder.decode(url, "UTF-8")
    val name = decodedUrl.substringBeforeLast(".").substringAfterLast("/")
        .filter { it.isWhitespace() || it.isLetterOrDigit() }
        .replace(" ", "_")

    Log.d(TAG, "name is $name")
    return name
}
