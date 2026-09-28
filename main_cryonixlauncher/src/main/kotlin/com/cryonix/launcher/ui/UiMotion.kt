package com.cryonix.launcher.ui

import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Shared Cryonix motion language.
 * Uses a short press and a smooth jelly return with a small overshoot.
 */
object UiMotion {
    private val settle = DecelerateInterpolator(1.6f)
    private val jelly = OvershootInterpolator(1.45f)

    fun press(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(0.94f)
            .scaleY(0.94f)
            .setDuration(70)
            .setInterpolator(settle)
            .withEndAction {
                view.animate().cancel()
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(210)
                    .setInterpolator(jelly)
                    .start()
            }
            .start()
    }

    fun morphIn(view: View) {
        view.animate().cancel()
        view.alpha = 1f
        view.scaleX = 0.84f
        view.scaleY = 0.84f
        view.translationX = 0f
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
             .setDuration(300)
            .setInterpolator(jelly)
            .start()
    }

    fun morphOut(view: View, end: () -> Unit) {
        view.animate().cancel()
        view.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(150)
            .setInterpolator(settle)
            .withEndAction(end)
            .start()
    }

    fun bindPress(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().cancel()
                    v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(65)
                        .setInterpolator(settle)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().cancel()
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(220)
                        .setInterpolator(jelly)
                        .start()
                }
            }
            false
        }
    }
}
