package com.novvy.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.novvy.app.domain.model.ProviderKind

/** Curated model lists per provider for the model selector sheet. */
object ProviderModels {
    fun forKind(kind: ProviderKind): List<String> = when (kind) {
        ProviderKind.OPENAI -> listOf("gpt-4.1-mini", "gpt-4.1", "gpt-4o", "gpt-4o-mini")
        ProviderKind.GEMINI -> listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.0-flash")
        ProviderKind.ANTHROPIC -> listOf("claude-3-5-sonnet-latest", "claude-3-5-haiku-latest", "claude-3-opus-latest")
        ProviderKind.GROQ -> listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768")
        ProviderKind.OPENROUTER -> listOf("openai/gpt-4o-mini", "anthropic/claude-3.5-sonnet", "meta-llama/llama-3.1-70b-instruct")
        ProviderKind.OLLAMA -> listOf("llama3.2", "llama3.1", "qwen2.5", "mistral")
        ProviderKind.MOCK -> listOf("novvy-demo")
    }
}

/** Bottom sheet to pick a model for a provider. */
@Composable
fun ModelSelectorSheet(
    providerLabel: String,
    current: String,
    models: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val offset = remember { Animatable(1f) }
    LaunchedEffect(Unit) { offset.animateTo(0f, tween(300)) }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.46f)).clickable { onDismiss() })
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .graphicsLayer { translationY = offset.value * 900f },
        ) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Box(
                    Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp)
                        .size(40.dp, 4.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.outline)
                )
                Column(Modifier.padding(22.dp)) {
                    Text("Choose a model", style = MaterialTheme.typography.titleLarge)
                    Text(providerLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    models.forEach { model ->
                        val selected = model == current
                        Surface(
                            onClick = { onSelect(model) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    model,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                if (selected) {
                                    Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}