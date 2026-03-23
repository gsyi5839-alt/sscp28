# BCBBS3 Project Guidelines

## Project Overview

BCBBS3 is a full-stack web application featuring a lottery game platform with multi-role authentication (USER/MEMBER/AGENT/ADMIN). The system includes captcha-gated login, password change enforcement policies, access line management, search functionality, and lottery game integration with upstream data provider (bw1284.cc).

## Technology Stack

### Frontend
- **Framework**: Vue 3.5 with Composition API (`<script setup lang="ts">`)
- **Language**: TypeScript 5.9 (strict mode)
- **Build Tool**: Vite 7
- **State Management**: Pinia 3
- **UI Library**: Element Plus 2.13 (with Chinese locale)
- **HTTP Client**: Axios
- **Routing**: Vue Router 4 (history mode)
- **Icons**: @element-plus/icons-vue
- **Auto-imports**: unplugin-auto-import, unplugin-vue-components (auto-register Element Plus components)

### Backend
- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Security**: Spring Security + JWT (jjwt 0.12.3)
- **Data Access**: Spring Data JPA/Hibernate
- **Validation**: Jakarta Bean Validation
- **Caching**: Spring Cache with Caffeine
- **Database**: MySQL 8 on localhost:3306
- **Build Tool**: Maven

### Infrastructure
- **Web Server**: Nginx (reverse proxy + static file serving)
- **Database**: MySQL 8, database/user: `xie080886`
- **Cache**: Caffeine (local in-memory)

## Project Structure

```
/root/sscp28/
├── frontend/                    # Vue 3 + TypeScript frontend
│   ├── src/
│   │   ├── api/                 # API client (Axios instance + endpoint wrappers)
│   │   │   └── index.ts         # Axios config, interceptors, API methods
│   │   ├── components/          # Vue components (PascalCase)
│   │   │   ├── GameHeader.vue
│   │   │   ├── MemberSidebar.vue
│   │   │   └── NoticeDialog.vue
│   │   ├── router/              # Vue Router configuration
│   │   │   └── index.ts         # Route definitions + navigation guards
│   │   ├── stores/              # Pinia stores
│   │   │   ├── auth.ts          # JWT token, user session, login/logout
│   │   │   └── cache.ts         # In-memory API response cache with TTL
│   │   ├── utils/               # Utility functions
│   │   │   ├── lotteryCalc.ts   # Dragon/Tiger, Three-pattern, Bull calculations
│   │   │   └── drawResultsConfig.ts
│   │   ├── views/               # Page components (PascalCase)
│   │   │   ├── Search.vue       # Public search page (homepage)
│   │   │   ├── SearchResults.vue
│   │   │   ├── MemberLogin.vue  # Member login with captcha
│   │   │   ├── AgentLogin.vue   # Agent/Admin login with captcha
│   │   │   ├── GameHome.vue     # Lottery game entry point
│   │   │   ├── DrawResults.vue  # Lottery draw results display
│   │   │   ├── Dashboard.vue    # User dashboard
│   │   │   ├── ChangePassword.vue
│   │   │   ├── ForceChangePassword.vue
│   │   │   ├── MemberPanel.vue  # Access lines panel
│   │   │   ├── AccountHistory.vue
│   │   │   ├── BetStatus.vue
│   │   │   └── Register.vue
│   │   ├── views/game/          # Game module components
│   │   │   ├── components/      # Game-specific components
│   │   │   ├── composables/     # Game logic composables
│   │   │   └── constants/       # Game constants
│   │   ├── App.vue              # Root component
│   │   ├── main.ts              # Application entry point
│   │   └── style.css            # Global styles with CSS variables
│   ├── public/                  # Static assets
│   ├── dist/                    # Build output (gitignored)
│   ├── scripts/
│   │   └── deploy-to-site-root.mjs  # Non-destructive deployment script
│   ├── .env.development         # Dev environment variables
│   ├── .env.production          # Production environment variables
│   ├── vite.config.ts           # Vite configuration
│   ├── tsconfig.json            # TypeScript configuration (project references)
│   ├── tsconfig.app.json        # TypeScript app config
│   ├── tsconfig.node.json       # TypeScript node config
│   └── package.json
├── backend/                     # Spring Boot backend
│   ├── src/main/java/com/bcbbs/backend/
│   │   ├── BackendApplication.java
│   │   ├── config/              # Configuration classes
│   │   │   ├── CacheConfig.java         # Caffeine cache configuration
│   │   │   ├── GlobalExceptionHandler.java  # Unified exception handling with errorId
│   │   │   ├── PasswordConfig.java
│   │   │   ├── RateLimitFilter.java
│   │   │   ├── RequestLoggingFilter.java
│   │   │   ├── RestTemplateConfig.java
│   │   │   └── SecurityConfig.java      # Spring Security + CORS config
│   │   ├── controller/          # REST API controllers
│   │   │   ├── AuthController.java      # Login, register, password change
│   │   │   ├── PublicController.java    # Public endpoints (captcha, lines, search)
│   │   │   ├── LotteryController.java   # Lottery data proxy
│   │   │   ├── AccountHistoryController.java
│   │   │   ├── FrontendLogController.java
│   │   │   └── HealthController.java
│   │   ├── dto/                 # Data Transfer Objects (*Request, *Response)
│   │   │   ├── ApiResponse.java         # Unified response wrapper
│   │   │   ├── AuthRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   ├── RoleLoginRequest.java
│   │   │   ├── ChangePasswordRequest.java
│   │   │   ├── ForceChangePasswordRequest.java
│   │   │   ├── LotteryInfoResponse.java
│   │   │   ├── LotteryListResponse.java
│   │   │   └── ...
│   │   ├── entity/              # JPA entities
│   │   │   ├── User.java                # User entity with role enum
│   │   │   ├── AccessLine.java
│   │   │   ├── AccountHistory.java
│   │   │   ├── CaptchaToken.java
│   │   │   ├── SearchItem.java
│   │   │   └── SettlementDetail.java
│   │   ├── repository/          # Spring Data repositories
│   │   ├── security/            # JWT filter and services
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtService.java
│   │   └── service/             # Business logic services
│   │       ├── UserService.java
│   │       ├── CaptchaService.java
│   │       ├── LotteryService.java      # Proxies bw1284.cc upstream
│   │       ├── AccessLineService.java
│   │       └── AccountHistoryService.java
│   ├── src/main/resources/
│   │   ├── application.yml      # Application configuration
│   │   └── logback-spring.xml   # Logging configuration
│   ├── src/test/java/           # Test classes
│   │   └── com/bcbbs/backend/service/
│   │       └── LotteryServiceTest.java
│   └── pom.xml                  # Maven configuration
├── scripts/                     # Operational scripts
│   ├── backup_database.sh       # Database backup script
│   ├── backup_manager.sh        # Interactive backup manager
│   ├── setup-ssl.sh             # SSL certificate setup
│   └── view-logs.sh             # Log viewer utility
├── nginx_configs/               # Nginx configuration files
│   ├── www.bcbbs3.cn.conf       # Main site config
│   └── 18118bw.cn.conf          # API domain config
└── database_backups/            # Database backup files
```

