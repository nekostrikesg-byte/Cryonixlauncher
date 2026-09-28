package com.cryonix.launcher.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.cryonix.launcher.R
import com.cryonix.launcher.core.LauncherInfo

class AboutActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.screen_about)
        findViewById<View>(R.id.about_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.about_version).text = "Version " + LauncherInfo().version
        findViewById<View>(R.id.about_github).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nekostrikesg-byte/Cryonixlauncher")))
        }
    }
}
