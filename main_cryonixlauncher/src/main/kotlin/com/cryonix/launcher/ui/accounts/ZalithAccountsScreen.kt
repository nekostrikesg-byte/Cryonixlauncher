package com.cryonix.launcher.ui.accounts

import android.app.AlertDialog
import android.app.Activity
import android.widget.EditText
import com.cryonix.launcher.minecraft.LocalProfileStore
import com.cryonix.launcher.minecraft.MinecraftSettingsStore

/**
 * Native Android account flow matching the Zalith 2+ account-card interaction:
 * account card -> account manager -> local/offline profile.
 */
object ZalithAccountsScreen {
    fun showLocalProfile(
        activity: Activity,
        store: MinecraftSettingsStore,
        onSaved: () -> Unit
    ) {
        val input = EditText(activity).apply {
            hint = "Profile name"
            setSingleLine(true)
            setText(store.profileName)
            setSelection(text.length)
        }

        AlertDialog.Builder(activity)
            .setTitle("Accounts")
            .setMessage(
                "Create or edit a local Cryonix profile. Online Minecraft services " +
                    "still require a valid Microsoft account."
            )
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val account = LocalProfileStore(activity).create(input.text.toString())
                store.profileName = account.name
                store.profileId = account.id
                onSaved()
            }
            .setNeutralButton("Use Player") { _, _ ->
                store.profileName = "Player"
                store.profileId = null
                onSaved()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
