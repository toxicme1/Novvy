package com.novvy.app.domain.model

/** Lifecycle state of a single agent working on a task. */
enum class AgentState {
    QUEUED,
    RUNNING,
    WAITING_APPROVAL,
    COMPLETE,
    FAILED,
    PAUSED,
    CANCELLED,
}

/** The specialist agents that make up Novvy's team. */
enum class AgentKind(val displayName: String, val tagline: String) {
    COORDINATOR("Coordinator", "Organizing the team and final result"),
    PLANNER("Planner", "Breaking the goal into clear steps"),
    RESEARCH("Research", "Searching and gathering facts & sources"),
    ANALYSIS("Analysis", "Comparing options and running numbers"),
    BUDGET("Budget", "Building costs and totals"),
    HOTEL("Hotel", "Comparing stays and availability"),
    ITINERARY("Itinerary", "Sequencing the plan day by day"),
    WRITER("Writer", "Drafting clear, on-tone content"),
    DOCUMENT("Document", "Formatting DOCX and PDF files"),
    TASK("Tasks", "Extracting deadlines and checklists"),
    REVIEWER("Reviewer", "Checking quality, clarity and gaps"),
    GMAIL("Gmail", "Drafting and sending email with approval"),
    DRIVE("Drive", "Saving files to Google Drive"),
}

/** A single agent's card in the live task view. */
data class AgentTask(
    val id: String,
    val kind: AgentKind,
    val action: String,
    val state: AgentState = AgentState.QUEUED,
    val detail: String = "",
    val progress: Float = 0f,
    val elapsedSeconds: Int = 0,
    val sources: List<String> = emptyList(),
    val draft: String = "",
    val needsApproval: Boolean = false,
)

enum class TaskStatus { RUNNING, WAITING_APPROVAL, COMPLETED, FAILED, PAUSED, CANCELLED }

/** The full state of one user task / run. */
data class NovvyTask(
    val id: String,
    val title: String,
    val request: String,
    val status: TaskStatus = TaskStatus.RUNNING,
    val agents: List<AgentTask> = emptyList(),
    val coordinatorMessage: String = "",
    val result: String = "",
    val approval: ApprovalRequest? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class ProviderKind(val displayName: String) {
    OPENAI("OpenAI"),
    ANTHROPIC("Anthropic Claude"),
    GEMINI("Google Gemini"),
    GROQ("Groq"),
    OPENROUTER("OpenRouter"),
    OLLAMA("Ollama (local)"),
    MOCK("Demo (offline)"),
}

data class ProviderConfig(
    val kind: ProviderKind,
    val model: String,
    val enabled: Boolean = true,
    val isDefault: Boolean = false,
    val isFallback: Boolean = false,
    val privateOnly: Boolean = false,
    val hasKey: Boolean = false,
)

enum class DocumentFormat(val ext: String, val mime: String) {
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PDF("pdf", "application/pdf"),
    TXT("txt", "text/plain"),
    MARKDOWN("md", "text/markdown"),
}

enum class ApprovalType {
    GMAIL_SEND,
    DRIVE_SAVE,
    PRIVATE_DATA,
    PURCHASE,
    DELETE_FILE,
    PUBLIC_POST,
    CONTACT_PERSON,
}

data class ApprovalRequest(
    val type: ApprovalType,
    val agentKind: AgentKind,
    val title: String,
    val summary: String,
    val detail: String,
)

data class EmailDraft(
    val to: String,
    val subject: String,
    val body: String,
    val attachmentName: String? = null,
)

data class DriveFile(
    val name: String,
    val folder: String,
    val format: DocumentFormat,
    val sizeBytes: Long = 0,
    val uploaded: Boolean = false,
)

data class EmailSummary(
    val id: String,
    val from: String,
    val subject: String,
    val preview: String,
    val urgent: Boolean,
    val unread: Boolean,
)

data class ChatMessage(val role: String, val text: String)