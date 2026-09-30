package com.cryonix.launcher.backends

import android.app.Activity
import android.content.Context
import com.cryonix.launcher.core.PojavBridge
import com.cryonix.launcher.minecraft.LaunchRequest
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.model.RendererProfile
import java.io.File

/**
 * The Pojav/Mojo backend.
 *
 * This is the backend that actually runs Minecraft: the vendored Pojav runtime
 * (Android OpenJDK + patched LWJGL/GLFW + OpenGL/Vulkan translation layer) is
 * compiled into the app and started in the `:game` process declared in the
 * manifest. Cryonix's Kotlin layer owns the UI, instances, downloads and
 * settings; this object is the boundary between the two.
 */
object PojavBackend : CryonixBackend {

    override val id: String = "pojav"
    override val displayName: String = "Pojav / Mojo Android runtime"
    override val sourceProject: String = "PojavLauncherTeam/PojavLauncher"

    enum class Availability {
        /** Everything needed to start the game is installed. */
        READY,

        /** Game files have to be downloaded before the first launch. */
        NEEDS_GAME_FILES,

        /** No Android-compatible Java runtime is installed. */
        NEEDS_RUNTIME,

        /** The ABI-specific native libraries are missing from the APK. */
        UNSUPPORTED_DEVICE,

        /** The backend could not use the game directory. */
        NO_STORAGE
    }

    data class Status(
        val availability: Availability,
        val message: String,
        val versionId: String,
        val instanceName: String,
        val javaMajor: Int,
        val runtimeName: String?,
        val account: String,
        val rendererId: String,
        val rendererName: String,
        val nativesInstalled: Boolean,
        val gameFilesInstalled: Boolean,
        val storageRoot: File?
    ) {
        val ready: Boolean get() = availability == Availability.READY
    }

    /** Current state of the backend for the selected instance. */
    fun status(context: Context, store: MinecraftSettingsStore): Status {
        val instance = store.selectedInstanceName?.takeIf { it in store.instances }
            ?: store.instances.firstOrNull().orEmpty()
        val versionId = selectedVersion(store, instance).orEmpty()
        val natives = PojavBridge.nativesAvailable(context)
        val rendererId = PojavBridge.rendererId(context, store.renderer)
        val rendererName = PojavBridge.rendererDisplayName(context)
        val account = PojavBridge.currentAccountName(context)

        if (!PojavBridge.ensureInitialized(context)) {
            return Status(
                Availability.NO_STORAGE,
                "The backend game directory is not available yet",
                versionId, instance, 0, null, account, rendererId, rendererName,
                natives, false, null
            )
        }

        val requiredJava = if (versionId.isBlank()) 0 else PojavBridge.requiredJavaMajor(versionId)
        val runtime = if (requiredJava > 0) PojavBridge.nearestRuntime(requiredJava) else null
        val filesInstalled = versionId.isNotBlank() && PojavBridge.gameFilesReady(versionId)

        val (availability, message) = when {
            !natives -> Availability.UNSUPPORTED_DEVICE to
                "No Minecraft natives for this device ABI are packaged in the APK"
            versionId.isBlank() -> Availability.NEEDS_GAME_FILES to
                "Select a Minecraft version for this instance first"
            !filesInstalled -> Availability.NEEDS_GAME_FILES to
                "Minecraft $versionId has to be downloaded before the first launch"
            runtime == null -> Availability.NEEDS_RUNTIME to
                "Java $requiredJava runtime is not installed (required by Minecraft $versionId)"
            else -> Availability.READY to
                "Ready to launch Minecraft $versionId with Java runtime " + runtime.name
        }

        return Status(
            availability = availability,
            message = message,
            versionId = versionId,
            instanceName = instance,
            javaMajor = requiredJava,
            runtimeName = runtime?.name,
            account = account,
            rendererId = rendererId,
            rendererName = rendererName,
            nativesInstalled = natives,
            gameFilesInstalled = filesInstalled,
            storageRoot = runCatching { File(net.kdt.pojavlaunch.Tools.DIR_GAME_HOME) }.getOrNull()
        )
    }

    override fun prepare(context: Context, request: LaunchRequest): BackendLaunch {
        val instance = request.instanceName.ifBlank { "default" }
        val rendererId = PojavBridge.rendererId(context, request.renderer.backend)
        if (!PojavBridge.ensureInitialized(context)) {
            return BackendLaunch(id, displayName, emptyList(), false, "Backend storage is unavailable")
        }
        val requiredJava = PojavBridge.requiredJavaMajor(request.version.id)
        val runtime = PojavBridge.nearestRuntime(requiredJava)
        if (runtime == null) {
            return BackendLaunch(
                id, displayName, emptyList(), false,
                "Install a Java $requiredJava runtime before launching " + request.version.id
            )
        }
        PojavBridge.ensureProfile(
            instanceName = instance,
            versionId = request.version.id,
            rendererId = rendererId,
            runtimeName = runtime.name,
            memoryMb = request.memoryMb,
            jvmArgs = request.jvmArgs
        )
        return BackendLaunch(
            backendId = id,
            backendName = displayName,
            arguments = listOf(
                "--version", request.version.id,
                "--renderer", rendererId,
                "--runtime", runtime.name,
                "--memory-mb", request.memoryMb.toString()
            ),
            ready = true,
            message = "Pojav backend prepared for Minecraft " + request.version.id
        )
    }

    /** Java version required by the selected Minecraft version. */
    fun requiredJavaMajor(versionId: String): Int = PojavBridge.requiredJavaMajor(versionId)

    /** True when the packaged natives match the running device. */
    fun nativesAvailable(context: Context): Boolean = PojavBridge.nativesAvailable(context)

    /** Opens the full upstream backend UI (accounts, mod installers, controls). */
    fun openBackendUi(activity: Activity): Boolean = PojavBridge.openBackendUi(activity)

    /** Starts Minecraft directly, assuming preparation already happened. */
    fun startGame(context: Context, versionId: String): Boolean =
        PojavBridge.launchGame(context, versionId)

    fun rendererIdFor(context: Context, backend: RendererProfile.Backend): String =
        PojavBridge.rendererId(context, backend)

    fun selectedVersion(store: MinecraftSettingsStore, instance: String): String? {
        val fromInstance = instance.takeIf { it.isNotBlank() }
            ?.let { store.instanceConfig(it).versionId }
        return store.selectedVersionId ?: fromInstance
    }
}
