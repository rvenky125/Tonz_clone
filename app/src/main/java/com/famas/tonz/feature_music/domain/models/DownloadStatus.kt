package com.famas.tonz.feature_music.domain.models

sealed class DownloadStatus {
    data class Started(val fileName: String, val filePath: String): DownloadStatus()

    data class Progress(
        val bytesSentTotal: Long,
        val contentLength: Long
    ) : DownloadStatus()

    object Finished : DownloadStatus()

    class Failed(
        val throwable: Throwable
    ) : DownloadStatus()
}
