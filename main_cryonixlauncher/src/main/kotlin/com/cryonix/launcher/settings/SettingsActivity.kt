package com.cryonix.launcher.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.cryonix.launcher.MainActivity
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore

class SettingsActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_settings)

        val store = MinecraftSettingsStore(this)
        val rendererValue = findViewById<TextView>(R.id.settings_global_renderer_value)
        rendererValue.text = "Selected: " + when (store.renderer) {
            com.cryonix.launcher.minecraft.model.RendererProfile.Backend.SYSTEM -> "Kryton Wrapper"
            else -> store.renderer.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        }

        findViewById<LinearLayout>(R.id.settings_back).setOnClickListener {
            finish()
        }
        findViewById<LinearLayout>(R.id.settings_home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<android.widget.ImageButton>(R.id.settings_versions).setOnClickListener {
            startActivity(Intent(this, MinecraftActivity::class.java).putExtra(MinecraftActivity.EXTRA_SECTION, "versions"))
        }
        findViewById<android.widget.ImageButton>(R.id.settings_accounts).setOnClickListener {
            startActivity(Intent(this, MinecraftActivity::class.java).putExtra(MinecraftActivity.EXTRA_SECTION, "accounts"))
        }
        findViewById<android.widget.ImageButton>(R.id.settings_download).setOnClickListener {
            startActivity(Intent(this, MinecraftActivity::class.java).putExtra(MinecraftActivity.EXTRA_SECTION, "versions"))
        }
        findViewById<LinearLayout>(R.id.settings_global_renderer).setOnClickListener {
            val choices = arrayOf("Kryton Wrapper", "OpenGL", "LTW", "Holy GL4ES", "Mobile GLUES", "Vulkan")
            val current = if (store.renderer.name == "SYSTEM") 0 else choices.indexOfFirst {
                it.replace(" ", "_").uppercase() == store.renderer.name
            }.coerceAtLeast(0)
            android.app.AlertDialog.Builder(this)
                .setTitle("Global Renderer")
                .setSingleChoiceItems(choices, current) { dialog, which ->
                    store.renderer = when (which) {
                        0 -> com.cryonix.launcher.minecraft.model.RendererProfile.Backend.SYSTEM
                        1 -> com.cryonix.launcher.minecraft.model.RendererProfile.Backend.OPENGL
                        2 -> com.cryonix.launcher.minecraft.model.RendererProfile.Backend.LTW
                        3 -> com.cryonix.launcher.minecraft.model.RendererProfile.Backend.HOLY_GL4ES
                        4 -> com.cryonix.launcher.minecraft.model.RendererProfile.Backend.MOBILE_GLUES
                        else -> com.cryonix.launcher.minecraft.model.RendererProfile.Backend.VULKAN
                    }
                    rendererValue.text = "Selected: " + choices[which]
                    dialog.dismiss()
                }
                .show()
        }
    }
}
