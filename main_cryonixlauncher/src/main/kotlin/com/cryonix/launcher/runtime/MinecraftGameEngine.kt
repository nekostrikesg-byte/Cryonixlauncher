package com.cryonix.launcher.runtime

import android.content.Context
import com.cryonix.launcher.core.PojavBridge
import com.cryonix.launcher.minecraft.MinecraftInstallResult
import com.cryonix.launcher.minecraft.PojavLaunchController
import java.io.File

/**
 * Stable engine boundary for the Cryonix launcher.
 *
 * The real Android Minecraft engine is the vendored Pojav/Mojo runtime:
 * Android OpenJDK + patched LWJGL/GLFW + gl4es/OSMesa/Vulkan translation layers
 * running in the `:game` process declared in the manifest. This boundary keeps
 * the launcher UI and the installer decoupled from that implementation.
 */
interface MinecraftGameEngine {
    val id: String

    fun preflight(install: MinecraftInstallResult): MinecraftRuntimeBridge.Preflight

    fun launch(
        install: MinecraftInstallResult,
        memoryMb: Int,
        profileName: String
    ): Result<Unit>
}

/** The Pojav/Mojo backend, exposed through the Cryonix engine boundary. */
class CryonixMojoEngine(private val context: Context) : MinecraftGameEngine {
    private val bridge = MinecraftRuntimeBridge(context)

    override val id: String = "pojav"

    override fun preflight(install: MinecraftInstallResult): MinecraftRuntimeBridge.Preflight {
        val required = PojavBridge.requiredJavaMajor(install.versionId)
        val runtime = PojavBridge.nearestRuntime(required)

        if (!PojavBridge.nativesAvailable(context)) {
            return MinecraftRuntimeBridge.Preflight(
                ready = false,
                message = "The Minecraft native libraries for this device are not packaged in the APK",
                requiredJavaMajor = required,
                runtime = null
            )
        }
        if (runtime == null && !PojavBridge.hasBundledRuntime(context)) {
            return MinecraftRuntimeBridge.Preflight(
                ready = false,
                message = "No Java $required runtime is installed for Minecraft " + install.versionId,
                requiredJavaMajor = required,
                runtime = null
            )
        }
        return MinecraftRuntimeBridge.Preflight(
            ready = true,
            message = "Pojav backend ready (Java " + (runtime?.major ?: required) + ")",
            requiredJavaMajor = required,
            runtime = runtime?.let {
                JavaRuntimeManager.RuntimeInfo(
                    id = it.name,
                    major = it.major,
                    root = it.root,
                    javaExecutable = File(it.root, "bin/java")
                )
            }
        )
    }

    override fun launch(
        install: MinecraftInstallResult,
        memoryMb: Int,
        profileName: String
    ): Result<Unit> {
        val check = preflight(install)
        if (!check.ready) {
            return Result.failure(IllegalStateException(check.message))
        }
        // Starting the game needs an Activity: the backend opens the `:game`
        // process and finishes the launcher process. Callers must go through
        // PojavLaunchController from a foreground activity.
        return Result.failure(
            IllegalStateException(
                "The Minecraft process must be started from the launcher screen " +
                    "(use the Play button, which drives PojavLaunchController)."
            )
        )
    }

    /** Convenience hook used by the UI to start the real backend pipeline. */
    fun launchFromActivity(activity: android.app.Activity): PojavLaunchController.Outcome =
        PojavLaunchController.start(
            activity,
            com.cryonix.launcher.minecraft.MinecraftSettingsStore(activity)
        )
}
