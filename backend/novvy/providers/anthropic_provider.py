"""Anthropic Claude provider (native Messages API)."""

from __future__ import annotations

import json
import urllib.error
import urllib.request
from typing import Any

from ..config import Settings, default_base_url, default_model
from ..models import LLMResponse, Message, ToolCall
from .base import to_anthropic_messages, to_anthropic_tools


class AnthropicProvider:
    name = "anthropic"

    def __init__(self, settings: Settings) -> None:
        self.settings = settings.for_provider(self.name)
        self.base_url = (self.settings.base_url or default_base_url(self.name)).rstrip("/")
        self.model = self.settings.model or default_model(self.name)
        self.api_key = self.settings.api_key

    def complete(
        self,
        messages: list[Message],
        tools: list[dict[str, Any]] | None = None,
    ) -> LLMResponse:
        system, conv = to_anthropic_messages(messages)
        payload: dict[str, Any] = {
            "model": self.model,
            "max_tokens": 1024,
            "messages": conv,
        }
        if system:
            payload["system"] = system
        anth_tools = to_anthropic_tools(tools)
        if anth_tools:
            payload["tools"] = anth_tools

        headers = {
            "Content-Type": "application/json",
            "x-api-key": self.api_key or "",
            "anthropic-version": "2023-06-01",
        }
        data = json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(
            f"{self.base_url}/messages", data=data, headers=headers, method="POST"
        )
        try:
            with urllib.request.urlopen(req, timeout=self.settings.request_timeout) as resp:
                body = json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as exc:  # noqa: PERF203
            detail = exc.read().decode("utf-8", "replace")
            return LLMResponse(content=f"provider error {exc.code}: {detail}")
        except urllib.error.URLError as exc:
            return LLMResponse(content=f"provider connection error: {exc.reason}")

        text_parts: list[str] = []
        tool_calls: list[ToolCall] = []
        for block in body.get("content", []):
            if block.get("type") == "text":
                text_parts.append(block.get("text", ""))
            elif block.get("type") == "tool_use":
                tool_calls.append(
                    ToolCall(
                        id=block.get("id", ""),
                        name=block.get("name", ""),
                        arguments=block.get("input", {}) or {},
                    )
                )
        return LLMResponse(content="\n".join(text_parts), tool_calls=tool_calls, raw=body)