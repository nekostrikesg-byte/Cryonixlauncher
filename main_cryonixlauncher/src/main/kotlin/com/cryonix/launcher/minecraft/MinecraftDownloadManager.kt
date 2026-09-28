package com.cryonix.launcher.minecraft

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale

/**
 * Phase 1 Minecraft installer.
 *
 * Downloads official Mojang version metadata, client JAR, allowed libraries
 * and asset objects. SHA-1 values are verified whenever Mojang provides one.
 * Java/native runtime execution is intentionally handled by a later phase.
 */
class MinecraftDownloadManager(private val context: Context) {

    interface ProgressListener {
        fun onStage(stage: String, completed: Int, total: Int)
        fun onFile(name: String, completedBytes: Long, totalBytes: Long)
    }

    fun install(
        instanceName: String,
        versionId: String,
        listener: ProgressListener? = null
    ): MinecraftInstallResult {
        require(instanceName.isNotBlank()) { "Instance name is required" }
        require(versionId.isNotBlank()) { "Minecraft version is required" }

        val safeInstance = safeName(instanceName)
        val root = File(context.filesDir, "minecraft")
        val cache = File(root, "cache")
        val instances = File(root, "instances")
        val instanceDir = File(instances, safeInstance)
        val gameDir = File(instanceDir, "game")
        val versionsDir = File(cache, "versions")
        val librariesDir = File(cache, "libraries")
        val assetsDir = File(cache, "assets")
        listOf(cache, instances, instanceDir, gameDir, versionsDir, librariesDir, assetsDir).forEach { it.mkdirs() }

        listener?.onStage("Fetching Mojang version metadata", 0, 1)
        val manifestEntry = VersionManifestService().fetchVersions()
            .firstOrNull { it.id == versionId }
            ?: error("Minecraft version " + versionId + " was not found in Mojang's manifest")

        val versionJson = File(versionsDir, versionId + ".json")
        downloadVerified(
            manifestEntry.url,
            versionJson,
            manifestEntry.sha1.ifBlank { null },
            listener,
            "version metadata"
        )

        val metadata = JSONObject(versionJson.readText())
        val client = metadata.optJSONObject("downloads")?.optJSONObject("client")
            ?: error("Minecraft version " + versionId + " has no client download")
        val clientUrl = client.optString("url")
        require(clientUrl.isNotBlank()) { "Minecraft client URL is missing" }

        val clientSha1 = client.optString("sha1").ifBlank { null }
        val versionDir = File(versionsDir, versionId)
        versionDir.mkdirs()
        val clientJar = File(versionDir, versionId + ".jar")

        val libraries = collectLibraries(metadata)
        val assetObjects = collectAssetObjects(metadata, assetsDir)
        val totalSteps = 1 + libraries.size + assetObjects.size
        var completed = 0

        listener?.onStage("Downloading Minecraft client", completed, totalSteps)
        downloadVerified(clientUrl, clientJar, clientSha1, listener, versionId + " client")
        completed++

        for (library in libraries) {
            val target = File(librariesDir, library.path)
            downloadVerified(library.url, target, library.sha1, listener, library.path)
            completed++
            listener?.onStage("Downloading libraries", completed, totalSteps)
        }

        for (asset in assetObjects) {
            val target = File(assetsDir, "objects/" + asset.sha1.take(2) + "/" + asset.sha1)
            downloadVerified(asset.url, target, asset.sha1, listener, "asset " + asset.sha1)
            completed++
            listener?.onStage("Downloading assets", completed, totalSteps)
        }

        val installState = JSONObject()
            .put("version", versionId)
            .put("instance", instanceName)
            .put("gameDirectory", gameDir.absolutePath)
            .put("versionJson", versionJson.absolutePath)
            .put("clientJar", clientJar.absolutePath)
            .put("libraries", libraries.size)
            .put("assets", assetObjects.size)
            .put("installedAt", System.currentTimeMillis())
        File(instanceDir, "cryonix-install.json").writeText(installState.toString(2))

        val totalBytes = clientJar.length() +
            libraries.sumOf { File(librariesDir, it.path).length() } +
            assetObjects.sumOf {
                File(assetsDir, "objects/" + it.sha1.take(2) + "/" + it.sha1).length()
            }

        listener?.onStage("Minecraft files installed", totalSteps, totalSteps)
        return MinecraftInstallResult(
            versionId = versionId,
            instanceName = instanceName,
            gameDirectory = gameDir,
            versionJson = versionJson,
            clientJar = clientJar,
            libraryCount = libraries.size,
            assetCount = assetObjects.size,
            totalBytes = totalBytes
        )
    }

    fun isInstalled(instanceName: String, versionId: String): Boolean {
        val state = File(
            File(File(context.filesDir, "minecraft"), "instances"),
            safeName(instanceName)
        ).resolve("cryonix-install.json")
        if (!state.isFile) return false
        return runCatching {
            val json = JSONObject(state.readText())
            json.optString("version") == versionId &&
                File(json.optString("clientJar")).isFile &&
                File(json.optString("versionJson")).isFile
        }.getOrDefault(false)
    }

    private data class Artifact(val url: String, val path: String, val sha1: String?)
    private data class Asset(val url: String, val sha1: String)

