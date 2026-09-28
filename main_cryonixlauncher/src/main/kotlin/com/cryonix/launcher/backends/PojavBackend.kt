package com.cryonix.launcher.backends

import android.content.Context
import com.cryonix.launcher.minecraft.LaunchPlanBuilder
import com.cryonix.launcher.minecraft.LaunchRequest

/**
 * Cryonix backend adapter for the PojavLauncher-compatible runtime.
 * PojavLauncher source remains separately licensed under LGPL-3.0.
 */
class PojavBackend : CryonixBackend {
    override val id = "pojav"
    override val displayName = "Cryonix Backends · Pojav"
    override val sourceProject = "PojavLauncherTeam/PojavLauncher"

    override fun prepare(context: Context, request: LaunchRequest): BackendLaunch {
        val plan = LaunchPlanBuilder().build(request)
        return BackendLaunch(
            backendId = id,
            backendName = displayName,
            arguments = plan.arguments,
            ready = false,
            message = "Pojav-compatible launch plan prepared; runtime components are not bundled yet."
        )
    }
}