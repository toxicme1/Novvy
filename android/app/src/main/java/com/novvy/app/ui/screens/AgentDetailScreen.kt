package com.novvy.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.AgentState
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.SectionTitle
import com.novvy.app.ui.components.StatePill
import com.novvy.app.ui.components.agentIcon
import com.novvy.app.ui.components.stateColor

@Composable
fun AgentDetailScreen(
    vm: NovvyViewModel,
    agentId: String,
    onBack: () -> Unit,
) {
    val state by vm.ui.collectAsState()
    val agent = state.task?.agents?.firstOrNull { it.id == agentId }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
            Text("Agent detail", style = MaterialTheme.typography.titleLarge)
        }

        if (agent == null) {
            Text("Agent not found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }

        val color = stateColor(agent.state)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) { Icon(agentIcon(agent.kind), null, tint = color, modifier = Modifier.size(28.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(agent.kind.displayName, style = MaterialTheme.typography.headlineSmall)
                Text(agent.kind.tagline, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatePill(agent.state)
        }

        Spacer(Modifier.height(4.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Text(agent.action, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(14.dp))
        }

        if (agent.detail.isNotBlank()) {
            SectionTitle(if (agent.kind.displayName == "Writer") "Draft" else "What it found")
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Text(agent.detail, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
            }
        }

        if (agent.sources.isNotEmpty()) {
            SectionTitle("Sources")
            agent.sources.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (agent.state == AgentState.WAITING_APPROVAL) {
            SectionTitle("Needs your approval")
            Text(
                "This step is waiting for you before it can continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { vm.approve() }, modifier = Modifier.weight(1f)) { Text("Approve") }
                OutlinedButton(onClick = { vm.cancelApproval() }, modifier = Modifier.weight(1f)) { Text("Cancel") }
            }
        }

        if (agent.state == AgentState.FAILED) {
            OutlinedButton(onClick = { vm.retryTask() }, modifier = Modifier.fillMaxWidth()) { Text("Retry task") }
        }
    }
}