package com.cryonix.launcher.minecraft.model

data class RendererProfile(
    val backend: Backend,
    val libraryId: String,
    val version: String,
    val enabled: Boolean
) {
    enum class Backend {
        SYSTEM, OPENGL, LTW, HOLY_GL4ES, MOBILE_GLUES, VULKAN
    }

    companion object {
        fun system() = RendererProfile(Backend.SYSTEM, "", "", true)
    }
}
