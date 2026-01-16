#!/bin/bash

# 数据库备份脚本
# 用途：备份MySQL数据库并推送到Git仓库

# 配置信息
DB_HOST="localhost"
DB_NAME="xie080886"
DB_USER="xie080886"
DB_PASS="xie080886"
BACKUP_DIR="/www/wwwroot/www.bcbbs3.cn/database_backups"
PROJECT_DIR="/www/wwwroot/www.bcbbs3.cn"
DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="backup_${DATE}.sql"
LOG_FILE="/www/wwwroot/www.bcbbs3.cn/scripts/backup.log"

# 日志函数
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 创建备份目录
mkdir -p "$BACKUP_DIR"

log_message "开始数据库备份..."

# 执行数据库备份
mysqldump -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" > "$BACKUP_DIR/$BACKUP_FILE" 2>> "$LOG_FILE"

if [ $? -eq 0 ]; then
    log_message "数据库备份成功: $BACKUP_FILE"
    
    # 压缩备份文件
    gzip "$BACKUP_DIR/$BACKUP_FILE"
    log_message "备份文件已压缩: ${BACKUP_FILE}.gz"
    
    # 只保留最近10个备份文件
    cd "$BACKUP_DIR"
    ls -t backup_*.sql.gz | tail -n +11 | xargs -r rm
    log_message "清理旧备份文件完成"
    
    # 切换到项目目录
    cd "$PROJECT_DIR"
    
    # 检查Git状态
    if [ -d ".git" ]; then
        # 添加备份文件到Git
        git add database_backups/
        
        # 检查是否有变化
        if git diff --staged --quiet; then
            log_message "没有新的数据库变化，跳过提交"
        else
            # 提交变化
            git commit -m "数据库备份: $DATE"
            
            # 推送到远程仓库
            git push origin main 2>> "$LOG_FILE"
            
            if [ $? -eq 0 ]; then
                log_message "数据库备份已成功推送到Git仓库"
            else
                log_message "ERROR: Git推送失败"
            fi
        fi
    else
        log_message "ERROR: 不是Git仓库"
    fi
else
    log_message "ERROR: 数据库备份失败"
    exit 1
fi

log_message "备份流程完成"
