package com.cryonix.launcher.minecraft

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.cryonix.launcher.runtime.CryonixMojoEngine
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import kotlin.concurrent.thread

class MinecraftTaskService : Service() {
    private var lastText = "Preparing…"
    private var lastProgress = 0

    companion object {
        const val EXTRA_INSTANCE = "instance"
        const val EXTRA_VERSION = "version"
        @Volatile private var activeControl: MinecraftTaskControl? = null
        @Volatile private var activeThread: Thread? = null
        @Volatile private var active = false

        fun isActive(): Boolean = active
    }

    override fun onCreate() {
        super.onCreate()
        MinecraftTaskNotification.ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            MinecraftTaskNotification.ACTION_PAUSE -> activeControl?.pause()
            MinecraftTaskNotification.ACTION_RESUME -> activeControl?.resume()
            MinecraftTaskNotification.ACTION_STOP -> {
                activeControl?.stop()
                activeThread?.interrupt()
            }
            else -> {
                if (!active) startTask(
                    intent?.getStringExtra(EXTRA_INSTANCE).orEmpty(),
                    intent?.getStringExtra(EXTRA_VERSION).orEmpty()
                )
            }
        }
        refreshNotification()
        return START_NOT_STICKY
    }

    private fun startTask(instance: String, version: String) {
        if (instance.isBlank() || version.isBlank()) {
            stopSelf()
            return
        }

        val control = MinecraftTaskControl()
        activeControl = control
        active = true
        startForeground(
            MinecraftTaskNotification.NOTIFICATION_ID,
            MinecraftTaskNotification.build(this, "Cryonix Minecraft task", "Preparing…", 0, false)
        )

        activeThread = thread(name = "cryonix-minecraft-task") {
            try {
                val manager = MinecraftDownloadManager(this)
                val result = manager.install(instance, version, object : MinecraftDownloadManager.ProgressListener {
                    override fun onStage(stage: String, completed: Int, total: Int) {
                        val value = if (total <= 0) 0 else (completed * 100 / total).coerceIn(0, 100)
                        update(stage, value, control)
                    }

                    override fun onFile(name: String, completedBytes: Long, totalBytes: Long) {
                        val value = if (totalBytes <= 0L) 0 else
                            ((completedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
                        update("Downloading • " + name, value, control)
                    }
                }, control)
                update("Minecraft files installed • checking runtime", 100, control)
                val check = CryonixMojoEngine(this).preflight(result)
                if (check.ready) {
                    CryonixMojoEngine(this).launch(
                        result,
                        MinecraftSettingsStore(this).memoryMb,
                        MinecraftSettingsStore(this).profileName
                    ).onFailure { error -> update("Engine blocked • " + (error.message ?: "unknown error"), 100, control) }
                } else {
                    update("Installed • " + check.message, 100, control)
                }
            } catch (_: InterruptedException) {
                if (control.stopped) update("Task stopped", 0, control)
            } catch (error: Throwable) {
                update("Failed • " + (error.message ?: "unknown error"), 0, control)
            } finally {
                active = false
                activeControl = null
                activeThread = null
                NotificationManagerCompat.from(this).cancel(MinecraftTaskNotification.NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun update(text: String, progress: Int, control: MinecraftTaskControl) {
        if (!active || control.stopped) return
        lastText = text
        lastProgress = progress
        NotificationManagerCompat.from(this).notify(
            MinecraftTaskNotification.NOTIFICATION_ID,
            MinecraftTaskNotification.build(
                this,
                "Cryonix Minecraft task",
                text,
                progress,
                control.paused
            )
        )
    }

    private fun refreshNotification() {
        val control = activeControl ?: return
        if (control.paused) {
            NotificationManagerCompat.from(this).notify(
                MinecraftTaskNotification.NOTIFICATION_ID,
                MinecraftTaskNotification.build(this, "Cryonix Minecraft task", "Paused • " + lastText, lastProgress, true)
            )
        } else {
            update(lastText, lastProgress, control)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
