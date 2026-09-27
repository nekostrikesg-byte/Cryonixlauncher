package com.cryonix.launcher.minecraft.model;

public final class GameAccount {
    public enum Type { MICROSOFT, LOCAL }

    public final String id;
    public final String name;
    public final Type type;
    public final boolean authenticated;
    public final boolean ownsMinecraft;

    public GameAccount(String id, String name, Type type, boolean authenticated, boolean ownsMinecraft) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.authenticated = authenticated;
        this.ownsMinecraft = ownsMinecraft;
    }

    public boolean canUseOnlineServices() {
        return type == Type.MICROSOFT && authenticated && ownsMinecraft;
    }
}