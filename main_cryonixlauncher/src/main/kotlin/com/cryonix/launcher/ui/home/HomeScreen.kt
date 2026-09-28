package com.cryonix.launcher.ui.home

import android.app.Activity
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
            UiMotion.press(launch)
            LaunchTaskManager.start(activity, store)
        }

        activity.findViewById<ImageButton>(R.id.home_versions).setOnClickListener {
            UiMotion.press(it)
            activity.startActivity(Intent(activity, InstancesActivity::class.java))
        }
        activity.findViewById<ImageButton>(R.id.home_accounts).setOnClickListener {
            UiMotion.press(it)
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        activity.findViewById<ImageButton>(R.id.home_refresh).setOnClickListener {
            UiMotion.press(it)
            activity.startActivity(Intent(activity, DownloadsActivity::class.java))
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        activity.findViewById<ImageButton>(R.id.home_settings).setOnClickListener {
            UiMotion.press(it)
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        activity.findViewById<TextView>(R.id.home_add_account).setOnClickListener {
            UiMotion.press(it)
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        activity.findViewById<TextView>(R.id.task_manager_close).setOnClickListener {
            UiMotion.press(it)
            LaunchTaskManager.close(activity)
        }

        bindPressAnimations(activity.findViewById(android.R.id.content))
        UiMotion.morphIn(activity.findViewById(R.id.home_launch))
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