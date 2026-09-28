package com.cryonix.launcher.runtime

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * Phase 2 Android-Java runtime selector.
 *
 * Cryonix does not execute an arbitrary host JVM. A runtime is considered
 * usable only when an Android-compatible Java binary has been installed
 * under the private Cryonix runtime directory.
 *
 * Runtime packages are intentionally not committed to the source tree.
 */
class JavaRuntimeManager(private val context: Context) {

    data class RuntimeInfo(
        val id: String,
        val major: Int,
        val root: File,
        val javaExecutable: File
    )

    private val root = File(File(context.filesDir, "minecraft"), "runtimes")

    fun requiredJavaMajor(versionJson: File): Int {
        val json = JSONObject(versionJson.readText())
        return json.optJSONObject("javaVersion")?.optInt("majorVersion", 8) ?: 8
    }

    fun available(): List<RuntimeInfo> {
        if (!root.isDirectory) return emptyList()
        return root.listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .mapNotNull { parseRuntime(it) }
            .sortedBy { it.major }
    }

    fun select(requiredMajor: Int): RuntimeInfo? {
        val candidates = available()
        return candidates
            .filter { it.major == requiredMajor }
            .firstOrNull { isUsable(it) }
            ?: candidates
                .filter { it.major >= requiredMajor }
                .firstOrNull { isUsable(it) }
    }

    fun isReady(requiredMajor: Int): Boolean = select(requiredMajor) != null

    fun installRoot(id: String): File {
        val clean = id.trim().replace(Regex("[^A-Za-z0-9._-]"), "_")
        return File(root, clean.take(64).ifBlank { "runtime" })
    }

    private fun parseRuntime(dir: File): RuntimeInfo? {
        val meta = File(dir, "cryonix-runtime.json")
        val json = runCatching { JSONObject(meta.readText()) }.getOrNull() ?: return null
        val major = json.optInt("major", -1)
        val executable = File(json.optString("java", File(dir, "bin/java").absolutePath))
        if (major <= 0 || !executable.isFile) return null
        return RuntimeInfo(dir.name, major, dir, executable)
    }

    private fun isUsable(info: RuntimeInfo): Boolean {
        if (!info.javaExecutable.isFile) return false
        if (!info.javaExecutable.canExecute()) {
            info.javaExecutable.setExecutable(true, false)
        }
        val abi = Build.SUPPORTED_ABIS.firstOrNull().orEmpty().lowercase(Locale.US)
        val marker = File(info.root, "cryonix-runtime.json")
        val json = runCatching { JSONObject(marker.readText()) }.getOrNull()
        val supported = json?.optJSONArray("abis")
        if (supported == null || supported.length() == 0) return true
        for (i in 0 until supported.length()) {
            if (supported.optString(i).lowercase(Locale.US) == abi) return true
        }
        return false
    }
}
