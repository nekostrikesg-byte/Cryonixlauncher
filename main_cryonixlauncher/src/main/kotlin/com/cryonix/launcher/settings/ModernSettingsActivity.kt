package com.cryonix.launcher.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.cryonix.launcher.MainActivity
import com.cryonix.launcher.R

class ModernSettingsActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(buildUi())
    }

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.cryonix_background))
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        val nav = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(4), dp(4), dp(4))
            setBackgroundResource(R.drawable.bg_nav)
        }
        val navWidth = dp(330)
        nav.layoutParams = LinearLayout.LayoutParams(navWidth, dp(46)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }

        fun navButton(icon: Int, label: String, action: () -> Unit): ImageButton {
            return ImageButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(52), dp(38)).apply { marginStart = dp(2) }
                background = if (label == "settings") getDrawable(R.drawable.bg_nav_selected) else null
                setImageResource(icon)
                imageTintList = android.content.res.ColorStateList.valueOf(
                    if (label == "settings") Color.WHITE else getColor(R.color.cryonix_icon)
                )
                contentDescription = label
                setPadding(dp(10), dp(10), dp(10), dp(10))
                setOnClickListener { action() }
            }
        }

        nav.addView(navButton(R.drawable.ic_cube, "instances") {
            startActivity(Intent(this, com.cryonix.launcher.instances.InstancesActivity::class.java))
        })
        nav.addView(navButton(R.drawable.ic_accounts, "accounts") {
            startActivity(Intent(this, com.cryonix.launcher.accounts.AccountsActivity::class.java))
        })
        nav.addView(navButton(R.drawable.ic_download, "downloads") {
            startActivity(Intent(this, com.cryonix.launcher.downloads.DownloadsActivity::class.java))
        })
        nav.addView(navButton(R.drawable.ic_settings, "settings") {})
        nav.addView(navButton(R.drawable.ic_home, "home") {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        })
        root.addView(nav)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(16), dp(8), dp(8))
        }
        header.addView(TextView(this).apply {
            text = "Settings"
            textSize = 22f
            setTextColor(getColor(R.color.cryonix_text))
        })
        header.addView(TextView(this).apply {
            text = "Cryonix control center • choose a section"
            textSize = 9f
            setTextColor(getColor(R.color.cryonix_text_secondary))
        })
        root.addView(header)

        val scroll = ScrollView(this).apply { isVerticalScrollBarEnabled = false }
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, dp(8), dp(12))
        }

        addSection(list, "GENERAL", "Launcher preferences, renderer and display options", R.drawable.ic_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        addSection(list, "GAME", "Version, memory, loader and per-instance launch options", R.drawable.ic_rocket) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        addSection(list, "CONTROLS", "Touch layout, buttons, mouse and gamepad", R.drawable.ic_gamepad) {
            startActivity(Intent(this, ControlsActivity::class.java))
        }
        addSection(list, "GAMEPAD", "Controller mapping and input presets", R.drawable.ic_gamepad) {
            startActivity(Intent(this, GamepadActivity::class.java))
        }
        addSection(list, "CURSOR STUDIO", "Pointer presets and mobile desktop controls", R.drawable.ic_renderer) {
            startActivity(Intent(this, CursorStudioActivity::class.java))
        }
        addSection(list, "JAVA & RUNTIME", "Runtime status, Java selection and engine preflight", R.drawable.ic_java) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        addSection(list, "ABOUT & CREDITS", "Version, source, licenses and project information", R.drawable.ic_home) {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return root
    }

    private fun addSection(parent: LinearLayout, title: String, subtitle: String, icon: Int, action: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setPadding(dp(12), dp(8), dp(10), dp(8))
            setBackgroundResource(R.drawable.bg_reference_row)
            setOnClickListener { action() }
        }
        val image = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(38), dp(38))
            background = getDrawable(R.drawable.bg_nav)
            setImageResource(icon)
            imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.cryonix_blue_bright))
            setPadding(dp(9), dp(9), dp(9), dp(9))
            contentDescription = title
        }
        row.addView(image)
        val text = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) }
        }
        text.addView(TextView(this).apply {
            this.text = title
            textSize = 12f
            setTextColor(getColor(R.color.cryonix_text))
        })
        text.addView(TextView(this).apply {
            this.text = subtitle
            textSize = 8f
            setTextColor(getColor(R.color.cryonix_text_secondary))
            maxLines = 1
        })
        row.addView(text)
        row.addView(TextView(this).apply {
            this.text = "›"
            textSize = 22f
            setTextColor(getColor(R.color.cryonix_muted))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(26), -1)
        })
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(62)).apply {
            bottomMargin = dp(6)
        })
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
