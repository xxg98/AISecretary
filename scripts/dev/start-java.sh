#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
JAVA_DIR="$ROOT_DIR/backend-service/java-service"

cd "$JAVA_DIR"
./mvnw -q -pl kai-lei-server -am spring-boot:run
