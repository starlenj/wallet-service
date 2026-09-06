# Wallet Service

A backend-focused digital wallet and money transfer system built with **Java and Spring Boot**.

The project is designed as a portfolio project for exploring production-oriented backend engineering concepts such as transactional consistency, concurrency control, idempotency, event-driven architecture, observability, and distributed systems.

Rather than focusing only on CRUD operations, the goal is to progressively introduce and solve problems commonly encountered in real-world payment and financial systems.

## Current Status

🚧 **Work in Progress**

Currently implemented:

- User creation
- Wallet creation
- Multiple currency support
- Wallet balance management
- Deposit operation
- Domain-level credit/debit operations
- Transactional wallet-to-wallet transfers
- Idempotent transfer creation with the `Idempotency-Key` header
- Request fingerprint validation for safe idempotency-key reuse
- Concurrent transfer protection with PostgreSQL advisory and pessimistic row locks
- Automatic debit and credit ledger entries for each transfer
- Transfer status tracking
- Transactional outbox records for completed transfers
- Scheduled publication of pending outbox events to Kafka
- Local Kafka and Kafka UI services
- PostgreSQL persistence
- Database migrations with Flyway
- Transfer persistence model
- Ledger persistence model
- Request validation
- Structured API error responses
- Dockerized application, PostgreSQL, Kafka, and Kafka UI environment

Consumer-side event processing, integration testing, and other advanced distributed-system features are currently under development.

---

## Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Spring Validation
- Spring Security
- Spring for Apache Kafka
- Gradle
- Docker / Docker Compose

Planned:

- Redis
- Testcontainers
- OpenTelemetry
- Prometheus
- Grafana
- Kubernetes
- CI/CD
- Load testing

---

## Architecture

The application currently follows a **modular monolith** architecture.

Each business capability is separated into its own module:

```text
src/main/java/com/nasuh/walletservice/
├── common/
│   ├── config/
│   └── exception/
│
├── identity/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
│
├── wallet/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
│
├── transfer/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
│
├── ledger/
│   ├── domain/
│   └── infrastructure/
│
└── outbox/
    ├── application/
    ├── domain/
    └── infrastructure/
```

The intention is to maintain clear module boundaries before introducing distributed infrastructure or extracting services.

### Layer Responsibilities

**API**

HTTP controllers, request models, response models and input validation.

**Application**

Application use cases and transaction orchestration.

**Domain**

Business entities, state and business rules.

**Infrastructure**

Persistence and external infrastructure implementations.

---

## Domain Model

The core domain currently consists of:

```text
User
 └── Wallet
      ├── Balance
      ├── Currency
      └── Status

Transfer
 ├── Source Wallet
 ├── Target Wallet
 ├── Amount
 ├── Currency
 ├── Idempotency Key
 ├── Request Hash
 └── Status

Ledger Entry
 ├── Wallet
 ├── Transfer
 ├── Type (DEBIT / CREDIT)
 └── Amount

Outbox Event
 ├── Aggregate Type / ID
 ├── Event Type
 ├── JSON Payload
 └── Status (PENDING / PUBLISHED)
```

Money is represented using `BigDecimal` rather than floating-point types.

Balance mutations are implemented as domain behavior:

```java
wallet.credit(amount);
wallet.debit(amount);
```

rather than exposing arbitrary balance setters.

Transfers run inside a single database transaction. Each request is serialized by its idempotency key, and both wallet rows are locked in a consistent ID order before their balances are changed. A successful transfer debits the source wallet, credits the target wallet, creates the transfer record, writes matching `DEBIT` and `CREDIT` ledger entries, and persists a `PENDING` outbox event. Any failure rolls back the complete operation.

A scheduled publisher reads pending outbox events in batches of up to 100, publishes their JSON payloads to the `wallet.transfer.completed` Kafka topic, and then marks them as `PUBLISHED`. The transfer ID is used as the Kafka record key, preserving per-transfer ordering at the partition level.

