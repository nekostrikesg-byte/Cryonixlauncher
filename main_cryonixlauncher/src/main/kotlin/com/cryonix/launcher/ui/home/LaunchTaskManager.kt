package com.cryonix.launcher.ui.home

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.PojavLaunchController
import com.cryonix.launcher.settings.JavaRuntimeActivity
import com.cryonix.launcher.ui.UiMotion

/**
 * Play button flow.
 *
 * Everything that starts Minecraft goes through the vendored Pojav backend:
 * the backend downloads/verifies the version, extracts its natives, picks the
 * Java runtime and then starts the game in its own `:game` process. The panel
 * below the button reports what the backend is doing — and, when a
 * prerequisite is missing, exactly which one.
 */
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
        title.text = "Minecraft"

        val instance = store.selectedInstanceName?.takeIf { it in store.instances }
            ?: store.instances.firstOrNull()
        val version = instance?.let { PojavLaunchController.versionFor(store, it) }

        details.text = "Instance: " + (instance ?: "none") +
            "\nVersion: " + (version ?: "not selected") +
            "\nProfile: " + store.profileName +
            "\nRenderer: " + store.renderer.name.lowercase() +
            "\nMemory: " + store.memoryMb + " MB"

        progress.max = 100
        bottomProgress.max = 100
        UiMotion.morphIn(panel)

        when (val outcome = PojavLaunchController.start(activity, store)) {
            is PojavLaunchController.Outcome.Launching -> {
                status.text = "Starting Minecraft " + outcome.versionId + "…"
                progress.progress = 100
                bottomProgress.progress = 100
                percent.text = "100%"
                details.text = details.text.toString() +
                    "\n\nBackend: downloading/verifying game files, then the game window opens."
            }

            is PojavLaunchController.Outcome.Blocked -> {
                status.text = "Blocked • " + outcome.message
                progress.progress = 0
                bottomProgress.progress = 0
                percent.text = "0%"
                if (outcome.reason == PojavLaunchController.BlockedReason.NO_RUNTIME) {
                    details.text = details.text.toString() +
                        "\n\nOpen the Java runtime manager to install a runtime."
                    activity.findViewById<View>(R.id.task_manager_title).setOnClickListener {
                        activity.startActivity(Intent(activity, JavaRuntimeActivity::class.java))
                    }
                    status.setOnClickListener {
                        activity.startActivity(Intent(activity, JavaRuntimeActivity::class.java))
                    }
                }
            }
        }
    }

    fun close(activity: Activity) {
        val panel = activity.findViewById<View>(R.id.task_manager_panel) ?: return
        UiMotion.morphOut(panel) { panel.visibility = View.GONE }
    }
}
