package com.cryonix.launcher.ui.launcher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.cryonix.launcher.R

internal object LauncherNavigationRows {
    fun add(
        context: Context,
        parent: LinearLayout,
        title: String,
        summary: String,
        icon: Int,
        onClick: () -> Unit
    ) {
        val row = LayoutInflater.from(context).inflate(R.layout.item_cryonix_navigation_row, parent, false)
        row.findViewById<ImageView>(R.id.navigation_row_icon).setImageResource(icon)
        row.findViewById<TextView>(R.id.navigation_row_title).text = title
        row.findViewById<TextView>(R.id.navigation_row_summary).text = summary
        row.setOnClickListener { onClick() }
        parent.addView(row)
        parent.addView(View(context).apply {
            setBackgroundColor(context.getColor(R.color.cryonix_divider))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 1)))
    }

    private fun dp(context: Context, value: Int) =
        (value * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
}
