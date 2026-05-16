# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

Budget Buddy is a personal finance REST API built with Spring Boot 3.5 and Java 21. It tracks transactions, budgets, savings, installments, subscriptions, and thresholds across multiple accounts and currencies (VND, SGD). The system is designed around Domain-Driven Design principles and Spring Modulith — understanding both is required to work correctly in this codebase.

---

## Architecture & System Design

### Spring Modulith — Three Bounded Contexts

The app is structured as three `@ApplicationModule`s that enforce strict boundary isolation:

- **`core`** — shared infrastructure only: JWT/Redis utilities, security config, global exception handler, and the `SendVerificationEmailEvent` that crosses module boundaries
- **`transaction`** — all financial domain: accounts, budgets, categories, installments, savings, subscriptions, thresholds, transactions
- **`user`** — identity domain: registration, login, Google OAuth, email verification, password reset

Modules communicate exclusively via Spring's `ApplicationEventPublisher` (configured async by default in Spring Modulith). Direct imports between `transaction` and `user` are a violation of the module contract.

### DDD-Inspired Dual Service Layer

Every feature in `transaction` and `user` follows a strict two-layer service pattern inspired by Domain-Driven Design:

**Application Service** (`application/service/`) — The DDD *application service*. Its only job is to orchestrate calls to the domain service. It contains no business logic, no validation, no repository access. It is a thin delegation layer:

```java
// CORRECT — application service delegates entirely
public void saveBudget(BudgetDTO budgetDTO) {
    budgetData.saveBudget(budgetDTO);
}
```

**Domain Service** (`domain/service/`) — Where all business logic lives. Validates ownership, enforces domain rules, calls repositories, handles exceptions. This is where you write real code.

Controllers call application services. Application services call domain services. Domain services call repositories. Nothing skips a layer.

---

## Critical Guardrails

### 1. Never Put Business Logic in the Application Service Layer

This is the most common mistake. If you find yourself writing `if`, `throw`, `repository.find...`, or any conditional in `application/service/impl/`, you are in the wrong layer. Move it to `domain/service/impl/`.

```java
// WRONG — logic creeping into application layer
public void saveBudget(BudgetDTO dto) {
    if (budgetRepository.exists(...)) throw new ConflictException(...); // NO
    budgetData.saveBudget(dto);
}

// CORRECT — application layer is a pure pass-through
public void saveBudget(BudgetDTO dto) {
    budgetData.saveBudget(dto);
}
```

### 2. Every Query Must Filter by `userId` — This Is a Security Rule

Every repository method that returns user data **must** include a `userId` predicate. Missing it means one authenticated user can read another user's financial records. This is not a data correctness issue — it is a **data leakage vulnerability**.

The current user's ID is always retrieved via `TransactionUtils.getCurrentUserId()` (reads from the Spring Security context). Call this at the top of every domain service method that touches data.

```java
// WRONG — missing userId filter leaks all users' data
budgetRepository.findById(budgetId);

// CORRECT — always scope to the authenticated user
Long userId = transactionUtils.getCurrentUserId();
budgetRepository.findByIdAndUserId(budgetId, userId);
```

Every `Repository` interface method that retrieves data should have `AndUserId` or `ByUserId` in its name. If you are adding a repository method without a `userId` parameter, stop and reconsider.

### 3. Liquibase Owns the Schema — Hibernate Does Not

`ddl-auto: none` is set intentionally. Hibernate never creates or alters tables. Every schema change — new column, new table, index, constraint — requires a new numbered changeset in `src/main/resources/db/changelog/changes/`. Never change an existing changeset; always add a new one.

### 4. Module Boundaries Are Enforced by `@ApplicationModule`

Do not add a direct Java import from `com.budget.buddy.transaction.*` into `com.budget.buddy.user.*` or vice versa. If two modules need to communicate, publish an event from `core` and have the consumer listen. The `SendVerificationEmailEvent` in `core/dto/` is the existing pattern.

### 5. Logging Uses Log4j2, Not SLF4J

Logback is explicitly excluded from the classpath. Use:

```java
private static final Logger logger = LogManager.getLogger(MyClass.class);
```

**Not** `LoggerFactory.getLogger(...)`. SLF4J calls will compile but may not route correctly.

---

## Authentication & Token Flow

Auth is stateless JWT (HS256). Redis stores active token state — JWT signature alone is not enough to authenticate a request.

