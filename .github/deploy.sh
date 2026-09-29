#!/usr/bin/env bash
set -euo pipefail

# Usage: upload.sh <SERVER_URL> <AUTH_TOKEN> <APK_PATH> <JSON_PATH>
SERVER_URL="$1"
AUTH_TOKEN="$2"
APK_PATH="$3"
JSON_PATH="$4"

upload_file() {
  local FILE_PATH="$1"
  local UPLOAD_NAME="$2"

  echo "Uploading ${FILE_PATH} as ${UPLOAD_NAME}..."

  HTTP_STATUS=$(curl -s -S -o /tmp/upload_response.txt -w "%{http_code}" \
    -X POST "${SERVER_URL}?file=${UPLOAD_NAME}" \
    -H "Authorization: Basic ${AUTH_TOKEN}" \
    -H "Content-Type: application/octet-stream" \
    -H "Content-Disposition: attachment; filename=\"${UPLOAD_NAME}\"" \
    --data-binary @"${FILE_PATH}")

  if [ "$HTTP_STATUS" -eq 200 ]; then
    echo "Upload successful!"
  else
    echo "Upload failed with HTTP ${HTTP_STATUS}" >&2
    cat /tmp/upload_response.txt >&2
    exit 1
  fi
}

upload_file "$APK_PATH" "app-release.apk"
upload_file "$JSON_PATH" "version.json"
