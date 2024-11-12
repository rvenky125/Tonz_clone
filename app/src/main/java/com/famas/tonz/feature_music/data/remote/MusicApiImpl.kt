package com.famas.tonz.feature_music.data.remote

import android.util.Log
import com.famas.tonz.core.TAG
import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.extractFileNameFromContentDisposition
import com.famas.tonz.feature_music.data.MusicWebPage
import com.famas.tonz.feature_music.domain.models.DownloadStatus
import com.famas.tonz.getDownloadFilePathWithFileName
import com.famas.tonz.sanitizeFileName
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.File
import java.io.IOException
import java.nio.channels.UnresolvedAddressException


class MusicApiImpl(
    private val httpClient: HttpClient,
) : MusicApi {
    override fun getStreamOfAudioFile(
        url: String,
        headers: Map<String, String>,
        suggestedFileName: String
    ): Flow<DownloadStatus> {
        println(url)
        return channelFlow {
            val downloadClient = HttpClient(CIO) {
                install(HttpTimeout)
            }

            try {
                val headRequest = downloadClient.head(url) {
                    timeout {
                        connectTimeoutMillis = Long.MAX_VALUE
                        requestTimeoutMillis = Long.MAX_VALUE
                        socketTimeoutMillis = Long.MAX_VALUE
                    }
                    headers {
                        headers.forEach { (t, u) ->
                            this.append(t, u)
                        }
                    }
                }
                val contentDispositionHeader = headRequest.headers["Content-Disposition"]
                val extractedFileName =
                    extractFileNameFromContentDisposition(contentDispositionHeader)

                val finalFileName = if (extractedFileName != null) {
                    val sanitizedFileName = sanitizeFileName(suggestedFileName)
                    sanitizedFileName
                } else {
                    suggestedFileName
                }

                val finalFilePath = getDownloadFilePathWithFileName(finalFileName)
                Log.d(TAG, "final file path: $finalFilePath")
                send(DownloadStatus.Started(finalFileName, filePath = finalFilePath))

                val request = downloadClient.get(url) {
                    timeout {
                        connectTimeoutMillis = Long.MAX_VALUE
                        requestTimeoutMillis = Long.MAX_VALUE
                        socketTimeoutMillis = Long.MAX_VALUE
                    }
                    headers {
                        headers.forEach { (t, u) ->
                            Log.d(TAG, "Adding header: $t $u")
                            this.append(t, u)
                        }
                    }
                    onDownload { bytesSentTotal, contentLength ->
                        send(DownloadStatus.Progress(bytesSentTotal, contentLength))
                    }
                }

                val file = File(finalFilePath)
                val bytesChannel = request.bodyAsChannel()
                bytesChannel.copyAndClose(file.writeChannel())
                send(DownloadStatus.Finished)
                downloadClient.close()
            } catch (e: Exception) {
                e.printStackTrace()
                downloadClient.close()
                send(DownloadStatus.Failed(e))
            }
        }
    }

    override suspend fun getMusicWebPages(): BasicResponse<MusicWebPage> {
        return try {
            httpClient.get(MusicApi.MUSIC_WEBPAGES_END_POINT).body()
        } catch (e: IOException) {
            Log.d("myTag", e.localizedMessage, e)
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: UnresolvedAddressException) {
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BasicResponse(msg = "Something went wrong", successful = false)
        }
    }
}