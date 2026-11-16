package com.novvy.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.EmptyState
import com.novvy.app.ui.components.agentIcon

@Composable
fun ResultScreen(
    vm: NovvyViewModel,
    onBack: () -> Unit,
    onEmailDraft: () -> Unit,
) {
    val state by vm.ui.collectAsState()
    val task = state.task
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
            Text("Result", style = MaterialTheme.typography.titleLarge)
        }

        if (task == null || task.result.isBlank()) {
            EmptyState("Nothing here yet", "Complete a task to see the result.", Icons.Filled.Description)
            return@Column
        }

        Text(task.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            "Prepared by ${task.agents.count { it.state == com.novvy.app.domain.model.AgentState.COMPLETE }} agents",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MarkdownLite(task.result)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { toast(context, "Demo: saving to Drive") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Filled.Cloud, null, modifier = Modifier.height(18.dp).width(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save to Drive")
            }
            OutlinedButton(
                onClick = { toast(context, "Demo: creating PDF") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Create PDF") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("Novvy", task.result))
                    toast(context, "Copied")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.height(18.dp).width(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Copy")
            }
            OutlinedButton(
                onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, task.title)
                        putExtra(Intent.EXTRA_TEXT, task.result)
                    }
                    context.startActivity(Intent.createChooser(send, "Share result"))
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Filled.Share, null, modifier = Modifier.height(18.dp).width(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Share")
            }
        }

        OutlinedButton(
            onClick = onEmailDraft,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Filled.Email, null, modifier = Modifier.height(18.dp).width(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Create email draft")
        }

        Spacer(Modifier.height(8.dp))
    }
}

/** Minimal markdown-ish renderer: # headings, ## subheads, - bullets. */
@Composable
private fun MarkdownLite(text: String) {
    text.lineSequence().forEach { raw ->
        val line = raw.trimEnd()
        when {
            line.startsWith("# ") -> Text(
                line.removePrefix("# "),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 6.dp),
            )
            line.startsWith("## ") -> Text(
                line.removePrefix("## "),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            line.startsWith("- ") || line.startsWith("• ") -> Row {
                Text("•", color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(line.drop(2), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
            line.isBlank() -> Spacer(Modifier.height(4.dp))
            else -> Text(line, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun toast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}