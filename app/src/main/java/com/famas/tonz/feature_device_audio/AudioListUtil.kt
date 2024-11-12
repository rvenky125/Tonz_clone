package com.famas.tonz.feature_device_audio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberAudioCoverImage(filePath: String): Bitmap? {
    val mediaMetadataRetriever = remember { MediaMetadataRetriever() }
    val bitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(key1 = filePath) {
        val loadImageJob = coroutineScope.launch {
            if (bitmap.value != null) {
                return@launch
            }
            delay(800)
            try {
                mediaMetadataRetriever.setDataSource(filePath)
                val imageBytes = mediaMetadataRetriever.embeddedPicture
                bitmap.value =
                    imageBytes?.size?.let { BitmapFactory.decodeByteArray(imageBytes, 0, it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        onDispose {
            loadImageJob.cancel()
        }
    }

    return bitmap.value
}