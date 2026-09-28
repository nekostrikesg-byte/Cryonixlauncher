package com.cryonix.launcher.backends

import android.content.Context
import com.cryonix.launcher.minecraft.LaunchPlanBuilder
import com.cryonix.launcher.minecraft.LaunchRequest

/**
 * Cryonix backend adapter for the MojoLauncher/Pojav-compatible runtime.
 * MojoLauncher is LGPL-3.0 and is based on PojavLauncher.
 */
class MojoBackend : CryonixBackend {
    override val id = "mojo"
    override val displayName = "Cryonix Backends · Mojo"
    override val sourceProject = "MojoLauncher/MojoLauncher"

    override fun prepare(context: Context, request: LaunchRequest): BackendLaunch {
        val plan = LaunchPlanBuilder().build(request)
        return BackendLaunch(
            backendId = id,
            backendName = displayName,
            arguments = plan.arguments,
            ready = false,
            message = "Mojo-compatible launch plan prepared; runtime components are not bundled yet."
        )
    }
}