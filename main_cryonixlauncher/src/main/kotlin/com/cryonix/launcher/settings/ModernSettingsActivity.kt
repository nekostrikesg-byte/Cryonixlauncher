package com.cryonix.launcher.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import com.cryonix.launcher.MainActivity
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore

class ModernSettingsActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("cryonix_plus_ui", MODE_PRIVATE) }
    private val store by lazy { MinecraftSettingsStore(this) }
    private val ink get() = getColor(R.color.cryonix_text)
    private val muted get() = getColor(R.color.cryonix_text_secondary)
    private val accent get() = getColor(R.color.cryonix_cyan)
    private val panel get() = getColor(R.color.cryonix_panel)
    private val line get() = getColor(R.color.cryonix_border)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(buildUi())
    }

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(getColor(R.color.cryonix_background))
        }

        val rail = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(12), dp(16), dp(12), dp(12))
            setBackgroundColor(getColor(R.color.cryonix_top))
        }
        rail.addView(label("C", 24f, ink, true).apply {
            gravity = Gravity.CENTER
            background = rounded(getColor(R.color.cryonix_blue), dp(13))
        }, LinearLayout.LayoutParams(dp(46), dp(46)))
        rail.addView(View(this).apply { setBackgroundColor(line) },
            LinearLayout.LayoutParams(dp(34), dp(1)).apply { topMargin = dp(16); bottomMargin = dp(12) })
        railButton(rail, "⌂", "Home") {
            startActivity(Intent(this, MainActivity::class.java)); finish()
        }
        railButton(rail, "⚙", "Settings") {}
        railButton(rail, "♙", "Accounts") {
            startActivity(Intent(this, AccountsActivity::class.java))
        }
        root.addView(rail, LinearLayout.LayoutParams(dp(76), -1))

        val contentColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(14), dp(22), dp(12))
        }
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        val titles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        titles.addView(label("Settings", 25f, ink, true))
        titles.addView(label("Make Cryonix Plus feel like yours", 11f, muted))
        header.addView(titles, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(label("PLUS EDITION", 10f, accent, true).apply {
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = rounded(getColor(R.color.cryonix_surface_alt), dp(9))
        })
        contentColumn.addView(header, LinearLayout.LayoutParams(-1, -2))
        contentColumn.addView(View(this).apply { setBackgroundColor(line) },
            LinearLayout.LayoutParams(-1, dp(1)).apply { topMargin = dp(12); bottomMargin = dp(12) })

        val scroll = ScrollView(this).apply { isVerticalScrollBarEnabled = false }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val hero = card()
        hero.addView(label("PERSONALIZE YOUR SETUP", 10f, accent, true))
        hero.addView(label("Everything in one place.", 21f, ink, true).apply {
            setPadding(0, dp(5), 0, dp(4))
        })
        hero.addView(label("Renderer, controls, Java runtime and launcher behaviour.", 11f, muted))
        body.addView(hero, full().apply { bottomMargin = dp(10) })

        body.addView(sectionHeading("GAME & PERFORMANCE", "Tune graphics and runtime behaviour"))
        category(body, "▣", "Video & renderer", "Renderer, graphics API and resolution", "video")
        category(body, "⌘", "Java & memory", "Runtime options, RAM and JVM arguments", "java")
        body.addView(sectionHeading("INPUT & COMFORT", "Make controls fit your device"))
        category(body, "✣", "Controls", "Touch gestures, button scaling and mouse", "controls")
        category(body, "◉", "Experimental", "Optional engine and compatibility settings", "experimental")
        body.addView(sectionHeading("LAUNCHER", "Profiles and general behaviour"))
        category(body, "☷", "General & libraries", "Language, metadata and library checks", "misc")

        val toggles = card()
        toggles.addView(label("QUICK PREFERENCES", 10f, accent, true))
        switchRow(toggles, "Keep screen awake", "Prevent display sleep while you play", store.keepScreenOn) {
            store.keepScreenOn = it
        }
        switchRow(toggles, "Use display cutout area", "Allow the game to extend into notch areas", store.useCutoutArea) {
            store.useCutoutArea = it
        }
        switchRow(toggles, "Force English launcher strings", "Requires restarting the launcher to fully apply", prefs.getBoolean("force_english", false)) {
            prefs.edit().putBoolean("force_english", it).apply()
        }
        body.addView(toggles, full().apply { topMargin = dp(10); bottomMargin = dp(10) })

        val footer = label("CRYONIXLAUNCHER PLUS  •  Pojav runtime preserved", 9f, getColor(R.color.cryonix_muted))
        footer.setPadding(dp(4), dp(8), dp(4), dp(8))
        body.addView(footer)
        scroll.addView(body)
        contentColumn.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(contentColumn, LinearLayout.LayoutParams(0, -1, 1f))
        return root
    }

    private fun sectionHeading(title: String, subtitle: String): View {
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(10), 0, dp(7))
        }
        wrap.addView(label(title, 10f, accent, true))
        wrap.addView(label(subtitle, 10f, muted).apply { setPadding(0, dp(3), 0, 0) })
        return wrap
    }

    private fun category(parent: LinearLayout, glyph: String, title: String, subtitle: String, section: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = rounded(panel, dp(12), line)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                startActivity(Intent(this@ModernSettingsActivity, PojavSettingsSectionActivity::class.java)
                    .putExtra("section", section))
            }
        }
        row.addView(label(glyph, 21f, accent, true).apply {
            gravity = Gravity.CENTER
            background = rounded(getColor(R.color.cryonix_surface_alt), dp(9))
        }, LinearLayout.LayoutParams(dp(44), dp(44)))
        val copy = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        copy.addView(label(title, 12f, ink, true))
        copy.addView(label(subtitle, 9f, muted).apply { setPadding(0, dp(4), 0, 0) })
        row.addView(copy, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(label("›", 23f, muted).apply { gravity = Gravity.CENTER })
        parent.addView(row, full().apply { bottomMargin = dp(6) })
    }

    private fun switchRow(parent: LinearLayout, title: String, subtitle: String, initial: Boolean, changed: (Boolean) -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        val copy = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        copy.addView(label(title, 11f, ink, true))
        copy.addView(label(subtitle, 9f, muted).apply { setPadding(0, dp(3), 0, 0) })
        row.addView(copy, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(Switch(this).apply {
            isChecked = initial
            setOnCheckedChangeListener { _, checked -> changed(checked) }
        })
        parent.addView(row, full())
        parent.addView(View(this).apply { setBackgroundColor(line) }, LinearLayout.LayoutParams(-1, dp(1)))
    }

    private fun railButton(parent: LinearLayout, glyph: String, description: String, action: () -> Unit) {
        parent.addView(label(glyph, 23f, muted, true).apply {
            gravity = Gravity.CENTER
            contentDescription = description
            background = rounded(getColor(R.color.cryonix_surface), dp(12))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(dp(46), dp(46)).apply { topMargin = dp(9) })
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(12), dp(14), dp(12))
        background = rounded(panel, dp(14), line)
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }

    private fun rounded(color: Int, radius: Int, stroke: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }

    private fun full() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
