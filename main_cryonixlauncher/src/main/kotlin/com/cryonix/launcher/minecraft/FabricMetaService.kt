package com.cryonix.launcher.minecraft

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class FabricMetaService {
    fun fetchGameVersions(): List<String> =
        getArray(GAME_URL).let { json ->
            buildList {
                for (i in 0 until json.length()) {
                    add(json.getJSONObject(i).optString("version"))
                }
            }
        }

    fun fetchLoaderVersions(gameVersion: String): List<String> =
        getArray(LOADER_URL + gameVersion).let { json ->
            buildList {
                for (i in 0 until json.length()) {
                    add(json.getJSONObject(i).getJSONObject("loader").optString("version"))
                }
            }
        }

    private fun getArray(endpoint: String): JSONArray {
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000

        return try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            JSONArray(body)
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val GAME_URL = "https://meta.fabricmc.net/v2/versions/game"
        const val LOADER_URL = "https://meta.fabricmc.net/v2/versions/loader/"
    }
}
