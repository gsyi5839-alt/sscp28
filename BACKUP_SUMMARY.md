# 项目完整备份摘要

## 备份时间
2026年1月27日 06:12:32 UTC

## 备份内容

### 1. 代码库备份
- **Git仓库**: github.com-bcbbs3:gsyi5839-alt/sscp28.git
- **分支**: main
- **最新提交**: 0ed10db - Complete project backup including latest database backup before deletion
- **项目文件总数**: 463个文件
- **项目总大小**: 272MB

### 2. 数据库备份
- **数据库名称**: xie080886
- **备份文件**: database_backups/backup_full_20260127_061232.sql
- **文件大小**: 13KB
- **备份的表**:
  - `access_lines` - 访问线路表
  - `captcha_tokens` - 验证码令牌表
  - `search_items` - 搜索项目表
  - `users` - 用户表

### 3. 项目结构
```
/www/wwwroot/www.bcbbs3.cn/
├── backend/          # Java Spring Boot后端
├── frontend/         # Vue.js前端
├── database_backups/ # 数据库备份文件
└── 其他配置文件
```

### 4. 关键配置信息
- **数据库配置**: backend/src/main/resources/application.yml
- **数据库名**: xie080886
- **数据库用户**: xie080886
- **后端端口**: 8080
- **前端端口**: 5173

### 5. 备份文件列表
所有数据库备份文件：
- backup_full_20260127_061232.sql (最新，13KB) ✅
- backup_full_20260127_061211.sql (209 bytes)
- backup_full_20260127_061149.sql (空文件)
- backup_20260125_020001.sql (空文件)
- backup_20260122_020001.sql (空文件)
- backup_20260119_020001.sql.gz (3.7KB)

## 恢复说明

### 恢复代码
```bash
git clone git@github.com-bcbbs3:gsyi5839-alt/sscp28.git
cd sscp28
```

### 恢复数据库
```bash
mysql -u 用户名 -p 数据库名 < database_backups/backup_full_20260127_061232.sql
```

## 验证状态
✅ 数据库备份已创建
✅ 所有文件已添加到Git
✅ 已推送到远程仓库
✅ 备份完整性已验证

## 注意事项
1. 所有代码和数据库备份已成功推送到GitHub远程仓库
2. 可以安全删除本地项目目录
3. 如需恢复项目，从GitHub克隆代码并导入数据库备份即可
4. 建议定期检查GitHub仓库确保备份可访问

---
**备份完成时间**: 2026-01-27 06:12 UTC
**备份状态**: ✅ 完整成功
