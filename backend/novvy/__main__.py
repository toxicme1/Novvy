"""Command line entry point: python -m novvy "your goal"."""

from __future__ import annotations

import argparse
import json
import sys

from .models import RunRequest
from .workspace import run_workspace


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(prog="novvy", description="Novvy multi-agent workspace")
    parser.add_argument("goal", nargs="?", help="High-level goal to run")
    parser.add_argument("--provider", default=None, help="mock|openai|anthropic|ollama")
    parser.add_argument("--model", default=None, help="Model name override")
    parser.add_argument("--json", action="store_true", help="Emit full JSON response")
    parser.add_argument(
        "--serve", action="store_true", help="Run the HTTP API server instead"
    )
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=8000)
    args = parser.parse_args(argv)

    if args.serve:
        import uvicorn

        uvicorn.run("novvy.main:app", host=args.host, port=args.port)
        return 0

    if not args.goal:
        parser.print_help()
        return 2

    req = RunRequest(goal=args.goal, provider=args.provider, model=args.model)
    response = run_workspace(req)
    if args.json:
        print(response.model_dump_json(indent=2))
    else:
        print(f"[{response.provider}] {response.goal}\n")
        for agent in response.agents:
            print(f"  {agent.name}: {agent.result}")
        print(f"\n{response.result}")
    return 0


if __name__ == "__main__":
    sys.exit(main())