Flow: `POST /token` (sends email verification) → `GET /verify` (activates email) → `POST /reset-password` (sets password, fully activates account) → `POST /login` (returns access + refresh tokens).

Redis key scheme (`RedisTokenService`):
- `access:jti:{jti}` → email — `JwtFilter` checks this on every authenticated request; if the key is gone, the token is rejected even if cryptographically valid
- `refresh:{sha256(token)}` → email — hashed to avoid storing raw tokens
- `user:accessJti:{email}` → current jti — enables single-session enforcement; login/logout rotates this

Account locks after **5 consecutive failed login attempts**. Unlock only happens via the reset-password flow.

---

## External Dependencies & Quirks

**PostgreSQL** — HikariCP pool is configured with `auto-commit: false`; transactions are managed by Spring/JPA. Never call `connection.commit()` manually.

**Redis** — used exclusively for token session state, not for caching domain data. If Redis is unavailable, all authenticated requests fail. There is no fallback.

**Budget Cycle** — the default date range for budgets is hardcoded as the **5th of each month to the 5th of the next month**. This logic lives in `BudgetDataImpl.getAllBudgetsForCurrentUser()`. It is not configurable per user.

**Transaction Filtering** — `TransactionSpecificationImpl` builds JPA `Specification` predicates dynamically. Valid sort fields are whitelisted as `date`, `amount`, `name`, `id` — any other field is silently ignored to prevent injection. Operators like `is`, `is not`, `contains`, `starts with`, `is between` are lowercased and normalized before matching.

**Email** — verification emails are sent via Google Gmail API (OAuth2), not plain SMTP. The `SendVerificationEmailEvent` is async; email delivery failures do not roll back user creation.

---

## Local Development

```bash
# Start dependencies only, run app locally
docker-compose up -d postgres redis
./mvnw spring-boot:run

# Full stack
docker-compose up -d

# Tests use H2 — no external services required
./mvnw test

# Run a specific test class
./mvnw test -Dtest=BudgetDataImplTest

# Coverage report → target/site/jacoco/index.html
./mvnw clean test jacoco:report
```

Required `.env` keys: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `JWT_SECRET` (Base64, 64+ bytes), `EMAIL_USERNAME`, `EMAIL_PASSWORD`, `EMAIL_CLIENT_ID`, `EMAIL_CLIENT_SECRET`, `GOOGLE_URL`, `GOOGLE_USER_INFO_URL`, `APP_CORS`, `COOKIES`.

**Swagger UI** available at `http://localhost:8080/swagger-ui.html` when running locally.

---

## Fast Lookup — API Endpoint → Controller

When the user says "the response of `/api/v1/accounts`" or "change `POST /transaction/inquiry`", jump directly to the controller. The chain is always:

`Controller → ApplicationService → ApplicationServiceImpl → DomainService (XxxData) → DomainServiceImpl (XxxDataImpl) → Repository`

| Base Path | Controller | Application Service Impl | Domain Service Impl |
|-----------|-----------|--------------------------|---------------------|
| `/api/v1/accounts` | `transaction/application/controller/AccountController.java` | `application/service/impl/AccountServiceImpl.java` | `domain/service/impl/AccountDataImpl.java` |
| `/api/v1/budgets` | `BudgetController.java` | `BudgetServiceImpl.java` | `BudgetDataImpl.java` |
| `/api/v1/categories` | `CategoryController.java` | `CategoryServiceImpl.java` | `CategoryDataImpl.java` |
| `/api/v1/transaction` | `TransactionController.java` | `TransactionServiceImpl.java` | `TransactionDataImpl.java` |
| `/api/v1/savings` | `SavingController.java` | `SavingServiceImpl.java` | `SavingDataImpl.java` |
| `/api/v1/installments` | `InstallmentController.java` | `InstallmentServiceImpl.java` | `InstallmentDataImpl.java` |
| `/api/v1/subscriptions` | `SubscriptionController.java` | `SubscriptionServiceImpl.java` | `SubscriptionDataImpl.java` |
| `/api/v1/thresholds` | `ThresholdController.java` | `ThresholdServiceImpl.java` | `ThresholdDataServiceImpl.java` |
| `/api/v1/auth` | `user/application/controller/AuthController.java` | `user/application/service/auth/impl/AuthenticationServiceImpl.java` | `user/domain/service/impl/UserDataImpl.java` |

### Endpoint cheat sheet

