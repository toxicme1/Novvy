package com.novvy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.NovvyTask
import com.novvy.app.domain.model.TaskStatus
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.NovvyOrb
import com.novvy.app.ui.components.NovvyPromptCard
import com.novvy.app.ui.components.QuickActionCard
import com.novvy.app.ui.components.SectionTitle
import com.novvy.app.ui.theme.GradientBlueCyan
import com.novvy.app.ui.theme.GradientCyanBlue
import com.novvy.app.ui.theme.GradientGreenCyan
import com.novvy.app.ui.theme.GradientRedViolet
import com.novvy.app.ui.theme.GradientVioletBlue
import com.novvy.app.ui.theme.GradientVioletCoral
import com.novvy.app.ui.theme.GradientVioletCyan
import java.util.Calendar

private data class QuickAction(val label: String, val icon: ImageVector, val goal: String, val subtitle: String, val gradient: List<androidx.compose.ui.graphics.Color>)

private val quickActions = listOf(
    QuickAction("Plan my week", Icons.Filled.CalendarMonth, "Plan my week with priorities and a schedule", "Priorities & schedule", GradientVioletCyan),
    QuickAction("Summarize email", Icons.Filled.Email, "Summarize my unread emails and list action items", "Inbox → action items", GradientRedViolet),
    QuickAction("Create document", Icons.Filled.Description, "Create a professional proposal document", "Proposals & reports", GradientCyanBlue),
    QuickAction("Save to Drive", Icons.Filled.Cloud, "Save a report to Google Drive", "Store it in Drive", GradientBlueCyan),
    QuickAction("Draft reply", Icons.Filled.EditNote, "Draft a professional reply to my latest client email", "Respond in your tone", GradientVioletCoral),
    QuickAction("Compare options", Icons.Filled.Insights, "Compare three laptops and recommend the best one", "Side-by-side picks", GradientGreenCyan),
    QuickAction("Make a budget", Icons.Filled.Savings, "Plan a trip from Nairobi to Mombasa under KSh 25,000", "Costs & totals", GradientCyanBlue),
    QuickAction("Study plan", Icons.Filled.Task, "Create a study plan for my exam", "Exam-ready plan", GradientVioletBlue),
)

@Composable
fun HomeScreen(
    vm: NovvyViewModel,
    onOpenTask: (String?) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by vm.ui.collectAsState()
    val greeting = rememberGreeting()

    LazyColumn(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Good ${greeting}, ${state.settings.displayName}",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "What would you like Novvy to do today?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                NovvyOrb(size = 52.dp)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccountChip("Gmail", state.settings.gmailConnected, Icons.Filled.Email, onOpenSettings)
                AccountChip("Drive", state.settings.driveConnected, Icons.Filled.Cloud, onOpenSettings)
            }
        }

        if (!state.settings.gmailConnected || !state.settings.driveConnected) {
            item { ConnectWorkspaceCard(onConnect = onOpenSettings) }
        }

        item {
            NovvyPromptCard(
                onSend = { goal -> vm.submitGoal(goal); onOpenTask(null) },
                gmailConnected = state.settings.gmailConnected,
                driveConnected = state.settings.driveConnected,
            )
        }

        item { SectionTitle("Quick actions") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(quickActions) { action ->
                    QuickActionCard(
                        title = action.label,
                        subtitle = action.subtitle,
                        icon = action.icon,
                        gradient = action.gradient,
                        modifier = Modifier.width(172.dp),
                    ) {
                        vm.submitGoal(action.goal)
                        onOpenTask(null)
                    }
                }
            }
        }

        item { SectionTitle("Recent tasks") }
        val recent = (listOfNotNull(state.task) + state.history).distinctBy { it.id }.take(6)
        if (recent.isEmpty()) {
            item { EmptyTasks() }
        } else {
            items(recent, key = { it.id }) { task -> TaskRow(task) { onOpenTask(task.id) } }
        }
    }
}

@Composable
private fun ConnectWorkspaceCard(onConnect: () -> Unit) {
    Card(
        onClick = onConnect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("Connect your workspace", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(4.dp))
            Text(
                "Save documents to Drive and prepare emails with Gmail.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun AccountChip(label: String, connected: Boolean, icon: ImageVector, onClick: () -> Unit) {
    val color = if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(if (connected) color else MaterialTheme.colorScheme.outline)
            )
            Spacer(Modifier.width(6.dp))
            Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                if (connected) label else "Connect $label",
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}

@Composable
private fun EmptyTasks() {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            NovvyOrb(size = 40.dp, showGlow = false)
            Spacer(Modifier.width(14.dp))
            Text(
                "No active tasks yet. Give Novvy your first goal.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TaskRow(task: NovvyTask, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    task.request,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            StatusChip(task.status)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusChip(status: TaskStatus) {
    val (label, color) = when (status) {
        TaskStatus.RUNNING -> "Running" to MaterialTheme.colorScheme.primary
        TaskStatus.WAITING_APPROVAL -> "Needs approval" to androidx.compose.ui.graphics.Color(0xFFF59E0B)
        TaskStatus.COMPLETED -> "Complete" to androidx.compose.ui.graphics.Color(0xFF22C55E)
        TaskStatus.FAILED -> "Failed" to MaterialTheme.colorScheme.error
        TaskStatus.PAUSED -> "Paused" to androidx.compose.ui.graphics.Color(0xFFFB923C)
        TaskStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
private fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "morning"
        in 12..16 -> "afternoon"
        in 17..20 -> "evening"
        else -> "night"
    }
}