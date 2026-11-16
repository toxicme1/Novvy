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

The `web/` folder is a static site (deployed to **https://novvy.vercel.app**) with:

- **Download Novvy APK** — downloads `novvy.apk` directly.
- **Install over USB (no terminal)** — browser-only ADB via WebUSB. It lists connected
  phones (like `adb devices`), you pick one, and it pushes + `pm install`s the APK.

> The install-over-USB feature uses WebUSB (yume-chan/adb compiled to WebAssembly) and
> requires **desktop Chrome or Edge over HTTPS** with **USB debugging** enabled on the
> phone. A web page cannot run a local `adb` binary, so this is the browser-native path.
> Rebuild the bundle with `web/build-adb.sh` (source in `web/src/app.js`).

You can also run the same UI locally from the backend root URL (`http://localhost:8000/`)
when the phone is plugged into that machine — then the `/api/adb/*` endpoints shell out to
a real `adb` binary.

Endpoints (backend): `GET /download`, `GET /api/adb/devices`, `POST /api/adb/install`.

## Android app (Kotlin + Jetpack Compose, Material 3)

Novvy is a full Android app — a **coordinator-led multi-agent workspace** with a
premium Material 3 UI (light + dark). It is not a thin client; the agent engine
runs on-device and is provider-agnostic.

**Screens:** Onboarding, Home (prompt + quick actions), Live Task (animated agent
status cards), Agent Detail, Result, Files, Inbox, Settings.

**Agent team:** Coordinator, Planner, Research, Analysis, Budget, Hotel,
Itinerary, Writer, Document, Tasks, Reviewer, Gmail, Drive. Sensitive actions
(Drive save, Gmail send) require explicit approval via an approval dialog.

**Architecture (clean):**
```
com.novvy.app
├── domain/model      # agents, tasks, providers, approvals
├── domain/provider   # AIProvider + Mock + OpenAI-compatible (secure keys)
├── domain/agent      # CoordinatorEngine (live orchestration, approval gates)
├── data              # DataStore settings + EncryptedSharedPreferences keys
├── google            # GoogleCredentials + Gmail/Drive/GAuth scaffolds
└── ui                # Compose theme, components, screens, NovvyApp nav
```

**Google credentials** — do NOT commit real keys. Fill them in
`android/local.properties` (git-ignored):
```
GOOGLE_WEB_CLIENT_ID=xxxxxxxx.apps.googleusercontent.com
GOOGLE_API_KEY=your-restricted-api-key
```
These flow through `BuildConfig` into
`android/app/src/main/java/com/novvy/app/google/GoogleCredentials.kt`.
See `android/local.properties.example`.

### Build the APK

```bash
./scripts/build_apk.sh          # local build -> dist/novvy.apk
```

Or let **GitHub Actions** build it: push to `main` (or run the
**Build Novvy APK** workflow) and download the `novvy-apk` artifact.

The debug APK is committed at [`dist/novvy.apk`](dist/novvy.apk).

### Install

```bash
adb install -r dist/novvy.apk
```

## Architecture

1. **Plan** — the `CoordinatorEngine` splits the goal into specialist agents.
2. **Assign** — each agent runs with live status (queued → running → complete),
   animating through the task.
3. **Approval gates** — Gmail/Drive steps pause until the user approves.
4. **Result** — the coordinator synthesizes a single result you can save/share.

Providers are pluggable (`AIProvider`): the offline `MockProvider` keeps the demo
fully functional; `OpenAICompatibleProvider` covers OpenAI, Groq, OpenRouter and
Ollama. API keys are stored encrypted with Android Keystore.

## License

MIT