## Build, Test, and Development Commands

### Frontend
```bash
cd frontend

# Install dependencies
npm install

# Development server (port 5173, proxies /api to localhost:8080)
npm run dev

# Production build (type-check + Vite build to dist/)
npm run build

# Deploy to site root (build + deploy to /www/wwwroot/www.bcbbs3.cn/)
npm run build:site

# Preview production build
npm run preview
```

### Backend
```bash
cd backend

# Run development server (port 8080)
mvn spring-boot:run

# Build JAR
mvn clean package

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=LotteryServiceTest

# Production deployment
nohup java -jar target/backend-0.0.1-SNAPSHOT.jar > nohup.out 2>&1 &
```

### Utilities
```bash
# View backend logs
./scripts/view-logs.sh

# Database backup
./scripts/backup_database.sh
```

## Code Style Guidelines

### TypeScript/Vue Frontend
- **Indentation**: 2 spaces
- **API**: Composition API with `<script setup lang="ts">`
- **File naming**: PascalCase for components (e.g., `GameHome.vue`, `NoticeDialog.vue`)
- **Imports**: Use `@/` alias for `frontend/src/`
- **Comments**: All comments must be in English
- **File size limit**: Single file must not exceed 500 lines - split into new files if needed

### UI/UX Guidelines
- **Tab-based Navigation**: Use tab-style switching for content categories instead of page navigation
  - Implement millisecond-level response for tab switches (no page reload)
  - Use `v-show` or dynamic component switching (`<component :is="...">`) instead of router navigation
  - Preload adjacent tab content for instant switching experience
  - Maintain scroll position and state when switching between tabs
  - Use Element Plus `el-tabs` component or custom tab implementation with CSS transitions
  - Avoid `router.push()` for tab navigation within the same feature module
