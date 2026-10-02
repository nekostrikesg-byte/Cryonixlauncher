package com.cryonix.launcher.ui.launcher

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cryonix.launcher.R
import com.cryonix.launcher.downloads.DownloadsActivity
import net.kdt.pojavlaunch.PojavProfile
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.value.MinecraftAccount
import java.io.File

/** Persistent launcher shell with a compact left rail and the reference-style header. */
class LauncherShellFragment : Fragment(R.layout.fragment_launcher_shell) {
    private enum class Tab(val tag: String, val buttonId: Int, val fragment: () -> Fragment) {
        HOME("cryonix-home", R.id.nav_home, { HomeTabFragment() }),
        ACCOUNTS("cryonix-accounts", R.id.nav_accounts, { AccountsTabFragment() }),
        SETTINGS("cryonix-settings", R.id.nav_settings, { SettingsTabFragment() }),
        GAME("cryonix-game", R.id.nav_game, { GameTabFragment() }),
        ABOUT("cryonix-about", R.id.nav_about, { AboutTabFragment() })
    }

    private var selectedTab = Tab.HOME
    private val buttons = mutableMapOf<Tab, ImageButton>()
    private var accountAvatar: ImageView? = null
    private var accountLabel: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tab.values().forEach { tab ->
            val button = view.findViewById<ImageButton>(tab.buttonId)
            buttons[tab] = button
            button.setOnClickListener { showTab(tab) }
        }
        accountAvatar = view.findViewById(R.id.header_account_avatar)
        accountLabel = view.findViewById(R.id.header_account_name)
        view.findViewById<View>(R.id.header_profile).setOnClickListener { openAccountsTab() }
        view.findViewById<View>(R.id.header_accounts).setOnClickListener { openAccountsTab() }
        view.findViewById<View>(R.id.header_downloads).setOnClickListener {
            startActivity(Intent(requireContext(), DownloadsActivity::class.java))
        }
        view.findViewById<View>(R.id.header_folder).setOnClickListener {
            runCatching { Tools.openPath(requireContext(), File(Tools.DIR_GAME_NEW), false) }
                .onFailure { Tools.showError(requireContext(), it) }
        }

        selectedTab = savedInstanceState?.getString(STATE_TAB)?.let { value ->
            Tab.values().firstOrNull { it.name == value }
        } ?: Tab.HOME

        val transaction = childFragmentManager.beginTransaction().setReorderingAllowed(true)
        Tab.values().forEach { tab ->
            val fragment = childFragmentManager.findFragmentByTag(tab.tag)
            if (fragment == null) {
                val created = tab.fragment()
                transaction.add(R.id.launcher_tab_container, created, tab.tag)
                if (tab != selectedTab) transaction.hide(created)
            } else if (tab == selectedTab) transaction.show(fragment) else transaction.hide(fragment)
        }
        transaction.commitNow()
        updateNavigationSelection()
        updateAccountLabel()
    }

    override fun onResume() {
        super.onResume()
        if (accountLabel != null) updateAccountLabel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_TAB, selectedTab.name)
        super.onSaveInstanceState(outState)
    }

    private fun showTab(tab: Tab) {
        if (tab == selectedTab) return
        val manager = childFragmentManager
        val oldFragment = manager.findFragmentByTag(selectedTab.tag)
        val newFragment = manager.findFragmentByTag(tab.tag)
        val transaction = manager.beginTransaction().setReorderingAllowed(true)
        if (oldFragment != null) transaction.hide(oldFragment)
        if (newFragment == null) transaction.add(R.id.launcher_tab_container, tab.fragment(), tab.tag)
        else transaction.show(newFragment)
        transaction.commit()
        selectedTab = tab
        updateNavigationSelection()
    }

    private fun updateNavigationSelection() {
        buttons.forEach { (tab, button) ->
            val selected = tab == selectedTab
            button.isSelected = selected
            button.imageTintList = ColorStateList.valueOf(
                requireContext().getColor(if (selected) R.color.cryonix_button_text else R.color.cryonix_text_secondary)
            )
        }
    }

    private fun updateAccountLabel() {
        val currentName = runCatching { PojavProfile.getCurrentProfileName(requireContext()) }.getOrDefault("")
        val account = runCatching { MinecraftAccount.load(currentName) }.getOrNull()
        val face = runCatching { account?.skinFace }.getOrNull()
        if (face != null) accountAvatar?.setImageBitmap(face)
        else accountAvatar?.setImageResource(R.drawable.cryonix_logo)
        accountLabel?.text = account?.username?.takeIf { it.isNotBlank() } ?: currentName.ifBlank { "Select account" }
    }

    fun openAccountsTab() {
        showTab(Tab.ACCOUNTS)
    }

    override fun onDestroyView() {
        accountAvatar = null
        accountLabel = null
        buttons.clear()
        super.onDestroyView()
    }

    private companion object {
        const val STATE_TAB = "cryonix.selected.tab"
    }
}
