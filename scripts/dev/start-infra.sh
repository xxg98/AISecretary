#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE_DIR="$ROOT_DIR/infrastructure/docker-compose"
ENV_FILE="$COMPOSE_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$COMPOSE_DIR/.env.example" "$ENV_FILE"
  echo "Created $ENV_FILE from .env.example"
fi

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_DIR/docker-compose.infra.yml" up -d
