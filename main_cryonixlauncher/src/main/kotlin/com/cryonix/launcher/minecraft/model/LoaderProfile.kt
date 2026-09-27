package com.cryonix.launcher.minecraft.model

data class LoaderProfile(
    val type: Type,
    val loaderVersion: String
) {
    enum class Type {
        VANILLA, FABRIC
    }

    companion object {
        fun vanilla() = LoaderProfile(Type.VANILLA, "")
        fun fabric(version: String) = LoaderProfile(Type.FABRIC, version)
    }
}
