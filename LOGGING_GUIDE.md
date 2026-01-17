# 📋 BCBBS3 后端日志系统使用指南

## 🎯 日志系统概述

系统已配置完善的日志记录功能，支持多级别、多文件、自动滚动归档的日志管理。

---

## 📁 日志文件说明

| 文件名 | 说明 | 级别 | 保留时间 | 用途 |
|--------|------|------|---------|------|
| `app.log` | 全部日志 | INFO+ | 30天 | 综合查看 |
| `error.log` | 错误日志 | ERROR | 60天 | 排查严重问题 |
| `warn.log` | 警告日志 | WARN | 30天 | 排查潜在问题 |
| `debug.log` | 调试日志 | DEBUG | 7天 | 开发调试 |
| `api.log` | API请求日志 | INFO+ | 30天 | 追踪请求 |
| `security.log` | 安全日志 | INFO+ | 90天 | 登录/权限审计 |
| `business.log` | 业务日志 | INFO+ | 90天 | 业务流程追踪 |

---

## 📍 日志文件位置

```
/www/wwwroot/www.bcbbs3.cn/backend/logs/
├── app.log          # 主日志文件
├── error.log        # 错误日志
├── warn.log         # 警告日志
├── debug.log        # 调试日志
├── api.log          # API请求日志
├── security.log     # 安全日志
├── business.log     # 业务日志
└── archive/         # 历史归档目录
    ├── app.2026-01-17.0.log.gz
    ├── error.2026-01-17.0.log.gz
    └── ...
```

---

## 🔍 常用日志查看命令

### 1. 实时查看日志

```bash
# 实时查看主日志
tail -f /www/wwwroot/www.bcbbs3.cn/backend/logs/app.log

# 实时查看错误日志
tail -f /www/wwwroot/www.bcbbs3.cn/backend/logs/error.log

# 实时查看API请求日志
tail -f /www/wwwroot/www.bcbbs3.cn/backend/logs/api.log

# 实时查看安全日志
tail -f /www/wwwroot/www.bcbbs3.cn/backend/logs/security.log
```

### 2. 根据错误ID查找

```bash
# 根据错误ID查找完整日志
grep "A1B2C3D4" /www/wwwroot/www.bcbbs3.cn/backend/logs/*.log

# 查找最近的错误
tail -100 /www/wwwroot/www.bcbbs3.cn/backend/logs/error.log
```

### 3. 根据时间范围查找

```bash
# 查找特定日期的日志
grep "2026-01-17 15:" /www/wwwroot/www.bcbbs3.cn/backend/logs/app.log

# 查找最近1小时的错误
grep "$(date -d '1 hour ago' +'%Y-%m-%d %H')" /www/wwwroot/www.bcbbs3.cn/backend/logs/error.log
```

### 4. 根据关键字查找

```bash
# 查找登录相关日志
grep -i "login\|登录" /www/wwwroot/www.bcbbs3.cn/backend/logs/security.log

# 查找特定用户的日志
grep "username=testuser" /www/wwwroot/www.bcbbs3.cn/backend/logs/api.log

# 查找特定IP的请求
grep "IP: 192.168.1.100" /www/wwwroot/www.bcbbs3.cn/backend/logs/api.log
```

### 5. 统计分析

```bash
# 统计错误数量
wc -l /www/wwwroot/www.bcbbs3.cn/backend/logs/error.log

# 统计今日请求数
grep "$(date +'%Y-%m-%d')" /www/wwwroot/www.bcbbs3.cn/backend/logs/api.log | wc -l

# 查看响应时间分布
grep "Duration:" /www/wwwroot/www.bcbbs3.cn/backend/logs/api.log | awk -F'Duration: ' '{print $2}' | sort -n | tail -20

# 查找慢请求（超过3秒）
grep "SLOW_REQUEST" /www/wwwroot/www.bcbbs3.cn/backend/logs/app.log
```

---

## 📊 日志格式说明

### 主日志格式

```
2026-01-17 14:56:20.522 [http-nio-8080-exec-1] DEBUG [com.bcbbs.backend.service.UserService] [loadUserByUsername:21] - Loading user: admin
|______________|  |___________________|  |___| |_______________________________|  |__________________|  |_________________|
     时间戳              线程名           级别            类名                          方法:行号              消息内容
```

### API日志格式

```
# 请求开始
2026-01-17 14:56:20.522 | [4669CCB7] >>> GET /api/public/health | IP: 127.0.0.1 | Headers: [...]
                          |________|      |_____________________|      |________|
                          请求ID          请求方法和URI                 客户端IP

# 请求结束
2026-01-17 14:56:21.362 | [4669CCB7] <<< 200 OK | Duration: 847ms | Request: {...} | Response: {...}
                          |________|     |_____|      |________|       |________|      |________|
                          请求ID       状态码        耗时           请求体          响应体
```

### 错误日志格式

