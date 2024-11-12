package com.famas.tonz.feature_trim_set_ringtone.data.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.famas.tonz.R
import com.famas.tonz.core.MainActivity
import com.famas.tonz.core.TAG
import com.famas.tonz.core.util.Constants
import com.famas.tonz.feature_trim_set_ringtone.data.remote.TrimSetRingtoneApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

@HiltWorker
class UploadRingtoneWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val json: Json,
    private val trimSetRingtoneApi: TrimSetRingtoneApi
) : CoroutineWorker(
    appContext = context,
    params = params
) {
    private val notificationManager =
        applicationContext.getSystemService(NotificationManager::class.java)
    private val notificationId = System.currentTimeMillis().toInt()

    override suspend fun doWork(): Result {
        val uploadRingtoneInputData = json.decodeFromString<UploadRingtoneInputData>(
            inputData.getString(
                UPLOAD_FILE_INPUT_DATA
            ) ?: return Result.failure()
        )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setForeground(getForegroundInfo())
        }

        return withContext(Dispatchers.IO) {
            try {
                val file = File(uploadRingtoneInputData.filePath)
                val result = trimSetRingtoneApi.uploadRingtone(
                    fileUri = file.toUri().toString(),
                    createdBy = uploadRingtoneInputData.userId,
                    shareToPublic = uploadRingtoneInputData.shareToPublic
                )
                Log.d(TAG, "result from ringtone upload: $result")
                if (!result.successful) {
                    return@withContext Result.failure()
                }
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    Constants.UPLOAD_SONG_NOTIFICATIONS_ID,
                    Constants.UPLOAD_SONG_NOTIFICATIONS_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
        }

        return ForegroundInfo(notificationId, getNotification())
    }

    private fun getNotification(): Notification {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            Constants.UPLOAD_SONG_NOTIFICATIONS_ID
        )
        notificationBuilder.apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle("Syncing files..")
            setOngoing(true)
            setAutoCancel(false)
            setContentIntent(pendingIntent)
        }

        return notificationBuilder.build()
    }

    companion object {
        const val UPLOAD_FILE_INPUT_DATA = "UPLOAD_FILE_INPUT_DATA"
        const val UPLOAD_FILE_WORKER_TAG = "UPLOAD_FILE_WORKER_TAG"
    }
}