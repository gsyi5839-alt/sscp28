# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BCBBS3 is a full-stack web application with a Vue 3 + TypeScript frontend and Spring Boot backend. The application features multi-role authentication (Member/Agent/Admin), password management, search functionality, and access line management.

## Common Commands

### Frontend (Vue 3 + TypeScript + Vite)

```bash
# Development server (port 5173, proxies /api to localhost:8080)
cd frontend && npm run dev

# Type check and production build
cd frontend && npm run build

# Build and deploy to site root
cd frontend && npm run build:site

# Preview production build
cd frontend && npm run preview
```

### Backend (Spring Boot + Maven)

```bash
# Run backend server (port 8080)
cd backend && mvn spring-boot:run

# Build JAR package
cd backend && mvn clean package

# Run packaged JAR
cd backend && java -jar target/backend-0.0.1-SNAPSHOT.jar

# Run in background (production)
cd backend && nohup java -jar target/backend-0.0.1-SNAPSHOT.jar > nohup.out 2>&1 &
```

### Database

**Connection**: MySQL on localhost:3306
**Database**: xie080886
**Credentials**: xie080886/xie080886
**Schema Management**: Hibernate auto-update (ddl-auto: update)

## Architecture Overview

### Frontend Structure

```
frontend/src/
├── api/            # Axios HTTP client with interceptors
│   └── index.ts    # API namespaces: authApi, searchApi, linesApi, captchaApi, passwordApi
├── views/          # Page components
│   ├── MemberLogin.vue        # Member login with captcha
│   ├── AgentLogin.vue         # Agent login with captcha
│   ├── ChangePassword.vue     # Authenticated password change
│   ├── ForceChangePassword.vue # Account unlock (no auth required)
│   ├── Search.vue & SearchResults.vue
│   ├── GameHome.vue           # Game platform gateway
│   ├── MemberPanel.vue        # Member dashboard
│   └── UserAgreement.vue
├── stores/         # Pinia state management
│   └── auth.ts     # Token, session, login/logout, password change
├── router/         # Vue Router with auth guards
└── components/     # Reusable UI components
```

**Key Frontend Patterns**:
- **State**: Pinia composition API stores
- **HTTP**: Axios with automatic Bearer token injection from localStorage
- **Auth Guard**: Router checks `isAuthenticated` before allowing protected routes
- **Error Handling**: Global 401 redirects to login page
- **UI Framework**: Element Plus components

### Backend Structure

```
backend/src/main/java/com/bcbbs/backend/
├── config/
│   ├── SecurityConfig.java           # Spring Security + JWT filter chain
│   ├── GlobalExceptionHandler.java   # Centralized error handling with error IDs
│   ├── RequestLoggingFilter.java     # Request/response logging
│   └── PasswordConfig.java           # BCrypt encoder
├── controller/
│   ├── AuthController.java           # /api/auth/** endpoints
│   └── PublicController.java         # /api/public/** endpoints
├── entity/
│   ├── User.java                     # Users with role-based auth
│   ├── AccessLine.java               # Member/Agent access URLs
│   ├── CaptchaToken.java             # One-time captcha tokens
│   └── SearchItem.java               # Search content
├── service/
│   ├── UserService.java              # User operations + UserDetailsService
│   ├── CaptchaService.java           # Captcha generation/validation
│   ├── SearchService.java
│   └── AccessLineService.java
├── repository/                       # Spring Data JPA repositories
└── security/
    ├── JwtService.java               # Token generation/validation
    └── JwtAuthenticationFilter.java  # Per-request token extraction
```

**Key Backend Patterns**:
- **Layered Architecture**: Controller → Service → Repository
- **Security**: JWT stateless authentication with 24-hour token expiration
- **Error Tracking**: Every error gets unique 8-char ID for correlation with logs
- **Logging**: Multi-channel logging (app.log, error.log, security.log, business.log)
- **Response Format**: All endpoints return `ApiResponse<T>` wrapper

## Authentication Flow

### JWT Authentication

