package com.cryonix.launcher.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.cryonix.launcher.R
import com.cryonix.launcher.core.PojavBridge
import com.cryonix.launcher.runtime.PojavRuntimeInstaller
import com.cryonix.launcher.ui.UiMotion
import kotlin.concurrent.thread

/**
 * Android Java runtime manager.
 *
 * Minecraft on Android cannot use a desktop JVM: the game runs on an Android
 * OpenJDK build (JRE 8/17/21) launched by the Pojav/Mojo backend through
 * MultiRT. This screen installs, lists and removes those runtimes.
 */
class JavaRuntimeActivity : Activity() {
    private lateinit var list: LinearLayout
    private lateinit var catalog: LinearLayout
    private lateinit var status: TextView
    private var busy = false

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_java_runtime)
        list = findViewById(R.id.java_runtime_list)
        catalog = findViewById(R.id.java_runtime_catalog)
        status = findViewById(R.id.java_runtime_status)

        findViewById<View>(R.id.java_runtime_back).setOnClickListener { finish() }
        findViewById<View>(R.id.java_runtime_local).setOnClickListener { pickRuntimeFile() }

        PojavBridge.ensureInitialized(this)
        render()
        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val installed = PojavRuntimeInstaller.installedRuntimes()
        val default = PojavBridge.defaultRuntimeName()
        status.text = if (installed.isEmpty()) {
            "No Java runtime installed • Minecraft cannot start yet"
        } else {
            installed.size.toString() + " runtime" + (if (installed.size == 1) "" else "s") +
                " • default: " + default.ifBlank { "automatic" }
        }

        list.removeAllViews()
        installed.forEach { runtime ->
            val isDefault = runtime.name == default
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(8), dp(8), dp(8))
                setBackgroundResource(if (isDefault) R.drawable.bg_cryonix_button else R.drawable.bg_cryonix_row)
                isClickable = true
                setOnClickListener { selectDefault(runtime.name) }
            }
            val text = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(54), 1f)
                this.text = runtime.name + "\nJava " + runtime.major +
                    (runtime.arch?.let { " • " + it } ?: "") +
                    (if (isDefault) " • DEFAULT" else "")
                textSize = 12f
                setTextColor(getColor(if (isDefault) R.color.cryonix_button_text else R.color.cryonix_text))
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val remove = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
                this.text = "×"
                gravity = android.view.Gravity.CENTER
                textSize = 20f
                setTextColor(getColor(R.color.cryonix_text_secondary))
                setBackgroundResource(R.drawable.bg_cryonix_row)
                setOnClickListener { confirmRemove(runtime.name) }
            }
            row.addView(text)
            row.addView(remove)
            list.addView(row, LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(6) })
        }

        catalog.removeAllViews()
        PojavRuntimeInstaller.availableForDevice().forEach { pkg ->
            val isInstalled = PojavRuntimeInstaller.isInstalled(pkg)
            val row = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(6) }
                text = pkg.label + "\n" + if (isInstalled) "installed" else pkg.note()
                textSize = 11f
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dp(12), 0, dp(12), 0)
                setBackgroundResource(if (isInstalled) R.drawable.bg_cryonix_row else R.drawable.bg_cryonix_button)
                setTextColor(getColor(if (isInstalled) R.color.cryonix_text_secondary else R.color.cryonix_button_text))
                isClickable = !isInstalled
                if (!isInstalled) setOnClickListener { install(pkg) }
            }
            catalog.addView(row)
        }
        bindPressAnimations(list)
        bindPressAnimations(catalog)
    }

    private fun install(pkg: PojavRuntimeInstaller.RuntimePackage) {
        if (busy) return
        if (pkg.url.isBlank()) {
            AlertDialog.Builder(this)
                .setTitle(pkg.label)
                .setMessage(
                    "This runtime has no public download. Export it from the PojavLauncherTeam " +
                        "OpenJDK CI artifacts and install the .tar.xz with \"Install from file\"."
                )
                .setPositiveButton("OK", null)
                .show()
            return
        }
        busy = true
        status.text = "Downloading " + pkg.label + "…"
        thread(name = "cryonix-jre-install") {
            val result = PojavRuntimeInstaller.downloadAndInstall(this, pkg) { progress ->
                runOnUiThread {
                    val percent = if (progress.totalBytes > 0) {
                        (progress.downloadedBytes * 100 / progress.totalBytes).toInt()
                    } else 0
                    status.text = progress.stage + " • " + percent + "%"
                }
            }
            runOnUiThread {
                busy = false
                result.onSuccess {
                    Toast.makeText(this, pkg.label + " installed", Toast.LENGTH_LONG).show()
                }.onFailure {
                    Toast.makeText(this, "Install failed: " + (it.message ?: "unknown"), Toast.LENGTH_LONG).show()
                }
                render()
            }
        }
    }

    private fun pickRuntimeFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PICK_RUNTIME)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_RUNTIME || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        busy = true
        status.text = "Installing runtime from file…"
        thread(name = "cryonix-jre-file") {
            val result = runCatching {
                val name = "local-" + System.currentTimeMillis() / 1000
                contentResolver.openInputStream(uri).use { stream ->
                    requireNotNull(stream) { "The selected file could not be read" }
                    PojavRuntimeInstaller.installStream(name, stream)
                }
                PojavBridge.setDefaultRuntime(name)
            }
            runOnUiThread {
                busy = false
                result.onSuccess {
                    Toast.makeText(this, "Runtime installed", Toast.LENGTH_LONG).show()
                }.onFailure {
                    Toast.makeText(this, "Install failed: " + (it.message ?: "unknown"), Toast.LENGTH_LONG).show()
                }
                render()
            }
        }
    }

    private fun selectDefault(name: String) {
        PojavBridge.setDefaultRuntime(name)
        render()
    }

    private fun confirmRemove(name: String) {
        AlertDialog.Builder(this)
            .setTitle("Remove runtime?")
            .setMessage(name)
            .setPositiveButton("Remove") { _, _ ->
                PojavRuntimeInstaller.remove(name)
                render()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun PojavRuntimeInstaller.RuntimePackage.note(): String = when {
        approxBytes <= 0 -> "tap for instructions"
        else -> (approxBytes / (1024 * 1024)).toString() + " MB download"
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun bindPressAnimations(root: View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }

    private companion object {
        const val PICK_RUNTIME = 4201
    }
}
