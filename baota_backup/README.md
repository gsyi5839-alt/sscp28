# 宝塔面板配置备份

## 备份内容

### 1. MySQL配置
- `mysql_my.cnf` - MySQL主配置文件备份

### 2. Nginx配置
- `nginx_configs/` - 所有Nginx虚拟主机配置（11个文件）

### 3. 防火墙规则
- `firewall_rules.txt` - UFW防火墙规则列表

## 重要端口
- **22113** - SSH端口（自定义）
- **8080** - Spring Boot后端
- **80/443** - HTTP/HTTPS
- **3306** - MySQL（仅本地访问）
- **888** - 宝塔面板端口

## 恢复方法

### MySQL配置恢复
```bash
sudo cp mysql_my.cnf /etc/my.cnf
sudo systemctl restart mysql
```

### Nginx配置恢复
```bash
sudo cp -r nginx_configs/* /etc/nginx/sites-available/
# 根据需要创建软链接到 sites-enabled
```

### 防火墙规则恢复
```bash
# 确保关键端口开放
sudo ufw allow 22113/tcp  # SSH
sudo ufw allow 80/tcp     # HTTP
sudo ufw allow 443/tcp    # HTTPS
sudo ufw allow 8080/tcp   # 后端API
sudo ufw enable
```

---
**备份时间**: 2026-01-17 23:46
**备份路径**: /www/wwwroot/www.bcbbs3.cn/baota_backup/
