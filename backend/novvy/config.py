"""Runtime configuration loaded from environment variables."""

from __future__ import annotations

import os
from dataclasses import dataclass


def _env(name: str, default: str | None = None) -> str | None:
    value = os.environ.get(name)
    return value if value not in (None, "") else default


@dataclass
class Settings:
    provider: str = _env("NOVVY_PROVIDER", "mock") or "mock"
    model: str | None = _env("NOVVY_MODEL")
    api_key: str | None = _env("NOVVY_API_KEY")
    base_url: str | None = _env("NOVVY_BASE_URL")
    request_timeout: float = float(_env("NOVVY_TIMEOUT", "60"))
    max_steps: int = int(_env("NOVVY_MAX_STEPS", "6"))

    # Per-provider overrides (fall back to the generic settings above).
    def for_provider(self, provider: str) -> "Settings":
        prefix = f"NOVVY_{provider.upper()}_"
        return Settings(
            provider=provider,
            model=_env(prefix + "MODEL", self.model),
            api_key=_env(prefix + "API_KEY", self.api_key),
            base_url=_env(prefix + "BASE_URL", self.base_url),
            request_timeout=self.request_timeout,
            max_steps=self.max_steps,
        )


settings = Settings()


def default_base_url(provider: str) -> str:
    return {
        "openai": "https://api.openai.com/v1",
        "anthropic": "https://api.anthropic.com/v1",
        "ollama": "http://localhost:11434/v1",
    }.get(provider, "")


def default_model(provider: str) -> str:
    return {
        "openai": "gpt-4o-mini",
        "anthropic": "claude-3-5-sonnet-latest",
        "ollama": "llama3.1",
        "mock": "mock-1",
    }.get(provider, "mock-1")