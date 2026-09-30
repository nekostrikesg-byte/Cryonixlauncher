package com.cryonix.launcher.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cryonix.launcher.minecraft.model.RendererProfile
import net.kdt.pojavlaunch.tasks.AsyncAssetManager
import net.kdt.pojavlaunch.JMinecraftVersionList
import net.kdt.pojavlaunch.lifecycle.ContextAwareDoneListener
import net.kdt.pojavlaunch.PojavProfile
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.multirt.MultiRTUtils
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.tasks.AsyncMinecraftDownloader
import net.kdt.pojavlaunch.tasks.AsyncVersionList
import net.kdt.pojavlaunch.tasks.MinecraftDownloader
import net.kdt.pojavlaunch.value.MinecraftAccount
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile
import java.io.File
import java.io.InputStream
import java.util.UUID

/**
 * Kotlin side of the Pojav/Mojo Android backend.
 *
 * Everything that talks to the vendored `net.kdt.pojavlaunch` runtime is kept
 * in this one object so the rest of the launcher stays Kotlin-only. The backend
 * itself is the upstream LWJGL/GLFW + OpenJDK-for-Android engine: it owns the
 * `.minecraft` working directory, the Java runtime packages and the game
 * process (declared in the manifest as the `:game` process).
 *
 * Nothing here fakes a launch: every step either succeeds, or reports what is
 * still missing (runtime, game files, account).
 */
object PojavBridge {
    private const val TAG = "PojavBridge"

    /** Pojav renderer ids available in this build (see values/headings_array.xml). */
    private const val RENDERER_GL4ES = "opengles2"
    private const val RENDERER_ZINK = "vulkan_zink"
    private const val RENDERER_LTW = "opengles3_ltw"

    /** Java major version used by Minecraft versions that do not declare one. */
    private const val LEGACY_JAVA_MAJOR = 8

    data class AccountInfo(
        val name: String,
        val username: String,
        val microsoft: Boolean,
        val demo: Boolean
    )

    data class RuntimeInfo(
        val name: String,
        val major: Int,
        val arch: String?,
        val root: File
    ) {
        val usable: Boolean get() = major > 0
    }

    data class VersionEntry(
        val id: String,
        val type: String,
        val url: String?
    )

    /**
     * Initializes the backend directories and preferences. Safe to call from
     * any activity/application start; it only touches app-private storage.
     */
    fun ensureInitialized(context: Context): Boolean {
        if (!Tools.checkStorageRoot(context)) {
            Log.w(TAG, "Backend storage root is not available")
            return false
        }
        return runCatching {
            // Implicitly initializes [Tools.DIR_GAME_HOME], version/library/asset
            // directories and the MultiRT runtime home.
            LauncherPreferences.loadPreferences(context)
            true
        }.getOrElse {
            Log.e(TAG, "Failed to initialize the backend runtime directories", it)
            false
        }
    }

    /** Unpacks the backend components (LWJGL3 classes, caciocavallo, security). */
    fun unpackBackendFiles(context: Context) {
        runCatching {
            AsyncAssetManager.unpackComponents(context)
            AsyncAssetManager.unpackSingleFiles(context)
        }.onFailure { Log.e(TAG, "Component unpack failed", it) }
    }

    /** True when the native LWJGL/renderer libraries shipped with the APK exist. */
    fun nativesAvailable(context: Context): Boolean {
        val nativeDir = context.applicationInfo.nativeLibraryDir
        return File(nativeDir, "liblwjgl.so").isFile && File(nativeDir, "libgl4es_114.so").isFile
    }

    // ---------------------------------------------------------------- accounts

    fun accounts(context: Context): List<AccountInfo> {
        val dir = File(Tools.DIR_ACCOUNT_NEW)
        val files = dir.listFiles { file -> file.isFile && file.name.endsWith(".json") } ?: return emptyList()
        return files.mapNotNull { file ->
            val name = file.name.removeSuffix(".json")
            val account = runCatching { MinecraftAccount.load(name) }.getOrNull()
            if (account == null) {
                AccountInfo(name, name, microsoft = false, demo = false)
            } else {
                AccountInfo(
                    name = name,
                    username = account.username ?: name,
                    microsoft = account.isMicrosoft,
                    demo = account.username?.startsWith("Demo.") == true
                )
            }
        }.sortedBy { it.username.lowercase() }
    }

