package com.cryonix.launcher.settings

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.cryonix.launcher.MainActivity
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.downloads.DownloadsActivity
import com.cryonix.launcher.instances.InstancesActivity

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
            setBackgroundResource(R.drawable.bg_top_bar)
            setPadding(dp(18), 0, dp(10), 0)
        }
        top.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, -1, 1f)
            text = "+  Add account"
            gravity = Gravity.CENTER_VERTICAL
            textSize = 15f
            setTextColor(getColor(R.color.cryonix_text))
            setOnClickListener {
                startActivity(Intent(this@ModernSettingsActivity, AccountsActivity::class.java))
            }
        })
        top.addView(ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
            background = android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
            setImageResource(R.drawable.ic_home)
            imageTintList = ColorStateList.valueOf(getColor(R.color.cryonix_text))
            setPadding(dp(10), dp(10), dp(10), dp(10))
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

        val scroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(28), dp(22), dp(28), dp(24))
        }

        list.addView(TextView(this).apply {
            text = "Categories"
            textSize = 14f
            setTextColor(getColor(R.color.cryonix_cyan))
            setPadding(dp(0), 0, 0, dp(12))
        })

        addSection(list, "Video and renderer", "Resolution, renderer and performance", R.drawable.ic_renderer) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        addSection(list, "Control customization", "Gestures, buttons and scaling", R.drawable.ic_gamepad) {
            startActivity(Intent(this, ControlsActivity::class.java))
        }
        addSection(list, "Java tweaks", "Java runtimes, JVM arguments, RAM amount and sandbox", R.drawable.ic_java) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        addSection(list, "Miscellaneous settings", "Version list, libraries check and launcher behavior", R.drawable.ic_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        addSection(list, "Experimental stuff", "New Cryonix features and engine experiments", R.drawable.ic_rocket) {
            startActivity(Intent(this, CursorStudioActivity::class.java))
        }

        val languageRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(7), dp(8), dp(7))
            setBackgroundResource(R.drawable.bg_simple_row)
            isClickable = true
            isFocusable = true
            setOnClickListener { startActivity(Intent(this@ModernSettingsActivity, SettingsActivity::class.java)) }
        }
        languageRow.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setImageResource(R.drawable.ic_home)
            imageTintList = ColorStateList.valueOf(getColor(R.color.cryonix_text))
        })
        val langText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10) }
        }
        langText.addView(TextView(this).apply {
            text = "Force language to English"
            textSize = 13f
            setTextColor(getColor(R.color.cryonix_text))
        })
        langText.addView(TextView(this).apply {
            text = "Shows original launcher strings after restart"
            textSize = 9f
            setTextColor(getColor(R.color.cryonix_text_secondary))
            maxLines = 1
        })
        languageRow.addView(langText)
        languageRow.addView(TextView(this).apply {
            text = "OFF"
            textSize = 9f
            setTextColor(getColor(R.color.cryonix_muted))
            gravity = Gravity.CENTER
            setPadding(dp(12), 0, dp(8), 0)
        })
        list.addView(languageRow, LinearLayout.LayoutParams(-1, dp(66)).apply { topMargin = dp(2) })

        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return root
    }

    private fun addSection(
        parent: LinearLayout,
        title: String,
        subtitle: String,
        icon: Int,
        action: () -> Unit
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(7), dp(8), dp(7))
            setBackgroundResource(R.drawable.bg_simple_row)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        row.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setImageResource(icon)
            imageTintList = ColorStateList.valueOf(getColor(R.color.cryonix_text))
        })

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
            layoutParams = LinearLayout.LayoutParams(dp(28), -1)
        })

        parent.addView(row, LinearLayout.LayoutParams(-1, dp(66)).apply {
            bottomMargin = dp(6)
        })
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
