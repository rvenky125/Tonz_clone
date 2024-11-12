package com.famas.tonz.core.device_util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@SuppressLint("HardwareIds")
suspend fun Context.getUniqueId(retryCount: Int = 1): String? {
    return try {
        val androidId = Settings.Secure.getString(
            this.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        if (!androidId.isNullOrBlank()) {
            androidId
        } else {
            if (retryCount < 5) {
                delay(100)
                Log.d("myTag", "Trying for device unique id: $retryCount")
                getUniqueId(retryCount = retryCount + 1)
            } else try {
                val id = getAdvertisingId(this)
                if (id != "00000000-0000-0000-0000-000000000000" && !id.isNullOrBlank()) id
                else getSerialOrGeneratedNumber()
            } catch (e: Exception) {
                getSerialOrGeneratedNumber()
            }
        }
    } catch (e: Exception) {
        Log.d("myTag", "exception at getting imei")
        getSerialOrGeneratedNumber()
    }
}


fun getSerialOrGeneratedNumber(): String? {
    var serialNumber: String?
    try {
        val c = Class.forName("android.os.SystemProperties")
        val get = c.getMethod("get", String::class.java)
        serialNumber = get.invoke(c, "gsm.sn1") as String?
        if (serialNumber == "" || serialNumber == null) serialNumber =
            get.invoke(c, "ril.serialnumber") as String?
        if (serialNumber == "" || serialNumber == null) serialNumber =
            get.invoke(c, "ro.serialno") as String?
        if (serialNumber == "" || serialNumber == null) serialNumber =
            get.invoke(c, "sys.serialnumber") as String?
        if (serialNumber == "" || serialNumber == null) serialNumber = Build.SERIAL
    } catch (e: java.lang.Exception) {
        return null
    }
    return serialNumber
}

suspend fun getAdvertisingId(context: Context): String? {
    return try {
        withContext(Dispatchers.IO) {
            val adInfo = com.google.android.gms.ads.identifier.AdvertisingIdClient
                .getAdvertisingIdInfo(context)
            if (adInfo.isLimitAdTrackingEnabled) {
                return@withContext null
            }
            adInfo.id
        }
    } catch (exception: Exception) {
        Log.d("myTag", "exception in getting ad id", exception)
        null
    }
}