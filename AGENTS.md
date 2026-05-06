# BCBBS3 项目指南

# 代码规范500行 超出需要进行管理，任何 AI 代理、都必须先阅读并遵守这里的规则，如果不合理超出请自行管理拆分出来，不管前面谁做的请帮忙擦屁股

# 工业级代码规范

## 项目概述

BCBBS3 是一个全栈 Web 应用程序，提供带有多角色身份验证（USER/MEMBER/AGENT/ADMIN）的彩票游戏平台。系统包含验证码登录、强制密码修改策略、访问线路管理、搜索功能以及与上游数据提供商（bw1284.cc）的彩票游戏集成。

## 技术栈

### 前端
- **框架**: Vue 3.5，使用 Composition API（组合式 API）(`<script setup lang="ts">`)
- **语言**: TypeScript 5.9（严格模式）
- **构建工具**: Vite 7
- **状态管理**: Pinia 3
- **UI 库**: Element Plus 2.13（中文语言包）
- **HTTP 客户端**: Axios
- **路由**: Vue Router 4（history 模式）
- **图标**: @element-plus/icons-vue
- **自动导入**: unplugin-auto-import、unplugin-vue-components（自动注册 Element Plus 组件）

### 后端
- **框架**: Spring Boot 3.2
- **语言**: Java 17
- **安全**: Spring Security + JWT (jjwt 0.12.3)
- **数据访问**: Spring Data JPA/Hibernate
- **校验**: Jakarta Bean Validation
- **缓存**: Spring Cache + Caffeine
- **数据库**: MySQL 8，地址 localhost:3306
- **构建工具**: Maven

### 基础设施
- **Web 服务器**: Nginx（反向代理 + 静态文件服务）
- **数据库**: MySQL 8，数据库/用户名: `xie080886`
- **缓存**: Caffeine（本地内存缓存）

## 项目结构

