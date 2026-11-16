"""Orchestrator agent: decomposes a goal, dispatches to workers, synthesizes.

This is a simple, robust map/reduce multi-agent pattern:

1. Plan  - split the goal into independent sub-tasks.
2. Assign- run one worker per sub-task.
3. Reduce- combine the worker outputs into a final answer.
"""

from __future__ import annotations

import re

from ..config import Settings
from ..models import Message
from ..tools import ToolRegistry
from .base import BaseAgent
from .worker import WorkerAgent

ORCHESTRATOR_SYSTEM_PROMPT = (
    "You are Novvy, the orchestrator of a multi-agent workspace. You receive a "
    "high-level goal, break it into independent sub-tasks, delegate each to a "
    "worker agent, and then synthesize a single coherent answer. Be concise."
)

_SPLIT_RE = re.compile(r"(?:[\n;]+|(?<=\.)\s+)")


class OrchestratorAgent(BaseAgent):
    role = "orchestrator"

    def __init__(self, provider, tools: ToolRegistry, settings: Settings) -> None:
        super().__init__(
            name="novvy-orchestrator",
            system_prompt=ORCHESTRATOR_SYSTEM_PROMPT,
            provider=provider,
            tools=tools,
            settings=settings,
            max_steps=settings.max_steps,
        )

    def plan(self, goal: str) -> list[str]:
        """Split a goal into sub-tasks. Provider-aware but with a safe fallback."""
        parts = [p.strip(" -•\t") for p in _SPLIT_RE.split(goal) if p and p.strip()]
        # Ask the model for a plan when it is not the offline mock provider.
        if getattr(self.provider, "name", "mock") != "mock":
            try:
                prompt = Message(
                    role="user",
                    content=(
                        "Break this goal into 1-4 independent sub-tasks. Respond with "
                        "one sub-task per line, no numbering:\n\n" + goal
                    ),
                )
                resp = self.provider.complete(
                    [Message(role="system", content=self.system_prompt), prompt]
                )
                lines = [ln.strip(" -•\t") for ln in resp.content.splitlines() if ln.strip()]
                if lines:
                    parts = lines
            except Exception:  # noqa: BLE001
                pass
        if not parts:
            parts = [goal]
        return parts[:4]

    def run(self, goal: str, context: dict | None = None) -> str:
        self.trace = []
        subtasks = self.plan(goal)
        self.trace.append({"event": "plan", "subtasks": subtasks})

        results: list[tuple[str, str]] = []
        for i, subtask in enumerate(subtasks, start=1):
            worker = WorkerAgent(self.provider, self.tools, self.settings, name=f"worker-{i}")
            answer = worker.run(subtask, context={"goal": goal})
            results.append((subtask, answer))
            self.trace.append(
                {"event": "worker", "name": worker.name, "subtask": subtask, "steps": worker.trace}
            )

        return self.synthesize(goal, results)

    def synthesize(self, goal: str, results: list[tuple[str, str]]) -> str:
        body = "\n".join(f"- {st}: {ans}" for st, ans in results)
        if getattr(self.provider, "name", "mock") == "mock":
            return f"Goal: {goal}\n\nResults:\n{body}"
        messages = [
            Message(role="system", content=self.system_prompt),
            Message(
                role="user",
                content=(
                    f"Goal: {goal}\n\nWorker results:\n{body}\n\n"
                    "Synthesize a single final answer."
                ),
            ),
        ]
        return self.provider.complete(messages).content or body