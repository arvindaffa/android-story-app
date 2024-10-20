package com.myprt.app.util

import android.animation.ObjectAnimator
import android.view.View

private const val ANIMATION_DURATION = 3000L

fun View.fadeIn() {
    ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f).apply {
        duration = ANIMATION_DURATION
    }.start()
}