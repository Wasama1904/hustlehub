package com.wasama.hustlehub.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wasama.hustlehub.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "HustleHub Push Alert"
        val body = message.notification?.body ?: message.data["body"] ?: "You have a new update in HustleHub!"
        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        syncTokenToFirestore(token)
    }

    private fun showNotification(title: String, body: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "hustlehub_deadlines"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "HustleHub Deadlines & Pushes", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Firebase Push Notifications and Deadline Alerts for HustleHub"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notif)
    }

    companion object {
        fun syncTokenToFirestore(token: String) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .update(
                    mapOf(
                        "fcmToken" to token,
                        "lastTokenUpdate" to System.currentTimeMillis()
                    )
                )
        }
    }
}
