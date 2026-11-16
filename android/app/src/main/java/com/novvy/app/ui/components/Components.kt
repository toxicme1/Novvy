package com.novvy.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.AgentKind
import com.novvy.app.domain.model.AgentState
import com.novvy.app.domain.model.AgentTask
import com.novvy.app.ui.theme.Coral
import com.novvy.app.ui.theme.SuccessGreen
import com.novvy.app.ui.theme.WarningAmber
import com.novvy.app.ui.theme.ErrorRed
import com.novvy.app.ui.theme.PausedOrange

// ---------------------------------------------------------------------------
// Icons
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// Status helpers
// ---------------------------------------------------------------------------

fun stateLabel(state: AgentState): String = when (state) {
    AgentState.QUEUED -> "Queued"
    AgentState.RUNNING -> "Running"
    AgentState.WAITING_APPROVAL -> "Needs approval"
    AgentState.COMPLETE -> "Complete"
    AgentState.FAILED -> "Failed"
    AgentState.PAUSED -> "Paused"
    AgentState.CANCELLED -> "Cancelled"
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

// ---------------------------------------------------------------------------
// Small building blocks
// ---------------------------------------------------------------------------

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(vertical = 8.dp),
    )
}

@Composable
fun StatePill(state: AgentState) {
    val color = stateColor(state)
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f),
        contentColor = color,
    ) {
        Text(
            text = stateLabel(state),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun ProgressBar(value: Float, color: Color, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(targetValue = value.coerceIn(0f, 1f), animationSpec = tween(400), label = "progress")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, icon: ImageVector = Icons.Filled.TravelExplore) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp)) }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------------------------------------------------------------------------
// Agent status card
// ---------------------------------------------------------------------------

@Composable
fun AgentStatusCard(
    agent: AgentTask,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val color = stateColor(agent.state)
    val running = agent.state == AgentState.RUNNING || agent.state == AgentState.WAITING_APPROVAL

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (running) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseScale",
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(if (running) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(agentIcon(agent.kind), null, tint = color, modifier = Modifier.size(22.dp))
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
                    Spacer(Modifier.height(10.dp))
                    ProgressBar(agent.progress, color)
                }
            }
            if (agent.needsApproval) {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(WarningAmber.copy(alpha = 0.12f)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.FactCheck, null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${agent.kind.displayName} needs approval",
                        style = MaterialTheme.typography.labelLarge,
                        color = WarningAmber,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Prompt card
// ---------------------------------------------------------------------------

@Composable
fun NovvyPromptCard(
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What would you like Novvy to do?") },
                minLines = 2,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Voice input",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp).clickable {}.padding(4.dp),
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Filled.AttachFile,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp).clickable {}.padding(4.dp),
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = { if (text.isNotBlank()) { onSend(text.trim()); text = "" } },
                    shape = CircleShape,
                    color = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom navigation
// ---------------------------------------------------------------------------

enum class NovvyTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    TASKS("Tasks", Icons.Filled.Task),
    FILES("Files", Icons.Filled.Folder),
    INBOX("Inbox", Icons.Filled.Inbox),
    SETTINGS("Settings", Icons.Filled.Settings),
}

@Composable
fun NovvyBottomNavigation(selected: NovvyTab, onSelect: (NovvyTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NovvyTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
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