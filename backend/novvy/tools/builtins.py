"""Tool registry and built-in tools available to Novvy agents."""

from __future__ import annotations

import ast
import datetime as _dt
import json
import operator
from dataclasses import dataclass, field
from typing import Any, Callable

_OPS = {
    ast.Add: operator.add,
    ast.Sub: operator.sub,
    ast.Mult: operator.mul,
    ast.Div: operator.truediv,
    ast.Pow: operator.pow,
    ast.Mod: operator.mod,
    ast.FloorDiv: operator.floordiv,
    ast.USub: operator.neg,
    ast.UAdd: operator.pos,
}


def _safe_eval(expr: str) -> float:
    """Evaluate a arithmetic expression using a restricted AST walker."""

    def _eval(node: ast.AST) -> float:
        if isinstance(node, ast.Expression):
            return _eval(node.body)
        if isinstance(node, ast.Constant) and isinstance(node.value, (int, float)):
            return float(node.value)
        if isinstance(node, ast.BinOp) and type(node.op) in _OPS:
            return _OPS[type(node.op)](_eval(node.left), _eval(node.right))
        if isinstance(node, ast.UnaryOp) and type(node.op) in _OPS:
            return _OPS[type(node.op)](_eval(node.operand))
        raise ValueError("unsupported expression")

    return _eval(ast.parse(expr, mode="eval"))


@dataclass
class Tool:
    name: str
    description: str
    parameters: dict[str, Any]
    func: Callable[..., Any]
    handler: Callable[[dict[str, Any]], str] | None = None

    def run(self, arguments: dict[str, Any]) -> str:
        try:
            return str(self.func(**arguments))
        except Exception as exc:  # noqa: BLE001
            return f"error: {exc}"

    def spec(self) -> dict[str, Any]:
        """OpenAI-style function-calling schema."""
        return {
            "type": "function",
            "function": {
                "name": self.name,
                "description": self.description,
                "parameters": self.parameters,
            },
        }


class ToolRegistry:
    def __init__(self) -> None:
        self._tools: dict[str, Tool] = {}

    def register(self, tool: Tool) -> None:
        self._tools[tool.name] = tool

    def get(self, name: str) -> Tool | None:
        return self._tools.get(name)

    def all(self) -> list[Tool]:
        return list(self._tools.values())

    def specs(self) -> list[dict[str, Any]]:
        return [t.spec() for t in self._tools.values()]

    def execute(self, name: str, arguments: dict[str, Any]) -> str:
        tool = self._tools.get(name)
        if tool is None:
            return f"error: unknown tool '{name}'"
        if not isinstance(arguments, dict):
            try:
                arguments = json.loads(arguments) if isinstance(arguments, str) else {}
            except json.JSONDecodeError:
                arguments = {}
        return tool.run(arguments)

    def names(self) -> list[str]:
        return list(self._tools.keys())


def build_default_registry() -> ToolRegistry:
    registry = ToolRegistry()

    def calculator(expression: str) -> str:
        return str(_safe_eval(expression))

    def current_time() -> str:
        return _dt.datetime.now(_dt.timezone.utc).isoformat()

    def text_stats(text: str) -> str:
        words = text.split()
        return json.dumps(
            {
                "characters": len(text),
                "words": len(words),
                "sentences": text.count(".") + text.count("!") + text.count("?"),
            }
        )

    def word_count(text: str) -> str:
        return str(len(text.split()))

    registry.register(
        Tool(
            name="calculator",
            description="Evaluate a basic arithmetic expression, e.g. '2 + 3 * 4'.",
            parameters={
                "type": "object",
                "properties": {
                    "expression": {"type": "string", "description": "Arithmetic expression"}
                },
                "required": ["expression"],
            },
            func=calculator,
        )
    )
    registry.register(
        Tool(
            name="current_time",
            description="Return the current UTC date and time in ISO-8601 format.",
            parameters={"type": "object", "properties": {}},
            func=current_time,
        )
    )
    registry.register(
        Tool(
            name="text_stats",
            description="Return character, word and sentence counts for a block of text.",
            parameters={
                "type": "object",
                "properties": {"text": {"type": "string"}},
                "required": ["text"],
            },
            func=text_stats,
        )
    )
    registry.register(
        Tool(
            name="word_count",
            description="Count the words in a block of text.",
            parameters={
                "type": "object",
                "properties": {"text": {"type": "string"}},
                "required": ["text"],
            },
            func=word_count,
        )
    )
    return registry