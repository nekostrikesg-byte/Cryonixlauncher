package com.cryonix.launcher.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import com.cryonix.launcher.ui.UiMotion
import com.cryonix.launcher.MainActivity
import com.cryonix.launcher.R
import com.cryonix.launcher.accounts.AccountsActivity
import com.cryonix.launcher.minecraft.MinecraftSettingsStore
import com.cryonix.launcher.minecraft.model.RendererProfile

class SettingsActivity : Activity() {
    private lateinit var store: MinecraftSettingsStore
    private lateinit var rendererValue: TextView
    private lateinit var vulkanValue: TextView
    private lateinit var graphicsValue: TextView
    private lateinit var resolutionValue: TextView
    private lateinit var gameValue: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_settings)

        store = MinecraftSettingsStore(this)
        rendererValue = findViewById(R.id.settings_global_renderer_value)
        vulkanValue = findViewById(R.id.settings_vulkan_value)
        graphicsValue = findViewById(R.id.settings_graphics_value)
        resolutionValue = findViewById(R.id.settings_resolution_value)
        gameValue = findViewById(R.id.settings_game_value)

        findViewById<ImageButton>(R.id.settings_back).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.settings_home).setOnClickListener { 
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<android.view.View>(R.id.settings_global_renderer).setOnClickListener { chooseRenderer() }
        findViewById<android.view.View>(R.id.settings_vulkan).setOnClickListener { chooseVulkan() }
        findViewById<android.view.View>(R.id.settings_graphics).setOnClickListener { chooseGraphics() }
        findViewById<android.view.View>(R.id.settings_resolution_rule).setOnClickListener { chooseResolution() }
        findViewById<android.view.View>(R.id.settings_game_value).setOnClickListener { chooseGame() }
        findViewById<android.view.View>(R.id.settings_sidebar_renderer).setOnClickListener {  bindNav(R.id.settings_nav_renderer, R.id.settings_section_renderer) }
        findViewById<android.view.View>(R.id.settings_sidebar_game).setOnClickListener {  scrollTo(R.id.settings_section_game) }
        findViewById<android.view.View>(R.id.settings_sidebar_controls).setOnClickListener {  scrollTo(R.id.settings_section_controls) }
        findViewById<android.view.View>(R.id.settings_sidebar_launcher).setOnClickListener {  scrollTo(R.id.settings_section_launcher) }
        findViewById<android.view.View>(R.id.settings_sidebar_java).setOnClickListener {  scrollTo(R.id.settings_section_java) }

        findViewById<android.view.View>(R.id.settings_java_value).setOnClickListener { showInfo("Java", "Automatic runtime selection is enabled. Java runtime installation/management will be added when the runtime backend is connected.") }

        bindNav(R.id.settings_nav_renderer, R.id.settings_section_renderer)
        findViewById<View>(R.id.settings_overview_general).setOnClickListener { scrollTo(R.id.settings_section_renderer) }
        findViewById<View>(R.id.settings_overview_game).setOnClickListener { scrollTo(R.id.settings_section_game) }
        findViewById<View>(R.id.settings_overview_display).setOnClickListener { scrollTo(R.id.settings_section_gamepad) }
        findViewById<View>(R.id.settings_overview_controls).setOnClickListener { scrollTo(R.id.settings_section_controls) }
        findViewById<View>(R.id.settings_overview_advanced).setOnClickListener { scrollTo(R.id.settings_section_launcher) }
        refresh()
        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
    }

    private fun scrollTo(targetId: Int) {
        val scroll = findViewById<ScrollView>(R.id.settings_scroll)
        val target = findViewById<View>(targetId)
        scroll.post { scroll.smoothScrollTo(0, target.top) }
    }

    private fun bindNav(navId: Int, targetId: Int) {
        val scroll = findViewById<ScrollView>(R.id.settings_scroll)
        findViewById<android.view.View>(navId).setOnClickListener { 
            val target = findViewById<android.view.View>(targetId)
            scroll.post { scroll.smoothScrollTo(0, target.top) }
        }
    }

    private fun chooseRenderer() {
        val values = arrayOf("Krypton Wrapper", "OpenGL", "LTW", "Holy GL4ES", "Mobile GLUES", "Vulkan")
        val current = rendererLabel(store.renderer)
        AlertDialog.Builder(this)
            .setTitle("Global Renderer")
            .setSingleChoiceItems(values, values.indexOf(current)) { dialog, which ->
                store.renderer = when (which) {
                    0 -> RendererProfile.Backend.SYSTEM
                    1 -> RendererProfile.Backend.OPENGL
                    2 -> RendererProfile.Backend.LTW
                    3 -> RendererProfile.Backend.HOLY_GL4ES
                    4 -> RendererProfile.Backend.MOBILE_GLUES
                    else -> RendererProfile.Backend.VULKAN
                }
                dialog.dismiss()
                refresh()
            }.show()
    }

    private fun chooseVulkan() {
        choose("Vulkan Driver", arrayOf("Turnip", "System", "Auto"), store.vulkanDriver) {
            store.vulkanDriver = it
            refresh()
        }
    }

    private fun chooseGraphics() {
        choose("Graphics API", arrayOf("OpenGL", "Vulkan"), store.graphicsApi) {
            store.graphicsApi = it
            refresh()
        }
    }

    private fun chooseResolution() {
        choose("Resolution Rule", arrayOf("Percentage · 100%", "Percentage · 75%", "Percentage · 50%", "Exact"), store.resolutionRule) {
            store.resolutionRule = it
            refresh()
        }
    }

    private fun chooseGame() {
        val values = arrayOf(1024, 1536, 2048, 3072, 4096)
        AlertDialog.Builder(this)
            .setTitle("Game Memory")
            .setSingleChoiceItems(
                values.map { "$it MB" }.toTypedArray(),
                values.indexOf(store.memoryMb).coerceAtLeast(0)
            ) { dialog, which ->
                store.memoryMb = values[which]
                dialog.dismiss()
                refresh()
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun choose(title: String, values: Array<String>, selected: String, save: (String) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setSingleChoiceItems(values, values.indexOf(selected).coerceAtLeast(0)) { dialog, which ->
                save(values[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showInfo(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show()
    }

    private fun refresh() {
        rendererValue.text = "Selected: " + rendererLabel(store.renderer)
        vulkanValue.text = store.vulkanDriver
        graphicsValue.text = store.graphicsApi
        resolutionValue.text = store.resolutionRule
        gameValue.text = "Version: Automatic\nLoader: ${store.loader.lowercase()}    •    Memory: ${store.memoryMb} MB"
    }

    private fun bindPressAnimations(root: View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }

    private fun rendererLabel(renderer: RendererProfile.Backend): String =
        when (renderer) {
            RendererProfile.Backend.SYSTEM -> "Krypton Wrapper"
            RendererProfile.Backend.OPENGL -> "OpenGL"
            RendererProfile.Backend.LTW -> "LTW"
            RendererProfile.Backend.HOLY_GL4ES -> "Holy GL4ES"
            RendererProfile.Backend.MOBILE_GLUES -> "Mobile GLUES"
            RendererProfile.Backend.VULKAN -> "Vulkan"
        }
}