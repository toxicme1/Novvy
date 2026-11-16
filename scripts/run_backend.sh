#!/usr/bin/env bash
# Run the Novvy FastAPI backend.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND="$ROOT/backend"

if [ ! -d "$BACKEND/.venv" ]; then
  python3 -m venv "$BACKEND/.venv"
  "$BACKEND/.venv/bin/pip" install -q -r "$BACKEND/requirements.txt"
fi

export NOVVY_PROVIDER="${NOVVY_PROVIDER:-mock}"
HOST="${HOST:-0.0.0.0}"
PORT="${PORT:-8000}"

exec "$BACKEND/.venv/bin/python" -m novvy --serve --host "$HOST" --port "$PORT" \
  --provider "$NOVVY_PROVIDER"