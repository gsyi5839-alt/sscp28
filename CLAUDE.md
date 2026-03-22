# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BCBBS3 is a Vue 3 + TypeScript frontend with a Spring Boot backend. Features multi-role auth (MEMBER/AGENT/ADMIN), captcha-gated login, password change enforcement, search, access line management, and lottery game integration.

## Tech Stack

- **Frontend**: Vue 3.5, TypeScript 5.9, Vite 7, Pinia 3, Element Plus 2.13, Axios, Vue Router 4
- **Backend**: Spring Boot 3.2, Java 17, Spring Security + JWT (jjwt 0.12), Spring Data JPA/Hibernate
- **Database**: MySQL 8 on `localhost:3306`, database/user/pass: `xie080886`
- **Schema**: Managed by Hibernate `ddl-auto: update` — no manual migrations needed

## Commands

### Frontend
```bash
cd frontend && npm install               # Install dependencies
cd frontend && npm run dev               # Dev server on :5173, proxies /api → :8080
cd frontend && npm run build             # Type-check (vue-tsc) + Vite build to dist/
cd frontend && npm run build:site        # build + deploy to /www/wwwroot/www.bcbbs3.cn/
```

### Backend
```bash
cd backend && mvn spring-boot:run        # Dev server on :8080
cd backend && mvn clean package          # Build JAR
cd backend && mvn test                   # Run tests
cd backend && mvn test -Dtest=FooTest    # Run a single test class
nohup java -jar target/backend-0.0.1-SNAPSHOT.jar > nohup.out 2>&1 &  # Production
```

**No frontend test runner is configured.** Include manual verification steps for UI/routing changes.

## Architecture

### Request Flow
All API calls go through `frontend/src/api/index.ts` (Axios instance). The interceptor auto-injects `Authorization: Bearer {token}` from localStorage. Global 401 handling redirects to login. All backend responses use `ApiResponse<T>` wrapper: `{ code, message, data, errorId?, timestamp? }`.

### Frontend State & Routing
- **Pinia store** (`stores/auth.ts`): manages JWT token (localStorage), user session, login/logout/register actions
- **Router** (`router/index.ts`): guards check `isAuthenticated`, fetch user profile on refresh, redirect to password change if `needPasswordChange` is set
- **Path alias**: `@` → `frontend/src/` (configured in vite.config.ts)
- **Env files**: `.env.development` (API → localhost:8080), `.env.production` (API → 18118bw.cn)

### Auth & Password Enforcement
Login is at `POST /api/auth/role-login` (requires captcha token). On success the frontend stores the JWT and checks `needPasswordChange`. The backend enforces a 3-strike password change policy:
- Logins 1–2 without changing: `needPasswordChange: true`, increments `loginCountWithoutChange`
- Login 3+: account disabled (`enabled: false`), returns 403
- Recovery (no JWT needed): `POST /api/auth/force-change-password` re-enables the account

`forceLoginMethod` and `passwordChanged`/`loginCountWithoutChange`/`enabled` fields on the `User` entity drive this — touch all four when modifying the flow.

### User Roles
The `User` entity has a `Role` enum: `USER`, `MEMBER`, `AGENT`, `ADMIN`. Login and access lines are role-specific. `AccessLine` entities have a `LineType` (MEMBER or AGENT) determining which login page sees them.

### Backend Layering
Controllers handle HTTP, services hold business logic, repositories handle persistence. DTOs follow `*Request`/`*Response` naming. Packages under `com.bcbbs.backend`: `controller/`, `service/`, `entity/`, `repository/`, `dto/`, `config/`, `security/`.

### Lottery Integration
`LotteryService` proxies upstream lottery data from `bw1284.cc`. Frontend utility `utils/lotteryCalc.ts` handles dragon/tiger, three-pattern, and bull game calculations. `DrawResults.vue` displays results with extended columns.

### Frontend Deployment Cache Strategy
`npm run build:site` copies assets **non-destructively**: old hashed chunks are preserved at the deploy root so users with cached HTML referencing old chunk hashes don't get 404s. Never delete the assets directory before deploying.

### Error Tracking
Every backend error gets a unique 8-char `errorId` via `GlobalExceptionHandler`. Logs are split into `app.log`, `error.log`, `security.log`, `business.log` under `backend/logs/`. Grep by errorId to correlate frontend error responses with backend stack traces.

### Security Config
`SecurityConfig.java` defines the JWT filter chain. Public endpoints: `/api/public/**`, `/api/auth/force-change-password`. Everything else requires a valid JWT. CORS allows: `localhost` (any port), `www.bcbbs3.cn`, `bcbbs3.cn`, `18118bw.cn`.

## Code Style

- **Frontend**: 2-space indentation, Composition API, strict TS. Vue SFCs use PascalCase (`GameHome.vue`)
- **Backend**: 4-space indentation, PascalCase classes, camelCase methods/fields

## Code Rules (from .cursor/rules/xx.mdc)

- Single file must not exceed **500 lines** — split into new files if needed
- All code comments must be in **English**
- No hardcoded mock data — always query real DB or API
- After completing a task, only update docs that were actually changed; do not create new doc files
- Review code logic for correctness before finishing
