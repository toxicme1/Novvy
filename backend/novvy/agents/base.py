"""Base agent: owns the reasoning/tool-calling loop."""

from __future__ import annotations

import uuid
from typing import Any

from ..config import Settings
from ..models import LLMResponse, Message
from ..providers import get_provider
from ..tools import ToolRegistry


class BaseAgent:
    role: str = "worker"

    def __init__(
        self,
        name: str,
        system_prompt: str,
        provider: Any,
        tools: ToolRegistry,
        settings: Settings,
        max_steps: int = 6,
    ) -> None:
        self.name = name
        self.system_prompt = system_prompt
        self.provider = provider
        self.tools = tools
        self.settings = settings
        self.max_steps = max_steps
        self.trace: list[dict[str, Any]] = []

    # -- reasoning loop ---------------------------------------------------
    def run(self, task: str, context: dict[str, Any] | None = None) -> str:
        self.trace = []
        messages: list[Message] = [
            Message(role="system", content=self.system_prompt),
        ]
        if context:
            ctx_text = "\n".join(f"- {k}: {v}" for k, v in context.items())
            messages.append(
                Message(role="system", content=f"Workspace context:\n{ctx_text}")
            )
        messages.append(Message(role="user", content=task))

        tool_specs = self.tools.specs()
        last_content = ""
        for step in range(self.max_steps):
            resp: LLMResponse = self.provider.complete(messages, tools=tool_specs)
            last_content = resp.content or last_content
            self.trace.append(
                {
                    "step": step + 1,
                    "assistant": resp.content,
                    "tool_calls": [
                        {"name": tc.name, "arguments": tc.arguments} for tc in resp.tool_calls
                    ],
                }
            )
            if not resp.tool_calls:
                return resp.content or last_content

            messages.append(
                Message(role="assistant", content=resp.content, tool_calls=resp.tool_calls)
            )
            for tc in resp.tool_calls:
                result = self.tools.execute(tc.name, tc.arguments)
                self.trace[-1].setdefault("tool_results", []).append(
                    {"name": tc.name, "result": result}
                )
                messages.append(
                    Message(
                        role="tool",
                        content=result,
                        tool_call_id=tc.id,
                        name=tc.name,
                    )
                )
        return last_content

    def new_id(self) -> str:
        return uuid.uuid4().hex[:12]


def build_agent(
    kind: str,
    settings: Settings,
    tools: ToolRegistry,
    provider_name: str | None = None,
    model: str | None = None,
) -> BaseAgent:
    if model:
        settings.model = model
    provider = get_provider(provider_name, settings)
    if kind == "orchestrator":
        from .orchestrator import OrchestratorAgent

        return OrchestratorAgent(provider, tools, settings)
    from .worker import WorkerAgent

    return WorkerAgent(provider, tools, settings)