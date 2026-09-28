package com.cryonix.launcher.ui.home

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.ui.UiMotion

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
        title.text = "Launch preflight"
        val version = store.selectedVersionId ?: store.instances.firstOrNull()?.let { store.instanceConfig(it).versionId }
        val profile = store.profileName
        val renderer = store.renderer.name.lowercase()
        details.text = "Version: " + (version ?: "not selected") +
            "\nProfile: " + profile +
            "\nLoader: " + store.loader.lowercase() +
            "\nRenderer: " + renderer +
            "\nMemory: " + store.memoryMb + " MB"

        if (version.isNullOrBlank()) {
            status.text = "Blocked • Select a Minecraft version first"
            progress.progress = 0
            bottomProgress.progress = 0
            percent.text = "0%"
        } else {
            status.text = "Ready for launch adapter"
            progress.progress = 100
            bottomProgress.progress = 100
            percent.text = "100%"
        }
        UiMotion.morphIn(panel)
    }

    fun close(activity: Activity) {
        val panel = activity.findViewById<View>(R.id.task_manager_panel) ?: return
        UiMotion.morphOut(panel) { panel.visibility = View.GONE }
    }
}
