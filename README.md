<div align="center">
  <img src="frontend/public/favicon.svg" width="96" alt="Online Banking System logo" />

  # Online Banking System

  **A secure, observable banking platform built with microservices, hexagonal architecture, and a tamper-evident financial ledger.**

  [![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=0B1B2B)](https://react.dev/)
  [![gRPC](https://img.shields.io/badge/gRPC-Protobuf-244C5A?style=for-the-badge&logo=google&logoColor=white)](https://grpc.io/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

  ![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-6C63FF?style=flat-square)
  ![Services](https://img.shields.io/badge/Design-Microservices-0F172A?style=flat-square)
  ![Events](https://img.shields.io/badge/Messaging-Event--Driven-22C55E?style=flat-square)
  ![Ledger](https://img.shields.io/badge/Ledger-SHA--256_Hash_Chained-DC2626?style=flat-square)
  ![Observability](https://img.shields.io/badge/Observability-OpenTelemetry-F59E0B?style=flat-square)

  Accounts, transfers, cards, billing, compliance, administration, and a cryptographically linked ledger—built as one complete banking platform.
</div>

---

## Overview

Online Banking System is a full-stack banking platform composed of independently deployable Spring Boot services and a React client.

It covers the complete banking lifecycle:

- Identity, authentication, and account recovery
- Checking, savings, and shared accounts
- Account invitations and membership management
- Policy-controlled money transfers
- Double-entry accounting
- Account-scoped SHA-256 hash chains
- Virtual cards and spending controls
- Bills and recurring subscriptions
- KYC document submission and review
- Administrative and treasury operations
- Event-driven email notifications
- Distributed tracing, metrics, and logs

The platform combines synchronous gRPC communication, Kafka-driven workflows, database-per-service persistence, MinIO document storage, RS256 authentication, and OpenTelemetry instrumentation behind a single reactive HTTP gateway.

## Core capabilities

| | Capability | What it provides |
|---|---|---|
| ⛓️ | **Account-scoped hash chains** | SHA-256-linked postings, independent chain heads, complete verification, and exact break detection |
| 📒 | **Double-entry ledger** | Balanced debit and credit postings, authoritative balances, auditable history, and idempotent writes |
| 👥 | **Shared accounts** | Checking and savings accounts, invitations, owners, members, display names, and freezing |
| 💸 | **Policy-controlled transfers** | Fees, receipts, KYC checks, AML rules, savings limits, idempotency, and history |
| 💳 | **Virtual cards** | Card issuance, freeze/unfreeze controls, spending limits, merchant charges, and charge history |
| 🔁 | **Billing automation** | One-time bill payments, recurring subscriptions, scheduled charging, cancellation, and retries |
| 🪪 | **KYC and compliance** | Document slots, presigned uploads, MinIO storage, submission, approval, and rejection |
| 🛡️ | **Identity and access** | Verification, RS256 JWTs, refresh rotation, password recovery, and role-based authorization |
| 🏦 | **Administration** | User blocking, account freezing, KYC review, treasury minting, and revenue reporting |
| 📈 | **Operational visibility** | Health endpoints, distributed traces, metrics, logs, and service-level monitoring |

---

## Tamper-evident ledger

The ledger does more than persist transactions.

Every financial movement creates balanced debit and credit postings. Each posting is also appended to an independent cryptographic chain belonging to the affected account.

```text
ACCOUNT A

┌────────────────────┐     ┌────────────────────┐     ┌────────────────────┐
│ Sequence 1         │     │ Sequence 2         │     │ Sequence 3         │
│ prev: GENESIS      │────►│ prev: hash(1)      │────►│ prev: hash(2)      │
│ hash: 7f4a...      │     │ hash: a91c...      │     │ hash: e203...      │
└────────────────────┘     └────────────────────┘     └────────────────────┘
                                                               │
                                                               ▼
                                                     Chain head: e203...
```

### What each hash protects

Every item hash is calculated using SHA-256 over a canonical representation containing:

```text
previous hash
  + account ID
  + sequence number
  + ledger item ID
  + ledger entry ID
  + creation timestamp
  + signed posting amount
  + counterparty account
  + idempotency key
  + source account
  + destination account
  + transfer amount
  + description
```

This means changing an old amount, timestamp, account, counterparty, description, identifier, or previous link produces a different hash.

Once an old item changes, the next item’s `prevHash` no longer matches, revealing exactly where ledger integrity was lost.

### Chain verification

The verification process:

1. Loads all account postings in sequence order.
2. Detects missing, duplicated, or reordered sequence numbers.
3. Rebuilds the canonical payload for every item.
4. Recomputes each SHA-256 hash.
5. Validates every `prevHash → itemHash` relationship.
6. Returns the first invalid sequence when verification fails.

A separately locked chain-head record tracks the latest sequence and hash while new postings are appended.

The React application includes an animated ledger visualizer that walks through the chain block by block, compares adjacent links, calls server-side verification, and highlights the first broken sequence.

> This is a **tamper-evident ledger**, not a blockchain. It uses deterministic account-level hash chaining to reveal unauthorized historical changes without introducing distributed consensus.

---

## Architecture

The React client communicates exclusively with the reactive HTTP gateway.

The gateway authenticates requests, applies authorization rules, validates input, and translates external HTTP operations into typed internal gRPC calls.

Each service owns its domain, persistence, schema migrations, and application use cases.

```text
React Client
     │
     │ HTTP / JSON
     ▼
┌─────────────────────┐
│    API Gateway      │
│ Security · OpenAPI  │
│ Validation · Errors │
└──────────┬──────────┘
           │ gRPC / Protobuf
           ▼
┌─────────────────────────────────────────────────────┐
│ Auth │ Accounts │ KYC │ Transfers │ Ledger          │
│ Cards │ Billing │ Notifications                     │
└─────────────────────────────────────────────────────┘
           │
           ├── PostgreSQL databases
           ├── Kafka events
           ├── MinIO documents
           ├── SMTP notifications
           └── OpenTelemetry
```

### Service communication

```text
Gateway ─────► Auth
        ├────► Accounts ─────► Auth
        ├────► KYC ──────────► Auth
        ├────► Transfers ────► Accounts
        │                ├───► KYC
        │                └───► Ledger
        ├────► Cards ─────────► Transfers
        │                └───► KYC
        └────► Billing ───────► Transfers
                         └────► KYC

Auth / Accounts / KYC ──Kafka──► Notifications
```

---

## Hexagonal architecture

Every core backend service follows **hexagonal architecture**, also known as ports and adapters.

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
                    │        Domain model         │
                    │ rules • invariants • state  │
                    └──────────────┬──────────────┘
                                   ▼
                    ┌─────────────────────────────┐
                    │       Outbound ports        │
                    └───────┬─────────┬───────────┘
                            ▼         ▼
                      PostgreSQL    gRPC / Kafka
```

The domain and application layers depend on interfaces rather than infrastructure.

JPA, Kafka, gRPC, MinIO, and external service integrations are implemented as adapters outside the business core. This keeps financial rules isolated from framework-specific code and makes infrastructure replaceable.

---

## Engineering highlights

| | Design choice | Why it matters |
|---|---|---|
| ⛓️ | **Account-level hash chaining** | Recomputed SHA-256 chains expose historical ledger changes and identify the first invalid sequence |
| 📒 | **Double-entry accounting** | Every movement produces balanced debit and credit postings |
| 🧩 | **Ports and adapters** | JPA, Kafka, gRPC, and storage integrations remain outside the business core |
| 📜 | **Contract-first gRPC** | Protobuf contracts provide typed communication between services |
| 🗃️ | **Database per service** | Services independently own and migrate their schemas through Flyway |
| ⚡ | **Event-driven notifications** | Kafka separates email delivery from authentication, account, and KYC workflows |
| 🧭 | **Transfer orchestration** | One flow coordinates KYC, AML, ownership, balances, fees, savings rules, and ledger posting |
| ♻️ | **Idempotent operations** | Repeated requests cannot duplicate accounts, transfers, charges, bills, or ledger entries |
| 🔐 | **Separated signing keys** | Auth signs tokens privately while the gateway receives only the public verification key |
| 🔎 | **End-to-end observability** | OpenTelemetry correlates traces, metrics, and logs across service boundaries |
| ⏱️ | **Scheduled financial jobs** | Subscription charging, retry processing, and savings-interest payouts run automatically |
| 🧱 | **Centralized API boundary** | The gateway owns authentication, authorization, validation, CORS, OpenAPI, and error mapping |

---

## Transfer lifecycle

```text
1. Gateway verifies the RS256 access token
                     │
2. Transfers service validates the request
                     │
3. KYC approval and AML policies are checked
                     │
4. Accounts service verifies ownership and debit eligibility
                     │
5. Fees and savings withdrawal limits are calculated
                     │
6. Ledger service records an idempotent double entry
                     │
7. Both account postings extend independent SHA-256 chains
                     │
8. The final result becomes queryable and observable
```

The resulting operation is authenticated, authorized, policy-checked, replay-safe, double-entered, hash-linked, auditable, and observable.

---

## Backend services

| Service | Responsibility |
|---|---|
| `gateway-service` | Reactive HTTP API, JWT verification, authorization, OpenAPI, validation, and HTTP-to-gRPC mapping |
| `auth-service` | Registration, verification, login, refresh rotation, password recovery, and user administration |
| `accounts-service` | Checking and savings accounts, shared ownership, invitations, names, debit checks, and freezing |
| `kyc-service` | KYC applications, document storage, submission, status tracking, and administrator review |
| `transfers-service` | Transfer orchestration, AML rules, fees, savings limits, treasury minting, and interest payouts |
| `ledger-service` | Double-entry records, balances, account histories, hash-chain creation, and integrity verification |
| `cards-service` | Virtual cards, freezing, spending limits, merchant charges, and charge history |
| `billing-service` | Bill payments, subscriptions, scheduled charges, cancellation, and retry handling |
| `notifications-service` | Kafka-driven email delivery through SMTP |

---

## Trust by design

- **RS256 authentication** separates token signing from token verification.
- **Refresh-token rotation** limits long-lived session exposure.
- **Role-based access control** separates customer and administrator operations.
- **KYC gates** protect transfers, card charges, and billing operations.
- **AML rules** enforce transfer amount and velocity limits.
- **Account ownership checks** prevent unauthorized debits.
- **Idempotency keys** prevent duplicate financial operations.
- **Double-entry accounting** records both sides of every movement.
- **Account-scoped SHA-256 chains** bind every posting to its payload and predecessor.
- **Chain verification** detects changed data, reordered entries, sequence gaps, and broken links.
- **Database-per-service isolation** establishes clear data ownership.
- **Secrets and generated keys remain outside Git** through ignored files and environment configuration.

---

## Technology stack

### Backend

- Java 21
- Spring Boot 4
- Spring gRPC
- Protocol Buffers
- Spring Security
- OAuth2 Resource Server
- Spring Data JPA
- Flyway
- PostgreSQL
- Apache Kafka
- MinIO
- OpenTelemetry
- Spring Boot Actuator

### Frontend

- React 19
- React Router
- Vite 6
- Tailwind CSS 4
- Axios
- Motion
- Lucide icons

### Infrastructure

- Docker Compose
- PostgreSQL 15
- Apache Kafka
- MinIO
- Grafana OpenTelemetry LGTM
- MailHog

---

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
├── deploy/jars/          # generated locally; ignored by Git
├── secrets/              # generated locally; ignored by Git
├── otel/                 # generated locally; ignored by Git
├── .env.example
└── README.md
```

---

## Local setup

### Prerequisites

- Java 21
- Node.js and npm
- Docker Desktop with Docker Compose
- OpenSSL
- MinIO Client (`mc`)

Run the commands from the repository root.

### 1. Prepare local configuration

```powershell
Copy-Item .env.example .env
New-Item -ItemType Directory -Force secrets, deploy/jars, otel
```

### 2. Generate JWT keys

```powershell
openssl genpkey `
  -algorithm RSA `
  -pkeyopt rsa_keygen_bits:2048 `
  -out secrets/auth-private.pem

openssl rsa `
  -pubout `
  -in secrets/auth-private.pem `
  -out secrets/auth-public.pem
```

### 3. Start the infrastructure

```powershell
docker compose `
  --project-directory . `
  -f examples/docker/docker-compose.infrastructure.example.yml `
  up -d
```

This starts PostgreSQL, Kafka, MinIO, and the OpenTelemetry LGTM stack.

### 4. Prepare MinIO

```powershell
mc alias set banking http://localhost:9000 minioadmin change-me
mc mb --ignore-existing banking/kyc-documents
mc cors set banking/kyc-documents examples/minio/cors.xml
```

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

### 6. Download the OpenTelemetry agent

```powershell
Invoke-WebRequest `
  "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar" `
  -OutFile "otel/opentelemetry-javaagent.jar"
```

### 7. Start the backend

```powershell
docker compose `
  --project-directory . `
  -f examples/docker/docker-compose.apps.example.yml `
  up -d
```

### 8. Start the frontend

```powershell
Set-Location frontend
Copy-Item .env.example .env
npm install
npm run dev
```

### Local endpoints

| Application | URL |
|---|---|
| Frontend | `http://localhost:5173` |
| Gateway API | `http://localhost:8080/api` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Grafana | `http://localhost:3000` |
| MailHog | `http://localhost:8025` |
| MinIO console | `http://localhost:9001` |

---

## Development

Run a backend service’s tests:

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

---

## Project report

For a complete view of the project—including system analysis, diagrams, and additional services—refer to the accompanying [project report (PDF)](docs/project-report.pdf).

---

## Security notice

The Compose files under `examples/` are development templates.

Production deployments must use:

- Unique credentials
- TLS for every public endpoint
- Protected network boundaries
- Managed secrets
- Restricted object-storage access
- Database backups
- Environment-specific configuration
- Firewall and service exposure rules
