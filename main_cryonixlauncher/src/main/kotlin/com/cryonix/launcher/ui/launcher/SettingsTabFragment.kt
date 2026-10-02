package com.cryonix.launcher.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.cryonix.launcher.R
import com.cryonix.launcher.downloads.DownloadsActivity
import com.cryonix.launcher.settings.AboutActivity
import net.kdt.pojavlaunch.CustomControlsActivity
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.fragments.ProfileTypeSelectFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceExperimentalFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment

/** Settings shortcuts that navigate directly into the integrated launcher's own preference screens. */
class SettingsTabFragment : Fragment(R.layout.fragment_tab_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rows = view.findViewById<LinearLayout>(R.id.settings_tab_rows)
        val context = requireContext()
        LauncherNavigationRows.add(context, rows, "Accounts", "Sign in or add a local/offline profile", R.drawable.ic_nav_account) {
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true)
        }
        LauncherNavigationRows.add(context, rows, "Launcher and runtime", "General launcher and engine preferences", R.drawable.ic_nav_settings) {
            openBackendPreference(LauncherPreferenceFragment::class.java)
        }
        LauncherNavigationRows.add(context, rows, "Minecraft and versions", "Create or edit profiles, versions and mod loaders", R.drawable.ic_nav_game) {
            Tools.swapFragment(requireActivity(), ProfileTypeSelectFragment::class.java, ProfileTypeSelectFragment.TAG, null)
        }
        LauncherNavigationRows.add(context, rows, "Renderer and video", "Graphics backend, renderer and display scaling", R.drawable.ic_setting_video) {
            openBackendPreference(LauncherPreferenceVideoFragment::class.java)
        }
        LauncherNavigationRows.add(context, rows, "Java runtime", "Install and choose an Android Java runtime", R.drawable.ic_setting_java_runtime) {
            openBackendPreference(LauncherPreferenceJavaFragment::class.java)
        }
        LauncherNavigationRows.add(context, rows, "Touch controls", "Edit the on-screen layout and controls", R.drawable.ic_menu_custom_controls) {
            startActivity(Intent(context, CustomControlsActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Controller", "Configure controller and input preferences", R.drawable.ic_gamepad) {
            openBackendPreference(LauncherPreferenceControlFragment::class.java)
        }
        LauncherNavigationRows.add(context, rows, "Downloads and tasks", "View active Minecraft downloads and engine tasks", R.drawable.ic_menu_install_jar) {
            startActivity(Intent(context, DownloadsActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Launcher options", "Storage, interface and miscellaneous preferences", R.drawable.ic_setting_misc) {
            openBackendPreference(LauncherPreferenceMiscellaneousFragment::class.java)
        }
        LauncherNavigationRows.add(context, rows, "Experimental options", "Optional backend and renderer preferences", R.drawable.ic_setting_engine) {
            openBackendPreference(LauncherPreferenceExperimentalFragment::class.java)
        }
        LauncherNavigationRows.add(context, rows, "Notifications", "Manage Cryonixlauncher notifications", R.drawable.ic_menu_news) {
            startActivity(Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName))
        }
        LauncherNavigationRows.add(context, rows, "About Cryonixlauncher", "Version, project information and credits", R.drawable.ic_nav_about) {
            startActivity(Intent(context, AboutActivity::class.java))
        }
    }

    private fun openBackendPreference(fragmentClass: Class<out androidx.fragment.app.Fragment>) {
        Tools.swapFragment(requireActivity(), fragmentClass, null, null)
    }
}
