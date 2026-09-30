package com.cryonix.launcher.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import com.cryonix.launcher.MainActivity
import com.cryonix.launcher.R
import com.cryonix.launcher.core.PojavBridge
import com.cryonix.launcher.instances.InstancesActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore

class PojavSettingsSectionActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("pojav_style_settings", MODE_PRIVATE) }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(buildUi(intent.getStringExtra("section") ?: "video"))
    }

    private fun buildUi(section: String): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getColor(R.color.cryonix_background))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(getColor(R.color.cryonix_top))
            setPadding(dp(12), 0, dp(8), 0)
        }
        top.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, -1, 1f)
            gravity = Gravity.CENTER_VERTICAL
            text = "‹  Back to the last screen"
            textSize = 14f
            setTextColor(getColor(R.color.cryonix_text))
            setOnClickListener { finish() }
        })
        top.addView(ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(46), dp(46))
            background = null
            setImageResource(R.drawable.ic_home)
            setColorFilter(getColor(R.color.cryonix_text))
            setPadding(dp(9), dp(9), dp(9), dp(9))
            setOnClickListener {
                startActivity(Intent(this@PojavSettingsSectionActivity, MainActivity::class.java))
                finish()
            }
        })
        root.addView(top, LinearLayout.LayoutParams(-1, dp(54)))
        root.addView(View(this).apply { setBackgroundColor(getColor(R.color.cryonix_line)) }, LinearLayout.LayoutParams(-1, dp(2)))

        val scroll = ScrollView(this).apply { isVerticalScrollBarEnabled = false }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(28), dp(16), dp(28), dp(24))
        }
        when (section) {
            "video" -> video(content)
            "controls" -> controls(content)
            "java" -> java(content)
            "misc" -> misc(content)
            "experimental" -> experimental(content)
            else -> video(content)
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return root
    }

    private fun video(parent: LinearLayout) {
        title(parent, "Video settings")
        setting(parent, "Renderer", "Choose the graphics renderer and backend") {
            val values = arrayOf("Zink", "GL4ES", "System")
            choose("Renderer", values, prefs.getInt("renderer", 0)) { which ->
                prefs.edit().putInt("renderer", which).apply()
            }
        }
        seekSetting(parent, "Resolution Scaler", "Allows you to decrease the game resolution.", "resolution", 25, 100, 100, "%")
    }

    private fun controls(parent: LinearLayout) {
        title(parent, "Control customization")
        switchSetting(parent, "Disable gestures", "Disables gestures, such as hold to break block, and tap to place a block.", "gestures")
        switchSetting(parent, "Disable double tap to swap hands", "Disables double tapping on the hotbar to swap it in the second hand.", "double_tap")
        switchSetting(parent, "Disable mouse delay", "Removes the pointer delay used by touch controls.", "mouse_delay")
    }

    private fun java(parent: LinearLayout) {
        title(parent, "Java Tweaks")
        val installed = PojavBridge.runtimes()
        val runtimeSubtitle = if (installed.isEmpty()) {
            "No runtime installed — tap to install one"
        } else {
            installed.joinToString { it.name + " (Java " + it.major + ")" } + " — tap to manage"
        }
        setting(parent, "Java Runtime", runtimeSubtitle) {
            startActivity(Intent(this, JavaRuntimeActivity::class.java))
        }
        seekSetting(parent, "RAM Allocation", "Amount of memory available to Minecraft.", "ram", 512, 6144, 2048, " MB") { value ->
            PojavBridge.applyPreferences(memoryMb = value)
        }
        val args = EditText(this).apply {
            setSingleLine(false)
            minLines = 2
            hint = "JVM Arguments"
            setText(prefs.getString("jvm_args", ""))
            setTextColor(getColor(R.color.cryonix_text))
            setHintTextColor(getColor(R.color.cryonix_muted))
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = getDrawable(R.drawable.bg_pojav_input)
            textSize = 10f
        }
        parent.addView(args, LinearLayout.LayoutParams(-1, dp(68)).apply { topMargin = dp(7) })
        button(parent, "SAVE", R.drawable.bg_pojav_button) {
            val jvmArgs = args.text.toString()
            prefs.edit().putString("jvm_args", jvmArgs).apply()
            PojavBridge.applyPreferences(jvmArgs = jvmArgs)
            Toast.makeText(this, "Java settings saved to the backend", Toast.LENGTH_SHORT).show()
        }
    }

    private fun misc(parent: LinearLayout) {
        title(parent, "Miscellaneous settings")
        setting(parent, "Select versions", "Choose installed Minecraft versions for the launcher") {
            startActivity(Intent(this, InstancesActivity::class.java))
        }
        switchSetting(parent, "Check libraries", "Verify required libraries before launch.", "libraries", true)
        switchSetting(parent, "Verify manifests", "Verify downloaded Minecraft metadata before install.", "manifest", true)
    }

    private fun experimental(parent: LinearLayout) {
        title(parent, "Experimental Stuff")
        switchSetting(parent, "Enable shader dumping", "Log converted shaders into the instance files.", "shader_dump")
        switchSetting(parent, "Force renderer to big core", "Request high-performance CPU scheduling for renderer work.", "big_core")
        switchSetting(parent, "Force rendering into system memory", "Use system memory for renderer buffers.", "system_memory")
        switchSetting(parent, "Forcefully use OpenSL for audio", "Use the OpenSL audio path when supported.", "opensl")
    }

    private fun title(parent: LinearLayout, text: String) {
        parent.addView(TextView(this).apply {
            this.text = text
            textSize = 20f
            setTextColor(getColor(R.color.cryonix_text))
            setPadding(0, 0, 0, dp(10))
        })
    }

    private fun setting(parent: LinearLayout, title: String, subtitle: String, action: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(7), dp(12), dp(7))
            setBackgroundResource(R.drawable.bg_pojav_row)
            isClickable = true
            setOnClickListener { action() }
        }
        row.addView(TextView(this).apply {
            this.text = title
            textSize = 13f
            setTextColor(getColor(R.color.cryonix_text))
        })
        row.addView(TextView(this).apply {
            this.text = subtitle
            textSize = 9f
            setTextColor(getColor(R.color.cryonix_text_secondary))
        })
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(58)).apply { bottomMargin = dp(7) })
    }

    private fun switchSetting(parent: LinearLayout, title: String, subtitle: String, key: String, default: Boolean = false) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(6), dp(8), dp(6))
            setBackgroundResource(R.drawable.bg_pojav_row)
        }
        val copy = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
        copy.addView(TextView(this).apply {
            this.text = title
            textSize = 13f
            setTextColor(getColor(R.color.cryonix_text))
        })
        copy.addView(TextView(this).apply {
            this.text = subtitle
            textSize = 9f
            setTextColor(getColor(R.color.cryonix_text_secondary))
            maxLines = 2
        })
        row.addView(copy)
        row.addView(Switch(this).apply {
            isChecked = prefs.getBoolean(key, default)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(key, checked).apply() }
        })
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(66)).apply { bottomMargin = dp(6) })
    }

    private fun seekSetting(
        parent: LinearLayout,
        title: String,
        subtitle: String,
        key: String,
        min: Int,
        max: Int,
        default: Int,
        suffix: String,
        onChanged: ((Int) -> Unit)? = null
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(7), dp(12), dp(7))
            setBackgroundResource(R.drawable.bg_pojav_row)
        }
        val line = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        line.addView(TextView(this).apply {
            text = title
            textSize = 13f
            setTextColor(getColor(R.color.cryonix_text))
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        })
        val value = TextView(this).apply {
            textSize = 10f
            setTextColor(getColor(R.color.cryonix_cyan))
        }
        line.addView(value)
        row.addView(line)
        row.addView(TextView(this).apply {
            text = subtitle
            textSize = 9f
            setTextColor(getColor(R.color.cryonix_text_secondary))
        })
        val seek = SeekBar(this)
        seek.max = max - min
        seek.progress = (prefs.getInt(key, default) - min).coerceIn(0, seek.max)
        fun update() {
            val current = min + seek.progress
            value.text = current.toString() + suffix
            prefs.edit().putInt(key, current).apply()
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) = update()
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {
                onChanged?.invoke(min + seek.progress)
            }
        })
        update()
        row.addView(seek)
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(92)).apply { bottomMargin = dp(7) })
    }

    private fun button(parent: LinearLayout, label: String, background: Int, action: () -> Unit) {
        parent.addView(TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 10f
            textStyle()
            setTextColor(Color.WHITE)
            setBackgroundResource(background)
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(7) })
    }

    private fun TextView.textStyle() {
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun choose(title: String, values: Array<String>, selected: Int, action: (Int) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setSingleChoiceItems(values, selected) { dialog, which ->
                action(which)
                dialog.dismiss()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