```
╔══════════════════════════════════════════════════════════════════════════════╗
║ 错误ID: A1B2C3D4 | 类型: 运行时异常
╠══════════════════════════════════════════════════════════════════════════════╣
║ 时间: 2026-01-17 14:56:20.522
║ 请求URI: /api/auth/login
║ 请求方法: POST
║ 客户端IP: 192.168.1.100
║ User-Agent: Mozilla/5.0 ...
║ 请求参数: username=admin, password=*****
╠══════════════════════════════════════════════════════════════════════════════╣
║ 异常类: java.lang.NullPointerException
║ 异常信息: Cannot invoke method on null object
╠══════════════════════════════════════════════════════════════════════════════╣
║ 堆栈跟踪:
java.lang.NullPointerException: ...
    at com.bcbbs.backend.service.UserService.findById(UserService.java:45)
    at ...
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

## 🔐 错误追踪系统

### 错误ID机制

每个错误都会生成唯一的8位错误ID（如 `A1B2C3D4`），便于追踪：

1. **用户看到的错误响应**：
```json
{
  "code": 500,
  "message": "服务器内部错误，请联系管理员 [错误ID: A1B2C3D4]",
  "errorId": "A1B2C3D4",
  "timestamp": "2026-01-17T14:56:20.522"
}
```

2. **根据错误ID查找详细日志**：
```bash
grep "A1B2C3D4" /www/wwwroot/www.bcbbs3.cn/backend/logs/*.log
```

---

## ⚙️ 配置说明

### Logback配置文件

位置：`/backend/src/main/resources/logback-spring.xml`

**主要配置项：**

```xml
<!-- 日志文件路径 -->
<property name="LOG_PATH" value="./logs"/>

<!-- 单个文件最大大小 -->
<maxFileSize>50MB</maxFileSize>

<!-- 保留天数 -->
<maxHistory>30</maxHistory>

<!-- 总大小上限 -->
<totalSizeCap>2GB</totalSizeCap>
```

### 日志级别配置

位置：`/backend/src/main/resources/application.yml`

```yaml
logging:
  level:
    root: INFO                    # 根日志级别
    com.bcbbs.backend: DEBUG      # 项目代码级别
    org.hibernate.SQL: DEBUG      # SQL日志
    org.springframework.web: DEBUG # Spring Web日志
```

---

## 📈 日志分析示例

### 1. 分析登录失败

```bash
# 查找所有登录失败
grep "登录\|Login\|认证失败\|凭证错误" /www/wwwroot/www.bcbbs3.cn/backend/logs/security.log

# 统计某IP的登录失败次数
grep "IP: 192.168.1.100" /www/wwwroot/www.bcbbs3.cn/backend/logs/security.log | grep -c "认证失败"
```

### 2. 分析API性能

```bash
# 查找最慢的10个请求
grep "Duration:" /www/wwwroot/www.bcbbs3.cn/backend/logs/api.log | \
  sed 's/.*Duration: \([0-9]*\)ms.*/\1/' | \
  sort -n | tail -10

# 统计各API的调用次数
grep ">>>" /www/wwwroot/www.bcbbs3.cn/backend/logs/api.log | \
  awk '{print $5}' | sort | uniq -c | sort -rn | head -10
```

### 3. 分析错误趋势

```bash
# 统计每小时错误数
grep "ERROR" /www/wwwroot/www.bcbbs3.cn/backend/logs/app.log | \
  awk '{print substr($1,1,13)}' | uniq -c

# 统计异常类型分布
grep "异常类:" /www/wwwroot/www.bcbbs3.cn/backend/logs/error.log | \
  awk '{print $2}' | sort | uniq -c | sort -rn
```

---

## 🛡️ 安全注意事项

### 日志中已隐藏的敏感信息

- ✅ 密码字段 (password, oldPassword, newPassword)
- ✅ JWT Token
- ✅ Cookie
- ✅ Authorization Header

### 定期清理

日志会自动滚动和压缩，但建议定期检查磁盘空间：

```bash
# 查看日志目录大小
du -sh /www/wwwroot/www.bcbbs3.cn/backend/logs/

# 手动清理30天前的归档
find /www/wwwroot/www.bcbbs3.cn/backend/logs/archive/ -mtime +30 -delete
```

---

## 🔧 故障排查流程

### 当用户报告错误时：

1. **获取错误ID**：让用户提供页面显示的错误ID
2. **查找日志**：
   ```bash
   grep "错误ID" /www/wwwroot/www.bcbbs3.cn/backend/logs/error.log
   ```
3. **查看完整堆栈**：在error.log中找到完整的错误信息
4. **查看请求上下文**：在api.log中查找同一请求ID的请求详情
5. **分析根因**：根据堆栈跟踪定位代码问题

### 快速排查脚本

```bash
#!/bin/bash
# 保存为 /www/wwwroot/www.bcbbs3.cn/backend/scripts/find_error.sh

ERROR_ID=$1
LOG_DIR="/www/wwwroot/www.bcbbs3.cn/backend/logs"

echo "=== 搜索错误ID: $ERROR_ID ==="
echo ""
echo "--- error.log ---"
grep -A 50 "$ERROR_ID" $LOG_DIR/error.log
echo ""
echo "--- api.log ---"
grep "$ERROR_ID" $LOG_DIR/api.log
echo ""
echo "--- app.log ---"
grep "$ERROR_ID" $LOG_DIR/app.log
```

使用方法：
```bash
bash /www/wwwroot/www.bcbbs3.cn/backend/scripts/find_error.sh A1B2C3D4
```

---

## 📊 日志监控建议

### 1. 设置告警

建议监控以下情况：
- error.log 文件大小突然增长
- 5分钟内错误数超过阈值
- 慢请求（>3秒）频繁出现

### 2. 日志聚合

对于生产环境，建议使用 ELK Stack 或 Loki 进行日志聚合分析。

### 3. 定时报告

可以设置 cron 任务，每日发送日志摘要：

```bash
# 每日凌晨发送前一天的错误摘要
0 0 * * * /www/wwwroot/www.bcbbs3.cn/backend/scripts/daily_log_report.sh
```

---

**文档版本：** 1.0  
**更新日期：** 2026-01-17  
**维护者：** BCBBS3 开发团队
