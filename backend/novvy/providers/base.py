"""Provider-agnostic LLM interface.

Every provider implements :meth:`LLMProvider.complete`, translating the
normalized internal :class:`~novvy.models.Message` / tool schema into whatever
the upstream API expects and returning a normalized
:class:`~novvy.models.LLMResponse`.
"""

from __future__ import annotations

from typing import Any, Protocol, runtime_checkable

from ..models import LLMResponse, Message


@runtime_checkable
class LLMProvider(Protocol):
    name: str

    def complete(
        self,
        messages: list[Message],
        tools: list[dict[str, Any]] | None = None,
    ) -> LLMResponse: ...


def to_openai_messages(messages: list[Message]) -> list[dict[str, Any]]:
    """Translate internal messages to OpenAI chat-completions format."""
    out: list[dict[str, Any]] = []
    for m in messages:
        if m.role == "assistant" and m.tool_calls:
            out.append(
                {
                    "role": "assistant",
                    "content": m.content or None,
                    "tool_calls": [
                        {
                            "id": tc.id,
                            "type": "function",
                            "function": {
                                "name": tc.name,
                                "arguments": _json_dumps(tc.arguments),
                            },
                        }
                        for tc in m.tool_calls
                    ],
                }
            )
        elif m.role == "tool":
            out.append(
                {
                    "role": "tool",
                    "tool_call_id": m.tool_call_id,
                    "content": m.content,
                }
            )
        else:
            out.append({"role": m.role, "content": m.content})
    return out


def to_anthropic_messages(messages: list[Message]) -> tuple[str, list[dict[str, Any]]]:
    """Translate internal messages to Anthropic format.

    Returns (system_prompt, messages).
    """
    system_parts: list[str] = []
    out: list[dict[str, Any]] = []
    for m in messages:
        if m.role == "system":
            system_parts.append(m.content)
        elif m.role == "user":
            out.append({"role": "user", "content": m.content})
        elif m.role == "assistant":
            content: list[dict[str, Any]] = []
            if m.content:
                content.append({"type": "text", "text": m.content})
            for tc in m.tool_calls:
                content.append(
                    {
                        "type": "tool_use",
                        "id": tc.id,
                        "name": tc.name,
                        "input": tc.arguments,
                    }
                )
            out.append({"role": "assistant", "content": content or m.content})
        elif m.role == "tool":
            out.append(
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "tool_result",
                            "tool_use_id": m.tool_call_id,
                            "content": m.content,
                        }
                    ],
                }
            )
    return "\n\n".join(system_parts), out


def to_anthropic_tools(tools: list[dict[str, Any]] | None) -> list[dict[str, Any]]:
    if not tools:
        return []
    out = []
    for t in tools:
        fn = t["function"]
        out.append(
            {
                "name": fn["name"],
                "description": fn["description"],
                "input_schema": fn["parameters"],
            }
        )
    return out


def _json_dumps(value: Any) -> str:
    import json

    return json.dumps(value)