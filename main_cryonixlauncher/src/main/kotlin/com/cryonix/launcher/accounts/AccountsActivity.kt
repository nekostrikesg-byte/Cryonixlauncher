package com.cryonix.launcher.accounts

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.LocalProfileStore
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.ui.UiMotion

class AccountsActivity : Activity() {
    private lateinit var settings: MinecraftSettingsStore
    private lateinit var nameView: TextView
    private lateinit var idView: TextView
    private lateinit var nameInput: EditText

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_accounts)

        settings = MinecraftSettingsStore(this)
        nameView = findViewById(R.id.accounts_name)
        idView = findViewById(R.id.accounts_id)
        nameInput = findViewById(R.id.accounts_profile_name)

        findViewById<View>(R.id.accounts_back).setOnClickListener { finish() }
        findViewById<View>(R.id.accounts_add_button).setOnClickListener { createProfile() }
        render()
        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
    }

    private fun render() {
        nameView.text = settings.profileName
        idView.text = settings.profileId?.let { "LOCAL • $it" } ?: "OFFLINE"
        nameInput.setText(settings.profileName)
        nameInput.setSelection(nameInput.text.length)
    }

    private fun createProfile() {
        val account = LocalProfileStore(this).create(nameInput.text.toString())
        settings.profileName = account.name
        settings.profileId = account.id
        render()
        UiMotion.press(findViewById(R.id.accounts_add_button))
    }

    private fun bindPressAnimations(root: View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }
}
