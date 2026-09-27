package com.cryonix.launcher.ui.settings

import android.app.AlertDialog
import android.app.Activity
import android.widget.TextView
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.model.RendererProfile

/**
 * Cryonix renderer settings interaction based on the Zalith 2+ settings-card
 * structure while using Cryonix's existing renderer registry/store.
 */
object ZalithSettingsScreen {
    fun showRendererPicker(
        activity: Activity,
        store: MinecraftSettingsStore,
        valueView: TextView
    ) {
        val choices = arrayOf(
            "Krypton Wrapper",
            "OpenGL",
            "LTW",
            "Holy GL4ES",
            "Mobile GLUES",
            "Vulkan"
        )
        val current = when (store.renderer) {
            RendererProfile.Backend.SYSTEM -> 0
            RendererProfile.Backend.OPENGL -> 1
            RendererProfile.Backend.LTW -> 2
            RendererProfile.Backend.HOLY_GL4ES -> 3
            RendererProfile.Backend.MOBILE_GLUES -> 4
            RendererProfile.Backend.VULKAN -> 5
        }

        AlertDialog.Builder(activity)
            .setTitle("Global Renderer")
            .setSingleChoiceItems(choices, current) { dialog, which ->
                store.renderer = when (which) {
                    0 -> RendererProfile.Backend.SYSTEM
                    1 -> RendererProfile.Backend.OPENGL
                    2 -> RendererProfile.Backend.LTW
                    3 -> RendererProfile.Backend.HOLY_GL4ES
                    4 -> RendererProfile.Backend.MOBILE_GLUES
                    else -> RendererProfile.Backend.VULKAN
                }
                valueView.text = "Selected: " + choices[which]
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
