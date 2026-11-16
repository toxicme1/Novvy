package com.novvy.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.TravelExplore
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.NovvyTask
import com.novvy.app.domain.model.TaskStatus
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.NovvyPromptCard
import com.novvy.app.ui.components.QuickActionChip
import com.novvy.app.ui.components.SectionTitle
import java.util.Calendar

private data class QuickAction(val label: String, val icon: ImageVector, val goal: String)

private val quickActions = listOf(
    QuickAction("Plan my week", Icons.Filled.CalendarMonth, "Plan my week with priorities and a schedule"),
    QuickAction("Summarize email", Icons.Filled.Email, "Summarize my unread emails and list action items"),
    QuickAction("Create document", Icons.Filled.Description, "Create a professional proposal document"),
    QuickAction("Save to Drive", Icons.Filled.Cloud, "Save a report to Google Drive"),
    QuickAction("Draft reply", Icons.Filled.Email, "Draft a professional reply to my latest client email"),
    QuickAction("Compare options", Icons.Filled.CompareArrows, "Compare three laptops and recommend the best one"),
    QuickAction("Make a budget", Icons.Filled.AccountBalanceWallet, "Plan a trip from Nairobi to Mombasa under KSh 25,000"),
    QuickAction("Study plan", Icons.Filled.AutoAwesome, "Create a study plan for my exam"),
)

@Composable
fun HomeScreen(
    vm: NovvyViewModel,
    onOpenTask: (String?) -> Unit,
) {
    val state by vm.ui.collectAsState()
    val greeting = rememberGreeting()

    LazyColumn(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Good ${greeting}, ${state.settings.displayName}",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            "Your AI team for real work.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountChip("Gmail", state.settings.gmailConnected, Icons.Filled.Email)
                    AccountChip("Drive", state.settings.driveConnected, Icons.Filled.Cloud)
                }
            }
        }

        item {
            NovvyPromptCard(onSend = { goal ->
                vm.submitGoal(goal)
                onOpenTask(null)
            })
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickActions) { action ->
                    QuickActionChip(action.label, action.icon) {
                        vm.submitGoal(action.goal)
                        onOpenTask(null)
                    }
                }
            }
        }

        item { SectionTitle("Recent tasks") }

        val recent = (listOfNotNull(state.task) + state.history).distinctBy { it.id }.take(8)
        if (recent.isEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "No active tasks yet. Give Novvy your first goal.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(recent, key = { it.id }) { task -> TaskRow(task) { onOpenTask(task.id) } }
        }
    }
}

@Composable
private fun AccountChip(label: String, connected: Boolean, icon: ImageVector) {
    val color = if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text("$label ${if (connected) "•" else "○"}", style = MaterialTheme.typography.labelMedium, color = color)
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
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    task.request,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(10.dp))
            StatusChip(task.status)
            Spacer(Modifier.width(4.dp))
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