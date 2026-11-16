package com.novvy.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.AgentTask
import com.novvy.app.domain.model.NovvyTask
import com.novvy.app.domain.model.TaskStatus
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.AgentStatusCard
import com.novvy.app.ui.components.EmptyState
import com.novvy.app.ui.theme.Violet
import kotlinx.coroutines.delay

@Composable
fun LiveTaskScreen(
    vm: NovvyViewModel,
    onBack: () -> Unit,
    onViewResult: () -> Unit,
    onOpenAgent: (String) -> Unit,
) {
    val state by vm.ui.collectAsState()
    val task = state.task

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
            Text("Live task", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (task != null && (task.status == TaskStatus.RUNNING || task.status == TaskStatus.WAITING_APPROVAL)) {
                IconButton(onClick = { vm.togglePause() }) {
                    Icon(if (state.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, if (state.isPaused) "Resume" else "Pause")
                }
                IconButton(onClick = { vm.stopTask() }) { Icon(Icons.Filled.Stop, "Stop", tint = MaterialTheme.colorScheme.error) }
            }
        }

        if (task == null) {
            EmptyState("No active task", "Give Novvy a goal from the Home screen.", Icons.Filled.AccountTree)
            return@Column
        }

        LazyColumn(
            Modifier.weight(1f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        ) {
            item {
                Column {
                    Text(task.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("“${task.request}”", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (task.status == TaskStatus.COMPLETED) {
                item {
                    Button(
                        onClick = onViewResult,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) { Text("View result", style = MaterialTheme.typography.titleMedium) }
                }
            }

            itemsIndexed(task.agents) { index, agent ->
                StaggeredAgentCard(agent, index) { onOpenAgent(agent.id) }
            }

            item {
                CoordinatorBubble(task.coordinatorMessage)
            }
        }
    }

    val approval = task?.approval
    if (approval != null) {
        AlertDialog(
            onDismissRequest = { vm.cancelApproval() },
            title = { Text(approval.title) },
            text = {
                Column {
                    Text(approval.summary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            approval.detail,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.approve() }) { Text("Approve") }
            },
            dismissButton = {
                TextButton(onClick = { vm.cancelApproval() }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun StaggeredAgentCard(agent: AgentTask, index: Int, onClick: () -> Unit) {
    var appear by remember(agent.id) { mutableStateOf(false) }
    LaunchedEffect(agent.id) {
        delay(index * 90L)
        appear = true
    }
    val anim by animateFloatAsState(targetValue = if (appear) 1f else 0f, animationSpec = tween(340), label = "appear")
    Box(
        Modifier.graphicsLayer {
            alpha = anim
            translationY = (1f - anim) * 48f
        }
    ) {
        AgentStatusCard(agent = agent, onClick = onClick)
    }
}

@Composable
private fun CoordinatorBubble(message: String) {
    Row {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(Violet),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.AccountTree, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                message.ifBlank { "Coordinating…" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}