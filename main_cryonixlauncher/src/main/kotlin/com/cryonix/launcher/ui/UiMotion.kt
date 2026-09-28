package com.cryonix.launcher.ui

import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Shared Cryonix motion language.
 * Compact press, elastic release and a soft zoom for screen transitions.
 */
object UiMotion {
    private val settle = DecelerateInterpolator(1.8f)
    private val jelly = OvershootInterpolator(1.55f)

    fun press(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(0.91f)
            .scaleY(0.91f)
            .setDuration(72)
            .setInterpolator(settle)
            .withEndAction {
                view.animate().cancel()
                view.animate()
                    .scaleX(1.025f)
                    .scaleY(1.025f)
                    .setDuration(105)
                    .setInterpolator(settle)
                    .withEndAction {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(190)
                            .setInterpolator(jelly)
                            .start()
                    }
                    .start()
            }
            .start()
    }

    fun morphIn(view: View) {
        view.animate().cancel()
        view.alpha = 0.88f
        view.scaleX = 0.86f
        view.scaleY = 0.86f
        view.translationY = 14f
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(330)
            .setInterpolator(jelly)
            .start()
    }

    fun morphOut(view: View, end: () -> Unit) {
        view.animate().cancel()
        view.animate()
            .alpha(0f)
            .scaleX(0.9f)
            .scaleY(0.9f)
            .translationY(-8f)
            .setDuration(170)
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
                        .scaleX(0.94f)
                        .scaleY(0.94f)
                        .setDuration(65)
                        .setInterpolator(settle)
                        .start()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().cancel()
                    v.animate()
                        .scaleX(1.035f)
                        .scaleY(1.035f)
                        .setDuration(100)
                        .setInterpolator(settle)
                        .withEndAction {
                            v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(205)
                                .setInterpolator(jelly)
                                .start()
                        }
                        .start()
                }
            }
            false
        }
    }
}
