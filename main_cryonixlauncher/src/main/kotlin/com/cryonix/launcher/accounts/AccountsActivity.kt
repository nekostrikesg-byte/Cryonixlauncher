package com.cryonix.launcher.accounts

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import com.cryonix.launcher.ui.UiMotion
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.LocalProfileStore
import com.cryonix.launcher.minecraft.MinecraftSettingsStore

class AccountsActivity : Activity() {
    private lateinit var settings: MinecraftSettingsStore
    private lateinit var nameView: TextView
    private lateinit var idView: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_accounts)

        settings = MinecraftSettingsStore(this)
        nameView = findViewById(R.id.accounts_name)
        idView = findViewById(R.id.accounts_id)

        findViewById<android.view.View>(R.id.accounts_back).setOnClickListener { UiMotion.press(it); finish() }
        findViewById<android.view.View>(R.id.accounts_add).setOnClickListener { UiMotion.press(it); createProfile() }
        findViewById<android.view.View>(R.id.accounts_add_button).setOnClickListener { UiMotion.press(it); createProfile() }
        render()
        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
    }

    private fun bindPressAnimations(root: View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }

    private fun render() {
        nameView.text = settings.profileName
        idView.text = settings.profileId?.let { "Local profile · $it" } ?: "Offline profile"
    }

    private fun createProfile() {
        val input = EditText(this).apply {
            hint = "Profile name"
            setSingleLine(true)
            setText(settings.profileName)
            setSelection(text.length)
        }
        AlertDialog.Builder(this)
            .setTitle("Create Local Profile")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val account = LocalProfileStore(this).create(input.text.toString())
                settings.profileName = account.name
                settings.profileId = account.id
                render()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
