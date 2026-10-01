package com.cryonix.launcher.accounts

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.ui.UiMotion

class AccountsActivity : Activity() {
    private lateinit var settings: MinecraftSettingsStore
    private lateinit var list: LinearLayout
    private lateinit var status: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_accounts)
        settings = MinecraftSettingsStore(this)
        list = findViewById(R.id.accounts_list)
        status = findViewById(R.id.accounts_status)

        findViewById<View>(R.id.accounts_back).setOnClickListener { finish() }
        findViewById<View>(R.id.accounts_add_button).setOnClickListener { createProfile() }
        findViewById<View>(R.id.accounts_microsoft_button).setOnClickListener {
            showInfo("Microsoft sign-in", "Microsoft authentication is not configured in the current Cryonix backend. No password or fake login is collected.")
        }
        render()
        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
    }

    override fun onResume() {
        super.onResume()
        if (::settings.isInitialized) render()
    }

    private fun render() {
        migrateLegacyProfile()
        val profiles = settings.profiles()
        list.removeAllViews()
        val active = settings.activeProfileId()
        status.text = if (profiles.isEmpty()) "No local profiles" else profiles.size.toString() + " local profile" + (if (profiles.size == 1) "" else "s") + " • Active: " + settings.profileName

        profiles.forEach { profile ->
            val isActive = profile.id == active
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(8), dp(8), dp(8))
                setBackgroundResource(if (isActive) R.drawable.bg_cryonix_button else R.drawable.bg_cryonix_row)
                isClickable = true
                setOnClickListener { settings.setActiveProfile(profile); render() }
            }
            val text = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(54), 1f)
                text = profile.name + "\n" + if (isActive) "ACTIVE • LOCAL PROFILE" else "LOCAL PROFILE"
                textSize = 12f
                setTextColor(getColor(if (isActive) R.color.cryonix_button_text else R.color.cryonix_text))
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val remove = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
                this.text = "×"
                gravity = android.view.Gravity.CENTER
                textSize = 20f
                setTextColor(getColor(if (isActive) R.color.cryonix_button_text else R.color.cryonix_text_secondary))
                setBackgroundResource(R.drawable.bg_cryonix_row)
                setOnClickListener { confirmRemove(profile.id, profile.name) }
            }
            row.addView(text)
            row.addView(remove)
            list.addView(row, LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(6) })
        }
        bindPressAnimations(list)
    }

    private fun migrateLegacyProfile() {
        if (settings.profiles().isNotEmpty()) return
        val id = settings.profileId
        val name = settings.profileName
        if (!id.isNullOrBlank() && name.isNotBlank()) {
            settings.saveProfiles(listOf(MinecraftSettingsStore.LocalProfile(id, name)))
        }
    }

    private fun createProfile() {
        val input = findViewById<EditText>(R.id.accounts_profile_name)
        val profile = settings.createLocalProfile(input.text.toString())
        if (profile == null) {
            input.error = "Use 3–16 letters, numbers or underscore; name must be unique."
            return
        }
        input.text.clear()
        render()
        UiMotion.press(findViewById(R.id.accounts_add_button))
    }

    private fun confirmRemove(id: String, name: String) {
        AlertDialog.Builder(this)
            .setTitle("Remove profile?")
            .setMessage(name)
            .setPositiveButton("Remove") { _, _ -> settings.removeProfile(id); render() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showInfo(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun bindPressAnimations(root: View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is ViewGroup) for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
    }
}
