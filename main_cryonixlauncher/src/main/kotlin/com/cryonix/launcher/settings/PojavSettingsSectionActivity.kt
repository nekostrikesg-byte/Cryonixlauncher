package com.cryonix.launcher.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** Legacy section entry point redirected to the integrated Pojav preferences host. */
class PojavSettingsSectionActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        startActivity(Intent(this, PojavSettingsActivity::class.java)
            .putExtra("section", intent.getStringExtra("section")))
        finish()
    }
}
