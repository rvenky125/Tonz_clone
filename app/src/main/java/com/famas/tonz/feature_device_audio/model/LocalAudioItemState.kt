package com.famas.tonz.feature_device_audio.model

import android.graphics.Bitmap
import android.net.Uri
import com.famas.tonz.core.model.LocalAudio
import com.famas.tonz.core.util.formatToUserReadableDateString
import com.famas.tonz.extensions.formatAsAudioDuration
import com.famas.tonz.extensions.formatAsFileSize

data class LocalAudioItemState(
    val id: String,
    val displayName: String,
    val name: String,
    val uri: Uri,
    val path: String,
    val size: String,
    val extension: String,
    val dateToShow: String,
    val image: Bitmap? = null
)

fun LocalAudio.toLocalAudioItemState(): LocalAudioItemState? {
    return LocalAudioItemState(
        id = id ?: return null,
        name = name,
        displayName = name.substringBeforeLast("."),
        uri = uri,
        path = path,
        size = duration.formatAsAudioDuration + " | " + size.formatAsFileSize,
        extension = name.substringAfterLast(".").uppercase(),
        dateToShow = timestamp.formatToUserReadableDateString(),
    )
}