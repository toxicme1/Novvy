"""Workspace: ties providers, tools and agents together for a single run."""

from __future__ import annotations

import time
import uuid

from .agents.base import build_agent
from .config import Settings, settings as global_settings
from .models import AgentTrace, RunRequest, RunResponse
from .providers import get_provider, resolve_name
from .tools import build_default_registry


def run_workspace(req: RunRequest, settings: Settings | None = None) -> RunResponse:
    settings = settings or global_settings
    # Shallow copy so per-request overrides don't leak across requests.
    import copy

    cfg = copy.copy(settings)
    if req.max_steps:
        cfg.max_steps = req.max_steps
    provider_name = resolve_name(get_provider(req.provider, cfg), req.provider, cfg)

    registry = build_default_registry()
    orchestrator = build_agent(
        "orchestrator", cfg, registry, provider_name=req.provider, model=req.model
    )

    started = time.perf_counter()
    result = orchestrator.run(req.goal)
    elapsed_ms = int((time.perf_counter() - started) * 1000)

    # Collect worker traces recorded on the orchestrator.
    agents: list[AgentTrace] = []
    for event in orchestrator.trace:
        if event.get("event") == "worker":
            agents.append(
                AgentTrace(
                    name=event["name"],
                    role="worker",
                    steps=event.get("steps", []),
                    result=_worker_result(event.get("steps", [])),
                )
            )

    return RunResponse(
        run_id=uuid.uuid4().hex[:12],
        goal=req.goal,
        provider=provider_name,
        orchestrator=orchestrator.name,
        agents=agents,
        result=result,
        elapsed_ms=elapsed_ms,
    )


def _worker_result(steps: list[dict]) -> str:
    for step in reversed(steps):
        if step.get("assistant"):
            return step["assistant"]
    return ""