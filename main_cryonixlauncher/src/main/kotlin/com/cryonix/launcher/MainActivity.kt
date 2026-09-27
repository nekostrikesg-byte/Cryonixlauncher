package com.cryonix.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.cryonix.launcher.minecraft.MinecraftActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.VersionManifestService
import com.cryonix.launcher.settings.SettingsActivity
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var versionText: TextView
    private lateinit var statusText: TextView
    private lateinit var store: MinecraftSettingsStore

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_launcher)

        store = MinecraftSettingsStore(this)
        versionText = findViewById(R.id.home_version)
        statusText = findViewById(R.id.home_status)

        findViewById<TextView>(R.id.home_launch).setOnClickListener {
            openMinecraft(launchNow = true)
        }
        findViewById<ImageButton>(R.id.home_versions).setOnClickListener {
            openMinecraft("versions")
        }
        findViewById<ImageButton>(R.id.home_accounts).setOnClickListener {
            openMinecraft("accounts")
        }
        findViewById<ImageButton>(R.id.home_refresh).setOnClickListener {
            refreshMetadata()
        }
        findViewById<ImageButton>(R.id.home_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<TextView>(R.id.home_add_account).setOnClickListener {
            openMinecraft("accounts")
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
        versionText.text = if (store.selectedVersionId == null) {
            "No installed versions"
        } else {
            store.selectedVersionId
        }
    }

    private fun refreshMetadata() {
        statusText.text = "Refreshing"
        thread {
            val result = runCatching {
                VersionManifestService().fetchVersions()
                    .firstOrNull { it.stable }
                    ?: error("No release versions were returned")
            }

            runOnUiThread {
                result.onSuccess {
                    store.selectedVersionId = it.id
                    statusText.text = "Ready"
                    renderState()
                }.onFailure {
                    statusText.text = "Ready"
                }
            }
        }
    }
}
