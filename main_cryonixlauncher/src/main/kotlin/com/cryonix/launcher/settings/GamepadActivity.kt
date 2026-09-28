package com.cryonix.launcher.settings

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.ui.UiMotion

class GamepadActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_gamepad)
        findViewById<TextView>(R.id.gamepad_back).setOnClickListener { UiMotion.press(it); finish() }
        findViewById<TextView>(R.id.gamepad_mapping).setOnClickListener { UiMotion.press(it); AlertDialog.Builder(this).setTitle("Controller Mapping").setMessage("No controller is connected.\nConnect a gamepad to configure buttons.").setPositiveButton("OK",null).show() }
        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
    }

    private fun bindPressAnimations(root: android.view.View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }
}