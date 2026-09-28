package com.cryonix.launcher.minecraft

import android.content.Context
import com.cryonix.launcher.minecraft.model.RendererProfile
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MinecraftSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    data class LocalProfile(val id: String, val name: String)

    data class InstanceConfig(
        val name: String,
        val versionId: String?,
        val loader: String,
        val loaderVersion: String,
        val javaRuntime: String,
        val memoryMb: Int,
        val resolutionRule: String
    )

    data class ServerEntry(
        val id: String,
        val name: String,
        val address: String,
        val port: Int = 25565
    )

    var selectedVersionId: String?
        get() = preferences.getString(KEY_VERSION, null)
        set(value) = preferences.edit().putString(KEY_VERSION, value).apply()

    var profileName: String
        get() = preferences.getString(KEY_PROFILE_NAME, "Player") ?: "Player"
        set(value) = preferences.edit().putString(KEY_PROFILE_NAME, value).apply()

    var profileId: String?
        get() = preferences.getString(KEY_PROFILE_ID, null)
        set(value) = preferences.edit().putString(KEY_PROFILE_ID, value).apply()

    var loader: String
        get() = preferences.getString(KEY_LOADER, "VANILLA") ?: "VANILLA"
        set(value) = preferences.edit().putString(KEY_LOADER, value).apply()

    var loaderVersion: String
        get() = preferences.getString(KEY_LOADER_VERSION, "") ?: ""
        set(value) = preferences.edit().putString(KEY_LOADER_VERSION, value).apply()

    var renderer: RendererProfile.Backend
        get() = runCatching {
            RendererProfile.Backend.valueOf(
                preferences.getString(KEY_RENDERER, RendererProfile.Backend.SYSTEM.name)
                    ?: RendererProfile.Backend.SYSTEM.name
            )
        }.getOrDefault(RendererProfile.Backend.SYSTEM)
        set(value) = preferences.edit().putString(KEY_RENDERER, value.name).apply()

    var vulkanDriver: String
        get() = preferences.getString(KEY_VULKAN, "Turnip") ?: "Turnip"
        set(value) = preferences.edit().putString(KEY_VULKAN, value).apply()

    var graphicsApi: String
        get() = preferences.getString(KEY_GRAPHICS, "OpenGL") ?: "OpenGL"
        set(value) = preferences.edit().putString(KEY_GRAPHICS, value).apply()

    var resolutionRule: String
        get() = preferences.getString(KEY_RESOLUTION, "Percentage · 100%") ?: "Percentage · 100%"
        set(value) = preferences.edit().putString(KEY_RESOLUTION, value).apply()

    var memoryMb: Int
        get() = preferences.getInt(KEY_MEMORY, 2048)
        set(value) = preferences.edit().putInt(KEY_MEMORY, value).apply()

    var touchControls: Boolean
        get() = preferences.getBoolean(KEY_TOUCH, true)
        set(value) = preferences.edit().putBoolean(KEY_TOUCH, value).apply()

    var autoRefresh: Boolean
        get() = preferences.getBoolean(KEY_AUTO_REFRESH, false)
        set(value) = preferences.edit().putBoolean(KEY_AUTO_REFRESH, value).apply()

    var keepScreenOn: Boolean
        get() = preferences.getBoolean(KEY_KEEP_SCREEN_ON, true)
        set(value) = preferences.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()

    var useCutoutArea: Boolean
        get() = preferences.getBoolean(KEY_CUTOUT, true)
        set(value) = preferences.edit().putBoolean(KEY_CUTOUT, value).apply()

    var cursorPreset: String
        get() = preferences.getString(KEY_CURSOR_PRESET, "arrow") ?: "arrow"
        set(value) = preferences.edit().putString(KEY_CURSOR_PRESET, value).apply()

    var cursorScale: Int
        get() = preferences.getInt(KEY_CURSOR_SCALE, 100)
        set(value) = preferences.edit().putInt(KEY_CURSOR_SCALE, value.coerceIn(50, 200)).apply()

    var instances: List<String>
        get() = preferences.getString(KEY_INSTANCES, "")
            .orEmpty().split("|").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        private set(value) = preferences.edit().putString(KEY_INSTANCES, value.joinToString("|")).apply()

    fun addInstance(name: String): Boolean {
        val clean = name.trim().replace("|", "")
        if (clean.isEmpty() || instances.any { it.equals(clean, ignoreCase = true) }) return false
        instances = instances + clean
        saveInstanceConfig(defaultInstanceConfig(clean))
        return true
    }

    fun removeInstance(name: String) {
        instances = instances.filterNot { it == name }
        preferences.edit().remove(instanceKey(name)).remove(serverKey(name)).apply()
    }

    fun duplicateInstance(name: String, newName: String): Boolean {
        val clean = newName.trim().replace("|", "")
        if (clean.isEmpty() || instances.any { it.equals(clean, ignoreCase = true) }) return false
        val source = instanceConfig(name)
        instances = instances + clean
        saveInstanceConfig(source.copy(name = clean))
        servers(name).forEach { addServer(clean, it.copy(id = UUID.randomUUID().toString())) }
        return true
    }

    fun instanceConfig(name: String): InstanceConfig {
        val fallback = defaultInstanceConfig(name)
        val raw = preferences.getString(instanceKey(name), null) ?: return fallback
        return runCatching {
            val o = JSONObject(raw)
            InstanceConfig(
                name = name,
                versionId = o.optString("version", "").ifBlank { null },
                loader = o.optString("loader", fallback.loader),
                loaderVersion = o.optString("loaderVersion", ""),
                javaRuntime = o.optString("javaRuntime", "Automatic"),
                memoryMb = o.optInt("memoryMb", fallback.memoryMb),
                resolutionRule = o.optString("resolution", fallback.resolutionRule)
            )
        }.getOrDefault(fallback)
    }

    fun saveInstanceConfig(config: InstanceConfig) {
        if (!instances.contains(config.name)) return
        val json = JSONObject()
            .put("version", config.versionId ?: "")
            .put("loader", config.loader)
            .put("loaderVersion", config.loaderVersion)
            .put("javaRuntime", config.javaRuntime)
            .put("memoryMb", config.memoryMb)
            .put("resolution", config.resolutionRule)
        preferences.edit().putString(instanceKey(config.name), json.toString()).apply()
    }

    fun profiles(): List<LocalProfile> {
        val raw = preferences.getString(KEY_PROFILES, null) ?: return emptyList()
        return runCatching {
            val a = JSONArray(raw)
            buildList {
                for (i in 0 until a.length()) {
                    val o = a.getJSONObject(i)
                    add(LocalProfile(o.optString("id"), o.optString("name")))
                }
            }.filter { it.id.isNotBlank() && it.name.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    fun saveProfiles(value: List<LocalProfile>) {
        val a = JSONArray()
        value.forEach { a.put(JSONObject().put("id", it.id).put("name", it.name)) }
        preferences.edit().putString(KEY_PROFILES, a.toString()).apply()
    }

    fun createLocalProfile(name: String): LocalProfile? {
        val clean = name.trim()
        if (clean.length !in 3..16 || !clean.matches(Regex("[A-Za-z0-9_]+"))) return null
        if (profiles().any { it.name.equals(clean, true) }) return null
        val p = LocalProfile(UUID.randomUUID().toString(), clean)
        saveProfiles(profiles() + p)
        setActiveProfile(p)
        return p
    }

    fun setActiveProfile(profile: LocalProfile) {
        profileName = profile.name
        profileId = profile.id
        preferences.edit().putString(KEY_ACTIVE_PROFILE, profile.id).apply()
    }

    fun activeProfileId(): String? = preferences.getString(KEY_ACTIVE_PROFILE, profileId)

    fun removeProfile(id: String) {
        val remaining = profiles().filterNot { it.id == id }
        saveProfiles(remaining)
        val active = activeProfileId()
        if (active == id) {
            remaining.firstOrNull()?.let { setActiveProfile(it) }
                ?: preferences.edit().remove(KEY_ACTIVE_PROFILE).apply()
        }
    }

    fun servers(instanceName: String): List<ServerEntry> {
        val raw = preferences.getString(serverKey(instanceName), null) ?: return emptyList()
        return runCatching {
            val a = JSONArray(raw)
            buildList {
                for (i in 0 until a.length()) {
                    val o = a.getJSONObject(i)
                    add(ServerEntry(o.optString("id"), o.optString("name"), o.optString("address"), o.optInt("port", 25565)))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun addServer(instanceName: String, server: ServerEntry) {
        val current = servers(instanceName).filterNot { it.id == server.id }
        val a = JSONArray()
        (current + server).forEach {
            a.put(JSONObject().put("id", it.id).put("name", it.name).put("address", it.address).put("port", it.port))
        }
        preferences.edit().putString(serverKey(instanceName), a.toString()).apply()
    }

    fun removeServer(instanceName: String, id: String) {
        val a = JSONArray()
        servers(instanceName).filterNot { it.id == id }.forEach {
            a.put(JSONObject().put("id", it.id).put("name", it.name).put("address", it.address).put("port", it.port))
        }
        preferences.edit().putString(serverKey(instanceName), a.toString()).apply()
    }

    private fun defaultInstanceConfig(name: String) = InstanceConfig(
        name, selectedVersionId, loader, loaderVersion, "Automatic", memoryMb, resolutionRule
    )

    private fun instanceKey(name: String) = "instance_" + name.hashCode()
    private fun serverKey(name: String) = "servers_" + name.hashCode()

    private companion object {
        const val PREFS = "cryonix_launcher_settings"
        const val KEY_VERSION = "selected_version"
        const val KEY_PROFILE_NAME = "profile_name"
        const val KEY_PROFILE_ID = "profile_id"
        const val KEY_LOADER = "loader"
        const val KEY_LOADER_VERSION = "loader_version"
        const val KEY_RENDERER = "renderer"
        const val KEY_VULKAN = "vulkan_driver"
        const val KEY_GRAPHICS = "graphics_api"
        const val KEY_RESOLUTION = "resolution_rule"
        const val KEY_MEMORY = "memory_mb"
        const val KEY_TOUCH = "touch_controls"
        const val KEY_AUTO_REFRESH = "auto_refresh"
        const val KEY_INSTANCES = "instances"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_CUTOUT = "use_cutout_area"
        const val KEY_CURSOR_PRESET = "cursor_preset"
        const val KEY_CURSOR_SCALE = "cursor_scale"
        const val KEY_PROFILES = "local_profiles"
        const val KEY_ACTIVE_PROFILE = "active_profile"
    }
}
