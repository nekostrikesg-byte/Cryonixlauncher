package com.cryonix.launcher.settings

import android.os.Bundle
import android.view.View
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

/** Cryonix-styled host for the full Pojav backend preference screens. */
class PojavSettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private lateinit var currentTitle: TextView

    private val backPreferenceListener = object : ExtraListener<String> {
        override fun onValueSet(key: String, value: String): Boolean {
            runOnUiThread {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    showSection(Section.OVERVIEW)
                }
            }
            return false
        }
    }

    private enum class Section(val title: String, val fragment: Class<out Fragment>) {
        OVERVIEW("OVERVIEW", LauncherPreferenceFragment::class.java),
        VIDEO("VIDEO & RENDERER", LauncherPreferenceVideoFragment::class.java),
        CONTROLS("CONTROLS", LauncherPreferenceControlFragment::class.java),
        JAVA("JAVA & MEMORY", LauncherPreferenceJavaFragment::class.java),
        MISC("MISCELLANEOUS", LauncherPreferenceMiscellaneousFragment::class.java),
        EXPERIMENTAL("EXPERIMENTAL", LauncherPreferenceExperimentalFragment::class.java)
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_pojav_settings)
        currentTitle = findViewById(R.id.pojav_settings_current)
        findViewById<View>(R.id.pojav_settings_back).setOnClickListener { finish() }

        bindNavigation(R.id.pojav_nav_overview, Section.OVERVIEW)
        bindNavigation(R.id.pojav_nav_video, Section.VIDEO)
        bindNavigation(R.id.pojav_nav_controls, Section.CONTROLS)
        bindNavigation(R.id.pojav_nav_java, Section.JAVA)
        bindNavigation(R.id.pojav_nav_misc, Section.MISC)
        bindNavigation(R.id.pojav_nav_experimental, Section.EXPERIMENTAL)
        supportFragmentManager.addOnBackStackChangedListener { updateHeaderForCurrentFragment() }

        if (state == null) {
            val initialSection = when (intent.getStringExtra("section")) {
                "video" -> Section.VIDEO
                "controls" -> Section.CONTROLS
                "java" -> Section.JAVA
                "misc" -> Section.MISC
                "experimental" -> Section.EXPERIMENTAL
                else -> Section.OVERVIEW
            }
            showSection(initialSection)
        } else updateHeaderForCurrentFragment()
    }

    override fun onStart() {
        super.onStart()
        ExtraCore.addExtraListener(ExtraConstants.BACK_PREFERENCE, backPreferenceListener)
    }

    override fun onStop() {
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.BACK_PREFERENCE, backPreferenceListener)
        super.onStop()
    }

    private fun bindNavigation(viewId: Int, section: Section) {
        findViewById<TextView>(viewId).apply {
            isClickable = true
            isFocusable = true
            setOnClickListener { showSection(section) }
        }
    }

    private fun showSection(section: Section) {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        currentTitle.text = section.title
        listOf(
            R.id.pojav_nav_overview to Section.OVERVIEW,
            R.id.pojav_nav_video to Section.VIDEO,
            R.id.pojav_nav_controls to Section.CONTROLS,
            R.id.pojav_nav_java to Section.JAVA,
            R.id.pojav_nav_misc to Section.MISC,
            R.id.pojav_nav_experimental to Section.EXPERIMENTAL
        ).forEach { (viewId, navSection) -> findViewById<View>(viewId).isSelected = navSection == section }

        val fragment = section.fragment.getDeclaredConstructor().newInstance()
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.pojav_settings_fragment_host, fragment, section.name)
            .commit()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragmentName = pref.fragment ?: return false
        val fragment = Fragment.instantiate(this, fragmentName, pref.extras)
        currentTitle.text = pref.title?.toString()?.uppercase() ?: "BACKEND SETTINGS"
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.pojav_settings_fragment_host, fragment, fragmentName)
            .addToBackStack(fragmentName)
            .commit()
        return true
    }

    private fun updateHeaderForCurrentFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.pojav_settings_fragment_host)
        currentTitle.text = when (fragment) {
            is LauncherPreferenceVideoFragment -> Section.VIDEO.title
            is LauncherPreferenceControlFragment -> Section.CONTROLS.title
            is LauncherPreferenceJavaFragment -> Section.JAVA.title
            is LauncherPreferenceMiscellaneousFragment -> Section.MISC.title
            is LauncherPreferenceExperimentalFragment -> Section.EXPERIMENTAL.title
            else -> Section.OVERVIEW.title
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}