1. **Login**: POST `/api/auth/role-login` with captcha
   - Validates captcha (4-digit code, 5-minute expiration)
   - Authenticates credentials via Spring Security
   - Verifies role (MEMBER/AGENT/ADMIN)
   - Returns JWT token in `AuthResponse`

2. **Token Storage**: Frontend stores token in localStorage

3. **Subsequent Requests**: Axios interceptor adds `Authorization: Bearer {token}` header

4. **Token Validation**: `JwtAuthenticationFilter` extracts and validates token on each request

### Password Change Enforcement

**Critical Feature**: Members and Agents must change initial password after first login.

**Flow**:
1. First login without password change:
   - Response includes `needPasswordChange: true`
   - Frontend force-redirects to `/change-password`
   - Backend increments `loginCountWithoutChange`

2. Second login without change:
   - Same warning, counter increments to 2

3. Third login without change:
   - Account disabled (`enabled: false`)
   - Returns 403 with "账户已停用" message
   - Frontend redirects to `/force-change-password`

4. **Account Unlock**: POST `/api/auth/force-change-password` (public endpoint)
   - No JWT required (allows locked-out users to unlock)
   - Validates old password + role + captcha
   - Updates password, sets `passwordChanged: true`, enables account
   - Returns new JWT token

**Important**: Never leaves users permanently locked; `/force-change-password` is the recovery mechanism.

## Database Entities

### Users Table
- **Roles**: USER, ADMIN, MEMBER, AGENT
- **Password Security**: BCrypt hashed
- **Password Tracking**:
  - `passwordChanged` (Boolean) - Tracks if initial password modified
  - `loginCountWithoutChange` (Integer) - Counts logins without change
  - `enabled` (Boolean) - Soft disable for password policy enforcement

### AccessLines Table
- **Types**: MEMBER, AGENT
- Stores access URLs for different user roles
- Filterable by type and active status

### CaptchaTokens Table
- 4-digit numeric codes
- UUID tokens for one-time use
- 5-minute expiration
- Marked as `used` after validation

### SearchItems Table
- Keyword-based search content
- Queried via `/api/public/search?q=keyword`

## API Endpoints

### Authentication (`/api/auth`)
```
POST /login                   # Basic login
POST /role-login              # Member/Agent login with captcha
POST /register                # User registration
POST /change-password         # Change password (requires JWT)
POST /force-change-password   # Account unlock (public, no JWT)
GET  /me                      # Get current user (requires JWT)
```

### Public (`/api/public`)
```
GET /search?q=keyword         # Search items
GET /lines?type=MEMBER|AGENT  # Get active access lines
GET /captcha                  # Generate captcha token
GET /health                   # Health check
```

### Response Format
All endpoints return standardized `ApiResponse`:
```json
{
  "code": 200,
  "message": "Success",
  "data": { /* payload */ },
  "errorId": "ABC12345",       // Only on errors
  "timestamp": "2026-01-17..." // Only on errors
}
```

## Configuration Files

### Frontend: vite.config.ts
- Dev server proxy: `/api` → `http://localhost:8080`
- Build output: `frontend/dist/`
- Path alias: `@` → `src`

### Backend: application.yml
```yaml
server.port: 8080
spring.datasource.url: jdbc:mysql://localhost:3306/xie080886
jwt.secret: YmNiYnMzLWJhY2tlbmQtand0LXNlY3JldC1rZXktMjAyNi1wcm9kdWN0aW9uLXNlY3VyZS10b2tlbg==
jwt.expiration: 86400000  # 24 hours
cors.allowed-origins: http://localhost:5173,https://www.bcbbs3.cn,http://www.bcbbs3.cn
```

### Logging: logback-spring.xml
- **app.log**: All logs (rolling, 50MB, 30-day retention)
- **error.log**: ERROR level only
- **security.log**: Authentication/authorization events
- **business.log**: Business logic tracking
- Async appenders for performance

## Deployment

