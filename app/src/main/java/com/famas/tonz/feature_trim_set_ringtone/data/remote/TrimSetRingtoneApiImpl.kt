package com.famas.tonz.feature_trim_set_ringtone.data.remote

import android.net.Uri
import androidx.core.net.toFile
import com.famas.tonz.core.data.BasicResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.url
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class TrimSetRingtoneApiImpl(
    private val httpClient: HttpClient
) : TrimSetRingtoneApi {

    override suspend fun uploadRingtone(
        fileUri: String,
        createdBy: String,
        shareToPublic: Boolean
    ): BasicResponse<Unit> {
        return httpClient.submitFormWithBinaryData(formData {
            val file = Uri.parse(fileUri).toFile()
            append("file", file.readBytes(), Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
            })
            append("created_by", createdBy)
            append("share_to_public", shareToPublic)
        }) {
            url(TrimSetRingtoneApi.ADD_POST_END_POINT)
            timeout {
                connectTimeoutMillis = Long.MAX_VALUE
                requestTimeoutMillis = Long.MAX_VALUE
                socketTimeoutMillis = Long.MAX_VALUE
            }
        }.body()
    }
}