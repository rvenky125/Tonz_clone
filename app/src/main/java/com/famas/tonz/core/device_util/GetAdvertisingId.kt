package com.famas.tonz.core.device_util

import android.content.Context

class GetAdvertisingId(private val context: Context) {
    suspend operator fun invoke(): String? {
        return context.getUniqueId()
    }
}