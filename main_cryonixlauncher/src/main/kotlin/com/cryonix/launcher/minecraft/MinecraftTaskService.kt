package com.cryonix.launcher.minecraft

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.cryonix.launcher.backends.PojavBackend
import com.cryonix.launcher.core.PojavBridge
import kotlin.concurrent.thread

/**
 * Background preparation for the Pojav/Mojo backend.
 *
 * The service only performs work that does not need a foreground activity:
 * unpacking the backend components (caciocavallo, LWJGL3 classes, security
 * files) and reporting which prerequisite is still missing. Downloading
 * Minecraft and starting the game requires an Activity — that is done by
 * [PojavLaunchController] from the Play button, exactly like the upstream
 * backend does it.
 */
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
        val control = MinecraftTaskControl()
        activeControl = control
        active = true
        startForeground(
            MinecraftTaskNotification.NOTIFICATION_ID,
            MinecraftTaskNotification.build(this, "Cryonix backend", "Preparing…", 0, false)
        )

        activeThread = thread(name = "cryonix-backend-prepare") {
            try {
                update("Checking the Android storage root", 10, control)
                val ready = PojavBridge.ensureInitialized(this)

                update("Unpacking backend components", 40, control)
                if (ready) PojavBridge.unpackBackendFiles(this)

                update("Checking native libraries and Java runtime", 70, control)
                val store = MinecraftSettingsStore(this)
                val status = PojavBackend.status(this, store)

                val text = when {
                    !ready -> "Backend storage is not available"
                    instance.isNotBlank() -> status.message + " • " + instance
                    else -> status.message
                }
                val progress = if (status.gameFilesInstalled) 100 else 70
                update(text, progress, control)
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
                "Cryonix backend",
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
                MinecraftTaskNotification.build(this, "Cryonix backend", "Paused • " + lastText, lastProgress, true)
            )
        } else {
            update(lastText, lastProgress, control)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
