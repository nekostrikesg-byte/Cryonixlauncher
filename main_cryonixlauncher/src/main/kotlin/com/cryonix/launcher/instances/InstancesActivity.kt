package com.cryonix.launcher.instances

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.ui.UiMotion

class InstancesActivity : Activity() {
    private lateinit var store: MinecraftSettingsStore

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_instances)
        store = MinecraftSettingsStore(this)

        findViewById<TextView>(R.id.instances_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.instances_add).setOnClickListener { addInstance() }

        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
        renderInstances()
    }

    override fun onResume() {
        super.onResume()
        if (::store.isInitialized) renderInstances()
    }

    private fun renderInstances() {
        val list = findViewById<LinearLayout>(R.id.instances_list)
        val empty = findViewById<TextView>(R.id.instances_empty)
        list.removeAllViews()

        val names = store.instances
        empty.visibility = if (names.isEmpty()) View.VISIBLE else View.GONE

        names.forEach { name ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(14, 8, 8, 8)
                setBackgroundResource(R.drawable.bg_reference_row)
            }

            val title = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 64, 1f)
                text = "$name\nVanilla • Ready"
                textSize = 13f
                setTextColor(getColor(R.color.cryonix_text))
                gravity = Gravity.CENTER_VERTICAL
            }

            val info = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(42, 42).apply { marginStart = 8 }
                text = "⋯"
                gravity = Gravity.CENTER
                textSize = 20f
                setTextColor(getColor(R.color.cryonix_text_secondary))
                setBackgroundResource(R.drawable.bg_reference_pill)
                setOnClickListener { showInstance(name) }
            }

            val delete = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(42, 42).apply { marginStart = 6 }
                text = "×"
                gravity = Gravity.CENTER
                textSize = 20f
                setTextColor(getColor(R.color.cryonix_text_secondary))
                setBackgroundResource(R.drawable.bg_reference_pill)
                setOnClickListener { confirmDelete(name) }
            }

            row.addView(title)
            row.addView(info)
            row.addView(delete)
            list.addView(row, LinearLayout.LayoutParams(-1, 72).apply { topMargin = 8 })
        }

        bindPressAnimations(list)
    }

    private fun addInstance() {
        val input = EditText(this).apply {
            hint = "Instance name"
            setSingleLine(true)
            setTextColor(getColor(R.color.cryonix_text))
            setHintTextColor(getColor(R.color.cryonix_text_secondary))
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("New Instance")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                if (store.addInstance(input.text.toString())) renderInstances()
            }
            .setNegativeButton("Cancel", null)
            .create()

        showCompactDialog(dialog)
    }

    private fun confirmDelete(name: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete instance?")
            .setMessage(name)
            .setPositiveButton("Delete") { _, _ ->
                store.removeInstance(name)
                renderInstances()
            }
            .setNegativeButton("Cancel", null)
            .create()

        showCompactDialog(dialog)
    }

    private fun showInstance(name: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(name)
            .setMessage("Vanilla • Ready")
            .setPositiveButton("OK", null)
            .create()
        showCompactDialog(dialog)
    }

    private fun showCompactDialog(dialog: AlertDialog) {
        dialog.setOnShowListener {
            val density = resources.displayMetrics.density
            dialog.window?.setLayout((340 * density).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        dialog.show()
    }

    private fun bindPressAnimations(root: View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }
}
