# Expense Tracker — Java + React

A full-stack expense tracker: Spring Boot 3 / Java 21 / MySQL backend, React 19 / TypeScript / Vite frontend. Built as a learning project — see `LEARNING.md` for concept explanations (Java, Spring Boot, JWT, React, TanStack Query, etc.) tied to where each concept is used in this codebase.

## Features

- Register / login / logout with JWT access + refresh tokens (refresh token rotation, BCrypt password hashing)
- Basic rate limiting on `/api/v1/auth/**` (10 requests/minute per IP) to slow down credential brute-forcing
- A scheduled job that purges expired/revoked refresh tokens daily, so that table doesn't grow unbounded
- Expenses: create, edit, delete, search, filter (category / date range / amount range), sort, paginate
- Categories: full CRUD, scoped per-user
- Budgets: monthly, overall or per-category, with live spend-vs-budget tracking
- Dashboard: total spend, month-over-month comparison, monthly bar chart, category breakdown pie chart, budget usage bars
- Profile page: view account details, update full name
- An admin-only endpoint (`GET /api/v1/admin/users`) that actually exercises role-based authorization — a `USER`-role JWT gets a 403 here, not just a UI that hides a button
- Every user can only ever see/modify their own data (enforced at the repository query level, not just the UI)
- A handful of backend unit tests (Mockito, no Spring context) and a frontend unit test suite (Vitest) covering the formatting utilities

## Stack

**Backend:** Java 21, Spring Boot 3.3, Spring Web, Spring Security, Spring Data JPA / Hibernate, JJWT, Bean Validation, Lombok, MySQL 8

**Frontend:** React 19, TypeScript, Vite, React Router, TanStack Query, Axios, React Hook Form + Zod, Tailwind CSS, Recharts

## Project structure

```
backend/   Spring Boot API — feature-based packages (auth, user, expense, category, budget, dashboard, security, common)
frontend/  React app — feature-based structure (features/auth, features/expenses, features/categories, features/budgets, features/dashboard)
```

## Running locally (without Docker)

### Prerequisites
- Java 21 (JDK)
- Maven 3.9+
- Node.js 20+
- A running MySQL 8 instance (or use `docker run` for just the DB — see below)

### 1. Start MySQL

If you don't already have MySQL running:

```bash
docker run --name expense-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=expense_tracker -p 3306:3306 -d mysql:8.0
```

(Spring Boot's `ddl-auto: update` will create all tables automatically on first run — no manual migration needed.)

### 2. Backend

```bash
cd backend
cp .env.example .env   # then edit values if needed
mvn spring-boot:run
```

The API starts on **http://localhost:8080**. Environment variables (`DB_HOST`, `JWT_SECRET`, etc.) are read from your shell environment or you can export them from `.env` manually — Spring Boot doesn't read `.env` files natively, so either `export $(cat .env | xargs)` before running, or set them in your IDE's run configuration.

### 3. Frontend

```bash
cd frontend
cp .env.example .env    # VITE_API_BASE_URL=http://localhost:8080/api/v1
npm install
npm run dev
```

The app starts on **http://localhost:5173**.

## Running with Docker Compose (everything at once)

From the project root:

```bash
docker compose up --build
```

This starts MySQL, the backend (port 8080), and the frontend (port 5173) together, wired up on a shared Docker network. First boot takes a minute while Maven/npm dependencies download inside the build stages.

Override secrets via a `.env` file at the project root (see `docker-compose.yml` for which variables it reads — `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`).

## API reference

All endpoints are versioned under `/api/v1`.

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout

GET    /api/v1/expenses          (supports ?search=&categoryId=&from=&to=&minAmount=&maxAmount=&page=&size=&sortBy=&direction=)
POST   /api/v1/expenses
GET    /api/v1/expenses/{id}
PUT    /api/v1/expenses/{id}
DELETE /api/v1/expenses/{id}

GET    /api/v1/categories
POST   /api/v1/categories
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}

GET    /api/v1/budgets
POST   /api/v1/budgets
PUT    /api/v1/budgets/{id}
DELETE /api/v1/budgets/{id}

GET    /api/v1/dashboard/summary
GET    /api/v1/dashboard/monthly?monthsBack=6
GET    /api/v1/dashboard/categories?from=&to=

GET    /api/v1/users/me
PUT    /api/v1/users/me

GET    /api/v1/admin/users        (requires Role.ADMIN — see note below)
```

Every response is wrapped as `{ success, message, data, timestamp }`.

**Testing the admin endpoint:** no signup flow grants `ADMIN` (registration always forces `Role.USER`, on purpose — self-service admin signup would be a real vulnerability). To try it locally, promote a user by hand:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```
Then log in again so the JWT picks up the new role.

## Running the tests

```bash
cd backend && mvn test
cd frontend && npm run test
```

## What's intentionally left as a next step

This is a solid, working MVP, not a maxed-out enterprise app. A few things called for in the original spec are still genuinely thin and worth calling out rather than leaving you to discover them:

- **Test coverage is a start, not comprehensive** — a couple of Mockito unit tests per side, not `@WebMvcTest`/`@DataJpaTest` slices, controller tests, or frontend component/form tests. Enough to demonstrate the pattern and catch regressions in the riskiest logic (password hashing, ownership checks), not enough to call "tested."
- **Rate limiting is single-instance, in-memory** — fine for one backend process, but resets on restart and won't coordinate across multiple instances behind a load balancer. A real deployment with horizontal scaling needs a shared store (Redis is the standard choice) instead of `RateLimitingFilter`'s in-memory map.
- **No account lockout** after repeated failed logins — rate limiting slows brute-forcing, but there's no per-account lockout/backoff on top of it.
- **CI** (GitHub Actions running `mvn test` and `npm run build` on push) — not set up.
- **`frontend/src/services/`** — created per the original folder-structure spec, still empty. All API logic ended up living in each feature's own `api/` folder plus the shared `lib/axios.ts`, which covers the same need; the empty folder is left as-is rather than populated with a synthetic file just to fill it.

## Learning mode

`LEARNING.md` in this repo walks through the Java/Spring/React concepts used throughout the code, in the CONCEPT / WHY / SIMPLE EXAMPLE / IN OUR PROJECT format you asked for, plus common beginner mistakes for each. Every source file also has inline comments explaining the non-obvious decisions right where they're made.
