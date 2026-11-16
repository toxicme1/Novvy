package com.novvy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.DocumentFormat
import com.novvy.app.domain.model.DriveFile
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.EmptyState
import com.novvy.app.ui.components.LoadingSkeletonCards
import com.novvy.app.ui.components.PullToRefreshColumn
import com.novvy.app.ui.theme.GradientVioletCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class FileFilter(val label: String, val format: DocumentFormat?)

private val fileFilters = listOf(
    FileFilter("All", null),
    FileFilter("DOCX", DocumentFormat.DOCX),
    FileFilter("PDF", DocumentFormat.PDF),
    FileFilter("Notes", DocumentFormat.MARKDOWN),
    FileFilter("TXT", DocumentFormat.TXT),
)

@Composable
fun FilesScreen(vm: NovvyViewModel) {
    val state by vm.ui.collectAsState()
    var filter by remember { mutableStateOf<FileFilter?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val files = state.files.filter { filter == null || it.format == filter?.format }

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
            Text("Files & documents", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Documents created and saved by Novvy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FileFilterRow(filter) { filter = it }
            StorageCard(count = state.files.size, connected = state.settings.driveConnected)

            if (refreshing) {
                LoadingSkeletonCards(count = 2)
            } else if (files.isEmpty()) {
                EmptyState(
                    title = "No documents yet",
                    subtitle = "Novvy can create and save DOCX, PDF and other files to Drive.",
                    actionLabel = "Create a document",
                    onAction = { vm.submitGoal("Create a professional proposal document") },
                )
            } else {
                files.forEach { file ->
                    FileCard(file)
                }
            }
        }
    }
}

@Composable
private fun FileFilterRow(selected: FileFilter?, onSelect: (FileFilter?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(fileFilters) { f ->
            val active = selected == f
            Surface(
                onClick = { onSelect(if (active) null else f) },
                shape = RoundedCornerShape(50),
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(f.label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
            }
        }
    }
}

@Composable
private fun StorageCard(count: Int, connected: Boolean) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(GradientVioletCyan))
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Cloud, null, tint = Color.White, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("$count files", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(
                    if (connected) "Google Drive connected" else "Drive not connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
        }
    }
}

@Composable
private fun FileCard(file: DriveFile) {
    val color = fileColor(file.format)
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Description, null, tint = color, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(file.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(file.folder, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text("${file.format.name} · ${formatSize(file.sizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.Cloud, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun fileColor(format: DocumentFormat): Color = when (format) {
    DocumentFormat.DOCX -> Color(0xFF3B82F6)
    DocumentFormat.PDF -> Color(0xFFEF4444)
    DocumentFormat.MARKDOWN -> Color(0xFF6C4CF1)
    DocumentFormat.TXT -> Color(0xFF9A9AB0)
}

private fun formatSize(bytes: Long): String = when {
    bytes <= 0 -> "—"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format("%.1f MB", bytes / (1024f * 1024f))
}