package com.cryonix.launcher.minecraft

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cryonix.launcher.R

object MinecraftTaskNotification {
    const val CHANNEL_ID = "cryonix_tasks"
    const val NOTIFICATION_ID = 2401

    const val ACTION_PAUSE = "git.neko.cryonix.task.PAUSE"
    const val ACTION_RESUME = "git.neko.cryonix.task.RESUME"
    const val ACTION_STOP = "git.neko.cryonix.task.STOP"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cryonix tasks",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Minecraft download and installation tasks"
                setShowBadge(false)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun build(
        context: Context,
        title: String,
        text: String,
        progress: Int,
        paused: Boolean
    ): Notification {
        ensureChannel(context)
        val pauseIntent = android.content.Intent(context, MinecraftTaskService::class.java)
            .setAction(if (paused) ACTION_RESUME else ACTION_PAUSE)
        val stopIntent = android.content.Intent(context, MinecraftTaskService::class.java)
            .setAction(ACTION_STOP)

        val pausePending = android.app.PendingIntent.getService(
            context, 2402, pauseIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= 23) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val stopPending = android.app.PendingIntent.getService(
            context, 2403, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= 23) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.cryonix_logo)
            .setContentTitle(title)
            .setContentText(text)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(100, progress.coerceIn(0, 100), false)
            .addAction(
                if (paused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                if (paused) "Resume" else "Pause",
                pausePending
            )
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending)
            .build()
    }
}
