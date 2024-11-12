package com.famas.tonz

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.request.CachePolicy
import java.io.File

@Composable
fun buildCoilDefaultImageLoader() = ImageLoader(LocalContext.current).newBuilder()
    .crossfade(true)
    .diskCachePolicy(CachePolicy.ENABLED)
    .memoryCachePolicy(CachePolicy.ENABLED)
    .build()

fun String.hexToColor() = Color(android.graphics.Color.parseColor(this))

fun getDownloadFilePathWithFileName(fileName: String, index: Int? = null): String {
    val file =
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            if (index == null) "$fileName.mp3" else "$fileName(${index}).mp3"
        )

    return if (file.exists()) {
        getDownloadFilePathWithFileName(fileName, (index ?: 0) + 1)
    } else {
        file.absolutePath
    }
}


// Function to extract filename from Content-Disposition header
fun extractFileNameFromContentDisposition(contentDisposition: String?): String? {
    contentDisposition?.let {
        val match = Regex("filename\\s*=\\s*\"([^\"]*)\"").find(it)
        if (match != null) {
            return match.groupValues[1]
        }
    }
    return null
}

// Function to sanitize file name (remove invalid characters)
fun sanitizeFileName(fileName: String): String {
    return fileName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
}