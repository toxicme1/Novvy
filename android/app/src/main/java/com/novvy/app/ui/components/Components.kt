package com.novvy.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.AgentKind
import com.novvy.app.domain.model.AgentState
import com.novvy.app.domain.model.AgentTask
import com.novvy.app.ui.theme.Cyan
import com.novvy.app.ui.theme.CyanGlow
import com.novvy.app.ui.theme.ErrorRed
import com.novvy.app.ui.theme.GradientBlueCyan
import com.novvy.app.ui.theme.GradientCyanBlue
import com.novvy.app.ui.theme.GradientGreenCyan
import com.novvy.app.ui.theme.GradientRedViolet
import com.novvy.app.ui.theme.GradientVioletBlue
import com.novvy.app.ui.theme.GradientVioletCyan
import com.novvy.app.ui.theme.GradientVioletCoral
import com.novvy.app.ui.theme.PausedOrange
import com.novvy.app.ui.theme.SuccessGreen
import com.novvy.app.ui.theme.Violet
import com.novvy.app.ui.theme.VioletGlow
import com.novvy.app.ui.theme.WarningAmber
import kotlin.math.roundToInt

// ── Agent visuals ─────────────────────────────────────────────────────────

fun agentIcon(kind: AgentKind): ImageVector = when (kind) {
    AgentKind.COORDINATOR -> Icons.Filled.AccountTree
    AgentKind.PLANNER -> Icons.Filled.Checklist
    AgentKind.RESEARCH -> Icons.Filled.TravelExplore
    AgentKind.ANALYSIS -> Icons.Filled.Insights
    AgentKind.BUDGET -> Icons.Filled.Savings
    AgentKind.HOTEL -> Icons.Filled.Hotel
    AgentKind.ITINERARY -> Icons.Filled.Map
    AgentKind.WRITER -> Icons.Filled.EditNote
    AgentKind.DOCUMENT -> Icons.Filled.Description
    AgentKind.TASK -> Icons.Filled.Task
    AgentKind.REVIEWER -> Icons.Filled.FactCheck
    AgentKind.GMAIL -> Icons.Filled.Email
    AgentKind.DRIVE -> Icons.Filled.Cloud
}

fun agentGradient(kind: AgentKind): List<Color> = when (kind) {
    AgentKind.COORDINATOR -> GradientVioletCoral
    AgentKind.PLANNER -> GradientVioletBlue
    AgentKind.RESEARCH -> GradientVioletCyan
    AgentKind.ANALYSIS, AgentKind.BUDGET, AgentKind.DOCUMENT -> GradientCyanBlue
    AgentKind.HOTEL, AgentKind.DRIVE -> GradientBlueCyan
    AgentKind.ITINERARY, AgentKind.REVIEWER, AgentKind.TASK -> GradientGreenCyan
    AgentKind.WRITER -> GradientVioletCoral
    AgentKind.GMAIL -> GradientRedViolet
}

fun stateLabel(state: AgentState): String = when (state) {
    AgentState.QUEUED -> "Queued"
    AgentState.RUNNING -> "Running"
    AgentState.WAITING_APPROVAL -> "Needs approval"
    AgentState.COMPLETE -> "Complete"
    AgentState.FAILED -> "Failed"
    AgentState.PAUSED -> "Paused"
    AgentState.CANCELLED -> "Cancelled"
}

fun stateIcon(state: AgentState): ImageVector = when (state) {
    AgentState.QUEUED -> Icons.Filled.Schedule
    AgentState.RUNNING -> Icons.Filled.Autorenew
    AgentState.WAITING_APPROVAL -> Icons.Filled.FactCheck
    AgentState.COMPLETE -> Icons.Filled.Check
    AgentState.FAILED -> Icons.Filled.ErrorOutline
    AgentState.PAUSED -> Icons.Filled.Pause
    AgentState.CANCELLED -> Icons.Filled.Cancel
}

