package com.famas.tonz.feature_trim_set_ringtone.data.remote

import com.famas.tonz.core.data.BasicResponse

interface TrimSetRingtoneApi {
    suspend fun uploadRingtone(fileUri: String, createdBy: String, shareToPublic: Boolean): BasicResponse<Unit>

    companion object {
        const val ADD_POST_END_POINT = "add_post/"
    }
}