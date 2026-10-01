package com.cryonix.launcher.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.downloads.DownloadsActivity
import com.cryonix.launcher.instances.InstancesActivity
import com.cryonix.launcher.settings.ControlsActivity
import com.cryonix.launcher.settings.GamepadActivity
import com.cryonix.launcher.settings.JavaRuntimeActivity
import com.cryonix.launcher.settings.RuntimeSettingsActivity

class SettingsTabFragment : Fragment(R.layout.fragment_tab_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rows = view.findViewById<LinearLayout>(R.id.settings_tab_rows)
        val context = requireContext()
        LauncherNavigationRows.add(context, rows, "Accounts", "Saved Minecraft accounts and local profiles", R.drawable.ic_nav_account) {
            startActivity(Intent(context, AccountsActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Launcher and runtime", "General launcher options and backend preferences", R.drawable.ic_nav_settings) {
            openRuntimeSettings()
        }
        LauncherNavigationRows.add(context, rows, "Minecraft and versions", "Instances, versions and mod loaders", R.drawable.ic_nav_game) {
            startActivity(Intent(context, InstancesActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Renderer and video", "Graphics backend, renderer and display scaling", R.drawable.ic_setting_video) {
            openRuntimeSettings("video")
        }
        LauncherNavigationRows.add(context, rows, "Java runtime", "Install and choose Java versions", R.drawable.ic_setting_java_runtime) {
            startActivity(Intent(context, JavaRuntimeActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Touch controls", "Edit on-screen controls and gestures", R.drawable.ic_menu_custom_controls) {
            startActivity(Intent(context, ControlsActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Controller", "Gamepad mapping and input options", R.drawable.ic_gamepad) {
            startActivity(Intent(context, GamepadActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Downloads and tasks", "Download Minecraft and review task status", R.drawable.ic_menu_install_jar) {
            startActivity(Intent(context, DownloadsActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Notifications", "Manage Cryonixlauncher notifications", R.drawable.ic_menu_news) {
            startActivity(Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName))
        }
        LauncherNavigationRows.add(context, rows, "About Cryonixlauncher", "Version, project information and credits", R.drawable.ic_nav_about) {
            startActivity(Intent(context, com.cryonix.launcher.settings.AboutActivity::class.java))
        }
    }

    private fun openRuntimeSettings(section: String? = null) {
        val intent = Intent(requireContext(), RuntimeSettingsActivity::class.java)
        if (section != null) intent.putExtra("section", section)
        startActivity(intent)
    }
}
