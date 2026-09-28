package com.cryonix.launcher.ui

import android.view.View
import android.view.animation.DecelerateInterpolator

object UiMotion {
    fun press(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(0.94f).scaleY(0.94f)
            .setDuration(70)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
            }.start()
    }

    fun morphIn(view: View) {
        view.alpha = 0f
        view.scaleX = 0.94f
        view.scaleY = 0.94f
        view.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(260)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun morphOut(view: View, end: () -> Unit) {
        view.animate().alpha(0f).scaleX(0.96f).scaleY(0.96f)
            .setDuration(180)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction(end)
            .start()
    }

    fun bindPress(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> press(v)
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
            }
            false
        }
    }
}