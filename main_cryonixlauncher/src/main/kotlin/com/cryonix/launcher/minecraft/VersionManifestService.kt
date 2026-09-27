package com.cryonix.launcher.minecraft

import com.cryonix.launcher.minecraft.model.MinecraftVersion
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class VersionManifestService {
    fun fetchVersions(): List<MinecraftVersion> {
        val connection = URL(MANIFEST_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.requestMethod = "GET"

        return try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val versions = JSONObject(body).getJSONArray("versions")
            buildList {
                for (i in 0 until versions.length()) {
                    val item = versions.getJSONObject(i)
                    add(
                        MinecraftVersion(
                            id = item.optString("id"),
                            type = item.optString("type"),
                            url = item.optString("url"),
                            sha1 = item.optString("sha1")
                        )
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val MANIFEST_URL =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    }
}