- **Multi-theme Support**: CSS variables in `style.css` support 5 themes (brown/default, red, green, cyan, blue)
  - Themes are applied via `html` class (e.g., `html.red`, `html.blue`)
  - All component styles should use CSS variables from `:root`

### Java Backend
- **Indentation**: 4 spaces
- **Class naming**: PascalCase (e.g., `AuthController`, `LotteryService`)
- **Method/field naming**: camelCase
- **Package naming**: lowercase (e.g., `com.bcbbs.backend`)
- **DTO naming pattern**: `*Request` and `*Response` suffixes
- **Comments**: All comments must be written in English
- **File size limit**: Single file must not exceed 500 lines

### General Rules
- 一直使用中文回复（包括所有技术回复、代码注释说明和文档内容）
- **Comments**: All code comments must be written in English
- **Complete Code**: Write complete, fully functional code - no placeholders or stubs
- **No Hardcoded Mock Data**: Always query real database or API endpoints, never use hardcoded mock data
- **File Size Limit**: Single file must not exceed 500 lines of code
  - When adding business logic, create new files for modular code organization
  - Split large files into smaller, focused modules
- **Directory Structure**: When creating new directories, check if similar directories already exist in the project
  - Frontend files go under `frontend/src/`
  - Backend files go under `backend/src/main/java/com/bcbbs/backend/`
  - Follow existing project structure for file placement
- **Database Management**: When creating new pages or features, check if database tables/entities are needed
  - Create or update JPA entities as required
  - Ensure database schema changes are handled properly
- **Documentation**: Only update modified project files, do not create unnecessary documentation
  - Once familiar with the project, do not write additional documentation files
- **Code Review**: Always perform code review after writing code
  - Check for logical errors and potential bugs
  - Verify correctness of implementation before finishing
- **Bug Prevention**: Strictly review code logic to prevent bugs
- Keep logic layered: controllers handle HTTP, services hold business logic, repositories handle persistence
- After completing a task, only update docs that were actually changed; do not create new doc files
- Review code logic for correctness before finishing

## Testing Instructions

### Backend Tests
- Uses Spring Boot test stack (`spring-boot-starter-test`, `spring-security-test`)
- Test classes located in `backend/src/test/java/`
- Naming convention:
  - Unit tests: `*Test` suffix
  - Integration tests: `*IT` suffix
- No enforced coverage threshold; add/extend tests for any changed authentication, security, or persistence behavior
- Example test: `LotteryServiceTest.java` tests lottery data proxying with MockRestServiceServer

### Frontend Tests
- No test runner is currently configured
- Include clear manual verification steps for UI or routing changes

## Security Considerations

### Authentication & Authorization
- JWT-based authentication with 24-hour expiration
- Token stored in localStorage, auto-injected via Axios interceptor
- Roles: USER, MEMBER, AGENT, ADMIN (defined in `User.Role` enum)
- Role-specific login endpoints with captcha validation

### Password Change Enforcement
- Backend enforces 3-strike password change policy for MEMBER and AGENT roles
- Logins 1-2 without changing password: `needPasswordChange: true`, increments `loginCountWithoutChange`
- Login 3+: Account disabled, user must use force-change-password endpoint
- Recovery endpoint: `POST /api/auth/force-change-password` (no JWT required)

### Security Configuration
- Public endpoints: `/api/public/**`, `/api/auth/force-change-password`, auth login/register endpoints
- All other endpoints require valid JWT
- CORS configured for: `localhost` (any port), `www.bcbbs3.cn`, `bcbbs3.cn`, `18118bw.cn`

### Error Tracking
- Every backend error gets a unique 8-character `errorId` via `GlobalExceptionHandler`
- Sensitive parameters (password, token) are masked in logs
- Security events logged to dedicated `security.log`

## Architecture Details

### Request Flow
1. Frontend API calls go through `frontend/src/api/index.ts` (Axios instance)
2. Interceptor auto-injects `Authorization: Bearer {token}` from localStorage
3. Global 401 handling redirects to login page
4. Backend responses use `ApiResponse<T>` wrapper: `{ code, message, data, errorId?, timestamp? }`
5. Public API calls append `t: Date.now()` parameter to bypass CDN/proxy/browser cache

