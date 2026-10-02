package com.cryonix.launcher.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.cryonix.launcher.R
import com.cryonix.launcher.downloads.DownloadsActivity
import net.kdt.pojavlaunch.CustomControlsActivity
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.fragments.ProfileEditorFragment
import net.kdt.pojavlaunch.fragments.ProfileTypeSelectFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import java.io.File

/** Game actions routed to Pojav's profile editor, version catalog and engine preferences. */
class GameTabFragment : Fragment(R.layout.fragment_tab_game) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rows = view.findViewById<LinearLayout>(R.id.game_tab_rows)
        val context = requireContext()
        LauncherNavigationRows.add(context, rows, "Minecraft profiles", "Create, select or edit versions and mod loaders", R.drawable.ic_nav_game) {
            Tools.swapFragment(requireActivity(), ProfileTypeSelectFragment::class.java, ProfileTypeSelectFragment.TAG, null)
        }
        LauncherNavigationRows.add(context, rows, "Edit selected profile", "Change the current version, game directory and JVM options", R.drawable.ic_edit_profile) {
            Tools.swapFragment(requireActivity(), ProfileEditorFragment::class.java, ProfileEditorFragment.TAG, null)
        }
        LauncherNavigationRows.add(context, rows, "Downloads and tasks", "Review version downloads and engine tasks", R.drawable.ic_menu_install_jar) {
            startActivity(Intent(context, DownloadsActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Renderer and video", "Choose a supported graphics renderer", R.drawable.ic_setting_video) {
            openBackendPreference(LauncherPreferenceVideoFragment::class.java)
        }
        LauncherNavigationRows.add(context, rows, "Java runtime", "Select and manage Android Java runtimes", R.drawable.ic_setting_java_runtime) {
            openBackendPreference(LauncherPreferenceJavaFragment::class.java)
        }
        LauncherNavigationRows.add(context, rows, "Touch controls", "Configure the on-screen layout", R.drawable.ic_menu_custom_controls) {
            startActivity(Intent(context, CustomControlsActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Controller", "Manage controller and gamepad preferences", R.drawable.ic_gamepad) {
            openBackendPreference(LauncherPreferenceControlFragment::class.java)
        }
        LauncherNavigationRows.add(context, rows, "Install mod or loader", "Open the existing installer for mod and loader files", R.drawable.ic_menu_install_jar) {
            val activity = requireActivity()
            if (activity is LauncherActivity) Tools.installMod(activity, false)
        }
        LauncherNavigationRows.add(context, rows, "Game directory", "Open the current profile's Minecraft files", R.drawable.ic_folder) {
            openGameDirectory()
        }
    }

    private fun openBackendPreference(fragmentClass: Class<out androidx.fragment.app.Fragment>) {
        Tools.swapFragment(requireActivity(), fragmentClass, null, null)
    }

    private fun openGameDirectory() {
        val context = requireContext()
        runCatching {
            LauncherProfiles.load()
            val profileKey = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "")
            val profile = LauncherProfiles.mainProfileJson.profiles[profileKey]
            val directory = if (profile == null) File(Tools.DIR_GAME_NEW) else Tools.getGameDirPath(profile)
            Tools.openPath(context, directory, false)
        }.onFailure { Tools.showError(context, it) }
    }
}
