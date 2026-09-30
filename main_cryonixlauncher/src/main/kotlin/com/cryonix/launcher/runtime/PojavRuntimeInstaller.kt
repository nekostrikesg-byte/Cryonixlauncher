package com.cryonix.launcher.runtime

import android.content.Context
import android.os.Build
import android.util.Log
import com.cryonix.launcher.core.PojavBridge
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale

/**
 * Java runtime (JRE) management for the Pojav/Mojo backend.
 *
 * The backend executes a real Android OpenJDK binary, which cannot be embedded
 * in the app source tree (a runtime is ~40 MB per architecture and is licensed
 * separately). This installer therefore resolves runtimes from the official
 * PojavLauncherTeam OpenJDK build repository and installs them into the same
 * `runtimes/` directory the backend reads through MultiRT.
 *
 * Everything is optional: a user can also drop a `.tar.xz` runtime in place
 * manually or install one from a local file / custom URL.
 */
object PojavRuntimeInstaller {
    private const val TAG = "CryonixJre"
    private const val RELEASE_BASE =
        "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download"

    /**
     * A downloadable Android Java runtime.
     *
     * @param url empty when the package has no public download and must be
     *            supplied by the user (for example an OpenJDK 21 build exported
     *            from the PojavLauncherTeam CI artifacts).
     */
    data class RuntimePackage(
        val id: String,
        val label: String,
        val javaMajor: Int,
        val abi: String,
        val url: String,
        val approxBytes: Long,
        val manualSource: Boolean = false
    )

    data class Progress(
        val packageId: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val stage: String
    )

    /** Curated runtimes published by the upstream backend project. */
    private val INDEX = listOf(
        RuntimePackage(
            id = "jre17-arm64",
            label = "OpenJDK 17 (arm64)",
            javaMajor = 17,
            abi = "arm64-v8a",
            url = "$RELEASE_BASE/jre17-ca01427/jre17-arm64-20220817-release.tar.xz",
            approxBytes = 21_057_916L
        ),
        RuntimePackage(
            id = "jre8-arm64",
            label = "OpenJDK 8 (arm64)",
            javaMajor = 8,
            abi = "arm64-v8a",
            url = "$RELEASE_BASE/jre8-40df388/jre8-arm64-20220811-release.tar.xz",
            approxBytes = 27_509_232L
        ),
        RuntimePackage(
            id = "jre17-arm64-2021",
            label = "OpenJDK 17 (arm64, older build)",
            javaMajor = 17,
            abi = "arm64-v8a",
            url = "$RELEASE_BASE/jre17-ec28559/jre17-arm64-20210825-release.tar.xz",
            approxBytes = 38_065_004L
        ),
        RuntimePackage(
            id = "jre17-arm32",
            label = "OpenJDK 17 (arm)",
            javaMajor = 17,
            abi = "armeabi-v7a",
            url = "$RELEASE_BASE/jre17-ec28559/jre17-arm-20210914-release.tar.xz",
            approxBytes = 37_378_360L
        ),
        RuntimePackage(
            id = "jre17-x86_64",
            label = "OpenJDK 17 (x86_64)",
            javaMajor = 17,
            abi = "x86_64",
            url = "$RELEASE_BASE/jre17-ec28559/jre17-x86_64-20210825-release.tar.xz",
            approxBytes = 39_230_848L
        ),
        RuntimePackage(
            id = "jre17-x86",
            label = "OpenJDK 17 (x86)",
            javaMajor = 17,
            abi = "x86",
            url = "$RELEASE_BASE/jre17-ec28559/jre17-x86-20220225-release.tar.xz",
            approxBytes = 37_930_912L
        ),
        RuntimePackage(
            id = "jre21-arm64",
            label = "OpenJDK 21 (arm64) — manual source",
            javaMajor = 21,
            abi = "arm64-v8a",
            url = "",
            approxBytes = 0L,
            manualSource = true
        )
    )

    /** Runtimes that match the device ABI, newest Java first. */
    fun availableForDevice(): List<RuntimePackage> {
        val abis = Build.SUPPORTED_ABIS.map { it.lowercase(Locale.US) }
        return INDEX
            .filter { pkg -> abis.contains(pkg.abi) }
            .sortedByDescending { it.javaMajor }
    }

    fun packageById(id: String): RuntimePackage? = INDEX.firstOrNull { it.id == id }

    fun installedRuntimes(): List<PojavBridge.RuntimeInfo> = PojavBridge.runtimes()

    fun isInstalled(pkg: RuntimePackage): Boolean {
        val runtime = PojavBridge.runtimes().firstOrNull { it.name == pkg.id }
        return runtime != null && runtime.major > 0
    }

    /**
     * Downloads [pkg] into the app cache and installs it as a backend runtime.
     * Blocking: run it off the main thread.
     */
    fun downloadAndInstall(
        context: Context,
        pkg: RuntimePackage,
        onProgress: (Progress) -> Unit = {}
    ): Result<String> {
        if (pkg.url.isBlank()) {
            return Result.failure(IllegalStateException(
                "This runtime has no public download. Install it from a file or a custom URL instead."))
        }
        val archive = File(context.cacheDir, pkg.id + ".tar.xz")
        return runCatching {
            download(pkg.url, archive, pkg) { downloaded, total ->
                onProgress(Progress(pkg.id, downloaded, total, "Downloading " + pkg.label))
            }
            onProgress(Progress(pkg.id, archive.length(), archive.length(), "Unpacking " + pkg.label))
            installArchive(pkg.id, archive)
            archive.delete()
            PojavBridge.setDefaultRuntime(pkg.id)
            pkg.id
        }.onFailure {
            Log.e(TAG, "Runtime install failed for ${pkg.id}", it)
            archive.delete()
        }
    }

    /** Installs a runtime from a local `.tar.xz` (`InputStream`). */
    fun installArchive(name: String, archive: File): String {
        val stream = archive.inputStream()
        return stream.use { installStream(name, it) }
    }

    fun installStream(name: String, stream: InputStream): String {
        val clean = PojavBridge.safeName(name).ifBlank { "runtime" }
        PojavBridge.installRuntime(clean, stream)
        normalize(clean)
        return clean
    }

    fun remove(name: String) {
        runCatching { PojavBridge.removeRuntime(name) }
            .onFailure { Log.e(TAG, "Failed to remove runtime $name", it) }
    }

    /**
     * Some runtime packs place the JRE in a nested directory. MultiRT expects
     * `release`, `bin/` and `lib/` at the root of the runtime folder.
     */
    private fun normalize(name: String) {
        val home = PojavBridge.runtimes().firstOrNull { it.name == name }?.root ?: return
        if (File(home, "release").isFile) return
        val nested = home.listFiles { file -> file.isDirectory }
            ?.firstOrNull { File(it, "release").isFile } ?: return
        nested.listFiles()?.forEach { child ->
            val target = File(home, child.name)
            if (!target.exists()) child.renameTo(target)
        }
        nested.delete()
        Log.i(TAG, "Flattened the nested runtime layout of " + name)
    }

    private fun download(url: String, target: File, pkg: RuntimePackage, onProgress: (Long, Long) -> Unit) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "CryonixLauncher")
        }
        try {
            connection.connect()
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Runtime download failed: HTTP " + connection.responseCode)
            }
            val total = connection.contentLengthLong.takeIf { it > 0 } ?: pkg.approxBytes
            connection.inputStream.use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var downloaded = 0L
                    var lastReport = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (downloaded - lastReport > 512 * 1024) {
                            lastReport = downloaded
                            onProgress(downloaded, total)
                        }
                    }
                    onProgress(downloaded, total)
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    fun sha1Of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
