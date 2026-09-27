package com.cryonix.launcher.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.cryonix.launcher.MainActivity
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.model.RendererProfile

class SettingsActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_settings)

        val store = MinecraftSettingsStore(this)
        val rendererValue = findViewById<TextView>(R.id.settings_global_renderer_value)
        rendererValue.text = "Selected: " + rendererLabel(store.renderer)

        findViewById<ImageButton>(R.id.settings_back).setOnClickListener {
            finish()
        }
        findViewById<ImageButton>(R.id.settings_home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.settings_versions).setOnClickListener {
            startActivity(
                Intent(this, MinecraftActivity::class.java)
                    .putExtra(MinecraftActivity.EXTRA_SECTION, "versions")
            )
        }
        findViewById<ImageButton>(R.id.settings_accounts).setOnClickListener {
            startActivity(
                Intent(this, MinecraftActivity::class.java)
                    .putExtra(MinecraftActivity.EXTRA_SECTION, "accounts")
            )
        }
        findViewById<ImageButton>(R.id.settings_download).setOnClickListener {
            startActivity(
                Intent(this, MinecraftActivity::class.java)
                    .putExtra(MinecraftActivity.EXTRA_SECTION, "versions")
            )
        }

        findViewById<LinearLayout>(R.id.settings_global_renderer).setOnClickListener {
            showRendererPicker(store, rendererValue)
        }
    }

    private fun showRendererPicker(
        store: MinecraftSettingsStore,
        valueView: TextView
    ) {
        val choices = arrayOf(
            "Kryton Wrapper",
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

        AlertDialog.Builder(this)
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

    private fun rendererLabel(renderer: RendererProfile.Backend): String =
        when (renderer) {
            RendererProfile.Backend.SYSTEM -> "Kryton Wrapper"
            RendererProfile.Backend.OPENGL -> "OpenGL"
            RendererProfile.Backend.LTW -> "LTW"
            RendererProfile.Backend.HOLY_GL4ES -> "Holy GL4ES"
            RendererProfile.Backend.MOBILE_GLUES -> "Mobile GLUES"
            RendererProfile.Backend.VULKAN -> "Vulkan"
        }
}
