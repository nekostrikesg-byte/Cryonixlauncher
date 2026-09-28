package com.cryonix.launcher.runtime

import android.content.Context
import com.cryonix.launcher.minecraft.MinecraftInstallResult

/**
 * Stable engine boundary for the Cryonix launcher.
 *
 * A real Android Minecraft engine must provide the Java runtime, custom
 * LWJGL/GLFW bridge, native renderer and game surface. Keeping this boundary
 * separate prevents the launcher UI and installer from being coupled to one
 * engine implementation.
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

class CryonixMojoEngine(context: Context) : MinecraftGameEngine {
    private val bridge = MinecraftRuntimeBridge(context)

    override val id: String = "mojo"

    override fun preflight(install: MinecraftInstallResult) =
        bridge.preflight(install)

    override fun launch(
        install: MinecraftInstallResult,
        memoryMb: Int,
        profileName: String
    ): Result<Unit> {
        val check = preflight(install)
        if (!check.ready) {
            return Result.failure(
                IllegalStateException(
                    check.message + ". Install an Android-compatible runtime and native engine first."
                )
            )
        }

        return Result.failure(
            IllegalStateException(
                "Android Java runtime is ready, but the Mojo/LWJGL game-surface engine is not bundled yet."
            )
        )
    }
}
