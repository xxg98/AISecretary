#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PYTHON_DIR="$ROOT_DIR/backend-service/python-service"
IMAGE_NAME="${PYTHON_IMAGE_NAME:-kai-lei-python-service:latest}"

cd "$PYTHON_DIR"
docker build -t "$IMAGE_NAME" .
