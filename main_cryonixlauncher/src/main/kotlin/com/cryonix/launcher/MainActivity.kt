package com.cryonix.launcher

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.VersionManifestService
import com.cryonix.launcher.ui.home.HomeScreen
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var store: MinecraftSettingsStore

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_launcher)

        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2401)
        }

        store = MinecraftSettingsStore(this)

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
        HomeScreen.renderInstances(this, store)
    }

    private fun refreshMetadata() {
        thread {
            val result = runCatching {
                VersionManifestService().fetchVersions()
                    .firstOrNull { it.stable }
                    ?: error("No release versions were returned")
            }

            runOnUiThread {
                result.onSuccess {
                    store.selectedVersionId = it.id
                    renderState()
                }.onFailure {
                }
            }
        }
    }
}
