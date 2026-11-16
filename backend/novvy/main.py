"""Novvy FastAPI application.

Run with:  uvicorn novvy.main:app --host 0.0.0.0 --port 8000
"""

from __future__ import annotations

import json
import os
from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, HTMLResponse, JSONResponse, StreamingResponse
from pydantic import BaseModel

from . import APP_NAME, __version__
from . import adb
from .config import settings as global_settings
from .models import HealthResponse, RunRequest, RunResponse
from .providers import KNOWN_PROVIDERS
from .tools import build_default_registry
from .workspace import run_workspace

STATIC_DIR = Path(__file__).parent / "static"
APK_PATH = Path(
    os.environ.get(
        "NOVVY_APK_PATH",
        str(Path(__file__).resolve().parents[2] / "dist" / "novvy.apk"),
    )
)

app = FastAPI(title=APP_NAME, version=__version__)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


class InstallRequest(BaseModel):
    device: str


@app.get("/", response_class=HTMLResponse, include_in_schema=False)
def index() -> FileResponse:
    """Serve the web interface (download + adb install)."""
    return FileResponse(STATIC_DIR / "index.html")


@app.get("/api/info")
def root() -> dict:
    return {
        "app": APP_NAME,
        "version": __version__,
        "endpoints": [
            "/",
            "/health",
            "/api/providers",
            "/api/tools",
            "/api/run",
            "/api/run/stream",
            "/api/adb/devices",
            "/api/adb/install",
            "/download",
        ],
    }


@app.get("/download")
def download_apk():
    """Stream the built APK as a file download."""
    if not APK_PATH.exists():
        return JSONResponse(
            {"error": f"apk not found at {APK_PATH}. Build it with scripts/build_apk.sh."},
            status_code=404,
        )
    return FileResponse(
        APK_PATH,
        media_type="application/vnd.android.package-archive",
        filename="novvy.apk",
    )


@app.get("/api/adb/devices")
def adb_devices() -> dict:
    """List devices visible to adb on the machine running Novvy."""
    return adb.list_devices()


@app.post("/api/adb/install")
def adb_install(req: InstallRequest) -> dict:
    """Install the Novvy APK onto the chosen device serial."""
    return adb.install_apk(req.device, str(APK_PATH))


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