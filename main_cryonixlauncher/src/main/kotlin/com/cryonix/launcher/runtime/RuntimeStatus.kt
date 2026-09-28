package com.cryonix.launcher.runtime

import android.content.Context

object RuntimeStatus {
    fun describe(context: Context): String =
        MinecraftRuntimeBridge(context).describe()
}
