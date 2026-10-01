package com.cryonix.launcher.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cryonix.launcher.R
import com.cryonix.launcher.core.PojavBridge
import com.cryonix.launcher.downloads.DownloadsActivity
import com.cryonix.launcher.instances.InstancesActivity
import com.kdt.mcgui.mcVersionSpinner
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import java.io.File

class HomeTabFragment : Fragment(R.layout.fragment_tab_home) {
    private var versionSpinner: mcVersionSpinner? = null
    private var accountLabel: TextView? = null
    private var launchStatus: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        versionSpinner = view.findViewById(R.id.mc_version_spinner)
        accountLabel = view.findViewById(R.id.home_current_account)
        launchStatus = view.findViewById(R.id.home_launch_status)

        versionSpinner?.reloadProfiles()
        view.findViewById<View>(R.id.edit_profile_button).setOnClickListener {
            versionSpinner?.openProfileEditor(requireActivity())
        }
        view.findViewById<View>(R.id.home_launch_game).setOnClickListener {
            launchStatus?.text = "Checking account and game profile…"
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
        }
        view.findViewById<View>(R.id.home_instances).setOnClickListener {
            startActivity(Intent(requireContext(), InstancesActivity::class.java))
        }
        view.findViewById<View>(R.id.home_downloads).setOnClickListener {
            startActivity(Intent(requireContext(), DownloadsActivity::class.java))
        }
        view.findViewById<View>(R.id.home_directory).setOnClickListener {
            runCatching { Tools.openPath(requireContext(), File(Tools.DIR_GAME_NEW), false) }
                .onFailure { Tools.showError(requireContext(), it) }
        }
        renderAccount()
    }

    override fun onResume() {
        super.onResume()
        versionSpinner?.reloadProfiles()
        renderAccount()
    }

    private fun renderAccount() {
        val name = runCatching { PojavBridge.currentAccountName(requireContext()) }.getOrDefault("")
        accountLabel?.text = if (name.isBlank()) "No account selected" else name
        if (name.isBlank()) launchStatus?.text = "Add an account to continue"
        else if (launchStatus?.text?.toString()?.startsWith("Checking") == true) launchStatus?.text = "Ready to launch"
    }

    override fun onDestroyView() {
        versionSpinner = null
        accountLabel = null
        launchStatus = null
        super.onDestroyView()
    }
}
