package com.cryonix.launcher.minecraft.model

data class GameAccount(
    val id: String,
    val name: String,
    val type: Type,
    val authenticated: Boolean,
    val ownsMinecraft: Boolean
) {
    enum class Type {
        MICROSOFT, LOCAL
    }

    fun canUseOnlineServices(): Boolean =
        type == Type.MICROSOFT && authenticated && ownsMinecraft
}
