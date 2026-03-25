# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BCBBS3 is a full-stack lottery game platform with multi-role authentication (USER/MEMBER/AGENT/ADMIN), captcha-gated login, password change enforcement, access line management, search, and lottery game integration with upstream provider (bw1284.cc).

## Tech Stack

- **Frontend**: Vue 3.5 (Composition API), TypeScript 5.9, Vite 7, Pinia 3, Element Plus 2.13 (Chinese locale), Axios, Vue Router 4
- **Backend**: Spring Boot 3.2, Java 17, Spring Security + JWT (jjwt 0.12.3), Spring Data JPA/Hibernate, Caffeine cache, Jakarta Bean Validation
- **Infrastructure**: MySQL 8 (localhost:3306, db/user/pass: `xie080886`), Nginx (reverse proxy + static serving)
- **Schema**: Hibernate `ddl-auto: update` — no manual migrations. DB auto-created if not exists (`createDatabaseIfNotExist=true`). Timezone: `Asia/Shanghai`

## Commands

### Frontend
```bash
cd frontend && npm install               # Install dependencies
cd frontend && npm run dev               # Dev server on :5173, proxies /api → :8080
cd frontend && npm run build             # Type-check (vue-tsc) + Vite build to dist/
cd frontend && npm run clean:site:assets # Clean old files under /www/wwwroot/www.bcbbs3.cn/assets/
cd frontend && npm run build:site        # One-command clean + build + deploy to /www/wwwroot/www.bcbbs3.cn/
cd frontend && npm run build:site:dry    # Dry-run clean + build + deploy flow
cd frontend && npm run deploy            # Deploy only (no rebuild)
cd frontend && npm run preview           # Preview production build locally
```

### Backend
```bash
cd backend && mvn spring-boot:run        # Dev server on :8080
cd backend && mvn clean package          # Build JAR
cd backend && mvn test                   # Run tests
cd backend && mvn test -Dtest=FooTest    # Run a single test class
nohup java -jar target/backend-0.0.1-SNAPSHOT.jar > nohup.out 2>&1 &  # Production
```

### Utilities
```bash
./scripts/view-logs.sh                   # Interactive log viewer for backend logs
./scripts/backup_database.sh             # Database backup
./scripts/backup_manager.sh              # Backup management (list, restore, cleanup)
```

**No frontend test runner is configured.** Include manual verification steps for UI/routing changes.

## Architecture

### Request Flow
1. Frontend API calls go through `frontend/src/api/index.ts` (Axios instance)
2. Interceptor auto-injects `Authorization: Bearer {token}` from localStorage
3. Global 401 handling redirects to login page
4. Backend responses use `ApiResponse<T>` wrapper: `{ code, message, data, errorId?, timestamp? }`
5. Public API calls append `t: Date.now()` parameter to bypass CDN/proxy/browser cache

### Frontend Key Details
- **API client**: Single file `api/index.ts` — all API calls (auth, lottery, bets, notices, account, etc.) are exported from here. No sub-modules.
- **Pinia stores**: `stores/auth.ts` (JWT + user session), `stores/cache.ts` (in-memory API cache with TTL)
- **Router** (`router/index.ts`): guards check `isAuthenticated`, fetch user profile on refresh, redirect to `/change-password` if `needPasswordChange` is set
- **Protected routes**: `dashboard`, `changePassword`, `userAgreement`, `gameHome`, `betStatus`, `accountHistory`
- **Path alias**: `@` → `frontend/src/`
- **Auto-imports**: Vite plugins (`unplugin-auto-import`, `unplugin-vue-components`) auto-register Element Plus components — no manual imports needed
- **Build meta**: Vite injects `<meta name="frontend-build" content="ISO timestamp">` into HTML at build time
- **Env files**: `.env.development` (API → localhost:8080), `.env.production` (API → 18118bw.cn)

### Auth & Password Enforcement
Login at `POST /api/auth/role-login` (requires captcha token). Backend enforces 3-strike password change policy:
- Logins 1–2 without changing: `needPasswordChange: true`, increments `loginCountWithoutChange`
- Login 3+: account disabled (`enabled: false`), returns 403
- Recovery (no JWT needed): `POST /api/auth/force-change-password` re-enables the account

Key fields on `User` entity: `forceLoginMethod`, `passwordChanged`, `loginCountWithoutChange`, `enabled` — touch all four when modifying the flow.

### User Roles
`User.Role` enum: `USER`, `MEMBER`, `AGENT`, `ADMIN`. `AccessLine` entities have `LineType` (MEMBER or AGENT) determining which login page sees them.

### Backend Layering
Controllers → Services → Repositories. DTOs use `*Request`/`*Response` naming. Entities use Lombok `@Data`/`@Builder`. Packages under `com.bcbbs.backend`: `controller/`, `service/`, `entity/`, `repository/`, `dto/`, `config/`, `security/`.

### Security Config
- JWT authentication with 24-hour expiration (86400000ms)
- **Public endpoints** (no JWT required): `/api/public/**`, `/api/auth/login`, `/api/auth/role-login`, `/api/auth/register`, `/api/auth/force-change-password`, `/api/log/frontend/**`, all OPTIONS preflight requests
- CORS allows: `localhost` (any port), `www.bcbbs3.cn`, `bcbbs3.cn`, `18118bw.cn`

