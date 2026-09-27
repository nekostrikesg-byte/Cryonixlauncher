package com.cryonix.launcher.minecraft.model

data class MinecraftVersion(
    val id: String,
    val type: String,
    val url: String,
    val sha1: String
) {
    val stable: Boolean
        get() = type.equals("release", ignoreCase = true)
}
