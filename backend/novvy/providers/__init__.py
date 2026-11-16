"""Provider factory. Central place that maps a provider name to an instance."""

from __future__ import annotations

from ..config import Settings, settings as global_settings
from .anthropic_provider import AnthropicProvider
from .mock import MockProvider
from .openai_provider import OpenAIProvider

KNOWN_PROVIDERS = ["mock", "openai", "anthropic", "ollama"]


def get_provider(name: str | None = None, settings: Settings | None = None) -> object:
    settings = settings or global_settings
    name = (name or settings.provider or "mock").lower()

    if name == "mock":
        return MockProvider()
    if name == "openai":
        return OpenAIProvider(settings, name="openai")
    if name == "ollama":
        return OpenAIProvider(settings, name="ollama")
    if name == "anthropic":
        return AnthropicProvider(settings)
    # Unknown provider: fall back to mock so the app keeps working.
    return MockProvider()


def resolve_name(provider: object, requested: str | None, settings: Settings | None = None) -> str:
    if requested:
        return requested.lower()
    return getattr(provider, "name", (settings or global_settings).provider)