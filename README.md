# SafeWallet API

A payment wallet backend built with Spring Boot. Designed to mirror the core backend patterns used in mobile financial services (MFS) — authentication, OTP verification, wallet management, and money transfer.

Built as a portfolio project targeting fintech engineering roles.

---

## What's implemented

- User registration with phone number and BCrypt password hashing
- Redis-based OTP verification — 6-digit code, 5-minute TTL, one-time use (deleted from cache after successful verify)
- JWT login — token signed with HS256, 24-hour expiry
- Protected routes — all wallet and transaction endpoints require a valid JWT
- Role-based access — USER and ADMIN roles stored in DB and embedded in JWT claims
- Balance check — only accessible to verified users

## Backend Engineering Deep‑Dives

During development I deliberately broke the transfer endpoint to understand how to fix it – then applied industry‑standard solutions.

- **Atomicity** – First, I removed the `@Transactional` annotation and simulated a failure mid‑transfer (a fraud‑check exception after debiting the sender but before crediting the recipient). The sender’s balance decreased while the recipient never received the money – money simply vanished. Adding `@Transactional` ensured that either **all operations succeed or everything rolls back**, keeping the database consistent.

- **Pessimistic Locking** – I fired two simultaneous transfer requests against the same wallet without any locking. Both requests read the same old balance, approved the transfer, and debited the sender twice, resulting in a **negative wallet balance**. Switching to a pessimistic write lock (`SELECT … FOR UPDATE`) forced the second request to wait and see the updated balance, completely eliminating the race condition.

- **Idempotency** – To prevent double‑charging from accidental retries, I introduced idempotency keys. A client can supply its own key (safe for network‑level retries), and if none is provided, the server generates a **deterministic key** based on sender, receiver, amount, and a one‑minute time window. The system stores the key in the transactions table and rejects duplicate keys with a `409 Conflict`, guaranteeing that the same logical transfer is processed only once.

## In progress

- Wallet-to-wallet transfer — atomic with `@Transactional`, idempotent via unique key constraint, pessimistic locking to prevent race conditions
- Daily fraud limit — hard block at ৳30,000/day using a running total table, no full-scan aggregation
- Soft fraud flagging — large amount, high velocity, off-hours detection
- Transaction history — paginated, labelled SENT or RECEIVED
- Top-up and withdrawal endpoints — ADMIN-only top-up, idempotent withdrawal
- Admin panel — view all users, balances, flagged transactions

---

## Tech stack

- **Java 17** · **Spring Boot 3.2**
- **Spring Security** — JWT filter chain, stateless session
- **Spring Data JPA** — repositories, pessimistic locking, JPQL
- **PostgreSQL** — primary database, Flyway migrations
- **Redis** — OTP storage with TTL, rate limiting counters
- **Docker + Docker Compose** — containerised local development
- **jjwt 0.11.5** — JWT generation and validation
- **JUnit 5 + Mockito** — unit tests

---

## Run locally

Requires: Docker, Java 17, Maven

```bash
git clone https://github.com/Tanjim003/Safewallet.git
cd Safewallet
cp .env.example .env        # fill in your values
docker-compose up -d        # starts PostgreSQL on 5433 + Redis on 6379
./mvnw spring-boot:run
```

App runs on `http://localhost:8080`

---

## Environment variables

Copy `.env.example` to `.env` and fill in your values. The `.env` file is gitignored — never commit real credentials.

```env
# PostgreSQL (for docker-compose)
POSTGRES_DB=wallet_db
POSTGRES_USER=wallet_user
POSTGRES_PASSWORD=change_me

# Spring Boot Datasource
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/wallet_db
SPRING_DATASOURCE_USERNAME=wallet_user
SPRING_DATASOURCE_PASSWORD=change_me

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=generate_a_random_base64_secret_here
JWT_EXPIRATION=86400000
```

**Generate a secure JWT secret:**
```bash
openssl rand -base64 32
```

---

## API endpoints

### Auth — public

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register with phone + password. Triggers OTP. |
| POST | `/api/auth/verify-otp` | Verify phone with OTP from Redis |
| POST | `/api/auth/login` | Returns signed JWT |

