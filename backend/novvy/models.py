"""Core data models for the Novvy multi-agent workspace."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from pydantic import BaseModel, Field


@dataclass
class Message:
    """A single chat message exchanged with a provider."""

    role: str  # system | user | assistant | tool
    content: str = ""
    tool_calls: list["ToolCall"] = field(default_factory=list)
    tool_call_id: str | None = None
    name: str | None = None


@dataclass
class ToolCall:
    """A request from the model to invoke a tool."""

    id: str
    name: str
    arguments: dict[str, Any] = field(default_factory=dict)


@dataclass
class LLMResponse:
    """Normalized provider response."""

    content: str = ""
    tool_calls: list[ToolCall] = field(default_factory=list)
    raw: Any = None


# ---------------------------------------------------------------------------
# API schemas
# ---------------------------------------------------------------------------


class RunRequest(BaseModel):
    goal: str = Field(..., description="High level goal for the workspace")
    provider: str | None = Field(
        None, description="Override provider name (mock, openai, anthropic, ollama)"
    )
    model: str | None = Field(None, description="Override model name")
    max_steps: int = Field(6, ge=1, le=20, description="Max reasoning steps per agent")


class AgentTrace(BaseModel):
    name: str
    role: str
    steps: list[dict[str, Any]] = Field(default_factory=list)
    result: str = ""


class RunResponse(BaseModel):
    run_id: str
    goal: str
    provider: str
    orchestrator: str
    agents: list[AgentTrace] = Field(default_factory=list)
    result: str = ""
    elapsed_ms: int = 0


class HealthResponse(BaseModel):
    status: str
    app: str
    provider: str
    models: list[str]