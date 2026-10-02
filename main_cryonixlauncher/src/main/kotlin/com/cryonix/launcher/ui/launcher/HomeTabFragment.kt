package com.cryonix.launcher.ui.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cryonix.launcher.R
import com.cryonix.launcher.settings.AboutActivity
import net.kdt.pojavlaunch.PojavProfile
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.fragments.ProfileEditorFragment
import net.kdt.pojavlaunch.fragments.ProfileTypeSelectFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment
import net.kdt.pojavlaunch.value.MinecraftAccount
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile
import java.io.File
import java.text.DateFormat
import java.util.Date

/** Cryonix dashboard; profile, auth and launch actions use the integrated Pojav engine directly. */
class HomeTabFragment : Fragment(R.layout.fragment_tab_home) {
    private var accountAvatar: ImageView? = null
    private var accountName: TextView? = null
    private var accountStatus: TextView? = null
    private var instanceTitle: TextView? = null
    private var instanceVersion: TextView? = null
    private var launchStatus: TextView? = null
    private var logTitle: TextView? = null
    private var logSubtitle: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        accountAvatar = view.findViewById(R.id.home_account_avatar)
        accountName = view.findViewById(R.id.home_current_account)
        accountStatus = view.findViewById(R.id.home_account_status)
        instanceTitle = view.findViewById(R.id.home_instance_title)
        instanceVersion = view.findViewById(R.id.home_instance_version)
        launchStatus = view.findViewById(R.id.home_launch_status)
        logTitle = view.findViewById(R.id.home_log_title)
        logSubtitle = view.findViewById(R.id.home_log_subtitle)

        view.findViewById<View>(R.id.home_launch_game).setOnClickListener {
            launchStatus?.text = "Checking account and game profile…"
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
        }
        view.findViewById<View>(R.id.home_instance_row).setOnClickListener {
            Tools.swapFragment(requireActivity(), ProfileEditorFragment::class.java, ProfileEditorFragment.TAG, null)
        }
        view.findViewById<View>(R.id.home_news_profiles).setOnClickListener {
            Tools.swapFragment(requireActivity(), ProfileTypeSelectFragment::class.java, ProfileTypeSelectFragment.TAG, null)
        }
        view.findViewById<View>(R.id.home_news_settings).setOnClickListener {
            Tools.swapFragment(requireActivity(), LauncherPreferenceVideoFragment::class.java, null, null)
        }
        view.findViewById<View>(R.id.home_news_view_all).setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
        view.findViewById<View>(R.id.home_activity_details).setOnClickListener { openGameDirectory() }
        view.findViewById<View>(R.id.home_log_title).setOnClickListener { openLatestLog() }
        view.findViewById<View>(R.id.home_current_account).setOnClickListener { openAccounts() }
        view.findViewById<View>(R.id.home_account_avatar).setOnClickListener { openAccounts() }
        view.findViewById<View>(R.id.home_discord).setOnClickListener {
            runCatching {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.discord_invite))))
            }
        }
        view.findViewById<View>(R.id.home_directory).setOnClickListener { openGameDirectory() }

        renderDashboard()
    }

    override fun onResume() {
        super.onResume()
        if (accountName != null) renderDashboard()
    }

    private fun renderDashboard() {
        val context = requireContext()
        val selectedAccount = runCatching { PojavProfile.getCurrentProfileName(context) }.getOrDefault("")
        val account = runCatching { MinecraftAccount.load(selectedAccount) }.getOrNull()
        val skinFace = runCatching { account?.skinFace }.getOrNull()
        if (skinFace != null) accountAvatar?.setImageBitmap(skinFace)
        else accountAvatar?.setImageResource(R.drawable.cryonix_logo)
        accountName?.text = account?.username?.takeIf { it.isNotBlank() } ?: selectedAccount.ifBlank { "Select an account" }
        accountStatus?.text = when {
            account == null -> "Offline"
            account.isMicrosoft -> "Microsoft"
            account.username?.startsWith("Demo.") == true -> "Demo"
            else -> "Offline"
        }
        if (selectedAccount.isBlank()) launchStatus?.text = "Add an account to continue"
        else if (launchStatus?.text?.startsWith("Checking") == true) launchStatus?.text = "Ready to launch"

        val profileDetails = runCatching { currentProfileDetails() }.getOrNull()
        instanceTitle?.text = profileDetails?.first ?: "Default profile"
        instanceVersion?.text = profileDetails?.second ?: "Choose a Minecraft version"
        renderLatestLog()
    }

    private fun currentProfile(): Pair<String, MinecraftProfile?> {
        LauncherProfiles.load()
        val profileKey = LauncherPreferences.DEFAULT_PREF.getString(
            LauncherPreferences.PREF_KEY_CURRENT_PROFILE,
            ""
        ).orEmpty()
        return profileKey to LauncherProfiles.mainProfileJson?.profiles?.get(profileKey)
    }

    private fun currentProfileDetails(): Pair<String, String> {
        val (profileKey, profile) = currentProfile()
        val title = profileKey.takeIf { it.isNotBlank() } ?: "Default profile"
        val version = profile?.lastVersionId?.takeIf { it.isNotBlank() } ?: "Choose a Minecraft version"
        return title to version
    }

    private fun currentGameDirectory(): File {
        val (_, profile) = currentProfile()
        return if (profile == null) File(Tools.DIR_GAME_NEW) else Tools.getGameDirPath(profile)
    }

    private fun renderLatestLog() {
        val files = runCatching {
            File(currentGameDirectory(), "logs").listFiles { file ->
                file.isFile && file.name.endsWith(".log", ignoreCase = true)
            }.orEmpty().sortedByDescending { it.lastModified() }
        }.getOrDefault(emptyList())
        if (files.isEmpty()) {
            logTitle?.text = "No log available yet"
            logSubtitle?.text = "Start a game to see its latest log here."
        } else {
            logTitle?.text = files.first().name
            logSubtitle?.text = "Updated ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(files.first().lastModified()))}"
        }
    }

    private fun openAccounts() {
        (parentFragment as? LauncherShellFragment)?.openAccountsTab()
    }

    private fun openGameDirectory() {
        runCatching { Tools.openPath(requireContext(), currentGameDirectory(), false) }
            .onFailure { Tools.showError(requireContext(), it) }
    }

    private fun openLatestLog() {
        runCatching {
            val latest = File(currentGameDirectory(), "logs")
                .listFiles { file -> file.isFile && file.name.endsWith(".log", ignoreCase = true) }
                .orEmpty()
                .maxByOrNull { it.lastModified() }
            if (latest == null) openGameDirectory() else Tools.openPath(requireContext(), latest, false)
        }.onFailure { Tools.showError(requireContext(), it) }
    }

    override fun onDestroyView() {
        accountAvatar = null
        accountName = null
        accountStatus = null
        instanceTitle = null
        instanceVersion = null
        launchStatus = null
        logTitle = null
        logSubtitle = null
        super.onDestroyView()
    }
}