### Frontend Deployment Process
```bash
npm run build:site
```
This runs:
1. Type checking: `vue-tsc -b`
2. Vite build to `frontend/dist/`
3. Deploy script: Copies assets and index.html to site root
   - **Non-destructive**: Preserves old hashed assets to prevent 404s for cached chunks
   - Copies `dist/assets/*` to `/www/wwwroot/www.bcbbs3.cn/assets/`
   - Copies `index.html` to site root

### Backend Deployment
Production backend runs via nohup:
```bash
cd backend
nohup java -jar target/backend-0.0.1-SNAPSHOT.jar > nohup.out 2>&1 &
```
Logs output to:
- `backend/nohup.out` (stdout/stderr)
- `logs/` directory (structured logs via logback)

## Code Standards (from .cursor/rules/xx.mdc)

1. **File Size**: Single file must not exceed 500 lines
2. **Comments**: All code must have English comments
3. **Package Organization**: Check existing directory structure before creating new files
4. **No Mock Data**: Always query real database or API
5. **Code Review**: Review code logic before completion to avoid bugs
6. **Documentation**: Only update changed files in existing docs; don't create unnecessary docs

## Important Implementation Notes

### Password Validation
When implementing password changes, always:
- Verify old password matches current hash
- Use BCrypt encoder from `PasswordConfig`
- Reset `loginCountWithoutChange` to 0
- Set `passwordChanged` flag to true
- Re-enable account if disabled

### Error Logging
GlobalExceptionHandler automatically:
- Generates unique error IDs for tracking
- Logs full context (IP, User-Agent, parameters)
- Masks sensitive parameters (password, token)
- Resolves client IP with proxy header support (`X-Forwarded-For`)

### CORS Handling
SecurityConfig allows:
- Origins: localhost (any port), www.bcbbs3.cn, bcbbs3.cn
- Methods: GET, POST, PUT, DELETE, OPTIONS
- Credentials: true (allows cookies/auth headers)

### Frontend Build Cache Strategy
The deployment script preserves old asset files to handle cache inconsistencies:
- Users with cached HTML may reference old chunk hashes
- Non-destructive copy ensures old chunks remain accessible
- Prevents 404 errors during gradual cache expiration

## Related Documentation

- **PASSWORD_CHANGE_FEATURE.md**: Detailed password enforcement flows
- **LOGGING_GUIDE.md**: Logging configuration and best practices
- **LOTTERY_API_INTEGRATION_GUIDE.md**: External API integration specs (if implementing lottery features)
- **PROJECT_ARCHITECTURE.md**: Comprehensive architecture documentation

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Frontend Framework | Vue | 3.5.24 |
| Build Tool | Vite | 7.2.4 |
| Language | TypeScript | 5.9.3 |
| State Management | Pinia | 3.0 |
| Routing | Vue Router | 4.6 |
| UI Components | Element Plus | 2.13.0 |
| HTTP Client | Axios | 1.13.2 |
| Backend Framework | Spring Boot | 3.2.0 |
| Language | Java | 17 |
| Security | Spring Security | 6.x |
| JWT | JJWT | 0.12.3 |
| ORM | Spring Data JPA | 6.x |
| Database | MySQL | 8.x |
| Build Tool | Maven | 3.x |

## Development Workflow

1. **Starting Development**:
   - Terminal 1: `cd backend && mvn spring-boot:run`
   - Terminal 2: `cd frontend && npm run dev`
   - Access: http://localhost:5173

2. **Making Changes**:
   - Frontend: Hot reload automatically
   - Backend: Spring Boot DevTools auto-restart on compile

3. **Testing Authentication**:
   - Create user via `/api/auth/register`
   - Login via `/api/auth/role-login` with captcha
   - Test password change enforcement flow

4. **Building for Production**:
   - Frontend: `npm run build:site`
   - Backend: `mvn clean package`
   - Deploy JAR to production server

5. **Troubleshooting**:
   - Frontend errors: Check browser console and network tab
   - Backend errors: Check logs in `logs/` directory or `backend/nohup.out`
   - Look for error IDs in error responses and grep logs for correlation
