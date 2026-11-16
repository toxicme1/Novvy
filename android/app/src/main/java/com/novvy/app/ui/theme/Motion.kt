package com.novvy.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/** Central motion tokens so animations stay consistent and purposeful. */
object NovvyMotion {
    const val Fast = 150
    const val Normal = 260
    const val Slow = 420

    val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Emphasized = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    fun <T> spring() = spring<T>(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
    fun <T> tween(duration: Int = Normal) = tween<T>(duration, easing = Standard)
}