### Frontend State & Routing
- **Pinia store** (`stores/auth.ts`): Manages JWT token, user session, login/logout actions
- **Router guards**: Check `isAuthenticated`, fetch user profile on refresh
- **Password enforcement**: Redirects to `/change-password` if `needPasswordChange` is set
- **Path alias**: `@` → `frontend/src/`
- **Auto-imports**: Vite plugins auto-register Element Plus components
- **Build meta**: Vite injects `<meta name="frontend-build" content="ISO timestamp">` into HTML

### Backend Layering
- **Controllers**: Handle HTTP requests, input validation, response wrapping
- **Services**: Contain business logic, transactional operations
- **Repositories**: Data access layer (Spring Data JPA)
- **Entities**: JPA entities with Lombok `@Data`/`@Builder`
- **DTOs**: Request/response objects for API contracts

### Caching Strategy
Backend uses Caffeine cache with different TTLs:
- `lotteryGames`: 1 hour (lottery catalog)
- `lotteryInfo`: 5 seconds (current issue info)
- `lotteryListFirstPage`: 5 seconds (latest draws)
- `lotteryListOtherPages`: 15 minutes (historical data)
- `accessLines`: 6 hours (rarely changes)

Frontend has in-memory cache store (`stores/cache.ts`) with TTL support for API responses.

### Lottery Integration
- `LotteryService` proxies upstream lottery data from `bw1284.cc`
- Frontend utility `utils/lotteryCalc.ts` handles game calculations:
  - Dragon/Tiger (龙虎)
  - Three-pattern (豹子/顺子/对子/半顺/杂六)
  - Bull game (牛牛)
  - Australian Lucky 10 racing games
  - BaoDou (宝斗) calculations
  - NiuNiu for 10-ball games

## Deployment Information

### Frontend Deployment
- Build output: `frontend/dist/`
- Deploy script: `frontend/scripts/deploy-to-site-root.mjs`
- **Important**: Deployment is non-destructive - old hashed chunks are preserved to avoid 404s for cached HTML referencing old chunk hashes
- Never delete the assets directory before deploying
- Site root: `/www/wwwroot/www.bcbbs3.cn/`

### Backend Deployment
- Build artifact: `backend/target/backend-0.0.1-SNAPSHOT.jar`
- Production run with `nohup` for background execution
- Logs written to `backend/logs/` directory

### Nginx Configuration
- SPA support: `try_files $uri $uri/ /index.html`
- API proxy: `/api/` → `http://127.0.0.1:8080/api/`
- Static file caching: 30d for images, 12h for JS/CSS
- CORS headers added for API endpoints
- index.html has no-cache headers to prevent stale chunk references

## Error Handling and Logging

### Log Files (backend/logs/)
- `app.log`: All application logs
- `error.log`: ERROR level only
- `warn.log`: WARN level only
- `security.log`: Security-related events
- `business.log`: Business logic events
- `api.log`: API request logs
- `debug.log`: DEBUG level (development only)
- `frontend.log`: Logs collected from browser/client

### Log Correlation
- Grep by `errorId` to correlate frontend error responses with backend stack traces
- Each error response includes unique 8-character error ID

## Database Configuration

### Connection
- **URL**: `jdbc:mysql://localhost:3306/xie080886`
- **Username/Password**: `xie080886`
- **Driver**: MySQL Connector/J
- **Pool**: HikariCP (max 10 connections, min 5 idle)

### Schema Management
- Managed by Hibernate `ddl-auto: update`
- No manual migrations needed
- Show SQL enabled in development

## Environment Variables

### Frontend (.env files)
```
# Development (.env.development)
VITE_API_URL=http://localhost:8080/api
VITE_APP_TITLE=BCBBS Platform

# Production (.env.production)
VITE_API_URL=https://18118bw.cn/api
VITE_APP_TITLE=BCBBS Platform
```

### Backend (application.yml)
Key configurations:
- Server port: 8080
- JWT expiration: 24 hours (86400000ms)
- CORS allowed origins: localhost, www.bcbbs3.cn, bcbbs3.cn, 18118bw.cn

## Commit & Pull Request Guidelines

- Use concise, imperative commit subjects (e.g., `Add ...`, `Update ...`)
- Occasional `chore:` prefixes acceptable
- Keep commit scope focused; avoid mixing frontend, backend, and ops changes unless required
- PRs should include:
  - What changed and why
  - Touched paths (e.g., `frontend/src/views/...`, `backend/src/main/java/...`)
  - Verification commands run (`npm run build`, `mvn test`, etc.)
  - Screenshots for UI changes
  - Linked issue/task when available
