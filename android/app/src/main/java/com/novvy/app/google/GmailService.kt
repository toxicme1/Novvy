package com.novvy.app.google

import com.novvy.app.domain.model.EmailDraft
import com.novvy.app.domain.model.EmailSummary

/**
 * Gmail integration boundary.
 *
 * A real implementation calls the Gmail REST API
 * (users.messages.list / get / send) with an OAuth token from [GoogleAuthManager].
 * The default [DemoGmailService] returns realistic sample data so the Inbox and
 * Gmail agent screens are fully usable offline.
 */
interface GmailService {
    suspend fun listInbox(): List<EmailSummary>
    suspend fun send(draft: EmailDraft): Result<Unit>
}

class DemoGmailService : GmailService {
    override suspend fun listInbox(): List<EmailSummary> = listOf(
        EmailSummary("1", "client@example.com", "Cleaning Service Proposal", "Hi, could you send over the proposal by Friday?", urgent = true, unread = true),
        EmailSummary("2", "no-reply@bank.co.ke", "Invoice INV-2043 due soon", "Your invoice of KSh 12,400 is due in 3 days.", urgent = true, unread = true),
        EmailSummary("3", "team@novvy.dev", "Weekly sync notes", "Notes and action items from this week's sync.", urgent = false, unread = false),
        EmailSummary("4", "hello@conference.io", "You're invited: Nairobi Tech Week", "Early-bird tickets close this weekend.", urgent = false, unread = true),
    )

    override suspend fun send(draft: EmailDraft): Result<Unit> {
        // Real impl: POST https://gmail.googleapis.com/gmail/v1/users/me/messages/send
        return Result.success(Unit)
    }
}