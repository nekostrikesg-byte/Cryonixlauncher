package com.cryonix.launcher.ui.home

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftDownloadManager
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.runtime.CryonixMojoEngine
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
        title.text = "Minecraft task"
        val instance = store.selectedInstanceName?.takeIf { it in store.instances } ?: store.instances.firstOrNull()
        val version = store.selectedVersionId ?: instance?.let { store.instanceConfig(it).versionId }
        details.text = "Instance: " + (instance ?: "none") +
            "\nVersion: " + (version ?: "not selected") +
            "\nProfile: " + store.profileName +
            "\nLoader: " + store.loader.lowercase() +
            "\nRenderer: " + store.renderer.name.lowercase() +
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
        status.text = "Starting background task…"
        percent.text = "0%"
        UiMotion.morphIn(panel)

        val intent = Intent(activity, com.cryonix.launcher.minecraft.MinecraftTaskService::class.java)
            .putExtra(com.cryonix.launcher.minecraft.MinecraftTaskService.EXTRA_INSTANCE, instance)
            .putExtra(com.cryonix.launcher.minecraft.MinecraftTaskService.EXTRA_VERSION, version)

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            activity.startForegroundService(intent)
        } else {
            activity.startService(intent)
        }
    }

    fun close(activity: Activity) {
        val panel = activity.findViewById<View>(R.id.task_manager_panel) ?: return
        UiMotion.morphOut(panel) { panel.visibility = View.GONE }
    }
}