    fun currentAccountName(context: Context): String =
        runCatching { PojavProfile.getCurrentProfileName(context) }.getOrDefault("")

    fun activateAccount(context: Context, name: String) {
        PojavProfile.setCurrentProfile(context, name)
    }

    /**
     * Creates (or updates) the offline account that matches a Cryonix local
     * profile. Offline accounts are marked as local so the backend knows it
     * must not talk to Mojang's session servers with them.
     */
    fun writeOfflineAccount(context: Context, username: String, versionId: String?): AccountInfo? {
        val clean = username.trim().ifBlank { "Player" }
        return runCatching {
            val account = MinecraftAccount()
            account.username = clean
            account.accessToken = "0"
            account.clientToken = "0"
            account.profileId = UUID.nameUUIDFromBytes("Cryonix:$clean".toByteArray()).toString()
            account.isMicrosoft = false
            account.msaRefreshToken = "0"
            if (!versionId.isNullOrBlank()) account.selectedVersion = versionId
            account.save()
            AccountInfo(clean, clean, microsoft = false, demo = false)
        }.getOrElse {
            Log.e(TAG, "Failed to write the offline account", it)
            null
        }
    }

    /** True when a Microsoft account (usable for online play) is installed. */
    fun hasOnlineAccount(context: Context): Boolean = accounts(context).any { it.microsoft }

    // ---------------------------------------------------------------- runtimes

    fun runtimes(): List<RuntimeInfo> = runCatching {
        MultiRTUtils.getRuntimes().map { runtime ->
            RuntimeInfo(
                name = runtime.name,
                major = runtime.javaVersion,
                arch = runtime.arch,
                root = File(Tools.MULTIRT_HOME, runtime.name)
            )
        }.sortedBy { it.major }
    }.getOrElse {
        Log.w(TAG, "Could not list the installed Java runtimes", it)
        emptyList()
    }

    fun defaultRuntimeName(): String =
        runCatching { LauncherPreferences.PREF_DEFAULT_RUNTIME }.getOrNull().orEmpty()

    fun setDefaultRuntime(name: String) {
        LauncherPreferences.PREF_DEFAULT_RUNTIME = name
        LauncherPreferences.DEFAULT_PREF.edit().putString("defaultRuntime", name).apply()
    }

    fun nearestRuntime(major: Int): RuntimeInfo? =
        runtimes().filter { it.major >= major }.minByOrNull { it.major }

    fun runtimeInstalled(name: String): Boolean =
        File(Tools.MULTIRT_HOME, name).let { it.isDirectory && File(it, "release").isFile }

    fun installRuntime(name: String, stream: InputStream) {
        MultiRTUtils.installRuntimeNamed(Tools.NATIVE_LIB_DIR, stream, name)
        MultiRTUtils.postPrepare(name)
        MultiRTUtils.forceReread(name)
    }

    fun removeRuntime(name: String) {
        MultiRTUtils.removeRuntimeNamed(name)
        if (defaultRuntimeName() == name) {
            val fallback = runtimes().firstOrNull()?.name.orEmpty()
            setDefaultRuntime(fallback)
        }
    }

    // ------------------------------------------------------------ game version

    fun versionJsonFile(versionId: String): File =
        File(Tools.DIR_HOME_VERSION, "$versionId/$versionId.json")

    fun clientJarFile(versionId: String): File =
        File(Tools.DIR_HOME_VERSION, "$versionId/$versionId.jar")

    fun versionJson(versionId: String): JMinecraftVersionList.Version? =
        runCatching { Tools.getVersionInfo(versionId) }.getOrNull()

    /** Java major version required by the given Minecraft version. */
    fun requiredJavaMajor(versionId: String): Int {
        val declared = versionJson(versionId)?.javaVersion?.majorVersion ?: 0
        if (declared > 0) return declared
        return legacyJavaMajorFor(versionId)
    }

    /**
     * Fallback mapping for versions whose JSON is not downloaded yet, based on
     * Mojang's public release history.
     */
    private fun legacyJavaMajorFor(versionId: String): Int {
        val release = releaseNumber(versionId)
        return when {
            release >= 20.5 -> 21
            release >= 17.0 -> 17
            else -> LEGACY_JAVA_MAJOR
        }
    }