### Caching Strategy
Backend Caffeine cache TTLs:
- `lotteryGames`: 1 hour | `lotteryInfo`: 5 seconds | `lotteryListFirstPage`: 5 seconds
- `lotteryListOtherPages`: 15 minutes | `accessLines`: 6 hours

Frontend `stores/cache.ts` provides TTL-based caching (e.g., lottery games cached 1 hour).

### Game Module Architecture
The game feature is a self-contained module under `views/game/` with its own sub-structure:
- `components/` — game-specific UI (LotteryHeader, BettingBalls, QuickBetBar, etc.) + `ssc/` sub-folder for SSC game variants
- `composables/` — game-specific hooks (`useLotteryData`, `useBetting`, `useDragonLeaderboard`, `useOddsStyles`, `useRecentDraws`, `useSummaryRoad`)
- `constants/` — odds tables (`odds.ts`, `sscOdds.ts`) and notice content

Game switching: `GameHeader` exposes `activeGameKey` via `defineModel`. `gameConfig.ts` (in `utils/`) maps gameKey → lotCode/gameName. `useLotteryData` accepts a reactive `lotCode` ref and auto-refetches on change.

### Lottery Integration
- `LotteryService` proxies upstream data from `bw1284.cc`
- `utils/lotteryCalc.ts`: Dragon/Tiger, Three-pattern (豹子/顺子/对子/半顺/杂六), Bull (牛牛), Australian Lucky 10
- `DrawResults.vue` displays results with extended columns

### Error Tracking & Logging
- Every backend error gets unique 8-char `errorId` via `GlobalExceptionHandler`
- Sensitive parameters (password, token) masked in logs
- Log files in `backend/logs/`: `app.log`, `error.log`, `warn.log`, `security.log`, `business.log`, `api.log`, `debug.log`, `frontend.log`
- Grep by errorId to correlate frontend error responses with backend stack traces

### Deployment

**Frontend**: `npm run build:site` performs clean + build + deploy to `/www/wwwroot/www.bcbbs3.cn/`.

**IMPORTANT — One-Command Clean Deploy Rule**: Every production deployment MUST follow this sequence:
1. **Run one command**: `cd /root/sscp28/frontend && npm run build:site`
   - Internally executes: clean old assets -> build -> deploy
2. **Verify** the deployed file count: `ls /www/wwwroot/www.bcbbs3.cn/assets/ | wc -l` (should match the current build output, typically 60-80 files, NOT thousands)

Skipping the clean step can cause old hashed chunks to accumulate (7000+ files), and browsers with cached `index.html` may load stale chunks, causing the site to appear "rolled back" to an old version.

**Backend**: `backend/target/backend-0.0.1-SNAPSHOT.jar` run with `nohup`.

**Backend Deployment with Process Manager**:
When using a process manager (e.g., pm2) to manage the backend service, you **MUST** follow this sequence:
1. **Stop** the managed process before building: `pm2 stop <app-name>`
2. **Build** the JAR: `mvn clean package`
3. **Deploy** the new JAR
4. **Start** the managed process after deployment: `pm2 start <app-name>` (or `pm2 restart <app-name>`)

This prevents file locking issues and ensures a clean deployment.

**Nginx**: SPA `try_files` → `/index.html`, API proxy `/api/` → `127.0.0.1:8080`, static caching (30d images, 12h JS/CSS), index.html has `no-cache` headers.

### Database
- **URL**: `jdbc:mysql://localhost:3306/xie080886?serverTimezone=Asia/Shanghai&createDatabaseIfNotExist=true`
- **Pool**: HikariCP (max 10 connections, min 5 idle)

## Code Style

### Frontend
- 2-space indentation, Composition API with `<script setup lang="ts">`, PascalCase file names
- Use `@/` import alias for `frontend/src/`

### Backend
- 4-space indentation, PascalCase classes, camelCase methods/fields, `*Request`/`*Response` DTO naming

### UI/UX Guidelines
- **Tab-based Navigation**: Use `v-show` or `<component :is="...">` for tab switches (no page reload). Avoid `router.push()` within the same feature module. Use Element Plus `el-tabs` or custom tabs with CSS transitions.

### General Rules
- 一直使用中文回复（包括所有技术回复、代码注释说明和文档内容）
- All code comments must be in **English**
- Write complete, fully functional code — no placeholders or stubs
- No hardcoded mock data — always query real DB or API
- Single file must not exceed **500 lines** — split into new files if needed
- Frontend files under `frontend/src/`, backend files under `backend/src/main/java/com/bcbbs/backend/`
- When creating features, check if new JPA entities/tables are needed
- Only update docs that were actually changed; do not create new doc files
- Review code logic for correctness before finishing

## Testing

### Backend
- Spring Boot test stack (`spring-boot-starter-test`, `spring-security-test`)
- Unit tests: `*Test` suffix, Integration tests: `*IT` suffix
- No coverage threshold; add tests for authentication, security, or persistence changes
- Example: `LotteryServiceTest.java` uses MockRestServiceServer

### Frontend
- No test runner configured — include manual verification steps for UI/routing changes

## Commit Guidelines
- Concise, imperative subjects (e.g., `Add ...`, `Update ...`). Occasional `chore:` prefix OK
- Keep scope focused; avoid mixing frontend/backend/ops changes unless required
