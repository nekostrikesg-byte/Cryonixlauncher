package com.cryonix.launcher.minecraft;

import com.cryonix.launcher.minecraft.model.RendererProfile;
import java.util.Arrays;
import java.util.List;

public final class RendererRegistry {
    private RendererRegistry() {}

    public static List<RendererProfile> builtIns() {
        return Arrays.asList(
                RendererProfile.system(),
                new RendererProfile(RendererProfile.Backend.OPENGL, "android-opengl", "system", true),
                new RendererProfile(RendererProfile.Backend.LTW, "ltw", "managed", false),
                new RendererProfile(RendererProfile.Backend.HOLY_GL4ES, "holy-gl4es", "managed", false),
                new RendererProfile(RendererProfile.Backend.MOBILE_GLUES, "mobile-glues", "managed", false),
                new RendererProfile(RendererProfile.Backend.VULKAN, "vulkan", "managed", false)
        );
    }
}