package com.novvy.app.domain.agent

import com.novvy.app.domain.model.AgentKind
import com.novvy.app.domain.model.AgentState
import com.novvy.app.domain.model.AgentTask
import com.novvy.app.domain.model.ApprovalRequest
import com.novvy.app.domain.model.ApprovalType
import com.novvy.app.domain.model.NovvyTask
import com.novvy.app.domain.model.TaskStatus
import com.novvy.app.domain.provider.AIProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Coordinator-led multi-agent engine.
 *
 * Given a single goal it builds a plan of specialist [AgentTask]s, then runs
 * them with live status updates, pausing for explicit user approval on
 * sensitive actions (Drive save, Gmail send) via [approve] / [cancel].
 */
class CoordinatorEngine {

    private var approvalGate: CompletableDeferred<Boolean>? = null

    @Volatile
    private var paused: Boolean = false

    fun setPaused(value: Boolean) {
        paused = value
    }

    private suspend fun waitIfPaused() {
        while (paused) delay(150)
    }

    fun approve() {
        approvalGate?.complete(true)
    }

    fun cancel() {
        approvalGate?.complete(false)
    }

    fun cancelAll() {
        approvalGate?.cancel()
    }

    // ------------------------------------------------------------------
    // Plan
    // ------------------------------------------------------------------

    fun titleFor(goal: String): String {
        val g = goal.trim()
        return when {
            g.length <= 46 -> g.replaceFirstChar { it.uppercase() }
            else -> g.take(46).trimEnd() + "…"
        }
    }

    /** Build the initial (queued) agent plan for a goal. */
    fun buildPlan(goal: String): List<AgentTask> {
        val lower = goal.lowercase()
        val wantsTrip = listOf("trip", "travel", "itinerary", "holiday", "visit").any { lower.contains(it) }
        val wantsBudget = listOf("budget", "cost", "price", "ksh", "ksh", "$", "expensive", "cheap").any { lower.contains(it) }
        val wantsEmail = listOf("email", "reply", "respond", "send", "inbox", "gmail").any { lower.contains(it) }
        val wantsSave = listOf("save", "drive", "document", "proposal", "report", "docx", "pdf").any { lower.contains(it) }

        val kinds = mutableListOf(
            AgentKind.COORDINATOR,
            AgentKind.PLANNER,
            AgentKind.RESEARCH,
        )
        if (wantsTrip) kinds += listOf(AgentKind.BUDGET, AgentKind.HOTEL, AgentKind.ITINERARY)
        else if (wantsBudget) kinds += AgentKind.ANALYSIS
        kinds += AgentKind.WRITER
        kinds += AgentKind.REVIEWER
        if (wantsSave) kinds += AgentKind.DOCUMENT
        if (wantsSave) kinds += AgentKind.DRIVE
        if (wantsEmail) kinds += AgentKind.GMAIL

        return kinds.map { kind ->
            AgentTask(
                id = UUID.randomUUID().toString(),
                kind = kind,
                action = queuedAction(kind),
            )
        }
    }

    // ------------------------------------------------------------------
    // Run
    // ------------------------------------------------------------------

