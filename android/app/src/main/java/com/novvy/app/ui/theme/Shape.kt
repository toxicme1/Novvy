package com.novvy.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Novvy's expressive shape system. */
val NovvyShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Convenience radii used across components. */
object Radii {
    val chip = 50
    val card = 22
    val cardSmall = 16
    val sheet = 28
    val button = 16
    val field = 18
}