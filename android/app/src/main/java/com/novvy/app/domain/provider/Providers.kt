package com.novvy.app.domain.provider

import com.novvy.app.domain.model.ProviderConfig
import com.novvy.app.domain.model.ProviderKind

/** Base URLs for OpenAI-compatible providers. */
object ProviderEndpoints {
    fun baseUrl(kind: ProviderKind): String = when (kind) {
        ProviderKind.OPENAI -> "https://api.openai.com/v1"
        ProviderKind.GROQ -> "https://api.groq.com/openai/v1"
        ProviderKind.OPENROUTER -> "https://openrouter.ai/api/v1"
        ProviderKind.OLLAMA -> "http://10.0.2.2:11434/v1"
        else -> ""
    }
}

/** Factory that turns a [ProviderConfig] + secret key into a live [AIProvider]. */
object Providers {
    fun create(config: ProviderConfig, apiKey: String): AIProvider = when (config.kind) {
        ProviderKind.MOCK -> MockProvider()
        ProviderKind.OPENAI,
        ProviderKind.GROQ,
        ProviderKind.OPENROUTER,
        ProviderKind.OLLAMA,
        -> OpenAICompatibleProvider(
            kind = config.kind,
            apiKey = apiKey,
            baseUrl = ProviderEndpoints.baseUrl(config.kind),
            model = config.model,
        )
        // Anthropic and Gemini use different wire formats. Until a native adapter
        // is added, route them through the OpenAI-compatible layer only if the user
        // points them at a compatible gateway; otherwise they fall back to demo mode.
        ProviderKind.ANTHROPIC,
        ProviderKind.GEMINI,
        -> MockProvider()
    }

    fun defaultConfigs(): List<ProviderConfig> = listOf(
        ProviderConfig(ProviderKind.MOCK, "novvy-demo", enabled = true, isDefault = true),
        ProviderConfig(ProviderKind.OPENAI, "gpt-4.1-mini", enabled = false, isFallback = true),
        ProviderConfig(ProviderKind.GEMINI, "gemini-2.5-flash", enabled = false),
        ProviderConfig(ProviderKind.ANTHROPIC, "claude-3-5-sonnet-latest", enabled = false),
        ProviderConfig(ProviderKind.OLLAMA, "llama3.2", enabled = false, privateOnly = true),
    )
}