package com.cryonix.launcher.minecraft;

import com.cryonix.launcher.minecraft.model.GameAccount;
import com.cryonix.launcher.minecraft.model.LoaderProfile;
import com.cryonix.launcher.minecraft.model.MinecraftVersion;
import com.cryonix.launcher.minecraft.model.RendererProfile;

public final class LaunchRequest {
    public final MinecraftVersion version;
    public final GameAccount account;
    public final LoaderProfile loader;
    public final RendererProfile renderer;
    public final int memoryMb;

    public LaunchRequest(MinecraftVersion version, GameAccount account,
                         LoaderProfile loader, RendererProfile renderer, int memoryMb) {
        this.version = version;
        this.account = account;
        this.loader = loader;
        this.renderer = renderer;
        this.memoryMb = memoryMb;
    }
}