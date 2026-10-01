package com.cryonix.launcher.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** Legacy entry point kept for older intents; opens the current Cryonix settings hub. */
class ModernSettingsActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }
}
