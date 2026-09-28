package com.cryonix.launcher.downloads

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.ui.UiMotion

class DownloadsActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_downloads)
        findViewById<TextView>(R.id.downloads_back).setOnClickListener { UiMotion.press(it); finish() }
        bindPressAnimations(findViewById(android.R.id.content))
        UiMotion.morphIn(findViewById(android.R.id.content))
    }

    private fun bindPressAnimations(root: android.view.View) {
        if (root.isClickable) UiMotion.bindPress(root)
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) bindPressAnimations(root.getChildAt(i))
        }
    }
}