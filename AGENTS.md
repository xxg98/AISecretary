# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

凯磊 AI 秘书 (KaiLei AI Secretary) is an intelligent secretary system for personal and team collaboration. Each user has an AI secretary that communicates with other AI secretaries via natural language, turning requests into structured, trackable workflows (task distribution, file transfer, reminders, progress tracking).

## Development Commands

### Infrastructure (Docker)

```bash
./scripts/dev/start-infra.sh    # Start MySQL, Redis, RabbitMQ, MinIO, Nacos
./scripts/dev/stop-infra.sh     # Stop all infrastructure containers
```

### Java Backend

```bash
cd backend-service/java-service
./mvnw -pl kai-lei-server -am spring-boot:run   # Start Java service (dev, port 8083)
./mvnw -DskipTests compile                       # Compile only
./mvnw -DskipTests package                       # Package (skip tests)
./mvnw test                                       # Run all tests
./mvnw -pl kai-lei-server -am -DskipTests package # Package server module only
```

Java 21 required. Maven wrapper (`mvnw`) included — no global Maven install needed.

### Python Backend

```bash
cd backend-service/python-service
python run.py                    # Start Python service (default port 8090)
pytest                           # Run tests
pip install -r requirements.txt  # Install dependencies (venv recommended)
```

The Python service depends on the infrastructure services (Redis, RabbitMQ) being up. Copy `.env.example` to `.env` before first run.

### PC Frontend (Electron + Vue 3)

```bash
cd frontend-pc
npm run dev-frontend  # Start Vue dev server (Vite, port 8080)
npm run dev-electron  # Start Electron main process
npm run dev           # Start both frontend + electron
npm run build         # Production build (frontend + electron + encrypt)
```

Uses [electron-egg](https://github.com/dromara/electron-egg) (ee-core v4) for the Electron framework. The renderer is Vue 3 + Vue Router + Vite, IPC bridge is in `electron/preload/bridge.js`.

### Full Check

```bash
./scripts/dev/check-all.sh  # Compile Java, check Python syntax, check collectors
```

## Architecture

```
User (natural language)
  → AI Secretary (intent parsing, task orchestration)
    → Inter-Secretary Communication (MQ/HTTP)
      → Recipient's AI Secretary (task creation, reminders)
        → Recipient (processes task)
```

### Module Map

| Module | Purpose |
|---|---|
| `backend-service/java-service` | Spring Boot 3.5.9 multi-module: core business, auth, task flow, file management |
| `backend-service/python-service` | FastAPI service: voice, doc parsing, OCR, AI toolchain, long-running async tasks via MQ |
| `frontend-pc` | Electron desktop app (Vue 3 + Vite renderer) for office scenarios |
| `frontend-app` | Mobile/multi-platform via uniapp (Android, iOS, H5) |
| `collectors` | MCP servers, active collectors, and third-party adapters (finance, ERP, CRM, OA) |
| `infrastructure` | Docker Compose configs for local dev middleware |

### Java Module Dependency Graph

```
kai-lei-common          (shared utils, base classes)
  ↑
kai-lei-framework       (framework: security, config, interceptors)
  ↑
kai-lei-system          (system domain: user, org, permission)
  ↑
kai-lei-business        (business domain: tasks, files, messaging)
  ↑
kai-lei-server          (startup entry point, config files, depends on kai-lei-business)
```

The server module (`kai-lei-server`) is the only runnable module. It pulls in all others transitively via `kai-lei-business`.

### Key Java Tech Stack

- **Spring Boot 3.5.9** + **Spring Cloud 2025.0.0** + **Spring Cloud Alibaba 2025.1.0.0**
- **Spring AI 1.1.2** — LLM integration (OpenAI-compatible API)
- **Nacos v3.0.0** — service discovery & config center (disabled in dev by default)
- **MyBatis-Plus 3.5.15** + Dynamic Datasource — ORM with read/write splitting support
- **Sa-Token 1.45.0** — token-based auth (UUID tokens, 30-day expiry, no cookies)
- **AutoTable** — auto DDL from entities (update mode in dev, auto-drop-column enabled)
- **X-File-Storage** — file storage abstraction (local, MinIO, etc.)
- **XXL-JOB 3.4.0** — distributed job scheduling
- **Knife4j** — Swagger API docs at `/swagger-ui.html`

### Python Service Architecture

```
app/
  main.py           — FastAPI app factory + lifespan
  api/v1/           — API route handlers
  core/config.py    — Pydantic Settings (env-driven, PYTHON_* and MQ_* prefixes)
  core/logging.py   — Loguru configuration
  mq/               — Message broker abstraction (RabbitMQ, Redis)
    factory.py      — Broker factory
    rabbitmq_broker.py
    logging_broker.py
  schemas/          — Pydantic request/response models
  services/         — Business logic
  tasks/            — Background/async task handlers
  integrations/     — Third-party API clients
```

Java ↔ Python communication: Python calls Java via HTTP (`JAVA_SERVICE_BASE_URL`), or they communicate asynchronously via MQ (RabbitMQ exchange `kai.lei.ai.secretary`).

### Infrastructure Services (docker-compose.infra.yml)

| Service | Image | Port(s) | Notes |
|---|---|---|---|
| MySQL | 8.4 | 3306 | utf8mb4, init scripts in `infrastructure/mysql/init/` |
| Redis | 7.4-alpine | 6379 | AOF persistence |
| RabbitMQ | 4.0-management | 5672, 15672 | Management UI on 15672 |
| MinIO | latest | 9000, 9001 | Console on 9001 |
| Nacos | v3.0.0 | 8848, 9848 | Standalone mode, auth disabled |

### Collectors

External data adapters that isolate third-party integration logic from the main services. Organized as:
- `collectors/mcp-servers/` — MCP protocol servers for Agent tool discovery
- `collectors/active-collectors/` — Pull-based or webhook-driven collectors (e.g., `finance/` for ERP integration)
- Communication with main services via HTTP, MQ, or MCP protocol

### PC Frontend Structure

```
frontend-pc/
  electron/           — Main process (ee-core framework)
    main.js           — Entry
    config/           — Environment configs (default, local, prod)
    controller/       — IPC controllers
    service/          — Business services
    preload/          — Preload scripts (bridge, lifecycle)
  frontend/           — Renderer process
    src/
      api/            — API client
      components/     — Vue components
      router/         — Vue Router config
      utils/          — IPC renderer helpers
```

### Configuration Hierarchy (Java)

1. `application.yml` — base config, imports from Nacos when enabled
2. `application-{profile}.yml` — profile-specific (dev, docker, prod)
3. Nacos remote config (disabled in dev, enabled in docker/prod)
4. Environment variables override via `${VAR:default}` placeholders

Dev profile (`application-dev.yml`): disables Nacos, sets dev DB credentials, enables Swagger/Knife4j, enables AutoTable update mode.
