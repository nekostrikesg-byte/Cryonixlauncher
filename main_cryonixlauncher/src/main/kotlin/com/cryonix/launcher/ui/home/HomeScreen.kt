package com.cryonix.launcher.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.instances.InstancesActivity
import com.cryonix.launcher.downloads.DownloadsActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.settings.SettingsActivity
import com.cryonix.launcher.ui.UiMotion

object HomeScreen {
    fun bind(activity: Activity, store: MinecraftSettingsStore, refresh: () -> Unit, render: () -> Unit) {
        val launch = activity.findViewById<TextView>(R.id.home_launch)
        launch.setOnClickListener {
            LaunchTaskManager.start(activity, store)
        }

        activity.findViewById<ImageButton>(R.id.home_versions).setOnClickListener {
            activity.startActivity(Intent(activity, InstancesActivity::class.java))
        }
        activity.findViewById<ImageButton>(R.id.home_accounts).setOnClickListener {
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
        }
        activity.findViewById<ImageButton>(R.id.home_refresh).setOnClickListener {
            activity.startActivity(Intent(activity, DownloadsActivity::class.java))
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        activity.findViewById<ImageButton>(R.id.home_settings).setOnClickListener {
            UiMotion.press(it)
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        activity.findViewById<TextView>(R.id.home_add_account).setOnClickListener {
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
        }
        activity.findViewById<TextView>(R.id.home_profile).setOnClickListener {
            AlertDialog.Builder(activity)
                .setTitle("Offline Profile")
                .setMessage("Profile: ishan1\nMode: Offline\nStatus: Ready")
                .setPositiveButton("Accounts") { _, _ ->
                    activity.startActivity(Intent(activity, AccountsActivity::class.java))
                }
                .setNegativeButton("Close", null)
                .show()
        }
        activity.findViewById<View>(R.id.home_add_instance).setOnClickListener {
            activity.startActivity(Intent(activity, InstancesActivity::class.java))
        }
        activity.findViewById<View>(R.id.home_manage_instances).setOnClickListener {
            activity.startActivity(Intent(activity, InstancesActivity::class.java))
        }
        activity.findViewById<View>(R.id.home_edit_profile).setOnClickListener {
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
        }
        activity.findViewById<View>(R.id.home_settings_row).setOnClickListener {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
        activity.findViewById<View>(R.id.home_weekly_card).setOnClickListener {
            showInstanceDialog(activity, "Cryonix Client", "fabric-loader-0.16.9-1.20.1")
        }
        activity.findViewById<View>(R.id.home_minecraft_card).setOnClickListener {
            showInstanceDialog(activity, "Minecraft 1.20.1", "Vanilla")
        }
        activity.findViewById<View>(R.id.home_cs_card).setOnClickListener {
            showInstanceDialog(activity, "CS Client", "Modpack")
        }
        activity.findViewById<View>(R.id.home_fabric_card).setOnClickListener {
            showInstanceDialog(activity, "Fabric Modpack", "Modpack")
        }

        activity.findViewById<TextView>(R.id.task_manager_close).setOnClickListener {
            LaunchTaskManager.close(activity)
        }

        bindPressAnimations(activity.findViewById(android.R.id.content))
        render()
        if (store.autoRefresh) refresh()
    }

    private fun bindPressAnimations(root: View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }
}

    private fun showInstanceDialog(activity: Activity, title: String, type: String) {
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage("Instance type: $type\nStatus: Ready\nTap Launch to start this profile.")
            .setPositiveButton("Launch") { _, _ ->
                LaunchTaskManager.start(activity, MinecraftSettingsStore(activity))
            }
            .setNegativeButton("Close", null)
            .show()
    }
