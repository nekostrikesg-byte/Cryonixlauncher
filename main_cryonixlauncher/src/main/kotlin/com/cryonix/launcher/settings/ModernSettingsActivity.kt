package com.cryonix.launcher.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.cryonix.launcher.MainActivity
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity

class ModernSettingsActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(buildUi())
    }

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.cryonix_background))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(getColor(R.color.cryonix_top))
            setPadding(dp(18), 0, dp(8), 0)
        }

        top.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, -1, 1f)
            gravity = Gravity.CENTER_VERTICAL
            text = "+  Add account"
            textSize = 15f
            setTextColor(getColor(R.color.cryonix_text))
            setOnClickListener {
                startActivity(Intent(this@ModernSettingsActivity, AccountsActivity::class.java))
            }
        })

        top.addView(ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(46), dp(46))
            background = null
            setImageResource(R.drawable.ic_home)
            setColorFilter(getColor(R.color.cryonix_text))
            setPadding(dp(9), dp(9), dp(9), dp(9))
            contentDescription = "Home"
            setOnClickListener {
                startActivity(Intent(this@ModernSettingsActivity, MainActivity::class.java))
                finish()
            }
        })

        root.addView(top, LinearLayout.LayoutParams(-1, dp(54)))
        root.addView(View(this).apply {
            setBackgroundColor(getColor(R.color.cryonix_line))
        }, LinearLayout.LayoutParams(-1, dp(2)))

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(28), dp(18), dp(28), dp(18))
        }

        body.addView(TextView(this).apply {
            text = "Categories"
            textSize = 14f
            setTextColor(getColor(R.color.cryonix_cyan))
            setPadding(0, 0, 0, dp(10))
        })

        row(body, "Video and renderer", "Resolution, renderer and performance", "video")
        row(body, "Control customization", "Gestures, buttons and scaling", "controls")
        row(body, "Java Tweaks", "Java runtimes, JVM arguments, RAM amount and sandbox", "java")
        row(body, "Miscellaneous settings", "Version list and libraries check", "misc")
        row(body, "Experimental Stuff", "Use new Cryonix engine options with consideration", "experimental")

        val language = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(5), dp(8), dp(5))
            setBackgroundResource(R.drawable.bg_pojav_row)
        }
        val languageCopy = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
        languageCopy.addView(TextView(this).apply {
            text = "Force language to English"
            textSize = 13f
            setTextColor(getColor(R.color.cryonix_text))
        })
        languageCopy.addView(TextView(this).apply {
            text = "Allows original launcher strings. Requires a restart."
            textSize = 9f
            setTextColor(getColor(R.color.cryonix_text_secondary))
        })
        language.addView(languageCopy)
        val forceEnglish = android.widget.Switch(this).apply {
            isChecked = getPreferences(MODE_PRIVATE).getBoolean("force_english", false)
            setOnCheckedChangeListener { _, checked ->
                getPreferences(MODE_PRIVATE).edit().putBoolean("force_english", checked).apply()
            }
        }
        language.addView(forceEnglish)
        body.addView(language, LinearLayout.LayoutParams(-1, dp(66)).apply { topMargin = dp(2) })

        val scroll = android.widget.ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            addView(body)
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return root
    }

    private fun row(parent: LinearLayout, title: String, subtitle: String, section: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))
            setBackgroundResource(R.drawable.bg_pojav_row)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                startActivity(Intent(this@ModernSettingsActivity, PojavSettingsSectionActivity::class.java)
                    .putExtra("section", section))
            }
        }

        val icon = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
            gravity = Gravity.CENTER
            text = when (section) {
                "video" -> "▣"
                "controls" -> "✚"
                "java" -> "▱"
                "misc" -> "☷"
                else -> "♟"
            }
            textSize = 22f
            setTextColor(getColor(R.color.cryonix_text))
        }
        row.addView(icon)

        val copy = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) }
        }
        copy.addView(TextView(this).apply {
            text = title
            textSize = 13f
            setTextColor(getColor(R.color.cryonix_text))
        })
        copy.addView(TextView(this).apply {
            text = subtitle
            textSize = 9f
            setTextColor(getColor(R.color.cryonix_text_secondary))
            maxLines = 1
        })
        row.addView(copy)
        row.addView(TextView(this).apply {
            text = "›"
            textSize = 22f
            setTextColor(getColor(R.color.cryonix_muted))
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(28), -1))
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(66)).apply {
            bottomMargin = dp(6)
        })
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
