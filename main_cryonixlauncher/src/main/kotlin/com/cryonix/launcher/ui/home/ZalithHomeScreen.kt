package com.cryonix.launcher.ui.home

import android.app.Activity
import android.content.Intent
import android.widget.ImageButton
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftActivity
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.settings.SettingsActivity

/**
 * Cryonix implementation of the Zalith 2+ home-screen interaction model.
 * The visual layout remains native XML so it stays compatible with Cryonix's
 * existing launcher architecture.
 */
object ZalithHomeScreen {
    fun bind(
        activity: Activity,
        store: MinecraftSettingsStore,
        refresh: () -> Unit,
        render: () -> Unit
    ) {
        activity.findViewById<TextView>(R.id.home_launch).setOnClickListener {
            openMinecraft(activity, launchNow = true)
        }
        activity.findViewById<ImageButton>(R.id.home_versions).setOnClickListener {
            openMinecraft(activity, "versions")
        }
        activity.findViewById<ImageButton>(R.id.home_accounts).setOnClickListener {
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
        }
        activity.findViewById<ImageButton>(R.id.home_refresh).setOnClickListener {
            refresh()
        }
        activity.findViewById<ImageButton>(R.id.home_settings).setOnClickListener {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
        activity.findViewById<TextView>(R.id.home_add_account).setOnClickListener {
            openMinecraft(activity, "accounts")
        }
        render()
        if (store.autoRefresh) refresh()
    }

    private fun openMinecraft(activity: Activity, section: String? = null, launchNow: Boolean = false) {
        activity.startActivity(
            Intent(activity, MinecraftActivity::class.java).apply {
                section?.let { putExtra(MinecraftActivity.EXTRA_SECTION, it) }
                putExtra(MinecraftActivity.EXTRA_LAUNCH_NOW, launchNow)
            }
        )
    }
}
