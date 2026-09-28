package com.cryonix.launcher.backends

import android.content.Context
import com.cryonix.launcher.minecraft.LaunchRequest

interface CryonixBackend {
    val id: String
    val displayName: String
    val sourceProject: String

    fun prepare(context: Context, request: LaunchRequest): BackendLaunch
}