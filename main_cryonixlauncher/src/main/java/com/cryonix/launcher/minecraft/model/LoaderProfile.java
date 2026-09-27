package com.cryonix.launcher.minecraft.model;

public final class LoaderProfile {
    public enum Type { VANILLA, FABRIC }

    public final Type type;
    public final String loaderVersion;

    public LoaderProfile(Type type, String loaderVersion) {
        this.type = type;
        this.loaderVersion = loaderVersion;
    }

    public static LoaderProfile vanilla() {
        return new LoaderProfile(Type.VANILLA, "");
    }

    public static LoaderProfile fabric(String version) {
        return new LoaderProfile(Type.FABRIC, version);
    }
}