    private fun releaseNumber(versionId: String): Double {
        val match = Regex("^1\\.(\\d+)(?:\\.(\\d+))?").find(versionId) ?: return 0.0
        val minor = match.groupValues[1].toDoubleOrNull() ?: return 0.0
        val patch = match.groupValues[2].toDoubleOrNull() ?: 0.0
        return minor + patch / 10.0
    }

    fun gameFilesReady(versionId: String): Boolean =
        versionJsonFile(versionId).isFile && clientJarFile(versionId).isFile

    fun versionList(onResult: (List<VersionEntry>) -> Unit) {
        AsyncVersionList().getVersionList({ list ->
            val versions = list?.versions?.map { version ->
                VersionEntry(version.id, version.type ?: "", version.url)
            }.orEmpty()
            onResult(versions)
        }, false)
    }

    fun versionEntry(versions: List<JMinecraftVersionList.Version>, versionId: String) =
        versions.firstOrNull { it.id == versionId }

    /**
     * Downloads/verifies the Minecraft version the backend will launch. The
     * downloader also installs the required Java runtime when it is bundled
     * with the backend.
     */
    fun downloadGame(
        activity: Activity,
        version: JMinecraftVersionList.Version?,
        versionId: String,
        onDone: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        MinecraftDownloader().start(activity, version, versionId,
            object : AsyncMinecraftDownloader.DoneListener {
                override fun onDownloadDone() = onDone()
                override fun onDownloadFailed(throwable: Throwable?) =
                    onError(throwable ?: IllegalStateException("Minecraft download failed"))
            })
    }

    /**
     * Installs the Java runtime bundled with the APK, when present, exactly the
     * way the backend does it on start-up. Large runtime packages are not part
     * of the Cryonix source tree, so this only does something when the APK was
     * built with `assets/components/jre*` (or the user installed a runtime).
     */
    fun unpackBundledRuntime(context: Context) {
        runCatching { AsyncAssetManager.unpackRuntime(context.assets) }
            .onFailure { Log.e(TAG, "Bundled runtime unpack failed", it) }
    }

    /** True when the APK carries a Java runtime the backend can install itself. */
    fun hasBundledRuntime(context: Context): Boolean = runCatching {
        context.assets.list("components").orEmpty().any { component ->
            component.startsWith("jre") &&
                context.assets.list("components/" + component).orEmpty().contains("version")
        }
    }.getOrDefault(false)

    /**
     * Downloads/verifies Minecraft [versionId] with the backend downloader and
     * then hands the version to the game process. This is the exact pipeline the
     * upstream launcher uses: hashed downloads, native extraction, bundled-JRE
     * installation and finally the `:game` process.
     */
    fun prepareAndLaunch(activity: Activity, versionId: String) {
        val listed = AsyncMinecraftDownloader.getListedVersion(
            AsyncMinecraftDownloader.normalizeVersionId(versionId)
        )
        MinecraftDownloader().start(
            activity,
            listed,
            AsyncMinecraftDownloader.normalizeVersionId(versionId),
            ContextAwareDoneListener(activity, versionId)
        )
    }

    fun versionListRaw(onResult: (JMinecraftVersionList?) -> Unit) {
        AsyncVersionList().getVersionList({ list -> onResult(list) }, false)
    }

    // ------------------------------------------------------------- profile/setup

