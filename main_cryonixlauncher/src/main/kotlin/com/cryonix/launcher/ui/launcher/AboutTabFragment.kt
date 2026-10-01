package com.cryonix.launcher.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cryonix.launcher.R
import com.cryonix.launcher.settings.AboutActivity

class AboutTabFragment : Fragment(R.layout.fragment_tab_about) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val info = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        view.findViewById<TextView>(R.id.about_tab_version).text = "Version ${info.versionName ?: "unknown"}"
        view.findViewById<View>(R.id.about_tab_project).setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
    }
}
