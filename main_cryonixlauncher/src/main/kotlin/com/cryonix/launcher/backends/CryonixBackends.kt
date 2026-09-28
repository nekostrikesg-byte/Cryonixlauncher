package com.cryonix.launcher.backends

import android.content.Context
import com.cryonix.launcher.minecraft.LaunchRequest

/**
 * Single backend entry point used by Cryonix Launcher.
 * The UI talks only to this facade; backend implementation stays replaceable.
 */
object CryonixBackends {
    const val NAME = "Cryonix Backends"

    private val mojo = MojoBackend()
    private val pojav = PojavBackend()

    @Volatile
    var selectedId: String = "mojo"

    fun selected(): CryonixBackend =
        if (selectedId == "pojav") pojav else mojo

    fun available(): List<CryonixBackend> = listOf(mojo, pojav)

    fun prepare(context: Context, request: LaunchRequest): BackendLaunch =
        selected().prepare(context, request)
}