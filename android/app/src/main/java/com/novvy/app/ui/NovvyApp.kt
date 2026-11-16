package com.novvy.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.novvy.app.domain.model.NovvyTask
import com.novvy.app.domain.model.TaskStatus
import com.novvy.app.ui.components.NovvyBottomNavigation
import com.novvy.app.ui.components.NovvyTab
import com.novvy.app.ui.components.SectionTitle
import com.novvy.app.ui.screens.AgentDetailScreen
import com.novvy.app.ui.screens.FilesScreen
import com.novvy.app.ui.screens.HomeScreen
import com.novvy.app.ui.screens.InboxScreen
import com.novvy.app.ui.screens.LiveTaskScreen
import com.novvy.app.ui.screens.OnboardingScreen
import com.novvy.app.ui.screens.ResultScreen
import com.novvy.app.ui.screens.SettingsScreen

@Composable
fun NovvyApp(vm: NovvyViewModel) {
    val state by vm.ui.collectAsState()
    if (!state.settings.onboardingDone) {
        OnboardingScreen(onFinish = { vm.completeOnboarding() })
        return
    }

    val navController = rememberNavController()
    val context = LocalContext.current
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    val selectedTab = when (route) {
        "home" -> NovvyTab.HOME
        "tasks" -> NovvyTab.TASKS
        "files" -> NovvyTab.FILES
        "inbox" -> NovvyTab.INBOX
        "settings" -> NovvyTab.SETTINGS
        else -> NovvyTab.TASKS
    }

    Column(Modifier.fillMaxSize().systemBarsPadding()) {
        Box(Modifier.weight(1f)) {
            NavHost(navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(
                        vm = vm,
                        onOpenTask = { navController.navigate("task") },
                    )
                }
                composable("tasks") {
                    TasksScreen(vm = vm, onOpenTask = { navController.navigate("task") })
                }
                composable("files") { FilesScreen(vm) }
                composable("inbox") {
                    InboxScreen(
                        vm = vm,
                        onSummarize = {
                            vm.submitGoal("Summarize my unread emails and create a priority action list")
                            navController.navigate("task")
                        },
                    )
                }
                composable("settings") { SettingsScreen(vm) }
                composable("task") {
                    LiveTaskScreen(
                        vm = vm,
                        onBack = { navController.popBackStack() },
                        onViewResult = { navController.navigate("result") },
                        onOpenAgent = { id -> navController.navigate("agent/$id") },
                    )
                }
                composable(
                    "agent/{agentId}",
                    arguments = listOf(navArgument("agentId") { type = NavType.StringType }),
                ) { entry ->
                    AgentDetailScreen(
                        vm = vm,
                        agentId = entry.arguments?.getString("agentId").orEmpty(),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable("result") {
                    ResultScreen(
                        vm = vm,
                        onBack = { navController.popBackStack() },
                        onEmailDraft = {
                            Toast.makeText(context, "Demo: opening email draft", Toast.LENGTH_SHORT).show()
                            navController.navigate("inbox")
                        },
                    )
                }
            }
        }
        NovvyBottomNavigation(
            selected = selectedTab,
            onSelect = { tab ->
                navController.navigate(tab.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
    }
}

private val NovvyTab.route: String
    get() = when (this) {
        NovvyTab.HOME -> "home"
        NovvyTab.TASKS -> "tasks"
        NovvyTab.FILES -> "files"
        NovvyTab.INBOX -> "inbox"
        NovvyTab.SETTINGS -> "settings"
    }

@Composable
private fun TasksScreen(vm: NovvyViewModel, onOpenTask: () -> Unit) {
    val state by vm.ui.collectAsState()
    val tasks = (listOfNotNull(state.task) + state.history).distinctBy { it.id }

    LazyColumn(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        item { Text("Tasks", style = MaterialTheme.typography.headlineSmall) }

        if (tasks.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("No tasks yet. Start one from Home.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                TaskCard(task, onClick = onOpenTask)
            }
        }
    }
}

@Composable
private fun TaskCard(task: NovvyTask, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(task.request, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            StatusChip(task.status)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusChip(status: TaskStatus) {
    val (label, color) = when (status) {
        TaskStatus.RUNNING -> "Running" to MaterialTheme.colorScheme.primary
        TaskStatus.WAITING_APPROVAL -> "Needs approval" to androidx.compose.ui.graphics.Color(0xFFF59E0B)
        TaskStatus.COMPLETED -> "Complete" to androidx.compose.ui.graphics.Color(0xFF22C55E)
        TaskStatus.FAILED -> "Failed" to MaterialTheme.colorScheme.error
        TaskStatus.PAUSED -> "Paused" to androidx.compose.ui.graphics.Color(0xFFFB923C)
        TaskStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}