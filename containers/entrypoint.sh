#!/usr/bin/env bash
#
# CardDemo batch/service container entrypoint.
#
# Responsibilities:
#   1. Start the lightweight HTTP health server (liveness/readiness probes).
#   2. Map the program's mainframe DD/ASSIGN names to files on the mounted data
#      volume, using containers/ddmap/<PROGRAM>.env. GnuCOBOL resolves an
#      `ASSIGN TO <DD>` to the file named by an environment variable of the same
#      name, so exporting DD=<path> reproduces the JCL DD-to-DSN binding while
#      keeping the AWS.M2.CARDDEMO.* dataset names intact.
#   3. Run the compiled COBOL program and report its exit status to the health
#      endpoint.
#
# This script is program-agnostic: onboarding a new program only requires adding
# containers/ddmap/<PROGRAM>.env (see PR #10..N).
set -euo pipefail

PROGRAM="${PROGRAM:?PROGRAM env var is required}"
DATA_DIR="${DATA_DIR:-/data}"
HEALTH_PORT="${HEALTH_PORT:-8080}"
HEALTH_GRACE="${HEALTH_GRACE:-5}"
MODE="${MODE:-oneshot}"        # oneshot = batch (exit after run); service = stay up
STATUS_FILE="${STATUS_FILE:-/tmp/carddemo_status}"
DDMAP_DIR="${DDMAP_DIR:-/opt/carddemo/containers/ddmap}"
BIN_DIR="${BIN_DIR:-/opt/carddemo/bin}"

export STATUS_FILE HEALTH_PORT

log() { echo "[entrypoint] $*" >&2; }
set_status() { echo "$1" > "${STATUS_FILE}"; }

set_status "starting"

# 1. Health server (background). It reads STATUS_FILE and serves /health, /ready.
python3 /opt/carddemo/containers/healthserver.py &
HEALTH_PID=$!
cleanup() { kill "${HEALTH_PID}" 2>/dev/null || true; }
trap cleanup EXIT

# 2. Map DD names -> data volume paths from the per-program ddmap file.
DDMAP_FILE="${DDMAP_DIR}/${PROGRAM}.env"
if [[ -f "${DDMAP_FILE}" ]]; then
  log "applying DD map ${DDMAP_FILE}"
  while IFS='=' read -r dd dataset; do
    dd="$(echo "${dd}" | tr -d '[:space:]')"
    [[ -z "${dd}" || "${dd}" == \#* ]] && continue
    # Drop any inline comment then strip surrounding whitespace.
    dataset="${dataset%%#*}"
    dataset="$(echo "${dataset}" | tr -d '[:space:]')"
    [[ -z "${dataset}" ]] && continue
    export "${dd}=${DATA_DIR}/${dataset}"
    log "  ${dd} -> ${DATA_DIR}/${dataset}"
  done < "${DDMAP_FILE}"
else
  log "WARNING: no DD map at ${DDMAP_FILE}; relying on inherited environment"
fi

# GnuCOBOL: honor per-DD env var filename mapping and default data path.
export COB_FILE_PATH="${DATA_DIR}"

# 3. Run the program. PROGRAM_PARM maps to the mainframe JCL PARM=; extra args
#    ("$@") are also forwarded.
set_status "running"
log "running ${PROGRAM} (mode=${MODE})"
rc=0
"${BIN_DIR}/${PROGRAM}" ${PROGRAM_PARM:-} "$@" || rc=$?

if [[ "${rc}" -eq 0 ]]; then
  set_status "succeeded"
  log "${PROGRAM} completed successfully"
else
  set_status "failed:${rc}"
  log "${PROGRAM} failed with exit code ${rc}"
fi

# 4. Keep the health endpoint reachable long enough for the orchestrator to
#    scrape the final status. service mode stays up (for ECS/EKS service +
#    autoscaling scaffolding); oneshot/batch exits with the program's code.
if [[ "${MODE}" == "service" ]]; then
  log "service mode: keeping health endpoint up"
  wait "${HEALTH_PID}"
else
  sleep "${HEALTH_GRACE}"
fi

exit "${rc}"
