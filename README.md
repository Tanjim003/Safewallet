# SafeWallet API

> **A production-oriented digital wallet backend built with Spring Boot, PostgreSQL, Redis and Docker.**
>
> SafeWallet is a portfolio project designed to demonstrate backend engineering concepts commonly found in fintech systems. It focuses on transactional consistency, concurrency control, fraud detection, idempotency, and clean architecture.

---

# Features

## Authentication & Security

- User registration with phone number and BCrypt password hashing
- Redis-based OTP verification (6-digit OTP, 5-minute TTL, one-time use)
- JWT authentication (24-hour expiry)
- Stateless Spring Security filter chain
- Role-based authorization (USER / ADMIN)

## Wallet

- Wallet balance
- Money transfer between users
- Admin-only wallet top-up
- User withdrawal
- Paginated transaction history
- Transaction types:
    - TRANSFER
    - TOP_UP
    - WITHDRAWAL

---

# Production Engineering Features

- ACID transactions using `@Transactional`
- Pessimistic row locking (`SELECT ... FOR UPDATE`)
- Idempotent transfers and withdrawals
- Redis OTP storage with automatic TTL
- Redis rate limiting
- Flyway database migrations
- Dockerized local environment
- Fraud detection engine
- Payment provider abstraction
- BigDecimal + DECIMAL(15,2) for all monetary values

---

# Backend Engineering Decisions

## Atomic Transactions

A simulated mid-transfer failure showed money disappearing when `@Transactional` was removed. Adding transactional boundaries guarantees all-or-nothing execution.

## Pessimistic Locking

Concurrent transfer requests previously produced negative balances. Wallet rows are now locked before balance validation and update, preventing race conditions.

## Idempotency

Duplicate requests caused by retries are prevented using unique idempotency keys stored at the database layer.

## Fraud Detection

Two-layer fraud engine:

### Hard Block

Transfers exceeding the configured daily limit are rejected before money moves.

### Soft Flag

Potentially suspicious transactions (large amount, high velocity) are allowed but flagged for later administrative review.

Running daily totals are maintained in a dedicated table to avoid expensive aggregation queries.

## Payment Provider Abstraction

`PaymentService` depends on a `PaymentProvider` interface rather than a specific gateway.

Current implementation:

- FakePaymentProvider

Future implementations can include:

- Stripe
- bKash
- Nagad

without changing business logic.

---

# Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway
- Docker & Docker Compose
- JWT (jjwt)
- JUnit 5
- Mockito

---

# Running Locally

```bash
git clone https://github.com/Tanjim003/Safewallet.git
cd Safewallet
cp .env.example .env
docker compose up -d
./mvnw spring-boot:run
```

---

# API

## Public

- POST `/api/auth/register`
- POST `/api/auth/verify-otp`
- POST `/api/auth/login`

## User

- GET `/api/wallet/balance`
- POST `/api/wallet/send`
- POST `/api/wallet/withdraw`
- GET `/api/transactions`

## Admin

- POST `/api/admin/wallet/topUp`
- GET `/api/admin/transactions`
- GET `/api/admin/transactions/flagged`

---

# Database

- users
- wallets
- transactions
- daily_transfer_totals

Managed by Flyway migrations.

---

# Project Structure

```text
config/
controller/
dto/
entity/
exception/
payment/
repository/
security/
service/
db/migration/
```

---

# Current Test Coverage

- OTP Service
- Transaction History

Next:

- Transfer Service
- Fraud Engine
- Payment Service

---

# Roadmap

## Completed

- Authentication
- OTP Verification
- JWT Security
- Wallet
- Transfers
- Transaction History
- Fraud Engine
- Admin Top-up
- Withdrawals
- Payment Provider Abstraction
- Docker
- Redis
- Flyway

## Planned

- Production deployment
- GitHub Actions CI/CD
- Stripe integration
- Webhooks
- Ledger
- Audit trail
- Double-entry accounting
- Monitoring & metrics
- Load testing

---

This project was intentionally built to explore backend engineering challenges including transaction consistency, concurrency, fraud prevention, and payment architecture while following production-oriented design principles.
