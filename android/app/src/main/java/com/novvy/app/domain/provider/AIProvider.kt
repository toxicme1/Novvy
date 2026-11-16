package com.novvy.app.domain.provider

import com.novvy.app.domain.model.ProviderKind

/** Abstraction over an LLM backend. Implementations must be safe to call off the main thread. */
interface AIProvider {
    val kind: ProviderKind

    /** Produce a completion for the given system + user prompt. */
    suspend fun complete(system: String, user: String): String
}

/**
 * Deterministic offline provider used by the demo so the whole multi-agent flow
 * works with no API keys and no network. Swap in a real provider via [Providers].
 */
class MockProvider : AIProvider {
    override val kind: ProviderKind = ProviderKind.MOCK

    override suspend fun complete(system: String, user: String): String {
        val goal = user.replace(Regex("\\s+"), " ").trim()
        return buildString {
            append("For “").append(goal.take(140)).append("”:\n")
            append("• Clarified the goal and the desired outcome.\n")
            append("• Gathered the key inputs and assumptions.\n")
            append("• Prepared a clear, structured draft ready for review.")
        }
    }
}