# BCBBS3 日志系统文档

## 📋 系统概述

前后端分离的日志系统，支持敏感信息自动脱敏，便于排查问题且不会泄露用户隐私。

---

## 🖥️ 后端日志 (Java/Spring Boot)

### 日志文件结构

| 日志文件 | 用途 | 保留策略 |
|---------|------|---------|
| `app.log` | 所有日志汇总 | 30天，最大2GB |
| `error.log` | ERROR级别错误 | 60天，最大2GB |
| `warn.log` | WARN级别警告 | 30天，最大1GB |
| `debug.log` | DEBUG调试信息 | 7天，最大1GB |
| `business.log` | 业务操作日志 | 90天，最大5GB |
| `api.log` | API请求/响应 | 30天，最大3GB |
| `security.log` | 安全相关(登录/权限) | 90天，最大3GB |
| `frontend.log` | 前端上报的错误 | 30天，最大1GB |

### 敏感信息脱敏规则

后端自动对以下敏感字段进行脱敏处理：

```java
// 请求体中的密码字段
"password": "*****"
"oldPassword": "*****"
"newPassword": "*****"

// 响应体中的令牌
"token": "*****"

// HTTP Header 中的敏感信息
Authorization: Bearer *****
Cookie: [hidden]
```

### 快速查看日志

```bash
# 使用脚本查看日志
./scripts/view-logs.sh -e 50      # 最近50条错误
./scripts/view-logs.sh -a --tail  # 实时跟踪所有日志
./scripts/view-logs.sh --grep "ERROR"  # 搜索关键字
```

---

## 🌐 前端日志 (Vue/TypeScript)

### 功能特性

- ✅ 自动捕获未处理的错误
- ✅ 自动捕获未处理的Promise拒绝
- ✅ 敏感信息脱敏（密码、Token、邮箱）
- ✅ 批量发送，减少网络请求
- ✅ Beacon API 支持页面卸载时发送
- ✅ 队列机制，防止日志丢失

### 脱敏规则

```typescript
// 前端自动脱敏内容
"password": "*****"
"oldPassword": "*****"
"newPassword": "*****"
"token": "*****"
"captcha": "*****"
"captchaCode": "*****"
Bearer eyJhbGci... → Bearer *****
user@example.com → [EMAIL_MASKED]
```

### 使用方法

```typescript
import { logger, createLogger } from '@/utils/logger'

// 使用默认logger
logger.info('用户登录成功', { userId: '123' })
logger.error('请求失败', error, { url: '/api/test' })

// 创建特定模块的logger
const authLogger = createLogger('AuthModule')
authLogger.warn('Token即将过期')
```

### 日志级别

- `DEBUG` - 调试信息
- `INFO` - 一般信息
- `WARN` - 警告
- `ERROR` - 错误
- `FATAL` - 严重错误

---

## 🔒 安全设计

### 1. 敏感信息脱敏

- 前后端都有独立的脱敏机制
- 正则表达式匹配敏感字段
- 密码字段永远不打到日志

### 2. 日志访问控制

- 前端日志API允许匿名访问（用于页面卸载时发送）
- 后端日志文件存储在服务器本地
- 日志文件权限控制

### 3. 错误ID追踪

每个后端错误都会生成唯一的 `errorId`，便于前后端关联：
```json
{
  "code": 500,
  "message": "服务器错误",
  "errorId": "A1B2C3D4"
}
```

---

## 📁 日志目录结构

```
/root/sscp28/
├── backend/logs/
│   ├── app.log           # 主日志
│   ├── error.log         # 错误日志
│   ├── warn.log          # 警告日志
│   ├── debug.log         # 调试日志
│   ├── business.log      # 业务日志
│   ├── api.log           # API日志
│   ├── security.log      # 安全日志
│   ├── frontend.log      # 前端日志
│   └── archive/          # 归档日志(.gz)
│       ├── app.2026-03-22.0.log.gz
│       └── ...
├── backend.log           # 后端启动日志
└── scripts/
    └── view-logs.sh      # 日志查看脚本
```

---

## 🛠️ 常用操作

### 查看实时日志

```bash
# 实时跟踪错误日志
tail -f /root/sscp28/backend/logs/error.log

# 或使用脚本
./scripts/view-logs.sh -e --tail
```

### 搜索日志

```bash
# 搜索特定错误ID
grep "A1B2C3D4" /root/sscp28/backend/logs/*.log

# 使用脚本搜索
./scripts/view-logs.sh --grep "A1B2C3D4"
```

### 清理旧日志

日志会自动归档和清理，无需手动操作：
- 超过50MB自动轮转
- 超过保留期自动删除
- 总大小超过限制自动清理最旧日志

---

## 🔍 排查问题流程

### 1. 用户报告问题
获取用户的:
- 操作时间
- 页面URL
- 错误提示信息中的 `errorId`

### 2. 查找后端日志
```bash
./scripts/view-logs.sh --grep "A1B2C3D4"
```

### 3. 查找前端日志
```bash
./scripts/view-logs.sh -f 50  # 最近50条前端日志
```

### 4. 关联分析
通过时间戳和用户ID关联前后端日志，定位问题根源。

---

## 📝 最佳实践

1. **不要在日志中打印敏感信息**
   ```typescript
   // ❌ 错误
   logger.info('用户登录', { password: user.password })
   
   // ✅ 正确
   logger.info('用户登录', { username: user.username })
   ```

2. **使用合适的日志级别**
   - DEBUG: 开发调试
   - INFO: 正常业务流程
   - WARN: 异常情况但可恢复
   - ERROR: 错误需要处理
   - FATAL: 系统级严重错误

3. **添加有用的上下文**
   ```typescript
   // ❌ 不清晰的日志
   logger.error('出错了')
   
   // ✅ 清晰的日志
   logger.error('获取账户历史失败', error, { 
     userId: currentUser.id,
     dateRange: { start, end }
   })
   ```

---

## ⚙️ 配置说明

### 后端日志配置
文件: `backend/src/main/resources/logback-spring.xml`

关键配置项:
- 日志级别
- 文件大小限制
- 保留天数
- 异步日志队列大小

### 前端日志配置
文件: `frontend/src/utils/logger.ts`

关键配置项:
- `flushInterval`: 批量发送间隔(默认5秒)
- `maxQueueSize`: 队列最大长度(默认100)
- 脱敏正则表达式规则
