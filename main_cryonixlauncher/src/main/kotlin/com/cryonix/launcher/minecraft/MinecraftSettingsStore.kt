package com.cryonix.launcher.minecraft

import android.content.Context
import com.cryonix.launcher.minecraft.model.RendererProfile

class MinecraftSettingsStore(context: Context) {
    private val preferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

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

    var autoRefresh: Boolean
        get() = preferences.getBoolean(KEY_AUTO_REFRESH, false)
        set(value) = preferences.edit().putBoolean(KEY_AUTO_REFRESH, value).apply()

    var keepScreenOn: Boolean
        get() = preferences.getBoolean(KEY_KEEP_SCREEN_ON, true)
        set(value) = preferences.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()

    private companion object {
        const val PREFS = "cryonix_launcher_settings"
        const val KEY_VERSION = "selected_version"
        const val KEY_PROFILE_NAME = "profile_name"
        const val KEY_PROFILE_ID = "profile_id"
        const val KEY_LOADER = "loader"
        const val KEY_LOADER_VERSION = "loader_version"
        const val KEY_RENDERER = "renderer"
        const val KEY_AUTO_REFRESH = "auto_refresh"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
    }
}
