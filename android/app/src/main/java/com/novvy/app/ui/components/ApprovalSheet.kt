package com.novvy.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.ApprovalRequest
import com.novvy.app.domain.model.ApprovalType
import com.novvy.app.ui.theme.SuccessGreen
import com.novvy.app.ui.theme.WarningAmber

/**
 * Premium approval sheet (slides up from the bottom with a scrim).
 * Replaces the plain AlertDialog so sensitive actions feel deliberate.
 */
@Composable
fun ApprovalSheet(
    approval: ApprovalRequest,
    onApprove: () -> Unit,
    onCancel: () -> Unit,
    onReview: () -> Unit = {},
) {
    val offset = remember { Animatable(1f) }
    LaunchedEffect(Unit) { offset.animateTo(0f, tween(320)) }

    Box(Modifier.fillMaxSize()) {
        // Scrim
        Box(
            Modifier.fillMaxSize()
                .background(Color.Black.copy(alpha = 0.46f))
                .clickable { onCancel() }
        )

        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer { translationY = offset.value * 900f }
        ) {
            Column(Modifier.padding(bottom = 28.dp)) {
                Box(
                    Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp)
                        .size(40.dp, 4.dp).clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outline)
                )
                Column(Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GradientIcon(agentIcon(approval.agentKind), agentGradient(approval.agentKind), size = 52.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(approval.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                approval.agentKind.displayName + " Agent",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        RiskChip(approval.type)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Detail rows
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val lines = approval.detail.split("\n").filter { it.isNotBlank() }
                            if (lines.isEmpty()) {
                                Text(approval.summary, style = MaterialTheme.typography.bodyMedium)
                            } else {
                                lines.forEach { line ->
                                    val idx = line.indexOf(':')
                                    if (idx > 0) {
                                        Row {
                                            Text(
                                                line.take(idx + 1),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.width(92.dp),
                                            )
                                            Text(
                                                line.substring(idx + 1).trim(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                    } else {
                                        Text(line, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        OutlinedButton(onClick = onReview, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                            Text("Review details")
                        }
                        Button(onClick = onApprove, modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(14.dp)) {
                            Text("Approve")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Novvy never sends or saves without your approval.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskChip(type: ApprovalType) {
    val (label, color) = when (type) {
        ApprovalType.GMAIL_SEND, ApprovalType.PUBLIC_POST, ApprovalType.CONTACT_PERSON -> "High" to Color(0xFFEF4444)
        ApprovalType.DRIVE_SAVE, ApprovalType.PURCHASE -> "Medium" to WarningAmber
        else -> "Low" to SuccessGreen
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(color))
            Spacer(Modifier.width(5.dp))
            Text("$label risk", style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}