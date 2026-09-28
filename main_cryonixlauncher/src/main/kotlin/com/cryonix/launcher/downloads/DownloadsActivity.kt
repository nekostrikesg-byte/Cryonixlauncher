package com.cryonix.launcher.downloads

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.VersionManifestService
import kotlin.concurrent.thread

class DownloadsActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_downloads)
        status = findViewById(R.id.downloads_status)
        progress = findViewById(R.id.downloads_progress)
        findViewById<TextView>(R.id.downloads_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.downloads_refresh).setOnClickListener { refreshMetadata() }
    }

    private fun refreshMetadata() {
        status.text = "Fetching official Mojang version metadata…"
        progress.visibility = View.VISIBLE
        progress.isIndeterminate = true
        thread {
            val result = runCatching { VersionManifestService().fetchVersions() }
            runOnUiThread {
                progress.visibility = View.GONE
                result.onSuccess {
                    val releases = it.count { v -> v.stable }
                    status.text = "Completed • $releases release versions available"
                }.onFailure {
                    status.text = "Failed • " + (it.message ?: "network error")
                }
            }
        }
    }
}
