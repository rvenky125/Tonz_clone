package com.famas.tonz.feature_music.data.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.famas.tonz.R
import com.famas.tonz.core.MainActivity
import com.famas.tonz.core.TAG
import com.famas.tonz.core.util.Constants
import com.famas.tonz.feature_music.data.remote.MusicApi
import com.famas.tonz.feature_music.domain.models.DownloadStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

@HiltWorker
class DownloadAudioFileWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val musicApi: MusicApi,
) : CoroutineWorker(
    appContext = context,
    params = params
) {
    private val notificationManager =
        applicationContext.getSystemService(NotificationManager::class.java)

    private val notificationId = System.currentTimeMillis().toInt()

    override suspend fun doWork(): Result {
        val timeStamp = System.currentTimeMillis()
        val downloadAudioFileInputData: DownloadAudioFileInputData = Json.decodeFromString(
            inputData.getString(Constants.DOWNLOAD_AUDIO_FILE_DATA) ?: return Result.failure()
        )

        Log.d(TAG, downloadAudioFileInputData.toString())

        fun createWorkData(progress: Int = 0): Data {
            return workDataOf(
                PROGRESS to progress,
                TIME_STAMP to timeStamp,
                FILE_NAME to downloadAudioFileInputData.fileName
            )
        }

        setProgress(createWorkData())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setForeground(getForegroundInfo())
        }

        return withContext(Dispatchers.IO) {
            var result = Result.failure(
                workDataOf(
                    FILE_NAME to downloadAudioFileInputData.fileName,
                    TIME_STAMP to timeStamp,
                )
            )
            var fileName: String? = null
            var filePath: String? = null

            musicApi.getStreamOfAudioFile(
                url = downloadAudioFileInputData.url,
                headers = downloadAudioFileInputData.headers,
                suggestedFileName = downloadAudioFileInputData.fileName
            ).collectLatest { downloadStatus ->
                when (downloadStatus) {
                    is DownloadStatus.Started -> {
                        fileName = downloadStatus.fileName
                        filePath = downloadStatus.filePath

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            setForeground(createForegroundInfo())
                        }
                    }

                    is DownloadStatus.Progress -> {
                        val progress =
                            ((downloadStatus.bytesSentTotal.toFloat() / downloadStatus.contentLength.toFloat()) * 100f).toInt()
                        setProgress(createWorkData(progress))
                        notificationManager.notify(
                            notificationId,
                            getProgressNotification(
                                progress,
                                "Downloading ${fileName ?: downloadAudioFileInputData.fileName}"
                            )
                        )
                    }

                    DownloadStatus.Finished -> {
                        result =
                            filePath?.let {
                                Result.success(
                                    workDataOf(
                                        FILE_URI to Uri.fromFile(File(it)).toString(),
                                        FILE_NAME to fileName,
                                        TIME_STAMP to timeStamp,
                                    )
                                )
                            } ?: Result.failure()
                        Log.d(TAG, "finished: $result")
                    }

                    is DownloadStatus.Failed -> {
                        filePath?.let { File(it).delete() }
                        result = Result.failure(
                            workDataOf(
                                FILE_NAME to (fileName ?: downloadAudioFileInputData.fileName),
                                TIME_STAMP to timeStamp,
                            )
                        )
                        downloadStatus.throwable.printStackTrace()
                    }
                }
            }
            result
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    Constants.DOWNLOAD_NOTIFICATIONS_ID,
                    Constants.DOWNLOAD_NOTIFICATIONS_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
        }

        return ForegroundInfo(notificationId, getNotification())
    }

    private fun getNotification(): Notification {
        val intent = Intent(applicationContext, MainActivity::class.java)
//        intent.extras?.putString(Constants.)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            Constants.DOWNLOAD_NOTIFICATIONS_ID
        )
        notificationBuilder.apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle("Downloading audio file")
            setOngoing(true)
            setAutoCancel(false)
            setContentIntent(pendingIntent)
        }

        return notificationBuilder.build()
    }

    private fun getProgressNotification(progress: Int = 0, contentTitle: String): Notification {
        Log.d(TAG, "$progress")
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
            Constants.DOWNLOAD_NOTIFICATIONS_ID
        )
        notificationBuilder.apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(contentTitle)
            setSilent(true)
            if (progress != 0) {
                setProgress(100, progress, false)
            }
            setOngoing(true)
            setAutoCancel(false)
            setContentIntent(pendingIntent)
        }

        return notificationBuilder.build()
    }

    companion object {
        const val FILE_URI: String = "FILE_URI"
        const val PROGRESS = "PROGRESS"
        const val TIME_STAMP = "TIME_STAMP"
        const val FILE_NAME = "FILE_NAME"
    }
}