```
/root/sscp28/
├── frontend/                    # Vue 3 + TypeScript 前端
│   ├── src/
│   │   ├── api/                 # API 客户端（Axios 实例 + 接口封装）
│   │   │   └── index.ts         # Axios 配置、拦截器、API 方法
│   │   ├── components/          # Vue 组件（大驼峰命名）
│   │   │   ├── GameHeader.vue
│   │   │   ├── MemberSidebar.vue
│   │   │   └── NoticeDialog.vue
│   │   ├── router/              # Vue Router 配置
│   │   │   └── index.ts         # 路由定义 + 导航守卫
│   │   ├── stores/              # Pinia stores
│   │   │   ├── auth.ts          # JWT token、用户会话、登录/登出
│   │   │   └── cache.ts         # 带 TTL 的内存 API 响应缓存
│   │   ├── utils/               # 工具函数
│   │   │   ├── lotteryCalc.ts   # 龙虎、三形态、牛牛计算
│   │   │   └── drawResultsConfig.ts
│   │   ├── views/               # 页面组件（大驼峰命名）
│   │   │   ├── Search.vue       # 公开搜索页（首页）
│   │   │   ├── SearchResults.vue
│   │   │   ├── MemberLogin.vue  # 会员登录（带验证码）
│   │   │   ├── AgentLogin.vue   # 代理/管理员登录（带验证码）
│   │   │   ├── GameHome.vue     # 彩票游戏入口
│   │   │   ├── DrawResults.vue  # 开奖结果展示
│   │   │   ├── Dashboard.vue    # 用户仪表盘
│   │   │   ├── ChangePassword.vue
│   │   │   ├── ForceChangePassword.vue
│   │   │   ├── MemberPanel.vue  # 访问线路面板
│   │   │   ├── AccountHistory.vue
│   │   │   ├── BetStatus.vue
│   │   │   └── Register.vue
│   │   ├── views/game/          # 游戏模块组件
│   │   │   ├── components/      # 游戏专用组件
│   │   │   ├── composables/     # 游戏逻辑 composables
│   │   │   └── constants/       # 游戏常量
│   │   ├── App.vue              # 根组件
│   │   ├── main.ts              # 应用入口
│   │   └── style.css            # 全局样式（CSS 变量）
│   ├── public/                  # 静态资源
│   ├── dist/                    # 构建输出（已 gitignore）
│   ├── scripts/
│   │   ├── clean-site-assets.mjs    # 部署前清理旧站点资源
│   │   └── deploy-to-site-root.mjs  # 非破坏性部署脚本
│   ├── .env.development         # 开发环境变量
│   ├── .env.production          # 生产环境变量
│   ├── vite.config.ts           # Vite 配置
│   ├── tsconfig.json            # TypeScript 配置（项目引用）
│   ├── tsconfig.app.json        # TypeScript 应用配置
│   ├── tsconfig.node.json       # TypeScript node 配置
│   └── package.json
├── backend/                     # Spring Boot 后端
│   ├── src/main/java/com/bcbbs/backend/
│   │   ├── BackendApplication.java
│   │   ├── config/              # 配置类
│   │   │   ├── CacheConfig.java         # Caffeine 缓存配置
│   │   │   ├── GlobalExceptionHandler.java  # 统一异常处理（带 errorId）
│   │   │   ├── PasswordConfig.java
│   │   │   ├── RateLimitFilter.java
│   │   │   ├── RequestLoggingFilter.java
│   │   │   ├── RestTemplateConfig.java
│   │   │   └── SecurityConfig.java      # Spring Security + CORS 配置
│   │   ├── controller/          # REST API 控制器
│   │   │   ├── AuthController.java      # 登录、注册、修改密码
│   │   │   ├── PublicController.java    # 公开接口（验证码、线路、搜索）
│   │   │   ├── LotteryController.java   # 彩票数据代理
│   │   │   ├── AccountHistoryController.java
│   │   │   ├── FrontendLogController.java
│   │   │   └── HealthController.java
│   │   ├── dto/                 # 数据传输对象（*Request、*Response）
│   │   │   ├── ApiResponse.java         # 统一响应包装
│   │   │   ├── AuthRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   ├── RoleLoginRequest.java
│   │   │   ├── ChangePasswordRequest.java
│   │   │   ├── ForceChangePasswordRequest.java
│   │   │   ├── LotteryInfoResponse.java
│   │   │   ├── LotteryListResponse.java
│   │   │   └── ...
│   │   ├── entity/              # JPA 实体
│   │   │   ├── User.java                # 用户实体（带角色枚举）
│   │   │   ├── AccessLine.java
│   │   │   ├── AccountHistory.java
│   │   │   ├── CaptchaToken.java
│   │   │   ├── SearchItem.java
│   │   │   └── SettlementDetail.java
│   │   ├── repository/          # Spring Data 仓库
│   │   ├── security/            # JWT 过滤器和服务
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtService.java
│   │   └── service/             # 业务逻辑服务
│   │       ├── UserService.java
│   │       ├── CaptchaService.java
│   │       ├── LotteryService.java      # 代理 bw1284.cc 上游数据
│   │       ├── AccessLineService.java
│   │       └── AccountHistoryService.java
│   ├── src/main/resources/
│   │   ├── application.yml      # 应用配置
│   │   └── logback-spring.xml   # 日志配置
│   ├── src/test/java/           # 测试类
│   │   └── com/bcbbs/backend/service/
│   │       └── LotteryServiceTest.java
│   └── pom.xml                  # Maven 配置
├── scripts/                     # 运维脚本
│   ├── backup_database.sh       # 数据库备份脚本
│   ├── backup_manager.sh        # 交互式备份管理器
│   ├── setup-ssl.sh             # SSL 证书设置
│   └── view-logs.sh             # 日志查看工具
├── nginx_configs/               # Nginx 配置文件
│   ├── www.bcbbs3.cn.conf       # 主站点配置
│   └── 18118bw.cn.conf          # API 域名配置
└── database_backups/            # 数据库备份文件
```

## 构建、测试和开发命令

