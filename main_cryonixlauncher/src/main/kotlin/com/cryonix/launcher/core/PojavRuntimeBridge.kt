package com.cryonix.launcher.core

import android.app.Activity
import android.content.Intent

/**
 * Thin Cryonix -> upstream Pojav runtime hand-off.
 *
 * Pojav's source tree remains under engine/pojav and its original package/class
 * names are not renamed. Cryonix only starts the upstream LauncherActivity.
 */
object PojavRuntimeBridge {
    private const val POJAV_LAUNCHER_ACTIVITY = "net.kdt.pojavlaunch.LauncherActivity"

    fun openLauncher(activity: Activity): Boolean {
        return runCatching {
            val component = Class.forName(POJAV_LAUNCHER_ACTIVITY)
            activity.startActivity(Intent(activity, component))
            true
        }.getOrElse { false }
    }
}