**Account** (`AccountController.java`): `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, `GET /types`, `DELETE /groups/{groupId}`

**Budget** (`BudgetController.java`): `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`

**Category** (`CategoryController.java`): `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`

**Transaction** (`TransactionController.java`): `POST /`, `POST /collection`, `POST /inquiry` (filter+search), `PUT /{id}`, `DELETE /{id}`, `POST /threshold`

**Saving** (`SavingController.java`): `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, `DELETE /`

**Installment** (`InstallmentController.java`): `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, `DELETE /`

**Subscription** (`SubscriptionController.java`): `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`

**Threshold** (`ThresholdController.java`): `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`

**Auth** (`AuthController.java`): `POST /token`, `POST /verify`, `POST /login`, `POST /refresh-token`, `POST /logout`, `POST /reset-password`, `GET /google`

---

## Fast Lookup — User Intent → File

| User says... | Where to act |
|--------------|--------------|
| "change response of /api/v1/accounts" | `AccountController.java` (DTO mapping) + `application/dto/account/AccountRetrieveResponse.java` |
| "add new endpoint to /accounts" | `AccountController.java` + `AccountService` + `AccountServiceImpl` + `AccountData` + `AccountDataImpl` |
| "fix login logic" / "auth bug" | `user/application/service/auth/impl/AuthenticationServiceImpl.java` |
| "JWT validation" / "token check" | `core/config/security/JwtFilter.java` + `core/utils/JwtUtil.java` + `core/utils/RedisTokenService.java` |
| "CORS / which paths require auth" | `core/config/security/SecurityConfig.java` |
| "global error response shape" | `core/config/exception/{GlobalExceptionHandler,ErrorResponse,ErrorCode}.java` |
| "add a DB column / new table" | New file in `src/main/resources/db/changelog/changes/00X-{name}.yaml` (next number) + update entity in `domain/model/{feature}/` |
| "transaction filter operators / sort fields" | `transaction/infrastructure/repository/custom/TransactionSpecificationImpl.java` |
| "change app config" (DB, Redis, etc.) | `src/main/resources/application.yaml` (prod) or `application-dev.yaml` (dev) |
| "logging config" | `src/main/resources/log4j2.yml` |
| "email verification flow" | `user/application/service/auth/impl/AuthenticationServiceImpl.java` + `core/dto/SendVerificationEmailEvent.java` |
| "Google OAuth" | `user/application/service/auth/impl/GoogleAuthServiceImpl.java` |
| "deployment / CI" | `Jenkinsfile`, `Dockerfile`, `docker-compose.yml` (project root) |

---

## Code Location Map

All paths relative to `src/main/java/com/budget/buddy/`. Base package: `com.budget.buddy`.

### Module: `core` — Shared infrastructure (security, config, utilities)

| Purpose | Path |
|---------|------|
| JWT filter (per-request auth) | `core/config/security/JwtFilter.java` |
| Security config (endpoint rules, CORS) | `core/config/security/SecurityConfig.java` |
| Global exception handler | `core/config/exception/GlobalExceptionHandler.java` |
| Custom exceptions | `core/config/exception/{AuthException,BadRequestException,ConflictException,NotFoundException}.java` |
| Error codes enum | `core/config/exception/ErrorCode.java` |
| JWT token utility | `core/utils/JwtUtil.java` |
| Redis token session store | `core/utils/RedisTokenService.java` |
| Shared app utilities | `core/utils/ApplicationUtil.java` |
| Swagger/OpenAPI config | `core/config/swagger/OpenApiConfig.java` |
| Cross-module event DTO | `core/dto/SendVerificationEmailEvent.java` |
| Audit config | `core/config/audit/AuditConfig.java` |
| ObjectMapper config | `core/config/other/ObjectMapperConfig.java` |

### Module: `transaction` — All financial domain logic

| Purpose | Path |
|---------|------|
| **Controllers** | `transaction/application/controller/{Feature}Controller.java` |
| **Application services** (thin delegation) | `transaction/application/service/{Feature}Service.java` + `impl/{Feature}ServiceImpl.java` |
| **Domain services** (business logic) | `transaction/domain/service/{Feature}Data.java` + `impl/{Feature}DataImpl.java` |
| **DTOs** | `transaction/application/dto/{feature}/` |
| **Entities** | `transaction/domain/model/{feature}/{Feature}.java` |
| **Repositories** | `transaction/infrastructure/repository/{Feature}Repository.java` |
| **Enums** (Currency, Direction, CategoryType) | `transaction/domain/enums/` |
| **Value Objects** | `transaction/domain/vo/{MoneyVO,CategoryVO}.java` |
| Transaction filter spec (dynamic predicates) | `transaction/infrastructure/repository/custom/TransactionSpecificationImpl.java` |
| Auth user ID extraction utility | `transaction/domain/utils/TransactionUtils.java` |
| Category DTO mapper | `transaction/application/mapper/CategoryMapper.java` |

Features: Account, Budget, Category, Installment, Saving, Subscription, Threshold, Transaction

### Module: `user` — Identity & authentication

| Purpose | Path |
|---------|------|
| Auth controller (login, register, OAuth, refresh) | `user/application/controller/AuthController.java` |
| Auth service (login/register/reset logic) | `user/application/service/auth/impl/AuthenticationServiceImpl.java` |
| Google OAuth service | `user/application/service/auth/impl/GoogleAuthServiceImpl.java` |
| User service (profile ops) | `user/application/service/user/impl/UserServiceImpl.java` |
| User domain logic | `user/domain/service/impl/UserDataImpl.java` |
| User entity | `user/domain/model/User.java` |
| Email verification entity | `user/domain/model/UserVerification.java` |
| Auth DTOs (login/register requests) | `user/application/dto/` |
| Repositories | `user/infrastructure/repository/{UserRepository,UserVerificationRepository}.java` |
| Value objects | `user/domain/vo/{EmailAddressVO,VerificationTokenVO}.java` |
| Constants | `user/application/constant/UserApplicationConstant.java` |

### Configuration & Resources

| Purpose | Path (from project root) |
|---------|------|
| App config (prod) | `src/main/resources/application.yaml` |
| App config (dev profile) | `src/main/resources/application-dev.yaml` |
| Liquibase changelog dir | `src/main/resources/db/changelog/changes/` (001–008 currently) |
| Log4j2 config | `src/main/resources/log4j2.yml` |
| Dockerfile (prod) | `Dockerfile` |
| Dockerfile (dev) | `Dockerfile.dev` |
| Docker Compose (full stack) | `docker-compose.yml` |
| Docker Compose (dev only) | `docker-compose.dev.yml` |
| Jenkins pipeline | `Jenkinsfile` |
| Maven wrapper | `mvnw` |

### Tests

| Purpose | Path (from project root) |
|---------|------|
| Domain service tests | `src/test/java/com/budget/buddy/transaction/domain/service/impl/{Feature}DataImplTest.java` |
| Application service tests | `src/test/java/com/budget/buddy/transaction/application/service/impl/{Feature}ServiceImplTest.java` |
| Auth service test | `src/test/java/com/budget/buddy/user/application/service/auth/impl/AuthenticationServiceImplTest.java` |

---

## Adding a New Feature — Required Steps

1. Entity in `transaction/domain/model/<feature>/`
2. Repository in `transaction/infrastructure/repository/` — all methods must include `userId`
3. DTOs in `transaction/application/dto/<feature>/`
4. Domain service interface + impl in `transaction/domain/service/` — business logic lives here
5. Application service interface + impl in `transaction/application/service/` — delegates only, no logic
6. Controller in `transaction/application/controller/`
7. Liquibase changeset in `db/changelog/changes/` with next sequence number
8. Unit tests for the domain service impl (JaCoCo measures this layer)

---

## AI Behavior Guidelines

**Always confirm before:**
- Adding a new Liquibase changeset that modifies existing columns or drops anything
- Choosing how two modules should communicate (event vs. shared core type)
- Changing token TTLs or Redis key structure — this affects all active sessions

**Always do without asking:**
- Scope every repository query to `userId` via `transactionUtils.getCurrentUserId()`
- Put business logic in `domain/service/impl/`, not `application/service/impl/`
- Use `LogManager.getLogger(...)` for logging, never `LoggerFactory`
- Add a Liquibase changeset for any schema change — never rely on `ddl-auto`

**Before touching auth (`core/config/security/`, `core/utils/`):** understand that `JwtFilter` validates both the JWT signature *and* the Redis JTI key. Removing either check breaks authentication silently for active sessions.

**Before touching `TransactionSpecificationImpl`:** the field whitelist for sorting is a security control, not a convenience. Do not expand it without review.
