package com.novvy.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.AgentState
import com.novvy.app.domain.model.AgentTask
import com.novvy.app.domain.model.TaskStatus
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.AgentStatusCard
import com.novvy.app.ui.components.ApprovalSheet
import com.novvy.app.ui.components.CoordinatorMessage
import com.novvy.app.ui.components.EmptyState
import com.novvy.app.ui.components.GradientProgressBar
import com.novvy.app.ui.components.StepIndicator
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
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    val waiting = task?.status == TaskStatus.WAITING_APPROVAL
    val completed = task?.status == TaskStatus.COMPLETED
    LaunchedEffect(waiting) { if (waiting) haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
    LaunchedEffect(completed) { if (completed) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }

    Box(Modifier.fillMaxSize()) {
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
                    IconButton(onClick = { vm.stopTask() }) {
                        Icon(Icons.Filled.Stop, "Stop", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (task == null) {
                EmptyState(title = "No active task", subtitle = "Give Novvy a goal from the Home screen.")
                return@Column
            }

            LazyColumn(
                Modifier.weight(1f).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            ) {
                item { TaskHero(task) }
                itemsIndexed(task.agents) { index, agent ->
                    StaggeredAgentCard(agent, index) { onOpenAgent(agent.id) }
                }
                item { CoordinatorMessage(task.coordinatorMessage) }
            }

            if (task.status == TaskStatus.COMPLETED) {
                StickyActionBar(
                    onDrive = { toast(context, "Demo: saving to Drive") },
                    onEmail = onViewResult,
                    onShare = { toast(context, "Demo: sharing") },
                    onOpen = onViewResult,
                )
            }
        }

        val approval = task?.approval
        if (approval != null) {
            ApprovalSheet(
                approval = approval,
                onApprove = { vm.approve() },
                onCancel = { vm.cancelApproval() },
            )
        }
    }
}

@Composable
private fun TaskHero(task: com.novvy.app.domain.model.NovvyTask) {
    val total = task.agents.size
    val completed = task.agents.count { it.state == AgentState.COMPLETE }
    val fraction = if (total == 0) 0f else completed.toFloat() / total
    val animated by animateFloatAsState(fraction, tween(500), label = "hero")

    Column {
        Text(task.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "“${task.request}”",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))
        StepIndicator(task.agents)
        Spacer(Modifier.height(10.dp))
        GradientProgressBar(animated)
        Spacer(Modifier.height(6.dp))
        Text(
            "$completed of $total steps complete",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StickyActionBar(
    onDrive: () -> Unit,
    onEmail: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
) {
    Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onOpen, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Filled.Cloud, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("View result")
            }
            OutlinedButton(onClick = onEmail, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Filled.Email, null, modifier = Modifier.size(18.dp)) }
            OutlinedButton(onClick = onShare, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Filled.Share, null, modifier = Modifier.size(18.dp)) }
            OutlinedButton(onClick = onDrive, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Filled.Cloud, null, modifier = Modifier.size(18.dp)) }
        }
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
    Box(Modifier.graphicsLayer { alpha = anim; translationY = (1f - anim) * 48f }) {
        AgentStatusCard(agent = agent, onClick = onClick)
    }
}

private fun toast(context: android.content.Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}