@Composable
fun stateColor(state: AgentState): Color = when (state) {
    AgentState.QUEUED -> MaterialTheme.colorScheme.onSurfaceVariant
    AgentState.RUNNING -> MaterialTheme.colorScheme.primary
    AgentState.WAITING_APPROVAL -> WarningAmber
    AgentState.COMPLETE -> SuccessGreen
    AgentState.FAILED -> ErrorRed
    AgentState.PAUSED -> PausedOrange
    AgentState.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
}

// ── Primitives ─────────────────────────────────────────────────────────────

@Composable
fun GradientIcon(
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    iconFraction: Float = 0.5f,
) {
    Box(
        modifier.size(size).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * iconFraction))
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(vertical = 6.dp),
    )
}

@Composable
fun StatePill(state: AgentState) {
    val color = stateColor(state)
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(stateIcon(state), null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(stateLabel(state), style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
fun GradientProgressBar(
    value: Float,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 6.dp,
    showPercent: Boolean = false,
) {
    val animated by animateFloatAsState(targetValue = value.coerceIn(0f, 1f), animationSpec = tween(500), label = "gp")
    Column(modifier) {
        Box(
            Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        ) {
            Box(
                Modifier.fillMaxWidth(animated).height(height).clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(GradientVioletCyan))
            )
        }
        if (showPercent) {
            Text(
                "${(animated * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    icon: ImageVector = Icons.Filled.TravelExplore,
) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NovvyOrb(size = 64.dp, state = OrbState.IDLE)
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
private fun Button(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))
    }
}

// ── Prompt card ────────────────────────────────────────────────────────────

@Composable
fun NovvyPromptCard(
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    gmailConnected: Boolean = false,
    driveConnected: Boolean = false,
) {
    var text by remember { mutableStateOf("") }
    var attached by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }

    Box(
        modifier.fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(GradientVioletCyan))
            .padding(1.5.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                    placeholder = { Text("Ask Novvy anything…") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )

                if (attached) {
                    AttachmentChip(onRemove = { attached = false })
                    Spacer(Modifier.height(10.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulseIconButton(
                        icon = if (listening) Icons.Filled.KeyboardVoice else Icons.Filled.Mic,
                        contentDescription = if (listening) "Stop listening" else "Voice input",
                        active = listening,
                        onClick = { listening = !listening },
                    )
                    Spacer(Modifier.width(6.dp))
                    PulseIconButton(
                        icon = Icons.Filled.AttachFile,
                        contentDescription = "Attach file",
                        active = attached,
                        onClick = { attached = !attached },
                    )
                    Spacer(Modifier.weight(1f))
                    ConnectionDots(gmailConnected, driveConnected)
                    Spacer(Modifier.width(10.dp))
                    SendButton(
                        enabled = text.isNotBlank(),
                        onClick = { onSend(text.trim()); text = ""; attached = false },
                    )
                }
                if (listening) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Listening…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentChip(onRemove: () -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("document.pdf", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.Cancel, "Remove", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(15.dp).clickable { onRemove() })
        }
    }
}

@Composable
private fun ConnectionDots(gmail: Boolean, drive: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DotWithLabel("Gmail", gmail)
        DotWithLabel("Drive", drive)
    }
}

@Composable
private fun DotWithLabel(label: String, connected: Boolean) {
    val color = if (connected) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PulseIconButton(icon: ImageVector, contentDescription: String, active: Boolean, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (active) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "s",
    )
    Box(
        Modifier.size(44.dp).clip(CircleShape)
            .background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription,
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp).scale(if (active) scale else 1f),
        )
    }
}

