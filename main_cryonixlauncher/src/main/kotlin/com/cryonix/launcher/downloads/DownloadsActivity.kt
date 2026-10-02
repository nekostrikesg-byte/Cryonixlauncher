package com.cryonix.launcher.downloads

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.cryonix.launcher.R
import com.kdt.mcgui.ProgressLayout
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.ProgressListener

/** Read-only task monitor for the existing Pojav download, auth and runtime tasks. */
class DownloadsActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var tasks: LinearLayout
    private data class TaskRow(val view: View, val title: TextView, val progress: ProgressBar)
    private val rows = linkedMapOf<String, TaskRow>()
    private val listeners = linkedMapOf<String, ProgressListener>()
    private val taskKeys = listOf(
        ProgressLayout.DOWNLOAD_MINECRAFT,
        ProgressLayout.UNPACK_RUNTIME,
        ProgressLayout.INSTALL_MODPACK,
        ProgressLayout.AUTHENTICATE_MICROSOFT,
        ProgressLayout.DOWNLOAD_VERSION_LIST,
        ProgressLayout.EXTRACT_COMPONENTS,
        ProgressLayout.EXTRACT_SINGLE_FILES
    )

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_downloads)
        status = findViewById(R.id.downloads_status)
        tasks = findViewById(R.id.downloads_tasks)
        findViewById<View>(R.id.downloads_back).setOnClickListener { finish() }
        findViewById<View>(R.id.downloads_refresh).setOnClickListener { renderEmptyStateIfNeeded() }
        renderEmptyStateIfNeeded()
    }

    override fun onStart() {
        super.onStart()
        taskKeys.forEach(::observeTask)
        updateStatus()
    }

    override fun onStop() {
        listeners.forEach { (key, listener) -> ProgressKeeper.removeListener(key, listener) }
        listeners.clear()
        super.onStop()
    }

    private fun observeTask(key: String) {
        val listener = object : ProgressListener {
            override fun onProgressStarted() = runOnUiThread { createTaskRow(key) }

            override fun onProgressUpdated(progress: Int, resid: Int, vararg message: Any?) {
                runOnUiThread {
                    val row = rows[key] ?: createTaskRow(key)
                    val label = if (resid > 0) runCatching { getString(resid, *message) }.getOrNull()
                        else message.firstOrNull()?.toString()
                    row.title.text = label ?: taskTitle(key)
                    row.progress.isIndeterminate = progress < 0
                    if (progress >= 0) row.progress.progress = progress.coerceIn(0, 100)
                    updateStatus()
                }
            }

            override fun onProgressEnded() = runOnUiThread {
                rows.remove(key)?.let { tasks.removeView(it.view) }
                updateStatus()
            }
        }
        listeners[key] = listener
        ProgressKeeper.addListener(key, listener)
    }

    private fun createTaskRow(key: String): TaskRow {
        rows[key]?.let { return it }
        if (rows.isEmpty()) tasks.removeAllViews()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setBackgroundResource(R.drawable.bg_cryonix_row)
        }
        val title = TextView(this).apply {
            text = taskTitle(key)
            textSize = 10f
            setTextColor(getColor(R.color.cryonix_text))
        }
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            isIndeterminate = true
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(5)).apply {
                topMargin = dp(7)
            }
        }
        row.addView(title)
        row.addView(progress)
        tasks.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(6)
        })
        val taskRow = TaskRow(row, title, progress)
        rows[key] = taskRow
        updateStatus()
        return taskRow
    }

    private fun taskTitle(key: String): String = when (key) {
        ProgressLayout.DOWNLOAD_MINECRAFT -> "Minecraft download"
        ProgressLayout.UNPACK_RUNTIME -> "Java runtime setup"
        ProgressLayout.INSTALL_MODPACK -> "Mod or modpack installation"
        ProgressLayout.AUTHENTICATE_MICROSOFT -> "Microsoft authentication"
        ProgressLayout.DOWNLOAD_VERSION_LIST -> "Version list update"
        ProgressLayout.EXTRACT_COMPONENTS -> "Engine component extraction"
        ProgressLayout.EXTRACT_SINGLE_FILES -> "Engine file extraction"
        else -> key
    }

    private fun updateStatus() {
        if (!::status.isInitialized) return
        val count = ProgressKeeper.getTaskCount()
        status.text = if (count == 0) "No active tasks · Downloads appear here while the engine is working"
            else "$count active engine task${if (count == 1) "" else "s"}"
        renderEmptyStateIfNeeded()
    }

    private fun renderEmptyStateIfNeeded() {
        if (!::tasks.isInitialized || rows.isNotEmpty()) return
        tasks.removeAllViews()
        val empty = TextView(this).apply {
            text = "No active downloads or tasks.\n\nChoose a Minecraft version or start an install from the Home and Game screens."
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.cryonix_text_secondary))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(180))
        }
        tasks.addView(empty)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
