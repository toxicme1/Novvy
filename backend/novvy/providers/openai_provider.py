"""OpenAI-compatible provider (works with OpenAI, Ollama, vLLM, Groq, ...).

Uses only the standard library (urllib) so there are no extra dependencies.
"""

from __future__ import annotations

import json
import urllib.error
import urllib.request
from typing import Any

from ..config import Settings, default_base_url, default_model
from ..models import LLMResponse, Message, ToolCall
from .base import to_openai_messages


class OpenAIProvider:
    def __init__(self, settings: Settings, name: str = "openai") -> None:
        self.name = name
        self.settings = settings.for_provider(name)
        self.base_url = (self.settings.base_url or default_base_url(name)).rstrip("/")
        self.model = self.settings.model or default_model(name)
        self.api_key = self.settings.api_key

    def complete(
        self,
        messages: list[Message],
        tools: list[dict[str, Any]] | None = None,
    ) -> LLMResponse:
        payload: dict[str, Any] = {
            "model": self.model,
            "messages": to_openai_messages(messages),
        }
        if tools:
            payload["tools"] = tools
            payload["tool_choice"] = "auto"

        headers = {"Content-Type": "application/json"}
        if self.api_key:
            headers["Authorization"] = f"Bearer {self.api_key}"

        data = json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(
            f"{self.base_url}/chat/completions", data=data, headers=headers, method="POST"
        )
        try:
            with urllib.request.urlopen(req, timeout=self.settings.request_timeout) as resp:
                body = json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as exc:  # noqa: PERF203
            detail = exc.read().decode("utf-8", "replace")
            return LLMResponse(content=f"provider error {exc.code}: {detail}")
        except urllib.error.URLError as exc:
            return LLMResponse(content=f"provider connection error: {exc.reason}")

        choice = body.get("choices", [{}])[0]
        message = choice.get("message", {})
        content = message.get("content") or ""
        tool_calls: list[ToolCall] = []
        for i, tc in enumerate(message.get("tool_calls") or []):
            fn = tc.get("function", {})
            try:
                args = json.loads(fn.get("arguments") or "{}")
            except json.JSONDecodeError:
                args = {"_raw": fn.get("arguments")}
            tool_calls.append(
                ToolCall(id=tc.get("id") or f"call_{i}", name=fn.get("name", ""), arguments=args)
            )
        return LLMResponse(content=content, tool_calls=tool_calls, raw=body)