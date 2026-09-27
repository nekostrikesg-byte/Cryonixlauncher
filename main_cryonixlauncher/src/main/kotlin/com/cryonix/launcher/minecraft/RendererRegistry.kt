package com.cryonix.launcher.minecraft

import com.cryonix.launcher.minecraft.model.RendererProfile

object RendererRegistry {
    fun builtIns(): List<RendererProfile> = listOf(
        RendererProfile.system(),
        RendererProfile(RendererProfile.Backend.OPENGL, "android-opengl", "system", true),
        RendererProfile(RendererProfile.Backend.LTW, "ltw", "managed", false),
        RendererProfile(RendererProfile.Backend.HOLY_GL4ES, "holy-gl4es", "managed", false),
        RendererProfile(RendererProfile.Backend.MOBILE_GLUES, "mobile-glues", "managed", false),
        RendererProfile(RendererProfile.Backend.VULKAN, "vulkan", "managed", false)
    )
}
