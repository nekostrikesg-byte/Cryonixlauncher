package com.cryonix.launcher.downloads

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftDownloadManager
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.VersionManifestService
import kotlin.concurrent.thread

class DownloadsActivity : Activity() {
    private lateinit var store: MinecraftSettingsStore
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var install: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_downloads)
        store = MinecraftSettingsStore(this)
        status = findViewById(R.id.downloads_status)
        progress = findViewById(R.id.downloads_progress)
        install = findViewById(R.id.downloads_install)

        findViewById<TextView>(R.id.downloads_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.downloads_refresh).setOnClickListener { refreshMetadata() }
        install.setOnClickListener { installSelectedVersion() }
    }

    private fun refreshMetadata() {
        status.text = "Fetching official Mojang version metadata…"
        progress.visibility = View.VISIBLE
        progress.isIndeterminate = true
        thread {
            val result = runCatching { VersionManifestService().fetchVersions() }
            runOnUiThread {
                progress.isIndeterminate = false
                progress.visibility = View.GONE
                result.onSuccess {
                    val releases = it.count { v -> v.stable }
                    status.text = "Completed • " + releases + " release versions available"
                }.onFailure {
                    status.text = "Failed • " + (it.message ?: "network error")
                }
            }
        }
    }

    private fun installSelectedVersion() {
        val instance = store.instances.firstOrNull()
        val version = store.selectedVersionId
            ?: instance?.let { store.instanceConfig(it).versionId }

        if (instance.isNullOrBlank()) {
            status.text = "Create an instance first"
            return
        }
        if (version.isNullOrBlank()) {
            status.text = "Select a Minecraft version first"
            return
        }

        install.isEnabled = false
        progress.visibility = View.VISIBLE
        progress.isIndeterminate = false
        progress.max = 100
        status.text = "Preparing " + version + "…"

        thread {
            runCatching {
                MinecraftDownloadManager(this).install(
                    instance,
                    version,
                    object : MinecraftDownloadManager.ProgressListener {
                        override fun onStage(stage: String, completed: Int, total: Int) {
                            val value = if (total <= 0) 0 else
                                (completed * 100 / total).coerceIn(0, 100)
                            runOnUiThread {
                                status.text = stage
                                progress.progress = value
                            }
                        }

                        override fun onFile(name: String, completedBytes: Long, totalBytes: Long) {
                            if (totalBytes <= 0L) return
                            val value = ((completedBytes * 100L) / totalBytes)
                                .toInt().coerceIn(0, 100)
                            runOnUiThread {
                                status.text = "Downloading • " + name
                                progress.progress = value
                            }
                        }
                    }
                )
            }.onSuccess {
                runOnUiThread {
                    progress.progress = 100
                    status.text = "Installed • " + it.libraryCount +
                        " libraries • " + it.assetCount + " assets"
                    install.isEnabled = true
                }
            }.onFailure {
                runOnUiThread {
                    install.isEnabled = true
                    status.text = "Failed • " + (it.message ?: "download error")
                }
            }
        }
    }
}
