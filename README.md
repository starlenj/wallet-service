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
- Concurrent transfer protection with PostgreSQL advisory and pessimistic row locks
- Automatic debit and credit ledger entries for each transfer
- Transfer status tracking
- PostgreSQL persistence
- Database migrations with Flyway
- Transfer persistence model
- Ledger persistence model
- Request validation
- Structured API error responses
- Dockerized application and PostgreSQL environment

Event-driven processing, integration testing, and other advanced distributed-system features are currently under development.

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
- Gradle
- Docker / Docker Compose

Planned:

- Kafka
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
└── ledger/
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
 └── Status

Ledger Entry
 ├── Wallet
 ├── Transfer
 ├── Type (DEBIT / CREDIT)
 └── Amount
```

Money is represented using `BigDecimal` rather than floating-point types.

Balance mutations are implemented as domain behavior:

```java
wallet.credit(amount);
wallet.debit(amount);
```

rather than exposing arbitrary balance setters.

Transfers run inside a single database transaction. Each request is serialized by its idempotency key, and both wallet rows are locked in a consistent ID order before their balances are changed. A successful transfer debits the source wallet, credits the target wallet, creates the transfer record, and writes matching `DEBIT` and `CREDIT` ledger entries. Any failure rolls back the complete operation.

---

## Database

PostgreSQL is used as the primary database.

Schema changes are managed using **Flyway** instead of relying on Hibernate to modify the database schema automatically.

Current migrations:

```text
V1__create_users_and_wallets.sql
V2__create_transfer_and_ledger_entries.sql
V3__add_idem_key_to_transfers.sql
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

Build and start both PostgreSQL and the application:

```bash
docker compose up -d --build
```

PostgreSQL is health-checked before the application starts. The API will be available at:

```text
http://localhost:8080
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

This option requires Java 17+. Start only PostgreSQL first:

```bash
docker compose up -d postgres
```

Then run Spring Boot:

```bash
./gradlew bootRun
```

The API will be available at:

```text
http://localhost:8080
```

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

The `Idempotency-Key` header is required. Repeating a request with the same key returns the previously created transfer without applying the balance changes again. The source and target wallets must be different and use the same currency. The source wallet must have sufficient balance.

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
      ├── Existing Transfer ──► Return Existing Result
      │
      ▼
Lock Both Wallet Rows in ID Order
      │
      ├── Validate balance
      │
      ▼
Load Target Wallet
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
Commit
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

Payment requests may be retried because of network failures. Transfer creation therefore requires an `Idempotency-Key`. PostgreSQL transaction-level advisory locking serializes concurrent requests that use the same key, while a unique database index prevents duplicate transfer records.

### Reliable Event Publishing

A future version will publish domain events through Kafka.

The system will explore the **Transactional Outbox Pattern** to handle cases where a database transaction succeeds but event publishing fails.

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

- [ ] Kafka
- [ ] Domain events
- [ ] Transactional Outbox Pattern
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
