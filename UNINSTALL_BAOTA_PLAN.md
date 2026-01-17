# 宝塔卸载方案

## ✅ 已完成备份

### 1. 数据库备份
- ✅ `database_backups/backup_20260117_234519.sql` (8.8KB)
- ✅ 包含所有表结构和数据（5个用户）
- ✅ 已推送到Git

### 2. 配置备份
- ✅ MySQL配置: `baota_backup/mysql_my.cnf`
- ✅ Nginx配置: `baota_backup/nginx_configs/` (11个文件)
- ✅ 防火墙规则: `baota_backup/firewall_rules.txt`

### 3. 源代码
- ✅ 所有源代码已推送到Git（690个文件）

## ⚠️ 当前状态

- **MySQL**: 宝塔MySQL服务已停止（无法重启）
- **后端服务**: 运行中（PID 928），但无法连接数据库
- **Nginx**: 运行中（宝塔版本）

## 🔄 卸载后恢复计划

由于MySQL已经停止且无法重启，直接卸载宝塔并重新安装独立MySQL更安全：

```bash
# 1. 卸载宝塔
/etc/init.d/bt stop
/etc/init.d/bt clear  # 清除数据
service bt stop

# 2. 安装独立MySQL
apt update
apt install -y mysql-server

# 3. 恢复数据库
mysql -u root < database_backups/backup_20260117_234519.sql
mysql -u root -e "CREATE DATABASE IF NOT EXISTS xie080886;"
mysql -u root xie080886 < database_backups/backup_20260117_234519.sql

# 4. 创建MySQL用户
mysql -u root -e "
CREATE USER IF NOT EXISTS 'xie080886'@'localhost' IDENTIFIED BY 'xie080886';
GRANT ALL PRIVILEGES ON xie080886.* TO 'xie080886'@'localhost';
FLUSH PRIVILEGES;"

# 5. 安装Nginx
apt install -y nginx
systemctl enable nginx
systemctl start nginx

# 6. 重启后端服务
cd /www/wwwroot/www.bcbbs3.cn/backend
kill 928  # 停止旧进程
nohup java -jar target/backend-0.0.1-SNAPSHOT.jar > nohup.out 2>&1 &
```

## ✅ 数据安全性

所有关键数据已安全备份到Git，即使卸载过程出现问题也可以完全恢复。

