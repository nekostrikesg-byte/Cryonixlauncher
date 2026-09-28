package com.cryonix.launcher.ui

import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

object UiMotion {
    fun press(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(0.965f).scaleY(0.965f)
            .setDuration(65)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f)
                    .setDuration(115)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }.start()
    }

    fun morphIn(view: View) {
        view.alpha = 0f
        view.scaleX = 0.97f
        view.scaleY = 0.97f
        view.translationX = 26f
        view.animate()
            .alpha(1f)
            .scaleX(1f).scaleY(1f)
            .translationX(0f)
            .setDuration(230)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun morphOut(view: View, end: () -> Unit) {
        view.animate()
            .alpha(0f)
            .scaleX(0.98f).scaleY(0.98f)
            .translationX(-18f)
            .setDuration(180)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction(end)
            .start()
    }

    fun bindPress(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN ->
                    v.animate().scaleX(0.975f).scaleY(0.975f).setDuration(55).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120)
                        .setInterpolator(DecelerateInterpolator()).start()
            }
            false
        }
    }
}