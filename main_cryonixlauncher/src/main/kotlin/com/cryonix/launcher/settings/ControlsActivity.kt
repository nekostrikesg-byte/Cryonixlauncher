package com.cryonix.launcher.settings

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.ui.UiMotion

class ControlsActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_controls)
        findViewById<TextView>(R.id.controls_back).setOnClickListener { UiMotion.press(it); finish() }
        findViewById<TextView>(R.id.controls_touch).setOnClickListener { UiMotion.press(it); choose("Touch Layout", arrayOf("Classic", "Compact", "Floating")) }
        findViewById<TextView>(R.id.controls_sensitivity).setOnClickListener { UiMotion.press(it); choose("Sensitivity", arrayOf("Low", "Normal", "High")) }
        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
    }

    private fun bindPressAnimations(root: android.view.View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }

    private fun choose(title:String, values:Array<String>) {
        AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(values,1){ d,_ -> d.dismiss() }.setNegativeButton("Cancel",null).show()
    }
}