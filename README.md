<div align="center">
  <img src="frontend/public/favicon.svg" width="92" alt="Online Banking System logo" />

  # Online Banking System

  **A production-ready banking platform built as a secure, observable microservices system.**

  [![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=0B1B2B)](https://react.dev/)
  [![gRPC](https://img.shields.io/badge/gRPC-Protobuf-244C5A?style=for-the-badge&logo=google&logoColor=white)](https://grpc.io/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

  ![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-6C63FF?style=flat-square)
  ![Services](https://img.shields.io/badge/Design-Microservices-0F172A?style=flat-square)
  ![Events](https://img.shields.io/badge/Messaging-Event--Driven-22C55E?style=flat-square)
  ![Observability](https://img.shields.io/badge/Observability-OpenTelemetry-F59E0B?style=flat-square)

  A complete banking backend and web application that student projects can reuse as a realistic payment and financial-services layer.
</div>

---

## Why this project exists

Student marketplaces, booking systems, stores, and subscription applications often stop at a simulated checkout. This project supplies the missing financial layer: real account balances, transfers, cards, billing, compliance, receipts, and an auditable ledger behind one HTTP gateway.

It is designed as both a working banking application and a reusable foundation for projects that need production-style payment behavior.

## Highlights

| | Capability | What it provides |
|---|---|---|
| 👥 | **Shared accounts** | Checking and savings accounts, invitations, ownership, members, display names, and account freezing |
| 💸 | **Safe transfers** | Fees, receipts, KYC checks, AML limits, savings rules, idempotency, and transfer history |
| 🔗 | **Tamper-evident ledger** | Double-entry records, account-scoped hash chains, balance calculation, and integrity verification |
| 💳 | **Virtual cards** | Card issuance, freeze/unfreeze controls, spending limits, and merchant charges |
| 🔁 | **Billing automation** | One-time bill payment, subscriptions, scheduled charging, and retry state |
| 🪪 | **KYC workflow** | Document upload, MinIO-backed storage, submission, and administrator review |
| 🛡️ | **Identity and access** | Email verification, RS256 JWTs, refresh rotation, password reset, and role-based authorization |
| 📈 | **Operational visibility** | Actuator health metrics and OpenTelemetry traces, metrics, and logs |

## Architecture

<div align="center">
  <img src="docs/banking-system-architecture.svg" width="100%" alt="Banking system architecture" />
</div>

The frontend communicates only with the reactive HTTP gateway. The gateway validates access tokens and translates HTTP requests into internal gRPC calls. Each domain service owns its data and exposes focused use cases through ports and adapters.

```text
React client
    │ HTTP
    ▼
API Gateway
    │ gRPC
    ├── Authentication
    ├── Accounts
    ├── KYC
    ├── Transfers ──► Ledger
    ├── Cards ──────► Transfers
    └── Billing ────► Transfers

Kafka ──► Notifications
PostgreSQL │ MinIO │ OpenTelemetry
```

### Hexagonal architecture, service by service

This is not a CRUD-only microservices demo. Every core backend service is organized around **hexagonal architecture (ports and adapters)**, keeping business rules independent from gRPC, databases, Kafka, and external services.

```text
                    ┌─────────────────────────────┐
 gRPC request ────► │       Inbound adapter       │
                    └──────────────┬──────────────┘
                                   ▼
                    ┌─────────────────────────────┐
                    │   Application use cases     │
                    │  commands • queries • DTOs  │
                    └──────────────┬──────────────┘
                                   ▼
                    ┌─────────────────────────────┐
                    │      Domain model           │
                    │ rules • invariants • state  │
                    └──────────────┬──────────────┘
                                   ▼
                    ┌─────────────────────────────┐
                    │       Outbound ports        │
                    └───────┬─────────┬───────────┘
                            ▼         ▼
                      PostgreSQL    gRPC / Kafka
```

The domain and use-case layers depend on interfaces, while adapters handle framework-specific details. That makes the financial rules easier to test, replace, and evolve without coupling them to infrastructure.

### Engineering highlights

| | Design choice | Why it matters |
|---|---|---|
| 🧩 | **Ports and adapters** | JPA, Kafka, gRPC, and storage integrations sit outside the business core |
| 📜 | **Contract-first gRPC** | Protobuf contracts define typed communication between services |
| 🗃️ | **Database per service** | Each service owns its schema and evolves it through Flyway migrations |
| ⚡ | **Event-driven notifications** | Kafka separates email delivery from account, authentication, and KYC workflows |
| 🧭 | **Transfer orchestration** | One flow coordinates identity, KYC, AML, account rules, fees, and ledger posting |
| ♻️ | **Idempotent operations** | Repeated requests cannot create duplicate accounts, transfers, charges, or ledger entries |
| 🔐 | **Separated signing keys** | The auth service signs tokens; the gateway only receives the public verification key |
| 🔎 | **End-to-end observability** | OpenTelemetry connects traces, metrics, and logs across service boundaries |
| ⏱️ | **Resilient scheduled jobs** | Subscription retries and savings-interest payouts run as controlled background workflows |
| 🧱 | **Centralized API boundary** | The reactive gateway owns authentication, authorization, validation, CORS, OpenAPI, and error mapping |

### What happens during a transfer?

```text
1. Gateway verifies the RS256 access token
                     │
2. Transfers service checks KYC and AML rules
                     │
3. Accounts service validates ownership and debit rules
                     │
4. Fee and savings limits are calculated
                     │
5. Ledger service records an idempotent double entry
                     │
6. Hash-linked entries preserve evidence of the result
```

The result is a financial operation that is authenticated, policy-checked, replay-safe, auditable, and observable across services.

### Backend services

| Service | Responsibility |
|---|---|
| `gateway-service` | Reactive HTTP API, JWT validation, authorization, OpenAPI, and HTTP-to-gRPC mapping |
| `auth-service` | Registration, verification, login, refresh tokens, password recovery, and user administration |
| `accounts-service` | Accounts, memberships, invitations, names, debit checks, and account state |
| `kyc-service` | KYC applications, document storage, submission, and review |
| `transfers-service` | Transfer orchestration, AML rules, fees, savings limits, minting, and interest payouts |
| `ledger-service` | Double-entry records, balances, entry history, and chained-hash verification |
| `cards-service` | Virtual cards, card state, spending limits, and charges |
| `billing-service` | Bills, subscriptions, scheduled charges, and retries |
| `notifications-service` | Kafka-driven email delivery |

## Trust by design

- **RS256 authentication** keeps token signing and verification keys separate.
- **Role-based access control** protects customer and administrator operations.
- **KYC gates** are enforced before transfers, card charges, and billing operations.
- **AML controls** enforce amount and velocity rules.
- **Idempotency keys** prevent duplicate financial operations.
- **Double-entry accounting** records both sides of every movement.
- **Hash-linked ledger entries** reveal changes to previously recorded transactions.
- **Database-per-service isolation** limits coupling and data ownership ambiguity.
- **Secrets and generated keys stay outside Git** through environment variables and ignored local files.

## Technology

**Backend**

- Java 21 and Spring Boot 4
- Spring gRPC and Protocol Buffers
- Spring Security and OAuth2 Resource Server
- Spring Data JPA and Flyway
- Kafka, PostgreSQL, MinIO, and SMTP
- OpenTelemetry and Spring Boot Actuator

**Frontend**

- React 19 and React Router
- Vite 6
- Tailwind CSS 4
- Axios, Motion, and Lucide icons

## Repository structure

```text
.
├── backend/
│   ├── accounts-service/
│   ├── auth-service/
│   ├── billing-service/
│   ├── cards-service/
│   ├── gateway-service/
│   ├── kyc-service/
│   ├── ledger-service/
│   ├── notifications-service/
│   ├── transfers-service/
│   └── postgres/init/
├── frontend/
├── docs/
│   └── project-report.pdf
├── examples/
│   ├── docker/
│   └── minio/
├── deploy/jars/          # created locally; gitignored
├── secrets/              # created locally; gitignored
├── otel/                 # created locally; agent JAR is gitignored
├── .env.example
└── README.md
```

## Local setup

### Prerequisites

- Java 21
- Node.js and npm
- Docker Desktop with Docker Compose
- OpenSSL
- MinIO Client (`mc`) for KYC bucket setup

Run every command below from the repository root—the directory containing `backend/`, `frontend/`, and `examples/`.

### 1. Prepare local configuration

```powershell
Copy-Item .env.example .env
New-Item -ItemType Directory -Force secrets, deploy/jars, otel
```

The root environment template contains local CORS, Kafka, and AML settings. Credentials in the example Compose files are development defaults and must be replaced before using the system outside a local machine.

### 2. Generate JWT keys

```powershell
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/auth-private.pem
openssl rsa -pubout -in secrets/auth-private.pem -out secrets/auth-public.pem
```

### 3. Start infrastructure

```powershell
docker compose --project-directory . -f examples/docker/docker-compose.infrastructure.example.yml up -d
```

This starts PostgreSQL, Kafka, MinIO, and the OpenTelemetry LGTM stack. Leave it running for the remaining steps.

### 4. Prepare MinIO for KYC uploads

```powershell
mc alias set banking http://localhost:9000 minioadmin change-me
mc mb --ignore-existing banking/kyc-documents
mc cors set banking/kyc-documents examples/minio/cors.xml
```

If you override the MinIO credentials in `.env`, use those values here too.

### 5. Build the backend

```powershell
$ErrorActionPreference = "Stop"
$services = @(
  "auth-service",
  "accounts-service",
  "ledger-service",
  "transfers-service",
  "kyc-service",
  "billing-service",
  "cards-service",
  "notifications-service",
  "gateway-service"
)

foreach ($service in $services) {
  Push-Location "backend/$service"
  try {
    .\mvnw.cmd clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
      throw "Build failed for $service"
    }

    $jar = Get-ChildItem "target/*.jar" -File |
      Where-Object { $_.Name -notmatch "\.jar\.original$" } |
      Sort-Object LastWriteTime -Descending |
      Select-Object -First 1

    if (-not $jar) {
      throw "No packaged JAR found for $service"
    }

    Copy-Item $jar.FullName "../../deploy/jars/$service.jar" -Force
  }
  finally {
    Pop-Location
  }
}
```

### 6. Enable OpenTelemetry

```powershell
Invoke-WebRequest `
  "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar" `
  -OutFile "otel/opentelemetry-javaagent.jar"
```

### 7. Start the backend

Before continuing, confirm that the infrastructure stack is running and that the JWT keys, nine service JARs, and OpenTelemetry agent were created by the previous steps.

```powershell
docker compose --project-directory . -f examples/docker/docker-compose.apps.example.yml up -d
```

Local endpoints:

- Gateway API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Grafana: `http://localhost:3000`
- MailHog: `http://localhost:8025`
- MinIO console: `http://localhost:9001`

### 8. Start the frontend

```powershell
Set-Location frontend
Copy-Item .env.example .env
npm install
npm run dev
```

The frontend is available at `http://localhost:5173`.

## Development

Run a backend service's tests:

```powershell
Set-Location backend/accounts-service
.\mvnw.cmd test
```

Build the frontend:

```powershell
Set-Location frontend
npm ci
npm run build
```

API documentation is generated by the gateway and exposed through Swagger UI while the gateway is running.

## Project report

For a complete view of the project—including system analysis, diagrams, and additional services—refer to the accompanying [project report (PDF)](docs/project-report.pdf).

## Important security note

The Compose files under `examples/` are development templates. Do not expose their default services or credentials to the internet. Production deployments must use unique credentials, protected network boundaries, TLS, managed secrets, restricted storage access, and environment-specific configuration.
