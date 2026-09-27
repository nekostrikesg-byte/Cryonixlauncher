package com.cryonix.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.cryonix.launcher.minecraft.MinecraftActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.VersionManifestService
import com.cryonix.launcher.settings.SettingsActivity
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var versionText: TextView
    private lateinit var profileText: TextView
    private lateinit var statusText: TextView
    private lateinit var store: MinecraftSettingsStore

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_launcher)

        store = MinecraftSettingsStore(this)
        versionText = findViewById(R.id.home_version)
        profileText = findViewById(R.id.home_profile)
        statusText = findViewById(R.id.home_status)

        findViewById<Button>(R.id.home_launch).setOnClickListener {
            openMinecraft(launchNow = true)
        }
        findViewById<Button>(R.id.home_versions).setOnClickListener {
            openMinecraft("versions")
        }
        findViewById<Button>(R.id.home_accounts).setOnClickListener {
            openMinecraft("accounts")
        }
        findViewById<Button>(R.id.home_loader).setOnClickListener {
            openMinecraft("loader")
        }
        findViewById<Button>(R.id.home_renderer).setOnClickListener {
            openMinecraft("renderer")
        }
        findViewById<Button>(R.id.home_refresh).setOnClickListener {
            refreshMetadata()
        }
        findViewById<android.widget.ImageButton>(R.id.home_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        renderState()
        if (store.autoRefresh) refreshMetadata()
    }

    override fun onResume() {
        super.onResume()
        if (::store.isInitialized) renderState()
    }

    private fun openMinecraft(section: String? = null, launchNow: Boolean = false) {
        startActivity(
            Intent(this, MinecraftActivity::class.java).apply {
                section?.let { putExtra(MinecraftActivity.EXTRA_SECTION, it) }
                putExtra(MinecraftActivity.EXTRA_LAUNCH_NOW, launchNow)
            }
        )
    }

    private fun renderState() {
        versionText.text = "Version: " + (store.selectedVersionId ?: "not selected")
        profileText.text = "Profile: " + store.profileName
    }

    private fun refreshMetadata() {
        statusText.text = "Refreshing official Minecraft metadata…"
        findViewById<Button>(R.id.home_refresh).isEnabled = false

        thread {
            val result = runCatching {
                VersionManifestService().fetchVersions()
                    .firstOrNull { it.stable }
                    ?: error("No release versions were returned")
            }

            runOnUiThread {
                findViewById<Button>(R.id.home_refresh).isEnabled = true
                result.onSuccess {
                    store.selectedVersionId = it.id
                    statusText.text = "Ready · latest release: " + it.id
                    renderState()
                }.onFailure {
                    statusText.text = "Ready · metadata refresh failed"
                }
            }
        }
    }
}
