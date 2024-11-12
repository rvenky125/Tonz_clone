package com.famas.tonz.core.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat


fun Long.formatToUserReadableDateString(): String {
    return try {
        val localDateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
        localDateTime.toString()
    } catch (e: Exception) {
        val dateFormat = SimpleDateFormat("dd-mm-YYYY")
        dateFormat.format(this)
    }
}