package com.novvy.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.EmailSummary
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.EmptyState
import com.novvy.app.ui.components.LoadingSkeletonCards
import com.novvy.app.ui.components.PullToRefreshColumn
import com.novvy.app.ui.theme.GradientVioletCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InboxScreen(vm: NovvyViewModel, onSummarize: () -> Unit) {
    val state by vm.ui.collectAsState()
    val unread = state.inbox.count { it.unread }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PullToRefreshColumn(
        modifier = Modifier,
        isRefreshing = refreshing,
        onRefresh = {
            refreshing = true
            scope.launch { delay(900); refreshing = false }
        },
    ) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Inbox", style = MaterialTheme.typography.headlineSmall)
            Text(
                if (unread > 0) "$unread unread messages" else "You're all caught up",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (refreshing) {
                LoadingSkeletonCards(count = 3)
            } else {
                if (state.inbox.isNotEmpty()) {
                    AiSummaryCard(state.inbox)
                }
                Button(onClick = onSummarize, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Summarize unread with Novvy")
                }
                if (state.inbox.isEmpty()) {
                    EmptyState(
                        title = "No email insights yet",
                        subtitle = "Connect Gmail to summarize messages and draft replies.",
                    )
                } else {
                    state.inbox.forEach { EmailCard(it) }
                }
            }
        }
    }
}

@Composable
private fun AiSummaryCard(emails: List<EmailSummary>) {
    val urgent = emails.count { it.urgent }
    val invoices = emails.count { it.subject.contains("invoice", true) }
    val meetings = emails.count { it.subject.contains("meeting", true) || it.subject.contains("invite", true) }

    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(GradientVioletCyan))) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Novvy summary", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                SummaryStat("Urgent", urgent, Icons.Filled.PriorityHigh, Modifier.weight(1f))
                SummaryStat("Invoices", invoices, Icons.Filled.Description, Modifier.weight(1f))
                SummaryStat("Meetings", meetings, Icons.Filled.CalendarMonth, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: Int, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(modifier) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(6.dp))
        Text("$value", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
private fun EmailCard(email: EmailSummary) {
    val accent = if (email.urgent) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(16.dp)) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    email.from.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        email.from,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (email.unread) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    if (email.unread) Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                }
                Text(email.subject, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(email.preview, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                if (email.urgent) {
                    Spacer(Modifier.width(4.dp))
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFFEF4444).copy(alpha = 0.12f)) {
                        Text("Urgent", style = MaterialTheme.typography.labelMedium, color = Color(0xFFEF4444), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}