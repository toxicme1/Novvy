package com.novvy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.NovvyTask
import com.novvy.app.domain.model.TaskStatus
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.EmptyState
import com.novvy.app.ui.components.SectionTitle
import com.novvy.app.ui.theme.SuccessGreen
import com.novvy.app.ui.theme.WarningAmber

private data class Filter(val label: String, val status: TaskStatus?)

private val filters = listOf(
    Filter("All", null),
    Filter("Running", TaskStatus.RUNNING),
    Filter("Needs approval", TaskStatus.WAITING_APPROVAL),
    Filter("Complete", TaskStatus.COMPLETED),
    Filter("Failed", TaskStatus.FAILED),
)

@Composable
fun TasksScreen(vm: NovvyViewModel, onOpenTask: () -> Unit) {
    val state by vm.ui.collectAsState()
    var filter by remember { mutableStateOf<Filter?>(null) }

    val all = (listOfNotNull(state.task) + state.history).distinctBy { it.id }
    val active = all.count { it.status == TaskStatus.RUNNING || it.status == TaskStatus.PAUSED }
    val approvals = all.count { it.status == TaskStatus.WAITING_APPROVAL }
    val completed = all.count { it.status == TaskStatus.COMPLETED }
    val filtered = all.filter { filter == null || it.status == filter?.status }

    LazyColumn(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            Column {
                Text("Tasks", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Track what Novvy is working on.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { FilterRow(filter) { filter = it } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard("Active", "$active", Icons.Filled.Autorenew, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                SummaryCard("Approvals", "$approvals", Icons.Filled.FactCheck, WarningAmber, Modifier.weight(1f))
                SummaryCard("Done", "$completed", Icons.Filled.CheckCircle, SuccessGreen, Modifier.weight(1f))
            }
        }
        item { SectionTitle(filter?.label ?: "All tasks") }

        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    title = "No tasks yet",
                    subtitle = "Give Novvy your first goal and watch your AI team work.",
                    actionLabel = "Create a task",
                    onAction = onOpenTask,
                )
            }
        } else {
            items(filtered, key = { it.id }) { task -> TaskCard(task, onOpenTask) }
        }
    }
}

@Composable
private fun FilterRow(selected: Filter?, onSelect: (Filter?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { f ->
            val active = selected == f
            Surface(
                onClick = { onSelect(if (active) null else f) },
                shape = RoundedCornerShape(50),
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(
                    f.label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = tint)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TaskCard(task: NovvyTask, onClick: () -> Unit) {
    val (label, color) = statusVisual(task.status)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(4.dp, 40.dp).clip(RoundedCornerShape(50)).background(color))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(task.request, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(color))
                    Spacer(Modifier.width(5.dp))
                    Text(label, style = MaterialTheme.typography.labelMedium, color = color)
                }
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun statusVisual(status: TaskStatus): Pair<String, Color> = when (status) {
    TaskStatus.RUNNING -> "Running" to Color(0xFF6C4CF1)
    TaskStatus.WAITING_APPROVAL -> "Needs approval" to WarningAmber
    TaskStatus.COMPLETED -> "Complete" to SuccessGreen
    TaskStatus.FAILED -> "Failed" to Color(0xFFEF4444)
    TaskStatus.PAUSED -> "Paused" to Color(0xFFFB923C)
    TaskStatus.CANCELLED -> "Cancelled" to Color(0xFF9A9AB0)
}