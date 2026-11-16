"""ADB helpers: locate adb, list devices, install an APK to a chosen device.

All subprocess calls use argument lists (no shell), and device serials are
validated against the serials reported by ``adb devices`` to avoid command
injection.
"""

from __future__ import annotations

import os
import re
import shutil
import subprocess
from pathlib import Path

SERIAL_RE = re.compile(r"^[A-Za-z0-9._:\-]+$")


def find_adb() -> str | None:
    """Locate the adb executable."""
    env = os.environ.get("NOVVY_ADB_PATH")
    if env and Path(env).exists():
        return env
    which = shutil.which("adb")
    if which:
        return which
    bases = [
        os.environ.get("ANDROID_HOME"),
        os.environ.get("ANDROID_SDK_ROOT"),
        str(Path.home() / "android-sdk"),
    ]
    for base in bases:
        if base:
            candidate = Path(base) / "platform-tools" / "adb"
            if candidate.exists():
                return str(candidate)
    return None


def list_devices() -> dict:
    """Run ``adb devices -l`` and parse the attached devices."""
    adb = find_adb()
    if not adb:
        return {
            "adb": None,
            "available": False,
            "devices": [],
            "error": "adb not found. Install Android platform-tools or set NOVVY_ADB_PATH.",
        }
    try:
        result = subprocess.run(
            [adb, "devices", "-l"], capture_output=True, text=True, timeout=20
        )
    except Exception as exc:  # noqa: BLE001
        return {"adb": adb, "available": False, "devices": [], "error": str(exc)}

    devices = []
    for line in result.stdout.splitlines()[1:]:
        line = line.strip()
        if not line:
            continue
        parts = line.split()
        serial = parts[0]
        state = parts[1] if len(parts) > 1 else "unknown"
        model = ""
        for token in parts[2:]:
            if token.startswith("model:"):
                model = token.split(":", 1)[1].replace("_", " ")
        devices.append({"serial": serial, "state": state, "model": model})
    return {"adb": adb, "available": True, "devices": devices, "error": None}


def install_apk(serial: str, apk_path: str) -> dict:
    """Install ``apk_path`` onto the given device serial via adb."""
    adb = find_adb()
    if not adb:
        return {"ok": False, "error": "adb not found"}

    serial = (serial or "").strip()
    if not SERIAL_RE.match(serial):
        return {"ok": False, "error": "invalid device serial"}

    known = {d["serial"] for d in list_devices().get("devices", [])}
    if serial not in known:
        return {"ok": False, "error": f"unknown or disconnected device: {serial}"}

    if not Path(apk_path).exists():
        return {"ok": False, "error": f"apk not found: {apk_path}"}

    try:
        result = subprocess.run(
            [adb, "-s", serial, "install", "-r", apk_path],
            capture_output=True,
            text=True,
            timeout=300,
        )
    except Exception as exc:  # noqa: BLE001
        return {"ok": False, "error": str(exc)}

    output = (result.stdout + result.stderr).strip()
    return {"ok": result.returncode == 0, "output": output, "returncode": result.returncode}