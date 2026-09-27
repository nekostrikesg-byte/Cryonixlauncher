package com.cryonix.launcher.minecraft

class LaunchPlanBuilder {
    fun build(request: LaunchRequest): LaunchPlan {
        require(request.memoryMb >= 512) { "At least 512 MB is required" }

        val args = buildList {
            add("--version")
            add(request.version.id)
            add("--launcher-brand")
            add("Cryonix Launcher")
            add("--loader")
            add(request.loader.type.name.lowercase())
            if (request.loader.loaderVersion.isNotEmpty()) {
                add("--loader-version")
                add(request.loader.loaderVersion)
            }
            add("--renderer")
            add(request.renderer.backend.name.lowercase())
            add("--memory-mb")
            add(request.memoryMb.toString())
        }
        return LaunchPlan(args)
    }
}
