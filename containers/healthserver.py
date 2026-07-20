#!/usr/bin/env python3
"""Lightweight HTTP health endpoint for CardDemo batch/service containers.

Runs as a sidecar thread started by entrypoint.sh. It reflects the run status
that the entrypoint writes to STATUS_FILE, exposing container-orchestrator
liveness/readiness probes without pulling in a web framework.

Endpoints:
  GET /health   Liveness.  200 while starting/running/succeeded; 503 on failure.
  GET /ready    Readiness. 200 once the COBOL program is running or done; else 503.
  GET /         JSON status document ({"program", "status", "healthy", "ready"}).

Status values (written by entrypoint.sh): starting, running, succeeded,
failed:<exit_code>.
"""
import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

STATUS_FILE = os.environ.get("STATUS_FILE", "/tmp/carddemo_status")
PORT = int(os.environ.get("HEALTH_PORT", "8080"))
PROGRAM = os.environ.get("PROGRAM", "unknown")


def read_status() -> str:
    try:
        with open(STATUS_FILE, "r", encoding="utf-8") as fh:
            return fh.read().strip() or "starting"
    except FileNotFoundError:
        return "starting"


def is_healthy(status: str) -> bool:
    # A failed run is unhealthy so the orchestrator can detect it.
    return not status.startswith("failed")


def is_ready(status: str) -> bool:
    # Ready once the program has actually started executing.
    return status in ("running", "succeeded") or status.startswith("failed")


class Handler(BaseHTTPRequestHandler):
    def _respond(self, code: int, payload: dict) -> None:
        body = json.dumps(payload).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:  # noqa: N802 (http.server API)
        status = read_status()
        payload = {
            "program": PROGRAM,
            "status": status,
            "healthy": is_healthy(status),
            "ready": is_ready(status),
        }
        if self.path in ("/health", "/healthz", "/livez"):
            self._respond(200 if is_healthy(status) else 503, payload)
        elif self.path in ("/ready", "/readyz"):
            self._respond(200 if is_ready(status) else 503, payload)
        else:
            self._respond(200, payload)

    def log_message(self, *_args) -> None:  # silence per-request logging
        return


def main() -> None:
    server = ThreadingHTTPServer(("0.0.0.0", PORT), Handler)
    server.serve_forever()


if __name__ == "__main__":
    main()
