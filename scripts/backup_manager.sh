#!/bin/bash

# 数据库备份管理脚本
# 用途：管理数据库备份系统

SCRIPT_DIR="/www/wwwroot/www.bcbbs3.cn/scripts"
BACKUP_DIR="/www/wwwroot/www.bcbbs3.cn/database_backups"
LOG_FILE="$SCRIPT_DIR/backup.log"

# 显示菜单
show_menu() {
    echo "================================"
    echo "  数据库备份管理系统"
    echo "================================"
    echo "1. 立即执行备份"
    echo "2. 查看备份列表"
    echo "3. 查看备份日志"
    echo "4. 查看定时任务"
    echo "5. 测试Git连接"
    echo "6. 查看备份统计"
    echo "0. 退出"
    echo "================================"
}

# 立即备份
do_backup() {
    echo "正在执行数据库备份..."
    $SCRIPT_DIR/backup_database.sh
    echo ""
    echo "备份完成！按任意键继续..."
    read -n 1
}

# 查看备份列表
list_backups() {
    echo ""
    echo "备份文件列表："
    echo "--------------------------------"
    ls -lh $BACKUP_DIR/*.sql.gz 2>/dev/null | awk '{print $9, "(" $5 ")", $6, $7, $8}'
    echo ""
    echo "总计: $(ls -1 $BACKUP_DIR/*.sql.gz 2>/dev/null | wc -l) 个备份文件"
    echo ""
    echo "按任意键继续..."
    read -n 1
}

# 查看日志
view_log() {
    echo ""
    echo "最近的备份日志 (最后20行)："
    echo "--------------------------------"
    tail -20 $LOG_FILE
    echo ""
    echo "按任意键继续..."
    read -n 1
}

# 查看定时任务
view_cron() {
    echo ""
    echo "当前定时任务："
    echo "--------------------------------"
    crontab -l | grep backup_database
    echo ""
    echo "按任意键继续..."
    read -n 1
}

# 测试Git连接
test_git() {
    echo ""
    echo "测试Git连接..."
    echo "--------------------------------"
    ssh -T git@github.com-bcbbs3
    echo ""
    echo "Git状态："
    cd /www/wwwroot/www.bcbbs3.cn
    git status
    echo ""
    echo "按任意键继续..."
    read -n 1
}

# 备份统计
backup_stats() {
    echo ""
    echo "备份统计信息："
    echo "--------------------------------"
    echo "备份目录: $BACKUP_DIR"
    echo "备份数量: $(ls -1 $BACKUP_DIR/*.sql.gz 2>/dev/null | wc -l) 个"
    
    if [ -f "$LOG_FILE" ]; then
        echo "日志大小: $(du -h $LOG_FILE | awk '{print $1}')"
        echo "最后备份: $(grep '备份流程完成' $LOG_FILE | tail -1)"
    fi
    
    TOTAL_SIZE=$(du -sh $BACKUP_DIR 2>/dev/null | awk '{print $1}')
    echo "总占用空间: $TOTAL_SIZE"
    
    echo ""
    echo "下次自动备份时间: 每3天凌晨2:00"
    echo ""
    echo "按任意键继续..."
    read -n 1
}

# 主循环
while true; do
    clear
    show_menu
    echo -n "请选择操作 [0-6]: "
    read choice
    
    case $choice in
        1) do_backup ;;
        2) list_backups ;;
        3) view_log ;;
        4) view_cron ;;
        5) test_git ;;
        6) backup_stats ;;
        0) echo "退出程序"; exit 0 ;;
        *) echo "无效选择，请重试"; sleep 2 ;;
    esac
done
