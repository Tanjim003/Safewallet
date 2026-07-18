# SafeWallet API

## Live API

The entire API is live at **[safewallet-vmmy.onrender.com](https://safewallet-vmmy.onrender.com)**.



> **A production-oriented digital wallet backend built with Spring Boot, PostgreSQL, Redis and Docker.**
>
> SafeWallet is a portfolio project designed to demonstrate backend engineering concepts commonly found in fintech systems. It focuses on transactional consistency, concurrency control, fraud detection, idempotency, and clean architecture.

---

## System Architecture

```text
                        +----------------+
                        |     Client     |
                        +--------+-------+
                                 |
                                 v
                     Spring Security + JWT
                                 |
                    +------------+-------------+
                    |                          |
                    v                          v
             Authentication              Wallet APIs
          (OTP + Login + JWT)      (Transfer / Withdraw)
                    |                          |
                    +------------+-------------+
                                 |
                         Service Layer
                                 |
        +------------+-----------+-------------+
        |            |                         |
        v            v                         v
 Fraud Engine   Payment Provider      Transaction Manager
                     |
                     v
             FakePaymentProvider
                     |
          (Replaceable by Stripe,
             bKash, Nagad...)
                                 |
                                 v
                    PostgreSQL + Redis
```

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

## Fraud Detection Engine

Every transfer passes through a lightweight fraud detection layer before completion.

### Hard Block

If a user exceeds the configured daily transfer limit:

```
Transfer Rejected
```

Money never leaves the wallet.

---

### Soft Flag

Transactions that appear suspicious are still processed but recorded for administrative review.

Current rules include:

- unusually large transfers
- high transaction frequency

Flagged transactions appear inside the admin dashboard for investigation.

---

### Why Daily Totals Table?

Instead of calculating

```sql
SUM(...)
```

over the entire transaction table on every request,

daily totals are maintained incrementally inside

```
daily_transfer_totals
```

making fraud validation significantly faster as transaction history grows.

## Payment Provider Abstraction
## Extensible Payment Architecture

Business logic depends on an abstraction rather than a concrete payment gateway.

```
PaymentService
      |
      v
PaymentProvider
      |
+-----+------+-------------+
|            |             |
v            v             v
Fake      Stripe        bKash
Provider
```

The current implementation uses `FakePaymentProvider` for development.

Replacing it with Stripe, bKash or Nagad only requires a new implementation of the `PaymentProvider` interface.

No business logic changes are required.

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
