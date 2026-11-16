package com.novvy.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.novvy.app.ui.theme.DarkBg
import com.novvy.app.ui.theme.DarkSurface
import com.novvy.app.ui.theme.LightBg
import com.novvy.app.ui.theme.LightSurface
import com.novvy.app.ui.theme.Violet
import com.novvy.app.data.ThemeMode
import com.novvy.app.domain.model.ProviderConfig
import com.novvy.app.google.GoogleCredentials
import com.novvy.app.ui.NovvyViewModel
import com.novvy.app.ui.components.SectionTitle

@Composable
fun SettingsScreen(vm: NovvyViewModel) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    var editing by remember { mutableStateOf<ProviderConfig?>(null) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        // Profile hero
        ProfileHero(
            name = state.settings.displayName,
            gmailConnected = state.settings.gmailConnected,
            driveConnected = state.settings.driveConnected,
            onNameChange = { vm.setDisplayName(it) },
        )

        // Appearance
        SectionTitle("Appearance")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeCard("System", state.settings.themeMode == ThemeMode.SYSTEM, isSystemInDarkTheme(), Modifier.weight(1f)) { vm.setTheme(ThemeMode.SYSTEM) }
            ThemeCard("Light", state.settings.themeMode == ThemeMode.LIGHT, false, Modifier.weight(1f)) { vm.setTheme(ThemeMode.LIGHT) }
            ThemeCard("Dark", state.settings.themeMode == ThemeMode.DARK, true, Modifier.weight(1f)) { vm.setTheme(ThemeMode.DARK) }
        }

        // Providers
        SectionTitle("AI Providers")
        state.settings.providers.forEach { provider ->
            ProviderCard(
                provider = provider,
                onEdit = { editing = provider },
                onToggle = { vm.updateProvider(provider.copy(enabled = it)) },
                onSetDefault = { vm.setDefaultProvider(provider.kind) },
            )
        }
        OutlinedButton(
            onClick = { Toast.makeText(context, "Add provider: edit a card's API key to enable it.", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("+ Add provider") }

        // Accounts
        SectionTitle("Connected accounts")
        AccountRow(
            label = "Gmail",
            connected = state.settings.gmailConnected,
            icon = Icons.Filled.Email,
            onToggle = { connected ->
                if (connected) vm.setGmailConnected(false)
                else if (GoogleCredentials.isConfigured) vm.setGmailConnected(true)
                else Toast.makeText(context, "Add GOOGLE_WEB_CLIENT_ID to android/local.properties first.", Toast.LENGTH_LONG).show()
            },
        )
        AccountRow(
            label = "Google Drive",
            connected = state.settings.driveConnected,
            icon = Icons.Filled.Cloud,
            onToggle = { connected ->
                if (connected) vm.setDriveConnected(false)
                else if (GoogleCredentials.isConfigured) vm.setDriveConnected(true)
                else Toast.makeText(context, "Add GOOGLE_WEB_CLIENT_ID to android/local.properties first.", Toast.LENGTH_LONG).show()
            },
        )

        SectionTitle("Privacy & approvals")
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column {
                PrivacyRow("Ask before sending email", "Never send without your approval.", true)
                Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
                PrivacyRow("Ask before saving to Drive", "Review the file name and folder first.", true)
                Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
                PrivacyRow("Ask before replacing files", "Prevent accidental overwrites.", true)
                Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
                PrivacyRow("Private mode", "Prefer on-device processing for sensitive work.", false)
            }
        }

        SectionTitle("About")
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("Novvy 1.1.0", style = MaterialTheme.typography.titleMedium)
                Text("Your AI team for real work.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("API keys are stored encrypted on this device (Android Keystore).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    val target = editing
    if (target != null) {
        ProviderEditDialog(
            provider = target,
            onDismiss = { editing = null },
            onSave = { key, model ->
                if (key.isNotBlank()) vm.saveProviderKey(target.kind.name, key)
                vm.updateProvider(target.copy(model = model.ifBlank { target.model }, enabled = true, hasKey = key.isNotBlank() || target.hasKey))
                editing = null
            },
        )
    }
}

@Composable
private fun ThemeCard(
    label: String,
    selected: Boolean,
    previewDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(Modifier.padding(10.dp)) {
            Box(
                Modifier.fillMaxWidth().height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (previewDark) DarkBg else LightBg)
            ) {
                Box(
                    Modifier.align(Alignment.TopStart).padding(8.dp)
                        .size(40.dp, 12.dp).clip(RoundedCornerShape(6.dp))
                        .background(if (previewDark) DarkSurface else LightSurface)
                )
                Box(
                    Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        .size(14.dp).clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (previewDark) androidx.compose.ui.graphics.Color(0xFFB9A8FF) else Violet)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                if (selected) {
                    Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: ProviderConfig,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onSetDefault: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(provider.kind.displayName, style = MaterialTheme.typography.titleMedium)
                        if (provider.isDefault) {
                            Spacer(Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)) {
                                Text(
                                    "Default",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text("Model: ${provider.model}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Lock,
                            null,
                            tint = if (provider.hasKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (provider.hasKey) "Key saved securely" else "No API key",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (provider.hasKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(checked = provider.enabled, onCheckedChange = onToggle)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Edit") }
                if (!provider.isDefault) {
                    OutlinedButton(onClick = onSetDefault, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Set default") }
                }
            }
        }
    }
}

@Composable
private fun ProviderEditDialog(
    provider: ProviderConfig,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var key by remember { mutableStateOf("") }
    var model by remember { mutableStateOf(provider.model) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(provider.kind.displayName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("API key") },
                    singleLine = true,
                    visualTransformation = if (key.isBlank()) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Text(
                    "Keys are stored encrypted on-device and never shown again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(key.trim(), model.trim()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AccountRow(
    label: String,
    connected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp).padding(9.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (connected) "Connected" else "Not connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = connected, onCheckedChange = onToggle)
        }
    }
}
@Composable
private fun ProfileHero(
    name: String,
    gmailConnected: Boolean,
    driveConnected: Boolean,
    onNameChange: (String) -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        name.firstOrNull()?.uppercase() ?: "N",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        name.ifBlank { "Your name" },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusDot("Gmail", gmailConnected)
                        StatusDot("Drive", driveConnected)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Edit name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
private fun StatusDot(label: String, connected: Boolean) {
    val color = if (connected) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun PrivacyRow(title: String, subtitle: String, default: Boolean) {
    var on by remember { mutableStateOf(default) }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = on, onCheckedChange = { on = it })
    }
}