---

## Database

PostgreSQL is used as the primary database.

Schema changes are managed using **Flyway** instead of relying on Hibernate to modify the database schema automatically.

Current migrations:

```text
V1__create_users_and_wallets.sql
V2__create_transfer_and_ledger_entries.sql
V3__add_idem_key_to_transfers.sql
V4__add_request_hash_to_transfers.sql
V5__create_outbox_events.sql
```

Hibernate is configured with schema validation:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

This allows Flyway to remain responsible for database schema evolution while Hibernate verifies that the entity mappings match the database.

---

## Running the Project

### Requirements

- Docker
- Docker Compose

### Docker Compose (Recommended)

Build and start PostgreSQL, Kafka, Kafka UI, and the application:

```bash
docker compose up -d --build
```

PostgreSQL is health-checked before the application starts. The API will be available at:

```text
http://localhost:8080
```

Kafka is exposed to the host at `localhost:9092`. Kafka UI is available at:

```text
http://localhost:8081
```

View service status and application logs:

```bash
docker compose ps
docker compose logs -f wallet-service
```

Stop the services:

```bash
docker compose down
```

To also remove the PostgreSQL data volume:

```bash
docker compose down -v
```

> `docker compose down -v` permanently deletes the local database data.

### Run the Application Locally

This option requires Java 17+. Start PostgreSQL and Kafka first:

```bash
docker compose up -d postgres kafka kafka-ui
```

Then run Spring Boot:

```bash
./gradlew bootRun
```

The API will be available at:

```text
http://localhost:8080
```

By default, the application connects to PostgreSQL at `localhost:5432` and Kafka at `localhost:9092`. These endpoints can be overridden with `SPRING_DATASOURCE_URL` and `SPRING_KAFKA_BOOTSTRAP_SERVERS`.

---

## API Examples

### Create User

```http
POST /api/v1/users
```

```json
{
  "email": "john@example.com",
  "fullName": "John Doe"
}
```

### Create Wallet

```http
POST /api/v1/wallets
```

```json
{
  "userId": 1,
  "currency": "TRY"
}
```

Example response:

```json
{
  "id": 1,
  "userId": 1,
  "currency": "TRY",
  "balance": 0,
  "status": "ACTIVE"
}
```

### Deposit Money

```http
POST /api/v1/wallets/1/deposit
```

```json
{
  "amount": 1000
}
```

### Get Wallet

```http
GET /api/v1/wallets/1
```

### Create Transfer

```http
POST /api/v1/transfers
Idempotency-Key: 8db8bc32-178c-4e61-a4af-7c454758db95
```

```json
{
  "sourceWalletId": 1,
  "targetWalletId": 2,
  "amount": 250.00
}
```

Example response:

```json
{
  "id": 1,
  "sourceWalletId": 1,
  "targetWalletId": 2,
  "amount": 250.00,
  "currency": "TRY",
  "status": "COMPLETED"
}
```

The `Idempotency-Key` header is required. Repeating the same request with the same key returns the previously created transfer without applying the balance changes again. The source wallet ID, target wallet ID, and normalized amount are hashed with SHA-256; reusing the key with a different request returns a conflict response. The source and target wallets must be different and use the same currency. The source wallet must have sufficient balance.

### Get Transfer

```http
GET /api/v1/transfers/1
```

### Error Responses

Invalid requests, missing resources, and insufficient balances return a structured response:

```json
{
  "timestamp": "2026-08-29T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance",
  "path": "/api/v1/transfers"
}
```

---

## Transfer Flow

All balance, transfer, and ledger changes occur within a single database transaction:

