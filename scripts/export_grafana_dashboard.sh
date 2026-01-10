#!/usr/bin/env bash
set -euo pipefail

# Экспортирует дашборд Grafana по UID в JSON.

UID="${1:-antifraud-overview}"
OUT="${2:-dashboards/grafana_export.json}"

GRAFANA_URL="${GRAFANA_URL:-http://localhost:3000}"
USER="${GRAFANA_USER:-admin}"
PASS="${GRAFANA_PASS:-admin}"

mkdir -p "$(dirname "$OUT")"

curl -s -u "$USER:$PASS" "$GRAFANA_URL/api/dashboards/uid/$UID"   | jq '.dashboard' > "$OUT"

echo "OK: exported to $OUT"
