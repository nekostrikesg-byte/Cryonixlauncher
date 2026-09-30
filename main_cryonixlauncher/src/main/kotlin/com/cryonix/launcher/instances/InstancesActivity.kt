package com.cryonix.launcher.instances

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
        findViewById<TextView>(R.id.instances_add).setOnClickListener {
            startActivity(Intent(this, NewInstanceActivity::class.java))
        }
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
            val config = store.instanceConfig(name)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(8), dp(8), dp(8))
                setBackgroundResource(R.drawable.bg_pojav_row)
                isClickable = true
                setOnClickListener { openDetail(name) }
            }
            val title = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(58), 1f)
                text = name + "\n" + (config.versionId ?: "Version not selected") + " • " + config.loader
                textSize = 12f
                setTextColor(getColor(R.color.cryonix_text))
                gravity = Gravity.CENTER_VERTICAL
            }
            val open = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(44)).apply { marginStart = dp(6) }
                text = "›"; gravity = Gravity.CENTER; textSize = 22f
                setTextColor(getColor(R.color.cryonix_text))
                setBackgroundResource(R.drawable.bg_pojav_row)
                setOnClickListener { openDetail(name) }
            }
            val delete = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)).apply { marginStart = dp(5) }
                text = "×"; gravity = Gravity.CENTER; textSize = 19f
                setTextColor(getColor(R.color.cryonix_text_secondary))
                setBackgroundResource(R.drawable.bg_pojav_row)
                setOnClickListener { confirmDelete(name) }
            }
            row.addView(title); row.addView(open); row.addView(delete)
            list.addView(row, LinearLayout.LayoutParams(-1, dp(72)).apply { topMargin = dp(7) })
        }
        bindPressAnimations(list)
    }

    private fun openDetail(name: String) {
        startActivity(Intent(this, InstanceDetailActivity::class.java).putExtra(InstanceDetailActivity.EXTRA_NAME, name))
    }

    private fun confirmDelete(name: String) {
        AlertDialog.Builder(this).setTitle("Delete instance?").setMessage(name)
            .setPositiveButton("Delete") { _, _ -> store.removeInstance(name); renderInstances() }
            .setNegativeButton("Cancel", null).show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun bindPressAnimations(root: View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is ViewGroup) for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
    }
}