```text
Transfer Request
      │
      ▼
Acquire Idempotency-Key Advisory Lock
      │
      ├── Same Key + Same Request ──► Return Existing Result
      ├── Same Key + Different Request ──► Conflict
      │
      ▼
Lock Both Wallet Rows in ID Order
      │
      ├── Validate balance
      │
      ▼
Debit Source
      │
      ▼
Credit Target
      │
      ▼
Create Transfer
      │
      ├── DEBIT Ledger Entry
      └── CREDIT Ledger Entry
      │
      ▼
Create PENDING Outbox Event
      │
      ▼
Commit
      │
      ▼
Scheduled Publisher ──► Kafka (`wallet.transfer.completed`)
      │
      ▼
Mark Outbox Event as PUBLISHED
```

If any operation fails, the entire transaction is rolled back.

---

## Engineering Problems to Explore

This project intentionally evolves toward scenarios that cannot be solved by simple CRUD operations.

### Transaction Consistency

A transfer must never result in money disappearing or being created accidentally.

### Concurrent Transfers

Consider a wallet containing `1000 TRY` receiving two simultaneous requests:

```text
Transfer A → 800 TRY
Transfer B → 800 TRY
```

The system must prevent both transactions from spending the same balance.

The current implementation uses pessimistic write locks and loads wallet rows in a consistent ID order to serialize conflicting balance updates and reduce deadlock risk. Further work will explore:

- Optimistic locking
- Transaction isolation levels
- Race conditions
- Lost updates

### Idempotency

Payment requests may be retried because of network failures. Transfer creation therefore requires an `Idempotency-Key`. PostgreSQL transaction-level advisory locking serializes concurrent requests that use the same key, while a unique database index prevents duplicate transfer records. A SHA-256 request hash also prevents an idempotency key from being silently reused for different transfer parameters.

### Reliable Event Publishing

Completed transfers and their outbox events are written in the same database transaction. A scheduled publisher sends pending events to Kafka after commit, avoiding the database/Kafka dual-write problem. Publisher retries, duplicate-delivery handling, and consumer-side idempotency remain areas for further development.

### Event Processing

Planned topics include:

- At-least-once delivery
- Duplicate events
- Idempotent consumers
- Retry strategies
- Dead Letter Queues

---

## Roadmap

### Phase 1 — Core Domain

- [x] User management
- [x] Wallet creation
- [x] Deposit
- [x] Credit/debit domain operations
- [x] Flyway migrations
- [x] Transfer database model
- [x] Ledger database model
- [x] Transactional wallet-to-wallet transfer
- [x] Ledger generation
- [x] Structured API exception handling

### Phase 2 — Consistency & Concurrency

- [x] Idempotency keys
- [x] Concurrent transfer protection
- [ ] Optimistic locking
- [x] Pessimistic locking
- [ ] Integration tests
- [ ] Testcontainers

### Phase 3 — Event-Driven Architecture

- [x] Kafka
- [x] Transfer completed events
- [x] Transactional Outbox Pattern
- [x] Scheduled outbox publisher
- [ ] Retry strategy
- [ ] Dead Letter Queue
- [ ] Idempotent consumers

### Phase 4 — Performance

- [ ] Redis
- [ ] Rate limiting
- [ ] Caching strategy
- [ ] Load testing
- [ ] Performance benchmarks

### Phase 5 — Observability

- [ ] OpenTelemetry
- [ ] Distributed tracing
- [ ] Prometheus metrics
- [ ] Grafana dashboards
- [ ] Structured logging

### Phase 6 — Deployment

- [x] Application Docker image
- [x] Docker Compose environment
- [ ] Kubernetes manifests
- [ ] Health/readiness probes
- [ ] CI/CD pipeline

---

## Project Goals

The primary purpose of this project is to demonstrate and experiment with backend engineering concepts commonly expected from senior engineers:

- Domain modeling
- Transaction boundaries
- Data consistency
- Concurrency
- Failure handling
- Database design
- Distributed messaging
- Idempotency
- Observability
- Scalability
- Testing strategies
- Architecture trade-offs

The project will evolve incrementally, with architectural decisions documented as new problems are introduced and solved.

---

## Disclaimer

This project is an educational and portfolio project.

It is **not intended to process real financial transactions or hold real funds**.
