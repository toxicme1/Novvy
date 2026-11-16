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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.AgentState
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.GradientIcon
import com.novvy.app.ui.components.GradientProgressBar
import com.novvy.app.ui.components.SectionTitle
import com.novvy.app.ui.components.StatePill
import com.novvy.app.ui.components.agentGradient
import com.novvy.app.ui.components.agentIcon
import com.novvy.app.ui.components.stateColor
import com.novvy.app.ui.theme.SuccessGreen
import com.novvy.app.ui.theme.WarningAmber

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

        // Hero
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                GradientIcon(agentIcon(agent.kind), agentGradient(agent.kind), size = 58.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(agent.kind.displayName, style = MaterialTheme.typography.headlineSmall)
                    Text(agent.kind.tagline, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    StatePill(agent.state)
                }
                if (agent.state == AgentState.RUNNING) {
                    CircularProgressIndicator(
                        progress = agent.progress.coerceIn(0f, 1f),
                        modifier = Modifier.size(40.dp),
                        color = color,
                        strokeWidth = 4.dp,
                    )
                }
            }
        }

        // Current activity
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Current activity", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(agent.action, style = MaterialTheme.typography.titleMedium)
                if (agent.state == AgentState.RUNNING) {
                    Spacer(Modifier.height(10.dp))
                    GradientProgressBar(agent.progress)
                }
            }
        }

        // Findings / draft
        if (agent.detail.isNotBlank()) {
            SectionTitle(if (agent.kind.displayName == "Writer") "Draft" else "What it found")
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Text(agent.detail, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
            }
        }

        // Sources
        if (agent.sources.isNotEmpty()) {
            SectionTitle("Sources")
            agent.sources.forEach { src ->
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Link, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(src, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Needs from user
        if (agent.state == AgentState.WAITING_APPROVAL) {
            SectionTitle("Novvy needs your input")
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.10f)),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FactCheck, null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Would you like to continue?", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OptionChip("Approve") { vm.approve() }
                        OptionChip("Cancel") { vm.cancelApproval() }
                    }
                }
            }
        }

        // Error
        if (agent.state == AgentState.FAILED) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Something went wrong", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text(
                        "This step failed. Retry, or switch providers in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { vm.retryTask() }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OptionChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
    }
}