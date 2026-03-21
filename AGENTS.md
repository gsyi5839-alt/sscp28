# Repository Guidelines

## Project Structure & Module Organization
- `frontend/`: Vue 3 + TypeScript app (Vite). Main code is in `frontend/src/` with `views/`, `components/`, `stores/`, `router/`, and `api/`.
- `backend/`: Spring Boot API (Java 17). Source is in `backend/src/main/java/com/bcbbs/backend/`, organized by `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `config/`, and `security/`.
- `backend/src/main/resources/`: runtime config (`application.yml`) and logging config.
- `scripts/`, `deploy.sh`, `nginx_configs/`: operational/deployment helpers.
- `database_backups/` and root `assets/`: tracked operational assets; handle with care.

## Build, Test, and Development Commands
- Frontend install: `cd frontend && npm install`
- Frontend dev server: `cd frontend && npm run dev` (Vite on local dev port)
- Frontend production build: `cd frontend && npm run build`
- Frontend build + site deploy: `cd frontend && npm run build:site`
- Backend local run: `cd backend && mvn spring-boot:run`
- Backend package: `cd backend && mvn clean package`
- Backend tests: `cd backend && mvn test`
- Full environment provisioning/deploy script: `./deploy.sh` (server-oriented, requires review before running).

## Coding Style & Naming Conventions
- TypeScript/Vue: follow current style in `frontend/src` (2-space indentation, Composition API, strict TS settings).
- Vue SFCs and component files use PascalCase (for example, `GameHome.vue`, `NoticeDialog.vue`).
- Java: 4-space indentation, PascalCase classes, camelCase methods/fields, package names lowercase.
- DTO naming pattern: `*Request` and `*Response`.
- Keep logic layered: controllers handle HTTP, services hold business logic, repositories handle persistence.

## Testing Guidelines
- Backend uses Spring Boot test stack (`spring-boot-starter-test`, `spring-security-test`).
- Add tests under `backend/src/test/java` with class names ending in `Test` (unit) or `IT` (integration).
- No enforced coverage threshold is configured; add/extend tests for any changed authentication, security, or persistence behavior.
- Frontend currently has no configured test runner; include clear manual verification steps in PRs for UI or routing changes.

## Commit & Pull Request Guidelines
- Existing history favors concise, imperative subjects (for example, `Add ...`, `Update ...`) and occasional `chore:` prefixes.
- Keep commit scope focused; avoid mixing frontend, backend, and ops changes unless required.
- PRs should include:
  - What changed and why
  - Touched paths (for example, `frontend/src/views/...`, `backend/src/main/java/...`)
  - Verification commands run (`npm run build`, `mvn test`, etc.)
  - Screenshots for UI changes and linked issue/task when available
