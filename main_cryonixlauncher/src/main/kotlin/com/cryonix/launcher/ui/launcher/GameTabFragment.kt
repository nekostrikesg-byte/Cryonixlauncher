package com.cryonix.launcher.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.cryonix.launcher.R
import com.cryonix.launcher.downloads.DownloadsActivity
import com.cryonix.launcher.instances.InstancesActivity
import com.cryonix.launcher.settings.ControlsActivity
import com.cryonix.launcher.settings.GamepadActivity
import com.cryonix.launcher.settings.JavaRuntimeActivity
import com.cryonix.launcher.settings.RuntimeSettingsActivity
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import java.io.File

class GameTabFragment : Fragment(R.layout.fragment_tab_game) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rows = view.findViewById<LinearLayout>(R.id.game_tab_rows)
        val context = requireContext()
        LauncherNavigationRows.add(context, rows, "Instances", "Create, select or edit a Minecraft profile", R.drawable.ic_nav_game) {
            startActivity(Intent(context, InstancesActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Minecraft downloads", "Fetch version files, libraries and assets", R.drawable.ic_menu_install_jar) {
            startActivity(Intent(context, DownloadsActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Renderer", "Choose a supported graphics renderer", R.drawable.ic_setting_video) {
            startActivity(Intent(context, RuntimeSettingsActivity::class.java).putExtra("section", "video"))
        }
        LauncherNavigationRows.add(context, rows, "Java runtime", "Manage Java used by game profiles", R.drawable.ic_setting_java_runtime) {
            startActivity(Intent(context, JavaRuntimeActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Touch controls", "Configure the on-screen layout", R.drawable.ic_menu_custom_controls) {
            startActivity(Intent(context, ControlsActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Controller", "Manage controller and gamepad mapping", R.drawable.ic_gamepad) {
            startActivity(Intent(context, GamepadActivity::class.java))
        }
        LauncherNavigationRows.add(context, rows, "Install mod or loader", "Open the existing installer for Fabric, Forge and mod files", R.drawable.ic_menu_install_jar) {
            val activity = requireActivity()
            if (activity is LauncherActivity) Tools.installMod(activity, false)
        }
        LauncherNavigationRows.add(context, rows, "Game directory", "Open the current profile's Minecraft files", R.drawable.ic_folder) {
            openGameDirectory()
        }
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
