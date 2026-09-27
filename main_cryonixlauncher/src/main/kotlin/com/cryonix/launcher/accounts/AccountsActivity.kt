package com.cryonix.launcher.accounts

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
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

        findViewById<android.view.View>(R.id.accounts_back).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.accounts_add).setOnClickListener { createProfile() }
        findViewById<android.view.View>(R.id.accounts_add_button).setOnClickListener { createProfile() }
        render()
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
