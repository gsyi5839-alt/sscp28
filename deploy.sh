#!/bin/bash

# BCBBS项目完整部署脚本 - 新域名: 18118bw.cn
# 此脚本将完成：Java环境、Maven、Node.js、MySQL安装配置、项目构建部署

# set -e  # 注释掉立即退出，允许继续执行

echo "======================================"
echo "  BCBBS 项目部署脚本"
echo "  新域名: 18118bw.cn"
echo "======================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目配置
PROJECT_DIR="/root/sscp28"
SITE_ROOT="/www/wwwroot/www.bcbbs3.cn"
DB_NAME="xie080886"
DB_USER="xie080886"
DB_PASS="xie080886"

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 1. 检查是否为root用户
if [ "$EUID" -ne 0 ]; then 
    log_error "请使用root用户运行此脚本"
    exit 1
fi

log_info "步骤 1/8: 更新系统软件包..."
apt update

# 2. 安装Java 17
log_info "步骤 2/8: 安装Java 17..."
if ! command -v java &> /dev/null; then
    apt install -y openjdk-17-jdk
    log_info "Java 17 安装完成"
else
    log_info "Java 已安装: $(java -version 2>&1 | head -n 1)"
fi

# 3. 安装Maven
log_info "步骤 3/8: 安装Maven..."
if ! command -v mvn &> /dev/null; then
    apt install -y maven
    log_info "Maven 安装完成"
else
    log_info "Maven 已安装: $(mvn -v | head -n 1)"
fi

# 4. 安装Node.js和npm
log_info "步骤 4/8: 安装Node.js..."
if ! command -v node &> /dev/null; then
    # 安装Node.js 18.x LTS
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt install -y nodejs
    log_info "Node.js 安装完成: $(node -v)"
else
    log_info "Node.js 已安装: $(node -v)"
fi

# 5. 安装MySQL
log_info "步骤 5/8: 检查MySQL..."
if ! command -v mysql &> /dev/null; then
    log_warn "MySQL未安装，请手动安装MySQL 8.0并创建数据库"
    log_warn "数据库名: $DB_NAME"
    log_warn "用户名: $DB_USER"
    log_warn "密码: $DB_PASS"
else
    log_info "MySQL 已安装: $(mysql --version)"
    
    # 导入数据库备份
    if [ -f "$PROJECT_DIR/database_backups/backup_full_20260127_061232.sql" ]; then
        log_info "正在导入数据库备份..."
        mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
        mysql -u root -p -e "CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASS';"
        mysql -u root -p -e "GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';"
        mysql -u root -p -e "FLUSH PRIVILEGES;"
        mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$PROJECT_DIR/database_backups/backup_full_20260127_061232.sql"
        log_info "数据库导入完成"
    else
        log_warn "未找到数据库备份文件，请手动创建数据库和表"
    fi
fi

# 6. 构建后端项目
log_info "步骤 6/8: 构建Spring Boot后端..."
cd "$PROJECT_DIR/backend"
mvn clean package -DskipTests
log_info "后端构建完成"

# 7. 构建前端项目
log_info "步骤 7/8: 构建Vue前端..."
cd "$PROJECT_DIR/frontend"
npm install
npm run build
log_info "前端构建完成"

# 8. 部署前端文件到站点根目录
log_info "步骤 8/8: 部署前端到站点根目录..."
if [ -d "$SITE_ROOT" ]; then
    # 使用项目自带的部署脚本
    npm run deploy
    log_info "前端部署完成"
else
    log_warn "站点根目录不存在: $SITE_ROOT"
    log_warn "请手动创建目录或修改 SITE_ROOT 变量"
fi

# 9. 配置Nginx
log_info "配置Nginx..."
if [ -f "/etc/nginx/sites-available/18118bw.cn.conf" ] || [ -f "/www/server/panel/vhost/nginx/18118bw.cn.conf" ]; then
    log_info "Nginx配置文件已存在"
else
    log_warn "请将 $PROJECT_DIR/nginx_configs/18118bw.cn.conf 复制到Nginx配置目录"
    log_warn "宝塔面板: /www/server/panel/vhost/nginx/"
    log_warn "普通Nginx: /etc/nginx/sites-available/"
fi

# 10. 启动后端服务
log_info "启动后端服务..."
cd "$PROJECT_DIR/backend"
nohup java -jar target/backend-0.0.1-SNAPSHOT.jar > "$PROJECT_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
log_info "后端服务已启动 (PID: $BACKEND_PID)"

# 总结
echo ""
echo "======================================"
echo -e "${GREEN}部署完成！${NC}"
echo "======================================"
echo ""
echo "📋 部署信息："
echo "  - 域名: 18118bw.cn"
echo "  - 后端API: http://localhost:8080"
echo "  - 前端路径: $SITE_ROOT"
echo "  - 后端日志: $PROJECT_DIR/backend.log"
echo "  - 数据库: $DB_NAME"
echo ""
echo "📝 后续步骤："
echo "  1. 在域名服务商将 18118bw.cn 解析到服务器IP"
echo "  2. 在宝塔面板添加站点配置（或复制Nginx配置文件）"
echo "  3. 在宝塔面板申请SSL证书"
echo "  4. 测试访问: http://18118bw.cn"
echo ""
echo "🔧 管理命令："
echo "  - 查看后端日志: tail -f $PROJECT_DIR/backend.log"
echo "  - 停止后端: kill $BACKEND_PID"
echo "  - 重启Nginx: systemctl restart nginx"
echo ""
