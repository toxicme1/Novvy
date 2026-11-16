"""Worker agent: performs a single delegated sub-task."""

from __future__ import annotations

from ..config import Settings
from ..tools import ToolRegistry
from .base import BaseAgent

WORKER_SYSTEM_PROMPT = (
    "You are Novvy worker agent. Solve the assigned sub-task precisely and "
    "concisely. Use the available tools whenever they help (for example the "
    "calculator for arithmetic). Report only the result of the sub-task."
)


class WorkerAgent(BaseAgent):
    role = "worker"

    def __init__(self, provider, tools: ToolRegistry, settings: Settings, name: str = "worker") -> None:
        super().__init__(
            name=name,
            system_prompt=WORKER_SYSTEM_PROMPT,
            provider=provider,
            tools=tools,
            settings=settings,
            max_steps=settings.max_steps,
        )