@Composable
private fun SendButton(enabled: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (enabled) 1f else 0.92f, tween(220), label = "send")
    Box(contentAlignment = Alignment.Center) {
        if (enabled) {
            Box(
                Modifier.size(60.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(VioletGlow.copy(alpha = 0.5f), Color.Transparent)))
            )
        }
        Box(
            Modifier.size(56.dp).scale(scale).clip(CircleShape)
                .background(
                    if (enabled) Brush.linearGradient(GradientVioletCoral)
                    else SolidColor(MaterialTheme.colorScheme.surfaceVariant)
                )
                .clickable(enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Send,
                contentDescription = "Send",
                tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ── Quick action card ──────────────────────────────────────────────────────

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Column(Modifier.padding(14.dp)) {
            GradientIcon(icon, gradient, size = 42.dp, iconFraction = 0.5f)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ── Agent status card ──────────────────────────────────────────────────────

@Composable
fun AgentStatusCard(
    agent: AgentTask,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    var expanded by remember(agent.id) { mutableStateOf(false) }
    val color = stateColor(agent.state)
    val running = agent.state == AgentState.RUNNING || agent.state == AgentState.WAITING_APPROVAL

    Card(
        onClick = { expanded = !expanded; onClick() },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (running) 3.dp else 1.dp),
        border = BorderStroke(1.dp, if (running) color.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    if (running) GlowPulse(color = color)
                    GradientIcon(agentIcon(agent.kind), agentGradient(agent.kind), size = 44.dp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(agent.kind.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        agent.action,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (agent.state == AgentState.COMPLETE) {
                    Icon(Icons.Filled.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                } else {
                    StatePill(agent.state)
                }
            }

            AnimatedVisibility(visible = running) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    GradientProgressBar(agent.progress, showPercent = true)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            "Step ${(agent.progress * 5).roundToInt().coerceIn(1, 5)} of 5",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            if (agent.needsApproval) {
                Spacer(Modifier.height(12.dp))
                ApprovalStrip(agent.kind.displayName)
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    if (agent.detail.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(agent.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (agent.sources.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        agent.sources.forEach {
                            Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovalStrip(agentName: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = WarningAmber.copy(alpha = 0.14f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.FactCheck, null, tint = WarningAmber, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("$agentName needs approval", style = MaterialTheme.typography.labelLarge, color = WarningAmber)
        }
    }
}

@Composable
private fun GlowPulse(color: Color) {
    val transition = rememberInfiniteTransition(label = "glow")
    val a by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "a",
    )
    Box(
        Modifier.size(54.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(color.copy(alpha = a), Color.Transparent)))
    )
}

// ── Coordinator message ────────────────────────────────────────────────────

@Composable
fun CoordinatorMessage(message: String, modifier: Modifier = Modifier) {
    Row(modifier) {
        NovvyOrb(size = 34.dp, state = OrbState.IDLE, showGlow = false)
        Spacer(Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                message.ifBlank { "Coordinating…" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(13.dp),
            )
        }
    }
}

// ── Step indicator (plan progress) ────────────────────────────────────────

@Composable
fun StepIndicator(agents: List<AgentTask>, modifier: Modifier = Modifier) {
    if (agents.isEmpty()) return
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        agents.forEachIndexed { index, agent ->
            val color = stateColor(agent.state)
            Box(
                Modifier.size(14.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center,
            ) {
                if (agent.state == AgentState.COMPLETE) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            if (index < agents.size - 1) {
                Box(
                    Modifier.weight(1f).height(3.dp)
                        .background(
                            if (agent.state == AgentState.COMPLETE)
                                Brush.horizontalGradient(GradientGreenCyan)
                            else SolidColor(MaterialTheme.colorScheme.outline)
                        )
                )
            }
        }
    }
}

// ── Bottom navigation ──────────────────────────────────────────────────────

enum class NovvyTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    TASKS("Tasks", Icons.Filled.Task),
    FILES("Files", Icons.Filled.Folder),
    INBOX("Inbox", Icons.Filled.Inbox),
    SETTINGS("Settings", Icons.Filled.Settings),
}

@Composable
fun NovvyBottomNavigation(
    selected: NovvyTab,
    onSelect: (NovvyTab) -> Unit,
    inboxUnread: Int = 0,
    tasksApprovals: Int = 0,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NovvyTab.entries.forEach { tab ->
            val count = when (tab) {
                NovvyTab.INBOX -> inboxUnread
                NovvyTab.TASKS -> tasksApprovals
                else -> 0
            }
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = {
                    if (count > 0) {
                        androidx.compose.material3.BadgedBox(
                            badge = {
                                androidx.compose.material3.Badge {
                                    Text("$count", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        ) { Icon(tab.icon, contentDescription = tab.label) }
                    } else {
                        Icon(tab.icon, contentDescription = tab.label)
                    }
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}