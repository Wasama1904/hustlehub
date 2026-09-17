package com.wasama.hustlehub.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wasama.hustlehub.R

class TimerService: Service() {
    companion object {
        const val ACTION_START = "START"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_STOP = "STOP"
        const val EXTRA_PROJECT_ID = "projectId"
        var runningProjectId: String? = null
        var startTimeMillis: Long = 0
    }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel("timer", "Time Tracking", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val projectId = intent?.getStringExtra(EXTRA_PROJECT_ID) ?: return START_NOT_STICKY
        when (intent.action) {
            ACTION_START -> {
                runningProjectId = projectId
                startTimeMillis = System.currentTimeMillis()
                startForeground(1, buildNotification("Running: 00:00:00"))
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "timer")
            .setContentTitle("HustleHub Timer")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}