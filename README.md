# Expense Tracker — Java + React

A full-stack expense tracker: Spring Boot 3 / Java 21 / MySQL backend, React 19 / TypeScript / Vite frontend. Built as a learning project — see `LEARNING.md` for concept explanations (Java, Spring Boot, JWT, React, TanStack Query, etc.) tied to where each concept is used in this codebase.

## Features

- Register / login / logout with JWT access + refresh tokens (refresh token rotation, BCrypt password hashing)
- Expenses: create, edit, delete, search, filter (category / date range / amount range), sort, paginate
- Categories: full CRUD, scoped per-user
- Budgets: monthly, overall or per-category, with live spend-vs-budget tracking
- Dashboard: total spend, month-over-month comparison, monthly bar chart, category breakdown pie chart, budget usage bars
- Every user can only ever see/modify their own data (enforced at the repository query level, not just the UI)

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
```

Every response is wrapped as `{ success, message, data, timestamp }`.

## What's intentionally left as a next step

This is a solid, working MVP, not a maxed-out enterprise app. A few things called for in the original spec that you may want to add incrementally (each is a good exercise in its own right):

- **Tests** — unit tests for services, `@WebMvcTest`/`@DataJpaTest` slices for controllers/repositories, and frontend component/form tests
- **Role-based authorization beyond the basic USER/ADMIN enum** — there's no admin-only endpoint yet to actually exercise `Role.ADMIN`
- **Rate limiting / account lockout** on the auth endpoints
- **Refresh token cleanup job** — expired/revoked rows currently just accumulate in the `refresh_tokens` table
- **CI** (GitHub Actions running `mvn test` and `npm run build` on push)

## Learning mode

`LEARNING.md` in this repo walks through the Java/Spring/React concepts used throughout the code, in the CONCEPT / WHY / SIMPLE EXAMPLE / IN OUR PROJECT format you asked for, plus common beginner mistakes for each. Every source file also has inline comments explaining the non-obvious decisions right where they're made.
