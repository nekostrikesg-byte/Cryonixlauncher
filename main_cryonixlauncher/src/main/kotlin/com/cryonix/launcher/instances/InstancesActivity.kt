package com.cryonix.launcher.instances

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.ui.UiMotion

class InstancesActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_instances)
        findViewById<TextView>(R.id.instances_back).setOnClickListener { UiMotion.press(it); finish() }
        findViewById<TextView>(R.id.instances_add).setOnClickListener { UiMotion.press(it); addInstance() }
        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
    }

    private fun bindPressAnimations(root: android.view.View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }

    private fun addInstance() {
        val input = EditText(this).apply { hint = "Instance name"; setSingleLine(true) }
        AlertDialog.Builder(this)
            .setTitle("New Instance")
            .setMessage("Create a local launcher instance.")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim().ifEmpty { "New Instance" }
                addCard(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addCard(name: String) {
        val list = findViewById<LinearLayout>(R.id.instances_list)
        val card = TextView(this).apply {
            text = "$name\nVanilla • Ready"
            textSize = 14f
            setTextColor(getColor(R.color.cryonix_text))
            setPadding(18, 18, 18, 18)
            setBackgroundResource(R.drawable.bg_reference_row)
            setOnClickListener { UiMotion.press(this); showInstance(name) }
        }
        val params = LinearLayout.LayoutParams(-1, 76)
        params.topMargin = 8
        list.addView(card, params)
    }

    private fun showInstance(name: String) {
        AlertDialog.Builder(this)
            .setTitle(name)
            .setMessage("Instance details\n\nLoader: Vanilla\nRenderer: System\nStatus: Ready")
            .setPositiveButton("OK", null)
            .show()
    }
}