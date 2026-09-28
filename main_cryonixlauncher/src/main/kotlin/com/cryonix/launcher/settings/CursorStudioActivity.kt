package com.cryonix.launcher.settings

import android.app.Activity
import android.os.Bundle
import android.view.PointerIcon
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftSettingsStore

class CursorStudioActivity : Activity() {
    private lateinit var store: MinecraftSettingsStore
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_cursor_studio)
        store = MinecraftSettingsStore(this)
        findViewById<View>(R.id.cursor_back).setOnClickListener { finish() }
        val group = findViewById<RadioGroup>(R.id.cursor_group)
        val ids = mapOf("arrow" to R.id.cursor_arrow, "hand" to R.id.cursor_hand, "text" to R.id.cursor_text, "crosshair" to R.id.cursor_crosshair)
        ids[store.cursorPreset]?.let { group.check(it) }
        group.setOnCheckedChangeListener { _, checked ->
            val preset = ids.entries.firstOrNull { it.value == checked }?.key ?: return@setOnCheckedChangeListener
            store.cursorPreset = preset
            applyPreset(preset)
            findViewById<TextView>(R.id.cursor_status).text = "Applied " + preset + " pointer to Cryonix launcher controls."
        }
        applyPreset(store.cursorPreset)
    }

    private fun applyPreset(preset: String) {
        val type = when (preset) {
            "hand" -> PointerIcon.TYPE_HAND
            "text" -> PointerIcon.TYPE_TEXT
            "crosshair" -> PointerIcon.TYPE_CROSSHAIR
            else -> PointerIcon.TYPE_ARROW
        }
        findViewById<View>(android.R.id.content).pointerIcon = PointerIcon.getSystemIcon(this, type)
    }
}
