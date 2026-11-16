"""Deterministic offline provider used for demos and tests.

It performs a tiny amount of "reasoning": if the incoming user task contains a
simple arithmetic expression it emits a ``calculator`` tool call, then produces
a final answer once the tool result is available. This exercises the full
agent tool-calling loop without any network access or API keys.
"""

from __future__ import annotations

import re
from typing import Any

from ..models import LLMResponse, Message, ToolCall

_MATH_RE = re.compile(r"-?\d+(?:\.\d+)?\s*[-+*/]\s*-?\d+(?:\.\d+)?(?:\s*[-+*/]\s*-?\d+(?:\.\d+)?)*")


class MockProvider:
    name = "mock"

    def __init__(self, model: str = "mock-1") -> None:
        self.model = model

    def complete(
        self,
        messages: list[Message],
        tools: list[dict[str, Any]] | None = None,
    ) -> LLMResponse:
        # If we just received a tool result, summarize and finish.
        last_tool = next((m for m in reversed(messages) if m.role == "tool"), None)
        if last_tool is not None:
            return LLMResponse(
                content=(
                    f"Using the `{last_tool.name}` tool I computed: {last_tool.content}. "
                    "Here is the result for the task."
                )
            )

        # Otherwise look at the most recent user message for arithmetic.
        user_msg = next((m for m in reversed(messages) if m.role == "user"), None)
        text = user_msg.content if user_msg else ""
        match = _MATH_RE.search(text)
        if match:
            expression = match.group(0).strip()
            return LLMResponse(
                content=f"I'll calculate `{expression}`.",
                tool_calls=[
                    ToolCall(
                        id="call_calc_1",
                        name="calculator",
                        arguments={"expression": expression},
                    )
                ],
            )

        # Default: produce a short deterministic acknowledgement.
        snippet = text.strip().splitlines()[0] if text.strip() else "(empty task)"
        return LLMResponse(
            content=(
                f"Task handled: \"{snippet[:120]}\". "
                "No external tools were required for this step."
            )
        )