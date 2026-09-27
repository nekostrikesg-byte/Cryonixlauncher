package com.cryonix.launcher.settings

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftSettingsStore

class SettingsActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_settings)

        val store = MinecraftSettingsStore(this)
        val keepScreenOn = findViewById<Switch>(R.id.settings_keep_screen_on)
        val autoRefresh = findViewById<Switch>(R.id.settings_auto_refresh)

        keepScreenOn.isChecked = store.keepScreenOn
        autoRefresh.isChecked = store.autoRefresh

        keepScreenOn.setOnCheckedChangeListener { _, checked ->
            store.keepScreenOn = checked
        }
        autoRefresh.setOnCheckedChangeListener { _, checked ->
            store.autoRefresh = checked
        }

        findViewById<Button>(R.id.settings_done).setOnClickListener {
            finish()
        }
    }
}
