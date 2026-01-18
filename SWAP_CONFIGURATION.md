# ✅ Swap配置完成

**配置时间**: 2026-01-18 00:02 UTC

---

## 📊 Swap信息

### 基本配置
- **文件路径**: `/swapfile`
- **大小**: 2 GB
- **类型**: 文件交换分区
- **权限**: 600 (仅root可访问)
- **UUID**: 82bc9768-4037-48ea-b7a1-2188356c3513

### 当前状态
```
NAME      TYPE SIZE USED PRIO
/swapfile file   2G   0B   -2
```

### 内存状态
```
               total        used        free      shared  buff/cache   available
Mem:           3.8Gi       1.9Gi       324Mi       3.0Mi       1.9Gi       1.9Gi
Swap:          2.0Gi          0B       2.0Gi
```

---

## ⚙️ 优化配置

### Swappiness (交换倾向)
- **设置值**: 10
- **说明**: 仅在内存使用超过90%时才使用Swap
- **默认值**: 60
- **配置文件**: `/etc/sysctl.conf`

### Cache Pressure (缓存压力)
- **设置值**: 50
- **说明**: 降低清理缓存的倾向，保留更多inode和dentry缓存
- **默认值**: 100
- **配置文件**: `/etc/sysctl.conf`

---

## 🔄 持久化配置

### /etc/fstab 条目
```
/swapfile none swap sw 0 0
```

**说明**: 系统重启后自动挂载Swap

### /etc/sysctl.conf 配置
```
vm.swappiness=10
vm.vfs_cache_pressure=50
```

**说明**: 系统重启后自动应用优化参数

---

## 📝 管理命令

### 查看Swap状态
```bash
swapon --show              # 显示Swap信息
free -h                    # 显示内存和Swap使用情况
cat /proc/swaps            # 详细Swap信息
```

### 临时禁用/启用Swap
```bash
sudo swapoff /swapfile     # 禁用Swap
sudo swapon /swapfile      # 启用Swap
```

### 调整Swap参数
```bash
# 临时调整（重启后失效）
sudo sysctl vm.swappiness=10

# 永久调整（修改配置文件）
sudo nano /etc/sysctl.conf
sudo sysctl -p             # 重新加载配置
```

### 删除Swap（如需要）
```bash
sudo swapoff /swapfile
sudo rm /swapfile
# 删除 /etc/fstab 中的 /swapfile 行
# 删除 /etc/sysctl.conf 中的 vm.swappiness 和 vm.vfs_cache_pressure 行
```

---

## 📊 性能影响

### 优点
- ✅ 防止内存不足导致的OOM Killer
- ✅ 支持更多并发进程
- ✅ 系统更稳定
- ✅ 后台进程可以暂存到Swap

### 注意事项
- Swap速度比内存慢很多
- Swappiness=10确保优先使用内存
- 定期监控Swap使用情况

---

## 🔍 监控建议

### 定期检查
```bash
# 每日检查脚本
#!/bin/bash
echo "=== Swap使用情况 $(date) ==="
free -h
swapon --show
echo ""
```

### 告警阈值
- Swap使用 > 50%: 考虑增加内存
- Swap使用 > 80%: 需要立即优化或扩容

---

## ✅ 配置验证

```bash
# 验证Swap已启用
$ swapon --show
NAME      TYPE SIZE USED PRIO
/swapfile file   2G   0B   -2

# 验证自动挂载配置
$ grep swap /etc/fstab
/swapfile none swap sw 0 0

# 验证优化参数
$ sysctl vm.swappiness vm.vfs_cache_pressure
vm.swappiness = 10
vm.vfs_cache_pressure = 50
```

---

**配置状态**: ✅ 完成并测试通过  
**重启后生效**: ✅ 已配置自动挂载  
**优化参数**: ✅ 已应用并持久化

