# BCBBS 用户管理工具

本目录包含用于管理BCBBS系统用户的脚本工具。

## 文件说明

### 1. manage_user.sh - 用户管理Shell脚本（推荐）

交互式用户管理工具，支持创建、删除、列出用户。

#### 使用方法

```bash
cd /root/sscp28/backend/scripts

# 查看帮助
./manage_user.sh

# 列出所有用户
./manage_user.sh list

# 创建会员用户
./manage_user.sh create AA1010 AA1011 MEMBER

# 创建代理用户
./manage_user.sh create agent001 pass123 AGENT

# 创建管理员用户
./manage_user.sh create admin admin123 ADMIN

# 删除用户
./manage_user.sh delete AA1010

# 清空所有用户（需要确认）
./manage_user.sh clear
```

#### 参数说明

- **用户名**: 登录账号（必填）
- **密码**: 登录密码（必填，会自动生成BCrypt哈希）
- **角色**: MEMBER（会员）、AGENT（代理）、ADMIN（管理员），默认为MEMBER

#### 特点

✅ 自动生成BCrypt密码哈希
✅ 支持用户创建、删除、列表查看
✅ 如果用户已存在，会更新密码和角色
✅ 自动设置用户为启用状态
✅ 默认标记为未修改初始密码（首次登录将触发强制修改密码）

### 2. create_user.sql - SQL初始化脚本

包含预定义用户的SQL脚本，可直接在数据库中执行。

#### 使用方法

```bash
# 方法1: 通过命令行执行
mysql -uxie080886 -pxie080886 xie080886 < create_user.sql

# 方法2: 在MySQL客户端中执行
mysql -uxie080886 -pxie080886 xie080886
source /root/sscp28/backend/scripts/create_user.sql;
```

## 快速开始

### 场景1：创建初始管理员账户

```bash
cd /root/sscp28/backend/scripts
./manage_user.sh create admin admin123 ADMIN
```

### 场景2：批量创建测试用户

```bash
cd /root/sscp28/backend/scripts

# 创建多个会员用户
./manage_user.sh create member001 pass001 MEMBER
./manage_user.sh create member002 pass002 MEMBER
./manage_user.sh create member003 pass003 MEMBER

# 创建代理用户
./manage_user.sh create agent001 agent123 AGENT
```

### 场景3：重置用户密码

```bash
# 只需重新创建用户即可，会自动更新密码
./manage_user.sh create AA1010 newpassword MEMBER
```

### 场景4：清理并重建用户

```bash
cd /root/sscp28/backend/scripts

# 清空所有用户
./manage_user.sh clear

# 创建新用户
./manage_user.sh create AA1010 AA1011 MEMBER
./manage_user.sh create admin admin123 ADMIN

# 查看结果
./manage_user.sh list
```

## 注意事项

1. **数据库配置**: 脚本中硬编码了数据库连接信息（xie080886），如需修改请编辑 `manage_user.sh`
2. **密码安全**: 创建的用户密码会自动使用BCrypt加密存储，安全性高
3. **用户状态**: 通过脚本创建的用户默认启用且密码已修改，可以直接登录
4. **角色权限**: 
   - MEMBER: 会员，普通用户权限
   - AGENT: 代理，代理商权限
   - ADMIN: 管理员，最高权限

## 技术细节

- **密码加密**: 使用Spring Security的BCryptPasswordEncoder
- **密码哈希**: $2a$10$开头的60字符BCrypt哈希
- **数据库**: MySQL 8.x
- **字符编码**: UTF-8

## 故障排除

### 问题1: 脚本无执行权限

```bash
chmod +x /root/sscp28/backend/scripts/manage_user.sh
```

### 问题2: 数据库连接失败

检查MySQL服务是否运行：
```bash
systemctl status mysql
```

### 问题3: Maven命令失败

确保在正确的目录执行，且Maven已安装：
```bash
mvn -version
```

## 维护建议

1. 定期备份用户数据
2. 生产环境建议使用更复杂的密码
3. 定期审查用户账户，删除不活跃用户
4. 管理员账户应使用强密码并定期更换

## 相关文件

- 数据库配置: `backend/src/main/resources/application.yml`
- 用户实体: `backend/src/main/java/com/bcbbs/backend/entity/User.java`
- 认证控制器: `backend/src/main/java/com/bcbbs/backend/controller/AuthController.java`