### 前端
```bash
cd frontend

# Install dependencies
npm install

# Development server (port 5173, proxies /api to localhost:8080)
npm run dev

# Production build (type-check + Vite build to dist/)
npm run build

# Clean production assets only (manual clean command)
npm run clean:site:assets

# One-command production deployment (clean old assets + build + deploy)
npm run build:site

# Dry-run deployment (clean/build/deploy simulation)
npm run build:site:dry

# Preview production build
npm run preview
```

### 后端
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

### 工具脚本
```bash
# View backend logs
./scripts/view-logs.sh

# Database backup
./scripts/backup_database.sh
```

## 代码风格指南

### TypeScript/Vue 前端
- **缩进**: 2 个空格
- **API**: 使用 Composition API（组合式 API）`<script setup lang="ts">`
- **文件命名**: 组件使用大驼峰命名（如 `GameHome.vue`、`NoticeDialog.vue`）
- **导入**: 使用 `@/` 别名指向 `frontend/src/`
- **注释**: 所有注释必须使用英文
- **文件大小限制**: 单个文件不得超过 500 行 - 需要时拆分为新文件

### UI/UX 指南
- **标签式导航**: 使用标签切换方式替代页面跳转来切换内容分类
  - 实现毫秒级标签切换响应（无页面重新加载）
  - 使用 `v-show` 或动态组件切换（`<component :is="...">`）替代路由导航
  - 预加载相邻标签内容以实现即时切换体验
  - 标签切换时保持滚动位置和状态
  - 使用 Element Plus `el-tabs` 组件或自定义带 CSS 过渡的标签实现
  - 避免在同一功能模块内使用 `router.push()` 进行标签导航
- **多主题支持**: `style.css` 中的 CSS 变量支持 5 种主题（棕色/默认、红色、绿色、青色、蓝色）
  - 主题通过 `html` class 应用（如 `html.red`、`html.blue`）
  - 所有组件样式应使用 `:root` 中的 CSS 变量

### Java 后端
- **缩进**: 4 个空格
- **类命名**: 大驼峰命名（如 `AuthController`、`LotteryService`）
- **方法/字段命名**: 小驼峰命名
- **包命名**: 全小写（如 `com.bcbbs.backend`）
- **DTO 命名规范**: 使用 `*Request` 和 `*Response` 后缀
- **注释**: 所有注释必须使用英文
- **文件大小限制**: 单个文件不得超过 500 行

### 通用规则
- 一直使用中文回复（包括所有技术回复、代码注释说明和文档内容）
- **注释**: 所有代码注释必须使用英文
- **完整代码**: 编写完整、可运行的代码 - 不使用占位符或存根
- **禁止硬编码模拟数据**: 始终查询真实数据库或 API 接口，绝不使用硬编码的模拟数据
- **文件大小限制**: 单个文件不得超过 500 行代码
  - 添加业务逻辑时，创建新文件以实现模块化代码组织
  - 将大文件拆分为更小、更专注的模块
- **目录结构**: 创建新目录时，检查项目中是否已存在类似目录
  - 前端文件放在 `frontend/src/` 下
  - 后端文件放在 `backend/src/main/java/com/bcbbs/backend/` 下
  - 遵循现有项目结构进行文件放置
- **数据库管理**: 创建新页面或功能时，检查是否需要数据库表/实体
  - 根据需要创建或更新 JPA 实体
  - 确保正确处理数据库 schema 变更
- **文档**: 只更新修改过的项目文件，不要创建不必要的文档
  - 熟悉项目后，不要编写额外的文档文件
- **代码审查**: 编写代码后始终进行代码审查
  - 检查逻辑错误和潜在 bug
  - 完成前验证实现的正确性
- **Bug 预防**: 严格审查代码逻辑以防止 bug
- 保持逻辑分层：控制器处理 HTTP，服务层处理业务逻辑，仓库层处理持久化
- 完成任务后，只更新实际变更的文档；不要创建新的文档文件
- 完成前审查代码逻辑的正确性

## 测试说明

