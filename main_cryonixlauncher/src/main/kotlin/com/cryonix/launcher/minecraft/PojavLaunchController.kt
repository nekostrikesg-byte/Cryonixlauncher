package com.cryonix.launcher.minecraft

import android.app.Activity
import com.cryonix.launcher.core.PojavBridge

/**
 * Drives a real Minecraft start through the vendored Pojav/Mojo backend.
 *
 * The backend owns the launch pipeline (hashed downloads, native extraction,
 * Java runtime selection, the `:game` process). Cryonix only decides *what* to
 * launch and makes sure every prerequisite exists before the pipeline starts.
 * When something is missing the controller reports it instead of pretending the
 * game started.
 */
object PojavLaunchController {

    sealed class Outcome {
        /** The backend pipeline was started; the game activity will follow. */
        data class Launching(val versionId: String, val instance: String) : Outcome()

        /** Something has to be installed/selected first. */
        data class Blocked(val reason: BlockedReason, val message: String) : Outcome()
    }

    enum class BlockedReason {
        NO_INSTANCE,
        NO_VERSION,
        NO_NATIVE_LIBS,
        NO_RUNTIME,
        NO_STORAGE
    }

    fun start(activity: Activity, store: MinecraftSettingsStore): Outcome {
        val instance = store.selectedInstanceName?.takeIf { it in store.instances }
            ?: store.instances.firstOrNull()
        if (instance.isNullOrBlank()) {
            return Outcome.Blocked(BlockedReason.NO_INSTANCE, "Create an instance before launching Minecraft")
        }

        val versionId = versionFor(store, instance)
        if (versionId.isNullOrBlank()) {
            return Outcome.Blocked(
                BlockedReason.NO_VERSION,
                "Select a Minecraft version for instance \"" + instance + "\" first"
            )
        }

        if (!PojavBridge.ensureInitialized(activity)) {
            return Outcome.Blocked(BlockedReason.NO_STORAGE, "The backend game directory is not writable")
        }

        if (!PojavBridge.nativesAvailable(activity)) {
            return Outcome.Blocked(
                BlockedReason.NO_NATIVE_LIBS,
                "This APK does not contain the Minecraft native libraries for this device"
            )
        }

        // The backend runs the game as a local (offline) profile unless the user
        // signed in with a Microsoft account, so an account file must exist.
        ensureAccount(activity, store, versionId)

        // LWJGL3 classes, caciocavallo and the security sandbox are shipped as
        // assets and have to be unpacked before the game process starts.
        runCatching { PojavBridge.unpackBackendFiles(activity) }

        val config = store.instanceConfig(instance)
        val rendererId = PojavBridge.rendererId(activity, store.renderer)
        val requiredJava = PojavBridge.requiredJavaMajor(versionId)
        var runtime = PojavBridge.nearestRuntime(requiredJava)
        if (runtime == null) {
            // A runtime shipped inside the APK can still be installed by the
            // backend pipeline itself.
            if (PojavBridge.hasBundledRuntime(activity)) {
                PojavBridge.unpackBundledRuntime(activity)
                runtime = PojavBridge.nearestRuntime(requiredJava)
            }
        }

        PojavBridge.applyPreferences(
            rendererId = rendererId,
            memoryMb = config.memoryMb,
            resolutionPercent = resolutionPercent(config.resolutionRule),
            jvmArgs = null
        )

        if (runtime == null && !PojavBridge.hasBundledRuntime(activity)) {
            return Outcome.Blocked(
                BlockedReason.NO_RUNTIME,
                "Minecraft " + versionId + " needs a Java " + requiredJava +
                    " runtime. Install one in Settings \u2192 Java Runtime first."
            )
        }

        PojavBridge.ensureProfile(
            instanceName = instance,
            versionId = versionId,
            rendererId = rendererId,
            runtimeName = runtime?.name,
            memoryMb = config.memoryMb,
            jvmArgs = null
        )

        PojavBridge.prepareAndLaunch(activity, versionId)
        return Outcome.Launching(versionId, instance)
    }

    /** The version the backend profile will launch for this instance. */
    fun versionFor(store: MinecraftSettingsStore, instance: String): String? =
        store.selectedVersionId ?: store.instanceConfig(instance).versionId

    private fun ensureAccount(activity: Activity, store: MinecraftSettingsStore, versionId: String) {
        val wanted = store.profileName
        val accounts = PojavBridge.accounts(activity)
        if (accounts.none { it.name == wanted }) {
            PojavBridge.writeOfflineAccount(activity, wanted, versionId)
        }
        if (PojavBridge.currentAccountName(activity) != wanted) {
            PojavBridge.activateAccount(activity, wanted)
        }
    }

    /** "Percentage · 100%" (and free-form rules) to the backend resolution ratio. */
    fun resolutionPercent(rule: String): Int {
        val digits = Regex("(\\d{2,3})").find(rule)?.groupValues?.get(1)?.toIntOrNull() ?: 100
        return digits.coerceIn(25, 100)
    }
}
