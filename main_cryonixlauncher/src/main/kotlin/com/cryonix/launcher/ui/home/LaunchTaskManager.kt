package com.cryonix.launcher.ui.home

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftDownloadManager
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.ui.UiMotion
import kotlin.concurrent.thread

object LaunchTaskManager {
    fun start(activity: Activity, store: MinecraftSettingsStore) {
        val panel = activity.findViewById<View>(R.id.task_manager_panel)
        val title = activity.findViewById<TextView>(R.id.task_manager_title)
        val status = activity.findViewById<TextView>(R.id.task_manager_status)
        val details = activity.findViewById<TextView>(R.id.task_manager_details)
        val progress = activity.findViewById<ProgressBar>(R.id.task_manager_progress)
        val bottomProgress = activity.findViewById<ProgressBar>(R.id.task_manager_progress_bottom)
        val percent = activity.findViewById<TextView>(R.id.task_manager_percent)

        panel.visibility = View.VISIBLE
        title.text = "Minecraft install"
        val instance = store.instances.firstOrNull()
        val version = store.selectedVersionId
            ?: instance?.let { store.instanceConfig(it).versionId }
        val profile = store.profileName
        val renderer = store.renderer.name.lowercase()

        details.text = "Instance: " + (instance ?: "none") +
            "\nVersion: " + (version ?: "not selected") +
            "\nProfile: " + profile +
            "\nLoader: " + store.loader.lowercase() +
            "\nRenderer: " + renderer +
            "\nMemory: " + store.memoryMb + " MB"

        if (instance.isNullOrBlank()) {
            status.text = "Blocked • Create an instance first"
            progress.progress = 0
            bottomProgress.progress = 0
            percent.text = "0%"
            UiMotion.morphIn(panel)
            return
        }

        if (version.isNullOrBlank()) {
            status.text = "Blocked • Select a Minecraft version first"
            progress.progress = 0
            bottomProgress.progress = 0
            percent.text = "0%"
            UiMotion.morphIn(panel)
            return
        }

        progress.max = 100
        bottomProgress.max = 100
        status.text = "Checking installed Minecraft files…"
        UiMotion.morphIn(panel)

        thread {
            val manager = MinecraftDownloadManager(activity)
            val installed = manager.isInstalled(instance, version)

            if (installed) {
                activity.runOnUiThread {
                    title.text = "Minecraft files ready"
                    status.text = "Installed • Phase 1 complete for " + version
                    progress.progress = 100
                    bottomProgress.progress = 100
                    percent.text = "100%"
                }
                return@thread
            }

            runCatching {
                manager.install(instance, version, object : MinecraftDownloadManager.ProgressListener {
                    override fun onStage(stage: String, completed: Int, total: Int) {
                        val value = if (total <= 0) 0 else (completed * 100 / total).coerceIn(0, 100)
                        activity.runOnUiThread {
                            status.text = stage
                            progress.progress = value
                            bottomProgress.progress = value
                            percent.text = value.toString() + "%"
                        }
                    }

                    override fun onFile(name: String, completedBytes: Long, totalBytes: Long) {
                        val value = if (totalBytes <= 0L) 0 else
                            ((completedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
                        activity.runOnUiThread {
                            status.text = "Downloading • " + name
                            progress.progress = value
                            bottomProgress.progress = value
                            percent.text = value.toString() + "%"
                        }
                    }
                })
            }.onSuccess {
                activity.runOnUiThread {
                    title.text = "Minecraft files ready"
                    status.text = "Installed • " + it.libraryCount + " libraries • " +
                        it.assetCount + " assets"
                    progress.progress = 100
                    bottomProgress.progress = 100
                    percent.text = "100%"
                }
            }.onFailure {
                activity.runOnUiThread {
                    title.text = "Minecraft install failed"
                    status.text = "Failed • " + (it.message ?: "unknown error")
                    percent.text = "0%"
                }
            }
        }
    }

    fun close(activity: Activity) {
        val panel = activity.findViewById<View>(R.id.task_manager_panel) ?: return
        UiMotion.morphOut(panel) { panel.visibility = View.GONE }
    }
}