### 后端测试
- 使用 Spring Boot 测试栈（`spring-boot-starter-test`、`spring-security-test`）
- 测试类位于 `backend/src/test/java/`
- 命名规范：
  - 单元测试：`*Test` 后缀
  - 集成测试：`*IT` 后缀
- 无强制覆盖率阈值；对任何身份验证、安全或持久化行为的变更添加/扩展测试
- 示例测试：`LotteryServiceTest.java` 使用 MockRestServiceServer 测试彩票数据代理

### 前端测试
- 当前未配置测试运行器
- 对 UI 或路由变更需包含清晰的手动验证步骤

## 安全注意事项

### 身份验证与授权
- 基于 JWT 的身份验证，24 小时过期
- Token 存储在 localStorage，通过 Axios 拦截器自动注入
- 角色：USER、MEMBER、AGENT、ADMIN（定义在 `User.Role` 枚举中）
- 特定角色的登录端点带验证码校验

### 强制密码修改策略
- 后端对 MEMBER 和 AGENT 角色强制执行三次警告密码修改策略
- 未修改密码的第 1-2 次登录：`needPasswordChange: true`，递增 `loginCountWithoutChange`
- 第 3 次及以后登录：账户被禁用，用户必须使用强制修改密码端点
- 恢复端点：`POST /api/auth/force-change-password`（无需 JWT）

### 安全配置
- 公开端点：`/api/public/**`、`/api/auth/force-change-password`、认证登录/注册端点
- 所有其他端点需要有效的 JWT
- CORS 配置允许：`localhost`（任意端口）、`www.bcbbs3.cn`、`bcbbs3.cn`、`18118bw.cn`

### 错误追踪
- 每个后端错误通过 `GlobalExceptionHandler` 获得唯一的 8 字符 `errorId`
- 敏感参数（password、token）在日志中被掩码处理
- 安全事件记录到专用的 `security.log`

## 架构详情

### 请求流程
1. 前端 API 调用通过 `frontend/src/api/index.ts`（Axios 实例）
2. 拦截器从 localStorage 自动注入 `Authorization: Bearer {token}`
3. 全局 401 处理重定向到登录页面
4. 后端响应使用 `ApiResponse<T>` 包装：`{ code, message, data, errorId?, timestamp? }`
5. 公开 API 调用附加 `t: Date.now()` 参数以绕过 CDN/代理/浏览器缓存

### 前端状态与路由
- **Pinia store**（`stores/auth.ts`）：管理 JWT token、用户会话、登录/登出操作
- **路由守卫**：检查 `isAuthenticated`，刷新时获取用户资料
- **密码强制修改**：如果设置了 `needPasswordChange` 则重定向到 `/change-password`
- **路径别名**：`@` → `frontend/src/`
- **自动导入**：Vite 插件自动注册 Element Plus 组件
- **构建元数据**：Vite 注入 `<meta name="frontend-build" content="ISO 时间戳">` 到 HTML

### 后端分层
- **Controllers（控制器）**：处理 HTTP 请求、输入校验、响应包装
- **Services（服务层）**：包含业务逻辑、事务操作
- **Repositories（仓库层）**：数据访问层（Spring Data JPA）
- **Entities（实体）**：JPA 实体，使用 Lombok `@Data`/`@Builder`
- **DTOs（数据传输对象）**：API 契约的请求/响应对象

### 缓存策略
后端使用 Caffeine 缓存，具有不同的 TTL：
- `lotteryGames`：1 小时（彩票目录）
- `lotteryInfo`：5 秒（当前期号信息）
- `lotteryListFirstPage`：5 秒（最新开奖）
- `lotteryListOtherPages`：15 分钟（历史数据）
- `accessLines`：6 小时（很少变化）

前端有内存缓存 store（`stores/cache.ts`），支持带 TTL 的 API 响应缓存。

