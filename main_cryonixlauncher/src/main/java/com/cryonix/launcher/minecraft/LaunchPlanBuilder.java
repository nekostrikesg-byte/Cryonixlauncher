package com.cryonix.launcher.minecraft;

import com.cryonix.launcher.minecraft.model.GameAccount;
import java.util.ArrayList;
import java.util.List;

public final class LaunchPlanBuilder {
    public LaunchPlan build(LaunchRequest request) {
        if (request.version == null) throw new IllegalArgumentException("Minecraft version is required");
        if (request.account == null) throw new IllegalArgumentException("Account is required");
        if (request.loader == null) throw new IllegalArgumentException("Loader is required");
        if (request.renderer == null) throw new IllegalArgumentException("Renderer is required");
        if (request.memoryMb < 512) throw new IllegalArgumentException("At least 512 MB is required");

        List<String> args = new ArrayList<>();
        args.add("--version");
        args.add(request.version.id);
        args.add("--launcher-brand");
        args.add("Cryonix Launcher");
        args.add("--loader");
        args.add(request.loader.type.name().toLowerCase());
        if (!request.loader.loaderVersion.isEmpty()) {
            args.add("--loader-version");
            args.add(request.loader.loaderVersion);
        }
        args.add("--renderer");
        args.add(request.renderer.backend.name().toLowerCase());
        args.add("--memory-mb");
        args.add(Integer.toString(request.memoryMb));
        return new LaunchPlan(args);
    }
}