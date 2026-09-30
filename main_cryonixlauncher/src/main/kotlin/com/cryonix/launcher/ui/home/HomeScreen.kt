package com.cryonix.launcher.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.instances.InstancesActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.settings.ModernSettingsActivity
import com.cryonix.launcher.settings.ControlsActivity
import com.cryonix.launcher.ui.UiMotion

object HomeScreen {
    private const val PICK_JAR = 4101

    fun bind(
        activity: Activity,
        store: MinecraftSettingsStore,
        refresh: () -> Unit,
        render: () -> Unit
    ) {
        activity.findViewById<TextView>(R.id.home_launch).setOnClickListener {
            LaunchTaskManager.start(activity, store)
        }
        activity.findViewById<TextView>(R.id.home_accounts).setOnClickListener {
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
        }
        activity.findViewById<TextView>(R.id.home_profile).setOnClickListener {
            activity.startActivity(Intent(activity, AccountsActivity::class.java))
        }
        activity.findViewById<View>(R.id.home_settings).setOnClickListener {
            activity.startActivity(Intent(activity, ModernSettingsActivity::class.java))
        }
        activity.findViewById<TextView>(R.id.home_wiki).setOnClickListener {
            openUrl(activity, "https://github.com/nekostrikesg-byte/Cryonixlauncher/wiki")
        }
        activity.findViewById<TextView>(R.id.home_discord).setOnClickListener {
            openUrl(activity, "https://discord.com/")
        }
        activity.findViewById<TextView>(R.id.home_custom_controls).setOnClickListener {
            activity.startActivity(Intent(activity, ControlsActivity::class.java))
        }
        activity.findViewById<TextView>(R.id.home_execute_jar).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/java-archive"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            activity.startActivityForResult(intent, PICK_JAR)
        }
        activity.findViewById<TextView>(R.id.home_share_log).setOnClickListener {
            val selected = store.selectedInstanceName ?: "No instance selected"
            val text = "Cryonix Launcher log\nBackend: Android Minecraft runtime\nStatus: " + selected
            activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_SUBJECT, "Cryonix Launcher log")
            }, "Share log"))
        }
        activity.findViewById<TextView>(R.id.home_open_directory).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            activity.startActivity(intent)
        }
        activity.findViewById<View>(R.id.home_edit_profile).setOnClickListener {
            activity.startActivity(Intent(activity, InstancesActivity::class.java))
        }
        activity.findViewById<TextView>(R.id.task_manager_close).setOnClickListener {
            LaunchTaskManager.close(activity)
        }
        renderInstances(activity, store)
        render()
        bindPressAnimations(activity.findViewById(android.R.id.content))
        if (store.autoRefresh) refresh()
    }

    fun renderInstances(activity: Activity, store: MinecraftSettingsStore) {
        val count = activity.findViewById<TextView>(R.id.home_instance_count)
        val selectedLabel = activity.findViewById<TextView>(R.id.home_selected_label)
        val selected = store.selectedInstanceName ?: store.instances.firstOrNull()
        count.text = store.instances.size.toString()
        selectedLabel.text = selected ?: "No instance"
        activity.findViewById<TextView>(R.id.home_profile).text =
            if (selected == null) "" else "offline • " + selected
    }

    private fun openUrl(activity: Activity, url: String) {
        runCatching { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    private fun bindPressAnimations(root: View) {
        UiMotion.bindPress(root)
    }
}
