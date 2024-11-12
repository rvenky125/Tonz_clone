package com.famas.tonz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.imageLoader
import coil.request.ImageRequest
import com.famas.tonz.core.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TonzFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        message.notification?.let {
            showNotification(it, message.data)
        }
    }

    private fun showNotification(
        notification: RemoteMessage.Notification,
        data: MutableMap<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java)
        data.forEach { (t, u) ->
            intent.putExtra(t, u)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent =
            PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "tonz_default_channel"
        val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSound(defaultUri)
            .setContentIntent(pendingIntent)

        if (notification.imageUrl != null) {
            try {
                imageLoader.enqueue(ImageRequest.Builder(this)
                    .listener { _, result ->
                        result.drawable.toBitmapOrNull()?.let {
                            notificationBuilder.setLargeIcon(it)
                        }
                        displayNotification(channelId, notificationBuilder)
                    }
                    .data(notification.imageUrl).build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            displayNotification(channelId, notificationBuilder)
        }
    }

    private fun displayNotification(
        channelId: String,
        notificationBuilder: NotificationCompat.Builder
    ) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tonz Default Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}