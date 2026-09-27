package com.cryonix.launcher.minecraft

import android.content.Context
import com.cryonix.launcher.minecraft.model.GameAccount
import java.nio.charset.StandardCharsets
import java.util.UUID

class LocalProfileStore(context: Context) {
    private val preferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun create(name: String?): GameAccount {
        val safeName = name?.trim()?.takeIf { it.isNotEmpty() } ?: "Player"
        val id = UUID.nameUUIDFromBytes(
            ("cryonix:$safeName").toByteArray(StandardCharsets.UTF_8)
        ).toString()

        preferences.edit().putString(id, safeName).apply()
        return GameAccount(id, safeName, GameAccount.Type.LOCAL, false, false)
    }

    fun delete(id: String) {
        preferences.edit().remove(id).apply()
    }

    private companion object {
        const val PREFS = "cryonix_minecraft_profiles"
    }
}
