#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
JAVA_DIR="$ROOT_DIR/backend-service/java-service"
PYTHON_DIR="$ROOT_DIR/backend-service/python-service"

echo "Checking Java service..."
(cd "$JAVA_DIR" && ./mvnw -q -DskipTests compile && ./mvnw -q -pl kai-lei-server -am -DskipTests package)

echo "Checking Python service syntax..."
(cd "$PYTHON_DIR" && python3 -m compileall app run.py)

echo "Checking collector examples..."
(cd "$ROOT_DIR" && python3 -m py_compile collectors/active-collectors/finance/collector.py)

echo "All checks passed."
