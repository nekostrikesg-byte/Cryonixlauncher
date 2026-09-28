package com.cryonix.launcher

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.cryonix.launcher.minecraft.MinecraftActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.VersionManifestService
import com.cryonix.launcher.settings.SettingsActivity
import com.cryonix.launcher.ui.home.HomeScreen
import com.cryonix.launcher.ui.UiMotion
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

        UiMotion.morphIn(findViewById(android.R.id.content))

        HomeScreen.bind(
            activity = this,
            store = store,
            refresh = ::refreshMetadata,
            render = ::renderState
        )
    }

    override fun onResume() {
        super.onResume()
        if (::store.isInitialized) renderState()
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
