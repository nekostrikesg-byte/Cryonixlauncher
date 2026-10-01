package com.cryonix.launcher.settings

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.cryonix.launcher.R
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.extra.ExtraListener
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceExperimentalFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment

/** Cryonix-branded host for the existing backend preference fragments. */
class RuntimeSettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private lateinit var title: TextView

    private val backPreferenceListener = object : ExtraListener<String> {
        override fun onValueSet(key: String, value: String): Boolean {
            runOnUiThread { navigateBack() }
            return false
        }
    }

    private enum class Section(val title: String, val fragment: Class<out Fragment>) {
        OVERVIEW("Runtime settings", LauncherPreferenceFragment::class.java),
        VIDEO("Renderer and video", LauncherPreferenceVideoFragment::class.java),
        CONTROLS("Touch controls", LauncherPreferenceControlFragment::class.java),
        JAVA("Java runtime", LauncherPreferenceJavaFragment::class.java),
        MISC("Launcher options", LauncherPreferenceMiscellaneousFragment::class.java),
        EXPERIMENTAL("Experimental options", LauncherPreferenceExperimentalFragment::class.java)
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_runtime_settings)
        title = findViewById(R.id.runtime_settings_title)
        findViewById<ImageButton>(R.id.runtime_settings_back).setOnClickListener { navigateBack() }
        supportFragmentManager.addOnBackStackChangedListener { updateTitle() }

        if (state == null) {
            val initial = when (intent.getStringExtra("section")) {
                "video" -> Section.VIDEO
                "controls" -> Section.CONTROLS
                "java" -> Section.JAVA
                "misc" -> Section.MISC
                "experimental" -> Section.EXPERIMENTAL
                else -> Section.OVERVIEW
            }
            showSection(initial)
        } else updateTitle()
    }

    override fun onStart() {
        super.onStart()
        ExtraCore.addExtraListener(ExtraConstants.BACK_PREFERENCE, backPreferenceListener)
    }

    override fun onStop() {
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.BACK_PREFERENCE, backPreferenceListener)
        super.onStop()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragmentName = pref.fragment ?: return false
        val fragment = Fragment.instantiate(this, fragmentName, pref.extras)
        title.text = pref.title?.toString() ?: "Runtime settings"
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.runtime_settings_fragment_host, fragment, fragmentName)
            .addToBackStack(fragmentName)
            .commit()
        return true
    }

    private fun showSection(section: Section) {
        title.text = section.title
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.runtime_settings_fragment_host, section.fragment.getDeclaredConstructor().newInstance(), section.name)
            .commit()
    }

    private fun navigateBack() {
        if (supportFragmentManager.backStackEntryCount > 0) supportFragmentManager.popBackStack()
        else finish()
    }

    private fun updateTitle() {
        val fragment = supportFragmentManager.findFragmentById(R.id.runtime_settings_fragment_host)
        title.text = when (fragment) {
            is LauncherPreferenceVideoFragment -> Section.VIDEO.title
            is LauncherPreferenceControlFragment -> Section.CONTROLS.title
            is LauncherPreferenceJavaFragment -> Section.JAVA.title
            is LauncherPreferenceMiscellaneousFragment -> Section.MISC.title
            is LauncherPreferenceExperimentalFragment -> Section.EXPERIMENTAL.title
            else -> Section.OVERVIEW.title
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() = navigateBack()
}