    /**
     * Writes the launcher profile the backend will read when the game starts.
     * The game directory is kept inside the backend home so mods/resource packs
     * of an instance stay together with the vanilla files.
     */
    fun ensureProfile(
        instanceName: String,
        versionId: String,
        rendererId: String,
        runtimeName: String?,
        memoryMb: Int,
        jvmArgs: String?
    ): String {
        LauncherProfiles.load()
        val rendererName = rendererId.ifBlank { RENDERER_GL4ES }
        val existingKey = LauncherProfiles.mainProfileJson.profiles.entries
            .firstOrNull { it.value.name == instanceName }?.key
        val profile = existingKey?.let { LauncherProfiles.mainProfileJson.profiles[it] }
            ?.let { MinecraftProfile(it) } ?: MinecraftProfile()
        profile.name = instanceName
        profile.lastVersionId = versionId
        profile.pojavRendererName = rendererName
        profile.gameDir = Tools.LAUNCHERPROFILES_RTPREFIX + "instances/" + safeName(instanceName)
        profile.javaDir = if (runtimeName.isNullOrBlank()) null
        else Tools.LAUNCHERPROFILES_RTPREFIX + runtimeName
        profile.javaArgs = jvmArgs?.takeIf { it.isNotBlank() }
        val key = existingKey ?: LauncherProfiles.getFreeProfileKey()
        LauncherProfiles.mainProfileJson.profiles[key] = profile
        LauncherProfiles.write()
        LauncherPreferences.DEFAULT_PREF.edit()
            .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, key)
            .apply()
        return key
    }

    fun currentProfileName(): String =
        LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "").orEmpty()

    /** Pushes the Cryonix launcher settings into the backend preferences. */
    fun applyPreferences(
        rendererId: String = "",
        memoryMb: Int = 0,
        resolutionPercent: Int = 0,
        jvmArgs: String? = null
    ) {
        val editor = LauncherPreferences.DEFAULT_PREF.edit()
        if (rendererId.isNotBlank()) {
            editor.putString("renderer", rendererId)
            LauncherPreferences.PREF_RENDERER = rendererId
        }
        if (memoryMb > 0) {
            editor.putInt("allocation", memoryMb)
            LauncherPreferences.PREF_RAM_ALLOCATION = memoryMb
        }
        if (resolutionPercent in 25..100) {
            editor.putInt("resolutionRatio", resolutionPercent)
            LauncherPreferences.PREF_SCALE_FACTOR = resolutionPercent / 100f
        }
        if (!jvmArgs.isNullOrBlank()) {
            editor.putString("javaArgs", jvmArgs)
            LauncherPreferences.PREF_CUSTOM_JAVA_ARGS = jvmArgs
        }
        editor.apply()
    }

    /**
     * Maps a Cryonix renderer choice to the backend renderer ids, falling back
     * to whatever the device actually supports.
     */
    fun rendererId(context: Context, backend: RendererProfile.Backend, preferred: String? = null): String {
        val compatible = runCatching {
            Tools.getCompatibleRenderers(context).rendererIds
        }.getOrDefault(listOf(RENDERER_GL4ES))
        val requested = preferred?.takeIf { it.isNotBlank() } ?: when (backend) {
            RendererProfile.Backend.LTW -> RENDERER_LTW
            RendererProfile.Backend.VULKAN -> RENDERER_ZINK
            else -> RENDERER_GL4ES
        }
        if (compatible.contains(requested)) return requested
        return compatible.firstOrNull() ?: RENDERER_GL4ES
    }

    /** Human readable renderer description for the launcher UI. */
    fun rendererDisplayName(context: Context): String = runCatching {
        val renderers = Tools.getCompatibleRenderers(context)
        val index = renderers.rendererIds.indexOf(LauncherPreferences.PREF_RENDERER)
        if (index >= 0) renderers.rendererDisplayNames[index] else LauncherPreferences.PREF_RENDERER
    }.getOrDefault(LauncherPreferences.PREF_RENDERER)

    // ------------------------------------------------------------------ launch

    /**
     * Starts the backend game activity. Minecraft then runs its own JVM in the
     * `:game` process declared in the manifest.
     */
    fun launchGame(context: Context, versionId: String): Boolean = runCatching {
        Tools.initStorageConstants(context)
        val intent = Intent().apply {
            setClassName(context.packageName, "net.kdt.pojavlaunch.MainActivity")
            putExtra("intent_version", versionId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    }.getOrElse {
        Log.e(TAG, "Unable to start the Minecraft game activity", it)
        false
    }

    /** Opens the upstream backend UI (accounts, mods, runtime manager, controls). */
    fun openBackendUi(activity: Activity): Boolean = runCatching {
        Tools.initStorageConstants(activity)
        activity.startActivity(
            Intent().apply {
                setClassName(activity.packageName, "net.kdt.pojavlaunch.LauncherActivity")
            }
        )
        true
    }.getOrElse {
        Log.e(TAG, "Unable to open the backend launcher screen", it)
        false
    }

    fun gameDirectoryFor(profile: MinecraftProfile): File = Tools.getGameDirPath(profile)

    fun safeName(value: String): String =
        value.trim().replace(Regex("[^A-Za-z0-9._ -]"), "_").take(48).ifBlank { "default" }
}
