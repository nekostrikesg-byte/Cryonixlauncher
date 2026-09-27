package com.cryonix.launcher.minecraft.model;

public final class RendererProfile {
    public enum Backend {
        SYSTEM, OPENGL, LTW, HOLY_GL4ES, MOBILE_GLUES, VULKAN
    }

    public final Backend backend;
    public final String libraryId;
    public final String version;
    public final boolean enabled;

    public RendererProfile(Backend backend, String libraryId, String version, boolean enabled) {
        this.backend = backend;
        this.libraryId = libraryId;
        this.version = version;
        this.enabled = enabled;
    }

    public static RendererProfile system() {
        return new RendererProfile(Backend.SYSTEM, "", "", true);
    }
}