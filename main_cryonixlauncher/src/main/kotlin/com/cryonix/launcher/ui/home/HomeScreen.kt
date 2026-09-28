package com.cryonix.launcher.ui.home

import android.app.Activity
import android.content.Intent
import android.widget.ImageButton
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.minecraft.MinecraftActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.settings.SettingsActivity

object HomeScreen {
    fun bind(activity: Activity, store: MinecraftSettingsStore, refresh: () -> Unit, render: () -> Unit) {
        activity.findViewById<TextView>(R.id.home_launch).setOnClickListener {
            activity.startActivity(Intent(activity, MinecraftActivity::class.java)
                .putExtra(MinecraftActivity.EXTRA_LAUNCH_NOW, true))
        }
        activity.findViewById<ImageButton>(R.id.home_versions).setOnClickListener {
            activity.startActivity(Intent(activity, MinecraftActivity::class.java)
                .putExtra(MinecraftActivity.EXTRA_SECTION, "versions"))
        }
        activity.findViewById<ImageButton>(R.id.home_accounts).setOnClickListener {
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
        }
        activity.findViewById<ImageButton>(R.id.home_refresh).setOnClickListener { refresh() }
        activity.findViewById<ImageButton>(R.id.home_settings).setOnClickListener {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
        activity.findViewById<TextView>(R.id.home_add_account).setOnClickListener {
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
        }
        render()
        if (store.autoRefresh) refresh()
    }
}