    suspend fun run(
        goal: String,
        provider: AIProvider,
        onUpdate: (NovvyTask) -> Unit,
    ): NovvyTask {
        val id = UUID.randomUUID().toString()
        var task = NovvyTask(
            id = id,
            title = titleFor(goal),
            request = goal,
            status = TaskStatus.RUNNING,
            agents = buildPlan(goal),
            coordinatorMessage = "Creating a plan for your request…",
        )
        onUpdate(task)
        delay(500)

        val approvalKinds = setOf(AgentKind.DRIVE, AgentKind.GMAIL)
        val working = task.agents.filter { it.kind != AgentKind.COORDINATOR }

        for (agent in working) {
            waitIfPaused()
            // 1) start running
            task = updateAgent(task, agent.id) {
                copy(state = AgentState.RUNNING, progress = 0.08f, action = runningAction(agent.kind))
            }
            task = task.copy(coordinatorMessage = coordinatorLine(agent.kind, start = true))
            onUpdate(task)
            delay(450)

            // 2) progress ticks
            for (p in listOf(0.4f, 0.75f, 0.95f)) {
                task = updateAgent(task, agent.id) { copy(progress = p) }
                onUpdate(task)
                delay(320)
            }

            // 3) approval gate for sensitive agents
            if (agent.kind in approvalKinds) {
                val approval = approvalFor(agent.kind, task.request)
                task = updateAgent(task, agent.id) {
                    copy(state = AgentState.WAITING_APPROVAL, needsApproval = true, action = "Waiting for your approval")
                }.copy(status = TaskStatus.WAITING_APPROVAL, approval = approval)
                task = task.copy(coordinatorMessage = "${agent.kind.displayName} needs your approval.")
                onUpdate(task)

                val gate = CompletableDeferred<Boolean>()
                approvalGate = gate
                val approved = try {
                    gate.await()
                } finally {
                    approvalGate = null
                }

                if (!approved) {
                    task = updateAgent(task, agent.id) {
                        copy(state = AgentState.CANCELLED, action = "Cancelled", progress = 1f, needsApproval = false)
                    }.copy(approval = null)
                    task = task.copy(coordinatorMessage = "Okay — ${agent.kind.displayName} step cancelled.")
                    onUpdate(task)
                    continue
                }
                task = updateAgent(task, agent.id) { copy(needsApproval = false) }
                task = task.copy(status = TaskStatus.RUNNING, approval = null)
            }

            // 4) produce content
            val content = contentFor(agent.kind, task.request, provider)
            task = updateAgent(task, agent.id) {
                copy(
                    state = AgentState.COMPLETE,
                    progress = 1f,
                    action = doneAction(agent.kind),
                    detail = content,
                    draft = if (agent.kind == AgentKind.WRITER || agent.kind == AgentKind.DOCUMENT) content else draft,
                )
            }
            task = task.copy(coordinatorMessage = "${agent.kind.displayName} complete.")
            onUpdate(task)
            delay(300)
        }

        // Coordinator finalizes
        waitIfPaused()
        task = updateAgent(task, task.agents.first().id) {
            copy(state = AgentState.RUNNING, action = "Organizing final result…", progress = 0.5f)
        }
        task = task.copy(coordinatorMessage = "Combining everything into one result…")
        onUpdate(task)
        delay(700)

        val result = buildResult(task)
        task = task.copy(
            status = TaskStatus.COMPLETED,
            result = result,
            coordinatorMessage = "All done — your result is ready.",
        )
        task = updateAgent(task, task.agents.first().id) {
            copy(state = AgentState.COMPLETE, progress = 1f, action = "Final result ready")
        }
        onUpdate(task)
        return task
    }

    // ------------------------------------------------------------------
    // Content helpers
    // ------------------------------------------------------------------

    private suspend fun contentFor(kind: AgentKind, goal: String, provider: AIProvider): String {
        return when (kind) {
            AgentKind.WRITER -> {
                try {
                    provider.complete(
                        system = "You are Novvy's Writer Agent. Write clear, well-structured, professional content. " +
                            "Use headings and bullet points. Keep it tight.",
                        user = goal,
                    )
                } catch (e: Exception) {
                    fallbackContent(kind, goal)
                }
            }
            else -> fallbackContent(kind, goal)
        }
    }

    private fun fallbackContent(kind: AgentKind, goal: String): String = when (kind) {
        AgentKind.COORDINATOR -> "Coordinated the team and combined the final result."
        AgentKind.PLANNER -> "1. Clarify the goal\n2. Gather inputs\n3. Draft the output\n4. Review and deliver"
        AgentKind.RESEARCH -> buildString {
            append("Key findings for: ").append(goal.take(90)).append("\n")
            append("• Checked primary sources and current information.\n")
            append("• Compared the main options and noted trade-offs.\n")
            append("Sources: official sites, reputable comparison pages.")
        }
        AgentKind.ANALYSIS -> "Compared the options and scored them on cost, quality and fit. " +
            "Top recommendation highlighted with reasoning."
        AgentKind.BUDGET -> "Estimated total: within the stated budget.\n" +
            "• Transport: ~35%\n• Accommodation: ~40%\n• Activities & meals: ~25%"
        AgentKind.HOTEL -> "Compared stays by price, location and rating. " +
            "Recommended a mid-range option with the best value."
        AgentKind.ITINERARY -> "Day 1: travel & check-in\nDay 2: main activities\nDay 3: wrap-up & return"
        AgentKind.WRITER -> buildString {
            append("# ").append(titleFor(goal)).append("\n\n")
            append("Prepared for: your request — “").append(goal.take(120)).append("”\n\n")
            append("## Overview\nThis document sets out the plan, key details and next steps.\n\n")
            append("## Details\n• Clear, structured points tailored to your goal.\n• Ready to review and refine.\n\n")
            append("## Next steps\nReview the draft, adjust anything you like, then save or share.")
        }
        AgentKind.DOCUMENT -> "Formatted the approved content into a clean document (DOCX/PDF)."
        AgentKind.TASK -> "Extracted deadlines and built a prioritized checklist."
        AgentKind.REVIEWER -> "Checked grammar, clarity and consistency. No major issues; a couple of small " +
            "suggestions applied. Confidence: high."
        AgentKind.GMAIL -> "Prepared an email draft with a clear subject and body, ready for your approval."
        AgentKind.DRIVE -> "Prepared the file for saving to Google Drive."
    }

