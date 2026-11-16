package com.novvy.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.novvy.app.domain.model.TaskStatus
import com.novvy.app.ui.components.NovvyBottomNavigation
import com.novvy.app.ui.components.NovvyTab
import com.novvy.app.ui.screens.AgentDetailScreen
import com.novvy.app.ui.screens.FilesScreen
import com.novvy.app.ui.screens.HomeScreen
import com.novvy.app.ui.screens.InboxScreen
import com.novvy.app.ui.screens.LiveTaskScreen
import com.novvy.app.ui.screens.OnboardingScreen
import com.novvy.app.ui.screens.ResultScreen
import com.novvy.app.ui.screens.SettingsScreen
import com.novvy.app.ui.screens.TasksScreen

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
                        onOpenSettings = { navController.navigate("settings") },
                    )
                }
                composable("tasks") { TasksScreen(vm = vm, onOpenTask = { navController.navigate("task") }) }
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
            inboxUnread = state.inbox.count { it.unread },
            tasksApprovals = state.history.count { it.status == TaskStatus.WAITING_APPROVAL } +
                (if (state.task?.status == TaskStatus.WAITING_APPROVAL) 1 else 0),
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