    private fun collectLibraries(metadata: JSONObject): List<Artifact> {
        val result = ArrayList<Artifact>()
        val array = metadata.optJSONArray("libraries") ?: JSONArray()
        for (i in 0 until array.length()) {
            val library = array.optJSONObject(i) ?: continue
            if (!isAllowed(library.optJSONArray("rules"))) continue

            val downloads = library.optJSONObject("downloads")
            val artifact = downloads?.optJSONObject("artifact")
            if (artifact != null) {
                val path = artifact.optString("path")
                val url = artifact.optString("url")
                if (path.isNotBlank() && url.isNotBlank()) {
                    result += Artifact(url, path, artifact.optString("sha1").ifBlank { null })
                }
            } else {
                val coordinates = library.optString("name")
                val parts = coordinates.split(":")
                if (parts.size >= 3) {
                    val groupPath = parts[0].replace('.', '/')
                    val path = groupPath + "/" + parts[1] + "/" + parts[2] + "/" +
                        parts[1] + "-" + parts[2] + ".jar"
                    result += Artifact(
                        "https://libraries.minecraft.net/" + path,
                        path,
                        null
                    )
                }
            }

            val classifiers = downloads?.optJSONObject("classifiers")
            val nativeName = selectNativeClassifier(library.optJSONObject("natives"))
            if (classifiers != null && nativeName != null) {
                val native = classifiers.optJSONObject(nativeName)
                if (native != null) {
                    val path = native.optString("path")
                    val url = native.optString("url")
                    if (path.isNotBlank() && url.isNotBlank()) {
                        result += Artifact(url, path, native.optString("sha1").ifBlank { null })
                    }
                }
            }
        }
        return result.distinctBy { it.path }
    }

    private fun collectAssetObjects(metadata: JSONObject, assetsDir: File): List<Asset> {
        val indexInfo = metadata.optJSONObject("assetIndex") ?: return emptyList()
        val indexUrl = indexInfo.optString("url")
        if (indexUrl.isBlank()) return emptyList()

        val indexName = metadata.optString("assets", "legacy")
        val indexFile = File(assetsDir, "indexes/" + indexName + ".json")
        downloadVerified(
            indexUrl,
            indexFile,
            indexInfo.optString("sha1").ifBlank { null },
            null,
            "asset index"
        )

        val objects = JSONObject(indexFile.readText()).optJSONObject("objects") ?: return emptyList()
        val result = ArrayList<Asset>()
        val keys = objects.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val object = objects.optJSONObject(key) ?: continue
            val sha1 = object.optString("hash")
            if (sha1.length != 40) continue
            result += Asset(
                "https://resources.download.minecraft.net/" + sha1.take(2) + "/" + sha1,
                sha1
            )
        }
        return result.distinctBy { it.sha1 }
    }

    private fun selectNativeClassifier(natives: JSONObject?): String? {
        if (natives == null) return null
        val os = System.getProperty("os.name").orEmpty().lowercase(Locale.US)
        val prefix = when {
            os.contains("win") -> "windows"
            os.contains("mac") -> "osx"
            else -> "linux"
        }
        return natives.optString(prefix + "-64").ifBlank {
            natives.optString(prefix).ifBlank { null }
        }
    }

    private fun isAllowed(rules: JSONArray?): Boolean {
        if (rules == null || rules.length() == 0) return true
        var allowed = false
        for (i in 0 until rules.length()) {
            val rule = rules.optJSONObject(i) ?: continue
            if (!ruleMatchesPlatform(rule.optJSONObject("os"))) continue
            allowed = rule.optString("action") == "allow"
        }
        return allowed
    }

    private fun ruleMatchesPlatform(os: JSONObject?): Boolean {
        if (os == null) return true
        val name = os.optString("name")
        if (name.isNotBlank() && name != "linux") return false
        val arch = os.optString("arch")
        if (arch.isNotBlank()) {
            val current = System.getProperty("os.arch").orEmpty().lowercase(Locale.US)
            if (!current.contains(arch.lowercase(Locale.US))) return false
        }
        return true
    }

    private fun downloadVerified(
        urlString: String,
        target: File,
        expectedSha1: String?,
        listener: ProgressListener?,
        displayName: String
    ) {
        require(urlString.startsWith("https://")) {
            "Refusing non-HTTPS download: " + urlString
        }
        target.parentFile?.mkdirs()

        if (target.isFile &&
            (expectedSha1 == null || sha1(target).equals(expectedSha1, true))
        ) {
            listener?.onFile(displayName, target.length(), target.length())
            return
        }

        val temp = File(target.parentFile, target.name + ".part")
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        connection.instanceFollowRedirects = true
        connection.requestMethod = "GET"

        try {
            if (connection.responseCode !in 200..299) {
                error("HTTP " + connection.responseCode + " while downloading " + displayName)
            }

            val total = connection.contentLengthLong.coerceAtLeast(0L)
            var completed = 0L
            val buffer = ByteArray(64 * 1024)

            BufferedInputStream(connection.inputStream).use { input ->
                FileOutputStream(temp).use { output ->
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        completed += read
                        listener?.onFile(displayName, completed, total)
                        if (Thread.currentThread().isInterrupted) {
                            throw InterruptedException("Download cancelled")
                        }
                    }
                    output.fd.sync()
                }
            }

            if (expectedSha1 != null) {
                val actual = sha1(temp)
                if (!actual.equals(expectedSha1, true)) {
                    temp.delete()
                    error("SHA-1 mismatch for " + displayName)
                }
            }

            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) {
                error("Could not finalize " + displayName)
            }
        } finally {
            connection.disconnect()
            if (temp.exists() && !target.exists()) temp.delete()
        }
    }

    private fun sha1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun safeName(value: String): String =
        value.trim()
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(80)
            .ifBlank { "instance" }
}
