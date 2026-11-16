package com.novvy.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novvy.app.data.SecureKeyStore
import com.novvy.app.data.SettingsRepository
import com.novvy.app.data.ThemeMode
import com.novvy.app.data.AppSettings
import com.novvy.app.domain.agent.CoordinatorEngine
import com.novvy.app.domain.model.AgentTask
import com.novvy.app.domain.model.DocumentFormat
import com.novvy.app.domain.model.DriveFile
import com.novvy.app.domain.model.EmailSummary
import com.novvy.app.domain.model.NovvyTask
import com.novvy.app.domain.model.ProviderConfig
import com.novvy.app.domain.model.TaskStatus
import com.novvy.app.domain.provider.AIProvider
import com.novvy.app.domain.provider.Providers
import com.novvy.app.google.DemoGmailService
import com.novvy.app.google.NovvyFolders
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NovvyUiState(
    val settings: AppSettings = AppSettings(),
    val task: NovvyTask? = null,
    val history: List<NovvyTask> = emptyList(),
    val inbox: List<EmailSummary> = emptyList(),
    val files: List<DriveFile> = emptyList(),
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
)

class NovvyViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository(app, SecureKeyStore(app))
    private val secureKeyStore = SecureKeyStore(app)
    private val engine = CoordinatorEngine()
    private val gmail = DemoGmailService()

    private val _ui = MutableStateFlow(NovvyUiState())
    val ui: StateFlow<NovvyUiState> = _ui.asStateFlow()

    private var runJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepo.settings.collect { s ->
                _ui.update { it.copy(settings = s) }
            }
        }
        _ui.update { it.copy(inbox = emptyList(), files = emptyList()) }
        viewModelScope.launch {
            _ui.update { it.copy(inbox = gmail.listInbox(), files = demoFiles()) }
        }
    }

    // ---- task lifecycle ------------------------------------------------

    fun submitGoal(goal: String) {
        if (_ui.value.isRunning || goal.isBlank()) return
        val plan = engine.buildPlan(goal)
        val task = NovvyTask(
            id = goal.hashCode().toString(),
            title = engine.titleFor(goal),
            request = goal,
            status = TaskStatus.RUNNING,
            agents = plan,
            coordinatorMessage = "Creating a plan for your request…",
        )
        _ui.update { it.copy(task = task, isRunning = true, isPaused = false) }

        runJob = viewModelScope.launch {
            val provider = currentProvider()
            engine.run(goal, provider) { updated ->
                _ui.update { state ->
                    val done = updated.status == TaskStatus.COMPLETED || updated.status == TaskStatus.FAILED
                    state.copy(
                        task = updated,
                        isRunning = !done,
                        history = if (done) (listOf(updated) + state.history).take(20) else state.history,
                    )
                }
            }
            _ui.update { it.copy(isRunning = false, isPaused = false) }
        }
    }

    fun togglePause() {
        val next = !_ui.value.isPaused
        engine.setPaused(next)
        _ui.update { it.copy(isPaused = next) }
    }

    fun stopTask() {
        runJob?.cancel()
        engine.cancelAll()
        _ui.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                task = it.task?.copy(status = TaskStatus.CANCELLED),
            )
        }
    }

    fun retryTask() {
        val req = _ui.value.task?.request ?: return
        submitGoal(req)
    }

    fun approve() = engine.approve()

    fun cancelApproval() = engine.cancel()

    fun clearTask() = _ui.update { it.copy(task = null) }

    // ---- providers -----------------------------------------------------

    fun saveProviderKey(providerId: String, key: String) {
        viewModelScope.launch {
            settingsRepo.saveProviderKey(providerId, key)
            refreshProviders()
        }
    }

    fun updateProvider(updated: ProviderConfig) {
        viewModelScope.launch {
            val list = _ui.value.settings.providers.map { if (it.kind == updated.kind) updated else it }
            settingsRepo.saveProviders(list)
            refreshProviders()
        }
    }

    fun setDefaultProvider(kind: ProviderKindArg) {
        viewModelScope.launch {
            val list = _ui.value.settings.providers.map {
                it.copy(isDefault = it.kind.name == kind.name)
            }
            settingsRepo.saveProviders(list)
            refreshProviders()
        }
    }

    private fun refreshProviders() {
        viewModelScope.launch {
            settingsRepo.saveProviders(_ui.value.settings.providers)
        }
    }

    private fun currentProvider(): AIProvider {
        val cfg = _ui.value.settings.providers.firstOrNull { it.isDefault }
            ?: _ui.value.settings.providers.first()
        val key = secureKeyStore.getKey(cfg.kind.name)
        return Providers.create(cfg, key)
    }

    // ---- settings ------------------------------------------------------

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settingsRepo.setThemeMode(mode) }
    fun setDisplayName(name: String) = viewModelScope.launch { settingsRepo.setDisplayName(name) }
    fun completeOnboarding() = viewModelScope.launch { settingsRepo.setOnboardingDone(true) }
    fun setGmailConnected(v: Boolean) = viewModelScope.launch { settingsRepo.setGmailConnected(v) }
    fun setDriveConnected(v: Boolean) = viewModelScope.launch { settingsRepo.setDriveConnected(v) }

    // ---- demo data -----------------------------------------------------

    private fun demoFiles(): List<DriveFile> = listOf(
        DriveFile("Cleaning_Proposal_Novvy.docx", NovvyFolders.businessProposals, DocumentFormat.DOCX, 42_000, true),
        DriveFile("Mombasa_Itinerary.pdf", NovvyFolders.travel, DocumentFormat.PDF, 128_000, true),
        DriveFile("Exam_Study_Plan.md", NovvyFolders.study, DocumentFormat.MARKDOWN, 6_400, true),
    )
}

// Small alias so the ViewModel API reads nicely without importing ProviderKind everywhere.
typealias ProviderKindArg = com.novvy.app.domain.model.ProviderKind