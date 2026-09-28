package com.cryonix.launcher.runtime

import android.content.Context
import com.cryonix.launcher.minecraft.MinecraftInstallResult
import org.json.JSONObject
import java.io.File

/**
 * Phase 2 game-process bridge.
 *
 * The bridge owns the boundary between Cryonix's installer and an
 * Android-compatible Java/native Minecraft engine. It deliberately refuses
 * to pretend that downloaded JARs alone are executable on Android.
 */
class MinecraftRuntimeBridge(private val context: Context) {

    data class Preflight(
        val ready: Boolean,
        val message: String,
        val requiredJavaMajor: Int,
        val runtime: JavaRuntimeManager.RuntimeInfo?
    )

    private val runtimes = JavaRuntimeManager(context)

    fun preflight(install: MinecraftInstallResult): Preflight {
        val required = runCatching {
            runtimes.requiredJavaMajor(install.versionJson)
        }.getOrDefault(8)

        val runtime = runtimes.select(required)
            ?: return Preflight(
                ready = false,
                message = "Android Java $required runtime is not installed",
                requiredJavaMajor = required,
                runtime = null
            )

        if (!install.clientJar.isFile) {
            return Preflight(false, "Minecraft client JAR is missing", required, runtime)
        }

        return Preflight(
            ready = true,
            message = "Android Java $required runtime is ready",
            requiredJavaMajor = required,
            runtime = runtime
        )
    }

    fun runtimeDirectory(): File =
        File(File(context.filesDir, "minecraft"), "runtimes")

    fun describe(): String {
        val runtimesFound = runtimes.available()
        return if (runtimesFound.isEmpty()) {
            "No Android-compatible Java runtime installed"
        } else {
            runtimesFound.joinToString { "Java " + it.major + " (" + it.id + ")" }
        }
    }

    fun validateRuntimeMetadata(runtimeDir: File): Boolean {
        val file = File(runtimeDir, "cryonix-runtime.json")
        return runCatching {
            val json = JSONObject(file.readText())
            json.optInt("major", -1) > 0 &&
                File(json.optString("java")).isFile
        }.getOrDefault(false)
    }
}
