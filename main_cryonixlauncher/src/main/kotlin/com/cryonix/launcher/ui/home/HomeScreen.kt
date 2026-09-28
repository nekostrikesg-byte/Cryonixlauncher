package com.cryonix.launcher.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.downloads.DownloadsActivity
import com.cryonix.launcher.instances.InstancesActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.settings.SettingsActivity
import com.cryonix.launcher.ui.UiMotion

object HomeScreen {
    fun bind(
        activity: Activity,
        store: MinecraftSettingsStore,
        refresh: () -> Unit,
        render: () -> Unit
    ) {
        activity.findViewById<TextView>(R.id.home_launch).setOnClickListener {
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
        }
        activity.findViewById<ImageButton>(R.id.home_settings).setOnClickListener {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
        activity.findViewById<TextView>(R.id.home_profile).setOnClickListener {
            showCompactDialog(
                activity,
                AlertDialog.Builder(activity)
                    .setTitle("Profile")
                    .setMessage("ishan1 • Offline")
                    .setPositiveButton("Accounts") { _, _ ->
                        activity.startActivity(Intent(activity, AccountsActivity::class.java))
                    }
                    .setNegativeButton("Close", null)
                    .create()
            )
        }
        activity.findViewById<View>(R.id.home_add_instance).setOnClickListener {
            showAddInstanceDialog(activity, store)
        }
        activity.findViewById<View>(R.id.home_new_instance).setOnClickListener {
            showAddInstanceDialog(activity, store)
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
        activity.findViewById<TextView>(R.id.task_manager_close).setOnClickListener {
            LaunchTaskManager.close(activity)
        }

        bindPressAnimations(activity.findViewById(android.R.id.content))
        renderInstances(activity, store)
        render()
        if (store.autoRefresh) refresh()
    }

    fun renderInstances(activity: Activity, store: MinecraftSettingsStore) {
        val list = activity.findViewById<LinearLayout>(R.id.home_instances_list)
        val count = activity.findViewById<TextView>(R.id.home_instance_count)
        list.removeAllViews()

        val names = store.instances
        count.text = names.size.toString()

        if (names.isEmpty()) {
            val empty = TextView(activity).apply {
                text = "No instances yet\nTap + New Instance to create one"
                textSize = 12f
                setTextColor(activity.getColor(R.color.cryonix_text_secondary))
                gravity = Gravity.CENTER
                setPadding(20, 10, 20, 10)
            }
            list.addView(empty, LinearLayout.LayoutParams(250, 72))
        } else {
            names.forEach { name ->
                list.addView(createInstanceCard(activity, store, name))
            }
        }

        bindPressAnimations(list)
    }

    private fun createInstanceCard(
        activity: Activity,
        store: MinecraftSettingsStore,
        name: String
    ): View {
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 8, 8, 8)
            setBackgroundResource(R.drawable.bg_reference_row)
        }

        val icon = ImageButton(activity).apply {
            layoutParams = LinearLayout.LayoutParams(dp(activity, 48), dp(activity, 48))
            setImageResource(R.drawable.ic_grass_block)
            background = null
            contentDescription = "Launch $name"
            setPadding(6, 6, 6, 6)
            setOnClickListener {
                UiMotion.press(this)
                LaunchTaskManager.start(activity, store)
            }
        }

        val title = TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply {
                marginStart = 8
            }
            text = name
            textSize = 13f
            setTextColor(activity.getColor(R.color.cryonix_text))
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val play = TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(dp(activity, 42), dp(activity, 42)).apply {
                marginStart = 6
            }
            text = "▶"
            gravity = Gravity.CENTER
            textSize = 13f
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.bg_reference_launch)
            contentDescription = "Launch $name"
            setOnClickListener {
                LaunchTaskManager.start(activity, store)
            }
        }

        val delete = TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(dp(activity, 38), dp(activity, 42)).apply {
                marginStart = 5
            }
            text = "×"
            gravity = Gravity.CENTER
            textSize = 20f
            setTextColor(activity.getColor(R.color.cryonix_text_secondary))
            setBackgroundResource(R.drawable.bg_reference_pill)
            contentDescription = "Delete $name"
            setOnClickListener {
                showDeleteDialog(activity, store, name)
            }
        }

        card.addView(icon)
        card.addView(title)
        card.addView(play)
        card.addView(delete)
        val params = LinearLayout.LayoutParams(dp(activity, 290), dp(activity, 72)).apply {
            marginEnd = 10
        }
        card.layoutParams = params
        return card
    }

    private fun showAddInstanceDialog(activity: Activity, store: MinecraftSettingsStore) {
        val input = EditText(activity).apply {
            hint = "Instance name"
            setSingleLine(true)
            setTextColor(activity.getColor(R.color.cryonix_text))
            setHintTextColor(activity.getColor(R.color.cryonix_text_secondary))
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle("New Instance")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                if (store.addInstance(input.text.toString())) {
                    renderInstances(activity, store)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        showCompactDialog(activity, dialog)
    }

    private fun showDeleteDialog(
        activity: Activity,
        store: MinecraftSettingsStore,
        name: String
    ) {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Delete instance?")
            .setMessage(name)
            .setPositiveButton("Delete") { _, _ ->
                store.removeInstance(name)
                renderInstances(activity, store)
            }
            .setNegativeButton("Cancel", null)
            .create()
        showCompactDialog(activity, dialog)
    }

    private fun showCompactDialog(activity: Activity, dialog: AlertDialog) {
        dialog.setOnShowListener {
            val density = activity.resources.displayMetrics.density
            dialog.window?.setLayout(dp(activity, 340), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        dialog.show()
    }

    private fun bindPressAnimations(root: View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }
}
