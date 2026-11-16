# Novvy

**Novvy** is a provider-agnostic, multi-agent workspace. A high-level goal goes
in; an orchestrator decomposes it into sub-tasks, worker agents solve each one
(with tool use), and the results are synthesized into a single answer.

It ships with two parts:

- **Backend** — a Python + FastAPI service that runs the multi-agent workspace
  and exposes a small HTTP/SSE API. Works with Anthropic, OpenAI-compatible
  endpoints, Ollama, or a fully offline **mock** provider (default).
- **Android app** — a dependency-free Kotlin client that talks to the backend
  and renders the orchestrator + per-agent results.

```
Novvy/
├── backend/            # FastAPI multi-agent workspace
│   └── novvy/
│       ├── providers/  # mock / openai / anthropic / ollama
│       ├── agents/     # orchestrator + workers
│       ├── tools/      # tool registry + builtins
│       ├── workspace.py
│       └── main.py
├── android/            # Kotlin Android client (Gradle)
├── dist/novvy.apk      # built debug APK
└── scripts/            # helper scripts
```

## Quick start — backend

```bash
cd backend
python3 -m venv .venv && . .venv/bin/activate
pip install -r requirements.txt

# Offline demo (no API keys needed)
python -m novvy "Plan a launch; calculate 12 * 8 + 4"

# Run the HTTP API
python -m novvy --serve --port 8000
# or: uvicorn novvy.main:app --host 0.0.0.0 --port 8000
```

### Providers

Novvy is provider-agnostic. Select one with `NOVVY_PROVIDER` (or `--provider`):

| Provider    | Notes                                             |
|-------------|---------------------------------------------------|
| `mock`      | Offline, deterministic. Default. No keys needed.  |
| `openai`    | OpenAI or any OpenAI-compatible `/chat/completions`. |
| `ollama`    | Local Ollama via its OpenAI-compatible endpoint.  |
| `anthropic` | Anthropic Claude native Messages API.             |

Configuration via environment variables:

```bash
export NOVVY_PROVIDER=anthropic
export NOVVY_ANTHROPIC_API_KEY=sk-ant-...
export NOVVY_MODEL=claude-3-5-sonnet-latest
```

Generic overrides also work: `NOVVY_API_KEY`, `NOVVY_BASE_URL`, `NOVVY_MODEL`,
or per-provider `NOVVY_<PROVIDER>_{API_KEY,BASE_URL,MODEL}`.

## HTTP API

| Method | Path              | Description                          |
|--------|-------------------|--------------------------------------|
| GET    | `/health`         | Liveness + active provider           |
| GET    | `/api/providers`  | Known providers and default          |
| GET    | `/api/tools`      | Registered tools (JSON schema)       |
| POST   | `/api/run`        | Run the workspace, returns JSON      |
| POST   | `/api/run/stream` | Run the workspace, streams SSE events|

```bash
curl -X POST http://localhost:8000/api/run \
  -H 'Content-Type: application/json' \
  -d '{"goal":"Plan a launch; calculate 7*6"}'
```

## Web interface (download + install to a device)

Open the backend root URL (`http://localhost:8000/`) for a web UI with:

- **Download & install to my phone** — runs `adb devices` on the machine running
  Novvy, lists connected devices, and installs `dist/novvy.apk` onto the one you pick.
- **Just download the APK file** — downloads `novvy.apk` directly.

> ADB installs only work for devices plugged into the machine running the
> backend (USB debugging enabled). A cloud-hosted backend sees no devices, so
> run it locally to use the install button.

Endpoints: `GET /download`, `GET /api/adb/devices`, `POST /api/adb/install`.

## Android app

The app is a thin client: enter a backend URL and a goal, tap **Run workspace**.

- Emulator → use `http://10.0.2.2:8000` (maps to the host machine).
- Physical device → use your machine's LAN IP, e.g. `http://192.168.1.20:8000`.

### Build the APK

```bash
./scripts/build_apk.sh
# output: dist/novvy.apk
```

The debug APK is already committed at [`dist/novvy.apk`](dist/novvy.apk).

### Install

```bash
adb install -r dist/novvy.apk
```

## Architecture

1. **Plan** — `OrchestratorAgent` splits the goal into independent sub-tasks.
2. **Assign** — one `WorkerAgent` per sub-task runs the reasoning loop.
3. **Tool use** — agents call registered tools (e.g. `calculator`) in a loop
   until they produce a final answer.
4. **Reduce** — the orchestrator synthesizes worker outputs into one answer.

Providers are pluggable: each translates the normalized message/tool format to
its upstream API and returns a normalized response, so switching providers never
touches the agent logic.

## License

MIT