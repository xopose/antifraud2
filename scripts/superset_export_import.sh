#!/usr/bin/env bash
set -euo pipefail

# Утилита для экспорта/импорта Superset через CLI внутри контейнера.

CMD="${1:-export}"
FILE="${2:-dashboards/superset_export.zip}"

if [[ "$CMD" == "export" ]]; then
  docker exec -it antifraud-superset superset export-dashboards --dashboard-ids 1 --path /tmp/superset_export.zip || true
  docker cp antifraud-superset:/tmp/superset_export.zip "$FILE" || true
  echo "OK: exported to $FILE (если в Superset есть dashboard id=1)"
elif [[ "$CMD" == "import" ]]; then
  docker cp "$FILE" antifraud-superset:/tmp/superset_import.zip
  docker exec -it antifraud-superset superset import-dashboards --path /tmp/superset_import.zip
  echo "OK: imported from $FILE"
else
  echo "Usage: $0 export|import <zip>"
  exit 1
fi