### 彩票集成
- `LotteryService` 代理来自 `bw1284.cc` 的上游彩票数据
- 前端工具 `utils/lotteryCalc.ts` 处理游戏计算：
  - 龙虎（Dragon/Tiger）
  - 三形态（豹子/顺子/对子/半顺/杂六）
  - 牛牛（Bull game）
  - 澳洲幸运 10 赛车游戏
  - 宝斗（BaoDou）计算
  - 10 球牛牛（NiuNiu for 10-ball games）

## 部署信息

### 前端部署
- 构建输出：`frontend/dist/`
- 清理脚本：`frontend/scripts/clean-site-assets.mjs`（删除站点 `assets/` 下的旧文件）
- 部署脚本：`frontend/scripts/deploy-to-site-root.mjs`（原子/非破坏性部署）
- 站点根目录：`/www/wwwroot/www.bcbbs3.cn/`
- **重要 — 一键清理构建部署规则**：每次生产部署必须遵循以下顺序：
  1. **运行一条命令**：`cd /root/sscp28/frontend && npm run build:site`
     - 此命令内部执行：清理旧资源 -> 构建 -> 部署
  2. **验证**部署的文件数量：`ls /www/wwwroot/www.bcbbs3.cn/assets/ | wc -l`（应与当前构建输出匹配，通常 60-80 个文件，不是数千个）
- 如果跳过清理步骤，旧的哈希 chunk 文件会累积（7000+ 个文件），缓存了旧 `index.html` 的浏览器可能加载过时的 chunk，导致网站看起来"回滚"到旧版本

### 后端部署
- 构建产物：`backend/target/backend-0.0.1-SNAPSHOT.jar`
- 生产环境使用 `nohup` 后台运行
- 日志写入 `backend/logs/` 目录

### Nginx 配置
- SPA 支持：`try_files $uri $uri/ /index.html`
- API 代理：`/api/` → `http://127.0.0.1:8080/api/`
- 静态文件缓存：图片 30 天，JS/CSS 12 小时
- API 端点添加 CORS 头
- index.html 设置 no-cache 头以防止引用过时的 chunk

## 错误处理和日志

### 日志文件（backend/logs/）
- `app.log`：所有应用日志
- `error.log`：仅 ERROR 级别
- `warn.log`：仅 WARN 级别
- `security.log`：安全相关事件
- `business.log`：业务逻辑事件
- `api.log`：API 请求日志
- `debug.log`：DEBUG 级别（仅开发环境）
- `frontend.log`：从浏览器/客户端收集的日志

### 日志关联
- 通过 `errorId` grep 来关联前端错误响应与后端堆栈跟踪
- 每个错误响应包含唯一的 8 字符错误 ID

## 数据库配置

### 连接信息
- **URL**：`jdbc:mysql://localhost:3306/xie080886`
- **用户名/密码**：`xie080886`
- **驱动**：MySQL Connector/J
- **连接池**：HikariCP（最大 10 连接，最小 5 空闲）

### Schema 管理
- 由 Hibernate `ddl-auto: update` 管理
- 无需手动迁移
- 开发环境启用 Show SQL

## 环境变量

### 前端（.env 文件）
```
# Development (.env.development)
VITE_API_URL=http://localhost:8080/api
VITE_APP_TITLE=BCBBS Platform

# Production (.env.production)
VITE_API_URL=https://18118bw.cn/api
VITE_APP_TITLE=BCBBS Platform
```

### 后端（application.yml）
主要配置：
- 服务器端口：8080
- JWT 过期时间：24 小时（86400000ms）
- CORS 允许的源：localhost、www.bcbbs3.cn、bcbbs3.cn、18118bw.cn

## 提交与 Pull Request 指南

- 使用简洁的祈使句提交主题（如 `Add ...`、`Update ...`）
- 可偶尔使用 `chore:` 前缀
- 保持提交范围专注；除非必要，避免混合前端、后端和运维变更
- PR 应包含：
  - 变更内容和原因
  - 涉及的路径（如 `frontend/src/views/...`、`backend/src/main/java/...`）
  - 运行的验证命令（`npm run build`、`mvn test` 等）
  - UI 变更的截图
  - 关联的 issue/任务（如有）
