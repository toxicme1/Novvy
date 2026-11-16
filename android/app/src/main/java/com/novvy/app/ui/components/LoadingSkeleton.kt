package com.novvy.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

private fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = -800f,
        targetValue = 1600f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shift",
    )
    background(
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                MaterialTheme.colorScheme.surfaceVariant,
            ),
            start = Offset(shift, 0f),
            end = Offset(shift + 500f, 0f),
        )
    )
}

private val ShimmerShape = RoundedCornerShape(10.dp)

@Composable
fun SkeletonLine(modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 14.dp) {
    Box(modifier.height(height).clip(ShimmerShape).shimmer())
}

/** A premium loading placeholder that mimics a card list. */
@Composable
fun LoadingSkeletonCards(count: Int = 3, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(count) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).shimmer())
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        SkeletonLine(Modifier.fillMaxWidth(0.6f), 16.dp)
                        Spacer(Modifier.height(8.dp))
                        SkeletonLine(Modifier.fillMaxWidth(0.9f), 12.dp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                SkeletonLine(Modifier.fillMaxWidth(), 8.dp)
            }
        }
    }
}