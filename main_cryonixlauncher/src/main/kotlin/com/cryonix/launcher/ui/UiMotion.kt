package com.cryonix.launcher.ui

import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

object UiMotion {
    fun press(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(0.90f).scaleY(0.90f)
            .setDuration(70)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1.055f).scaleY(1.055f)
                    .setDuration(120)
                    .setInterpolator(OvershootInterpolator(2.2f))
                    .withEndAction {
                        view.animate().scaleX(1f).scaleY(1f)
                            .setDuration(150)
                            .setInterpolator(OvershootInterpolator(1.4f))
                            .start()
                    }.start()
            }.start()
    }

    fun morphIn(view: View) {
        view.alpha = 0f
        view.scaleX = 0.82f
        view.scaleY = 0.82f
        view.animate()
            .alpha(1f).scaleX(1.035f).scaleY(1.035f)
            .setDuration(280)
            .setInterpolator(OvershootInterpolator(1.25f))
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f)
                    .setDuration(120)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }.start()
    }

    fun morphOut(view: View, end: () -> Unit) {
        view.animate().alpha(0f).scaleX(0.90f).scaleY(0.90f)
            .setDuration(190)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction(end)
            .start()
    }

    fun bindPress(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().cancel()
                    v.animate().scaleX(0.96f).scaleY(0.96f)
                        .setDuration(55).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.035f).scaleY(1.035f)
                        .setDuration(85)
                        .setInterpolator(OvershootInterpolator(2f))
                        .withEndAction {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(110).start()
                        }.start()
                }
            }
            false
        }
    }
}