"""Novvy FastAPI application.

Run with:  uvicorn novvy.main:app --host 0.0.0.0 --port 8000
"""

from __future__ import annotations

import json

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse

from . import APP_NAME, __version__
from .config import settings as global_settings
from .models import HealthResponse, RunRequest, RunResponse
from .providers import KNOWN_PROVIDERS
from .tools import build_default_registry
from .workspace import run_workspace

app = FastAPI(title=APP_NAME, version=__version__)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/", include_in_schema=False)
def root() -> dict:
    return {
        "app": APP_NAME,
        "version": __version__,
        "endpoints": ["/health", "/api/providers", "/api/tools", "/api/run", "/api/run/stream"],
    }


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="ok",
        app=APP_NAME,
        provider=global_settings.provider,
        models=KNOWN_PROVIDERS,
    )


@app.get("/api/providers")
def providers() -> dict:
    return {
        "default": global_settings.provider,
        "known": KNOWN_PROVIDERS,
        "model": global_settings.model,
    }


@app.get("/api/tools")
def tools() -> dict:
    return {"tools": build_default_registry().specs()}


@app.post("/api/run", response_model=RunResponse)
def run(req: RunRequest) -> RunResponse:
    return run_workspace(req)


@app.post("/api/run/stream")
def run_stream(req: RunRequest) -> StreamingResponse:
    """Server-sent events stream of the orchestration run."""

    def event_stream():
        response = run_workspace(req)
        # Emit a start event, then each agent result, then the final result.
        yield _sse("start", {"goal": req.goal, "provider": response.provider})
        for agent in response.agents:
            yield _sse("agent", agent.model_dump())
        yield _sse("result", response.model_dump())
        yield _sse("done", {"elapsed_ms": response.elapsed_ms})

    return StreamingResponse(event_stream(), media_type="text/event-stream")


def _sse(event: str, data: dict) -> str:
    return f"event: {event}\ndata: {json.dumps(data)}\n\n"