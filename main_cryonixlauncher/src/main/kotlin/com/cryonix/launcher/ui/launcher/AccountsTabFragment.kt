package com.cryonix.launcher.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.core.PojavBridge
import com.kdt.mcgui.mcAccountSpinner
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore

class AccountsTabFragment : Fragment(R.layout.fragment_tab_accounts) {
    private lateinit var accountList: LinearLayout
    private lateinit var status: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        accountList = view.findViewById(R.id.account_tab_list)
        status = view.findViewById(R.id.account_tab_status)
        view.findViewById<View>(R.id.account_tab_add).setOnClickListener {
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true)
        }
        view.findViewById<View>(R.id.account_tab_profiles).setOnClickListener {
            startActivity(Intent(requireContext(), AccountsActivity::class.java))
        }
        renderAccounts()
    }

    override fun onResume() {
        super.onResume()
        if (::accountList.isInitialized) renderAccounts()
    }

    private fun renderAccounts() {
        val context = requireContext()
        val accounts = PojavBridge.accounts(context)
        val activeName = PojavBridge.currentAccountName(context)
        status.text = if (accounts.isEmpty()) "No saved Minecraft accounts" else
            "${accounts.size} saved account${if (accounts.size == 1) "" else "s"} · Active: ${activeName.ifBlank { "none" }}"
        accountList.removeAllViews()

        if (accounts.isEmpty()) {
            accountList.addView(TextView(context).apply {
                text = "Add a local/offline profile or sign in to your Microsoft account."
                textSize = 10f
                setTextColor(context.getColor(R.color.cryonix_text_secondary))
                setPadding(dp(8), dp(12), dp(8), dp(12))
            })
        }

        accounts.forEach { account ->
            val row = LayoutInflater.from(context).inflate(R.layout.item_launcher_account, accountList, false)
            val active = account.name == activeName
            row.findViewById<TextView>(R.id.launcher_account_name).text =
                account.username + "\n" + when {
                    account.microsoft -> "MICROSOFT ACCOUNT"
                    account.demo -> "DEMO ACCOUNT"
                    else -> "LOCAL / OFFLINE"
                }
            val activeView = row.findViewById<TextView>(R.id.launcher_account_active)
            activeView.visibility = if (active) View.VISIBLE else View.INVISIBLE
            row.setOnClickListener {
                PojavBridge.activateAccount(context, account.name)
                backendAccountSpinner()?.selectAccountByName(account.name)
                renderAccounts()
            }
            row.findViewById<View>(R.id.launcher_account_remove).setOnClickListener {
                backendAccountSpinner()?.let { spinner ->
                    spinner.selectAccountByName(account.name)
                    spinner.removeCurrentAccount()
                }
                renderAccounts()
            }
            accountList.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(53)))
            accountList.addView(View(context).apply {
                setBackgroundColor(context.getColor(R.color.cryonix_divider))
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)))
        }
    }

    private fun backendAccountSpinner(): mcAccountSpinner? =
        (activity as? LauncherActivity)?.findViewById(R.id.account_spinner)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
