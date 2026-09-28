package com.cryonix.launcher.ui.home

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.ui.UiMotion

object LaunchTaskManager {
    private val handler = Handler(Looper.getMainLooper())
    private var token = 0

    fun start(activity: Activity, store: MinecraftSettingsStore) {
        val panel = activity.findViewById<View>(R.id.task_manager_panel)
        val title = activity.findViewById<TextView>(R.id.task_manager_title)
        val status = activity.findViewById<TextView>(R.id.task_manager_status)
        val details = activity.findViewById<TextView>(R.id.task_manager_details)
        val progress = activity.findViewById<ProgressBar>(R.id.task_manager_progress)

        token++
        val run = token
        panel.visibility = View.VISIBLE
        UiMotion.morphIn(panel)

        val version = store.selectedVersionId ?: "not selected"
        val profile = store.profileName
        val loader = store.loader.lowercase()
        val renderer = store.renderer.name.lowercase()

        title.text = "Launch Tasks"
        status.text = "Preparing launch metadata…"
        details.text = "Version: $version\nProfile: $profile\nLoader: $loader\nRenderer: $renderer"
        progress.progress = 8

        val stages = listOf(
            "Reading instance metadata…" to 20,
            "Checking version manifest…" to 35,
            "Preparing asset downloads…" to 52,
            "Preparing libraries…" to 68,
            "Preparing runtime…" to 84,
            "Launch task ready" to 100
        )

        fun step(index: Int) {
            if (run != token) return
            if (index >= stages.size) return
            val (label, value) = stages[index]
            status.text = label
            progress.progress = value
            handler.postDelayed({ step(index + 1) }, 520)
        }
        handler.postDelayed({ step(0) }, 320)
    }

    fun close(activity: Activity) {
        token++
        val panel = activity.findViewById<View>(R.id.task_manager_panel) ?: return
        UiMotion.morphOut(panel) { panel.visibility = View.GONE }
    }
}