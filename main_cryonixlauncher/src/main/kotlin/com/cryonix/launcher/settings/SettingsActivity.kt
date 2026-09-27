package com.cryonix.launcher.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.view.View
import android.widget.TextView
import com.cryonix.launcher.MainActivity
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.model.RendererProfile
import com.cryonix.launcher.ui.settings.ZalithSettingsScreen

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

        findViewById<View>(R.id.settings_global_renderer).setOnClickListener {
            showRendererPicker(store, rendererValue)
        }
    }

    private fun showRendererPicker(
        store: MinecraftSettingsStore,
        valueView: TextView
    ) = ZalithSettingsScreen.showRendererPicker(this, store, valueView)

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
