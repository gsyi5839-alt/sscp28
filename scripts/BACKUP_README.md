# 数据库自动备份系统

## 备份配置

数据库自动备份系统已配置完成，具体信息如下：

### 备份周期
- **频率**: 每3天执行一次
- **执行时间**: 凌晨 2:00 AM
- **Cron表达式**: `0 2 */3 * *`

### 备份内容
- **数据库**: xie080886
- **格式**: SQL文件（gzip压缩）
- **保留策略**: 最近10个备份文件

### 备份位置
- **本地目录**: `/www/wwwroot/www.bcbbs3.cn/database_backups/`
- **远程仓库**: GitHub (gsyi5839-alt/sscp28)
- **分支**: main

### 脚本位置
- **备份脚本**: `/www/wwwroot/www.bcbbs3.cn/scripts/backup_database.sh`
- **日志文件**: `/www/wwwroot/www.bcbbs3.cn/scripts/backup.log`

## 手动执行备份

如需立即执行备份，运行以下命令：

```bash
/www/wwwroot/www.bcbbs3.cn/scripts/backup_database.sh
```

## 查看备份日志

```bash
cat /www/wwwroot/www.bcbbs3.cn/scripts/backup.log
```

## 查看定时任务

```bash
crontab -l
```

## 备份文件命名规则

格式: `backup_YYYYMMDD_HHMMSS.sql.gz`

示例: `backup_20260116_201210.sql.gz`

## 恢复数据库

解压并恢复备份文件：

```bash
# 解压备份文件
gunzip backup_YYYYMMDD_HHMMSS.sql.gz

# 恢复数据库
mysql -u xie080886 -p xie080886 < backup_YYYYMMDD_HHMMSS.sql
```

## 注意事项

1. 备份文件会自动推送到Git仓库，无需手动操作
2. 系统会自动清理超过10个的旧备份文件
3. 每次备份都会记录详细日志
4. 如果数据库无变化，不会创建新的提交