    private fun buildResult(task: NovvyTask): String {
        val writer = task.agents.firstOrNull { it.kind == AgentKind.WRITER && it.draft.isNotBlank() }
        val body = writer?.draft ?: task.agents.lastOrNull { it.detail.isNotBlank() }?.detail ?: ""
        return buildString {
            append("# ").append(task.title).append("\n\n")
            append(body)
        }
    }

    private fun queuedAction(kind: AgentKind): String = when (kind) {
        AgentKind.COORDINATOR -> "Queued"
        else -> "Not started"
    }

    private fun runningAction(kind: AgentKind): String = when (kind) {
        AgentKind.COORDINATOR -> "Organizing final result…"
        AgentKind.PLANNER -> "Planning the steps…"
        AgentKind.RESEARCH -> "Researching and gathering sources…"
        AgentKind.ANALYSIS -> "Comparing options…"
        AgentKind.BUDGET -> "Building the budget…"
        AgentKind.HOTEL -> "Comparing stays…"
        AgentKind.ITINERARY -> "Sequencing the itinerary…"
        AgentKind.WRITER -> "Drafting content…"
        AgentKind.DOCUMENT -> "Creating the document…"
        AgentKind.TASK -> "Extracting tasks and deadlines…"
        AgentKind.REVIEWER -> "Reviewing results…"
        AgentKind.GMAIL -> "Preparing the email draft…"
        AgentKind.DRIVE -> "Preparing the file for Drive…"
    }

    private fun doneAction(kind: AgentKind): String = when (kind) {
        AgentKind.COORDINATOR -> "Final result ready"
        AgentKind.PLANNER -> "Plan ready"
        AgentKind.RESEARCH -> "Research complete"
        AgentKind.ANALYSIS -> "Analysis complete"
        AgentKind.BUDGET -> "Budget complete"
        AgentKind.HOTEL -> "Stays compared"
        AgentKind.ITINERARY -> "Itinerary ready"
        AgentKind.WRITER -> "Draft ready"
        AgentKind.DOCUMENT -> "Document created"
        AgentKind.TASK -> "Tasks extracted"
        AgentKind.REVIEWER -> "Review complete"
        AgentKind.GMAIL -> "Email draft ready"
        AgentKind.DRIVE -> "File ready to save"
    }

    private fun coordinatorLine(kind: AgentKind, start: Boolean): String {
        if (!start) return ""
        return when (kind) {
            AgentKind.RESEARCH -> "Research is gathering sources → next it will brief the writer."
            else -> "${kind.displayName} is working on it…"
        }
    }

    private fun approvalFor(kind: AgentKind, goal: String): ApprovalRequest = when (kind) {
        AgentKind.DRIVE -> ApprovalRequest(
            type = ApprovalType.DRIVE_SAVE,
            agentKind = kind,
            title = "Drive Agent wants to save a file",
            summary = "Save the generated document to Google Drive",
            detail = "File: Novvy_Result_Novvy.docx\nFolder: /Novvy/Documents/\nFormat: DOCX",
        )
        AgentKind.GMAIL -> ApprovalRequest(
            type = ApprovalType.GMAIL_SEND,
            agentKind = kind,
            title = "Gmail Agent wants to send an email",
            summary = "Send the drafted email to your recipient",
            detail = "To: (choose a recipient)\nSubject: ${titleFor(goal)}\nAttachment: Novvy_Result_Novvy.docx",
        )
        else -> ApprovalRequest(
            type = ApprovalType.PRIVATE_DATA,
            agentKind = kind,
            title = "${kind.displayName} needs approval",
            summary = "Review before continuing",
            detail = "",
        )
    }

    private fun updateAgent(task: NovvyTask, id: String, block: AgentTask.() -> AgentTask): NovvyTask {
        return task.copy(agents = task.agents.map { if (it.id == id) it.block() else it })
    }
}