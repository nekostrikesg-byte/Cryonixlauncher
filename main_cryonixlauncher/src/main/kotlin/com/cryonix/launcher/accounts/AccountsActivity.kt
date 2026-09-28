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

        findViewById<android.view.View>(R.id.accounts_back).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.accounts_add).setOnClickListener { createProfile() }
        findViewById<android.view.View>(R.id.accounts_add_button).setOnClickListener { createProfile() }
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
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_offline_profile)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setDimAmount(0.62f)
        dialog.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val card = dialog.findViewById<View>(R.id.offline_dialog_card)
        val input = dialog.findViewById<EditText>(R.id.offline_profile_name)
        input.setText(settings.profileName)
        input.setSelection(input.text.length)

        dialog.findViewById<View>(R.id.offline_cancel).setOnClickListener {
            UiMotion.press(it)
            UiMotion.morphOut(card) { dialog.dismiss() }
        }
        dialog.findViewById<View>(R.id.offline_create).setOnClickListener {
            UiMotion.press(it)
            val account = LocalProfileStore(this).create(input.text.toString())
            settings.profileName = account.name
            settings.profileId = account.id
            render()
            UiMotion.morphOut(card) { dialog.dismiss() }
        }

        dialog.setOnShowListener {
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.55f).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
            UiMotion.morphIn(card)
            input.requestFocus()
            dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        dialog.show()
    }
}
