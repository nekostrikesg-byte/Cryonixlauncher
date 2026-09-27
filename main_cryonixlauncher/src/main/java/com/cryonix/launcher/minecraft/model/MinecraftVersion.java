package com.cryonix.launcher.minecraft.model;

public final class MinecraftVersion {
    public final String id;
    public final String type;
    public final String url;
    public final String sha1;
    public final boolean stable;

    public MinecraftVersion(String id, String type, String url, String sha1) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.sha1 = sha1;
        this.stable = "release".equalsIgnoreCase(type);
    }
}