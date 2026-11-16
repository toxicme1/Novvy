package com.novvy.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.novvy.app.ui.theme.Coral
import com.novvy.app.ui.theme.Cyan
import com.novvy.app.ui.theme.GradientGreenCyan
import com.novvy.app.ui.theme.GradientRedViolet
import com.novvy.app.ui.theme.GradientVioletCyan
import com.novvy.app.ui.theme.GradientVioletCoral
import com.novvy.app.ui.theme.Violet

enum class OrbState { IDLE, THINKING, SUCCESS, ERROR }

/**
 * Novvy's animated AI orb. A soft gradient sphere with a gentle glow.
 * - IDLE: calm breathing pulse
 * - THINKING: rotating gradient
 * - SUCCESS: green with a check
 * - ERROR: red with an error glyph
 */
@Composable
fun NovvyOrb(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    state: OrbState = OrbState.IDLE,
    showGlow: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "orb")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
        label = "rot",
    )
    val breathe by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe",
    )

    val gradient = when (state) {
        OrbState.SUCCESS -> GradientGreenCyan
        OrbState.ERROR -> GradientRedViolet
        OrbState.THINKING -> listOf(Violet, Cyan, Coral)
        OrbState.IDLE -> GradientVioletCyan
    }
    val icon: ImageVector = when (state) {
        OrbState.SUCCESS -> Icons.Filled.Check
        OrbState.ERROR -> Icons.Filled.ErrorOutline
        OrbState.THINKING -> Icons.Filled.Bolt
        OrbState.IDLE -> Icons.Filled.AutoAwesome
    }
    val glow = gradient.last()

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (showGlow) {
            Box(
                Modifier
                    .size(size * 1.5f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(glow.copy(alpha = 0.38f), Color.Transparent)
                        )
                    )
            )
        }
        Box(
            Modifier
                .size(size)
                .scale(if (state == OrbState.THINKING) 1f else breathe)
                .clip(CircleShape)
                .background(Brush.sweepGradient(gradient))
                .then(
                    if (state == OrbState.THINKING)
                        Modifier.rotate(rotation)
                    else Modifier
                )
        )
        // gloss highlight
        Box(
            Modifier
                .size(size * 0.42f)
                .offset(x = -size * 0.16f, y = -size * 0.18f)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.28f))
        )
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.46f),
        )
    }
}

/** Simple brand gradient used by headers. */
val BrandGradient: Brush
    @Composable get() = Brush.linearGradient(listOf(Violet, Cyan, Coral))