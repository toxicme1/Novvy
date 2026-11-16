package com.novvy.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.novvy.app.ui.theme.Cyan
import com.novvy.app.ui.theme.Violet

private data class OnboardStep(val icon: ImageVector, val title: String, val body: String)

private val steps = listOf(
    OnboardStep(Icons.Filled.AutoAwesome, "Meet Novvy", "Your AI team for research, writing, planning and documents."),
    OnboardStep(Icons.Filled.Groups, "Watch your agents work", "Novvy breaks your goal into steps and assigns specialist agents — live."),
    OnboardStep(Icons.Filled.Cloud, "Save and share your work", "Connect Google Drive to store DOCX and PDF files. Connect Gmail to draft and send emails."),
    OnboardStep(Icons.Filled.Shield, "You stay in control", "Novvy asks before sending emails, saving files, or touching private data."),
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val step = steps[index]
    val last = index == steps.lastIndex

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(88.dp).clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Violet, Cyan))),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.AutoAwesome, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(40.dp)) }

        Spacer(Modifier.height(36.dp))

        AnimatedContent(targetState = index, transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(160)) }, label = "step") { i ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(steps[i].icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                Spacer(Modifier.height(16.dp))
                Text(steps[i].title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text(
                    steps[i].body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(steps.size) { i ->
                Box(
                    Modifier.size(if (i == index) 22.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(if (i == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = { if (last) onFinish() else index++ },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(if (last) "Get started" else "Next", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Skip for now") }
    }
}