### Wallet — requires JWT

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wallet/balance` | Get current balance for verified user |
| POST | `/api/wallet/withdraw` | Withdraw — idempotent *(in progress)* |

### Transactions — requires JWT *(in progress)*

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions/transfer` | Send money — atomic, idempotent, rate-limited |
| GET | `/api/transactions/history` | Paginated history with SENT/RECEIVED labels |
| GET | `/api/transactions/{id}` | Single transaction detail |

### Admin — requires ADMIN role *(in progress)*

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/wallet/topup` | Credit any user's wallet |
| GET | `/api/admin/transactions` | All transactions across all users |
| GET | `/api/admin/transactions/flagged` | Transactions flagged by fraud engine |

---

## Testing with Postman

**Register**
```json
POST /api/auth/register
Content-Type: application/json

{
  "phone": "01712345678",
  "fullName": "Test User",
  "password": "secret123"
}
```

**Verify OTP** — OTP prints to application logs during local development
```json
POST /api/auth/verify-otp
Content-Type: application/json

{
  "phone": "01712345678",
  "otp": "847293"
}
```

**Login**
```json
POST /api/auth/login
Content-Type: application/json

{
  "phone": "01712345678",
  "password": "secret123"
}
```

Response includes `token`. Pass it on all subsequent requests:
```
Authorization: Bearer <token>
```

---

## Database schema

Four tables managed by Flyway migrations — runs automatically on startup:

| Table | Purpose |
|-------|---------|
| `users` | Phone, BCrypt password, verified flag, role |
| `wallets` | One per user, DECIMAL(15,2) balance, frozen flag |
| `transactions` | Sender, receiver, amount, idempotency key (UNIQUE), fraud flags |
| `daily_transfer_totals` | Running daily sent total per wallet, UNIQUE(wallet_id, date) |

---

## Key design decisions

**Idempotency key on transfers** — the client generates a UUID and sends it with every transfer request. Stored with a UNIQUE constraint in the database. If the same request is retried due to a network failure or timeout, the second call returns the original transaction without moving money again. Enforced at the database level, not just in application code.

**Pessimistic locking on wallet rows** — transfer uses `SELECT ... FOR UPDATE` on both wallet rows before reading balances. Prevents two concurrent requests from reading the same balance simultaneously and both succeeding when only one should.

**Redis for OTP, not the database** — OTP needs automatic expiry after 5 minutes and must be deleted immediately after use. Redis TTL handles expiry natively. A database-based approach would require a scheduled cleanup job and is slower to read under load.

**DECIMAL(15,2) for all money columns** — floating point arithmetic has rounding errors: `0.1 + 0.2 = 0.30000000000000004`. Every money value uses `BigDecimal` in Java and `DECIMAL` in PostgreSQL.

**daily_transfer_totals table for fraud limits** — instead of running `SUM(amount)` across the full transactions table on every transfer request (which gets slower as data grows), a separate table keeps a running daily total per wallet updated incrementally. The fraud check reads exactly one row.

---

## Project structure

```
src/main/java/com/wallet/safewallet/
├── config/          # SecurityConfig, RedisConfig, PasswordEncoderConfig
├── controller/      # AuthController, WalletController, TransactionController, AdminController
├── dto/             # RegisterRequest, TransferRequest, ApiResponse, LoginRequest
├── entity/          # User, Wallet, Transaction, DailyTransferTotal, TransactionType
├── exception/       # GlobalExceptionHandler, custom exceptions
├── repository/      # JPA repositories with custom queries and locking
├── security/        # JwtUtil, JwtAuthFilter
└── service/         # AuthService, OtpService, WalletService, TransactionService,
                     # RateLimitService, FraudDetectionService

src/main/resources/
├── application.yml        # reads all config from environment variables
└── db/migration/          # V1__create_users.sql through V4__create_daily_transfer_totals.sql
```

---

## Running tests

```bash
./mvnw test
```

Current coverage: `OtpService` unit tests — generate OTP, verify correct OTP, verify wrong OTP, verify expired OTP.