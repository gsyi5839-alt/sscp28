# 🔍 登录流程调试指南

**问题描述**: 会员登录后应该跳转到修改密码页面，但实际跳转到了 `/member` 页面

---

## 📋 正确的流程

```
1. 访问 https://www.bcbbs3.cn/member/login
2. 输入账号、密码、验证码
3. 点击登录
   ↓
4. 【第一次登录】→ 跳转到 /change-password (修改密码页面)
5. 修改密码
   ↓
6. 跳转到 /user-agreement (用户协议页面)
7. 点击"同意"
   ↓
8. 跳转到 /game (游戏首页)
```

---

## 🔍 调试步骤

### 步骤1: 查看后端日志（实时）

打开终端，执行以下命令查看实时日志：

```bash
tail -f /www/wwwroot/www.bcbbs3.cn/backend/app.log
```

### 步骤2: 执行登录操作

1. 打开浏览器，访问 https://www.bcbbs3.cn/member/login
2. 输入账号密码登录
3. 观察终端中的日志输出

### 步骤3: 查看关键日志信息

在日志中查找以下关键信息：

```
========== 角色登录请求开始 ==========
请求参数 - 用户名: xxx, 角色: MEMBER
...
【重要】用户 xxx 登录 - 数据库原始值:
  - passwordChanged = [true/false/null]
  - loginCount = [数字]
...
【判断】passwordChanged = xxx, 是否需要修改密码？
...
✅ 【关键】用户未修改过密码，设置 needPasswordChange = true
...
【最终响应】AuthResponse 构建完成:
  - 🔴 needPasswordChange: [true/false]
```

---

## 🔧 快速测试命令

### 1. 查看用户数据库状态

```bash
mysql -uxie080886 -pxie080886 xie080886 -e "
SELECT username, role, password_changed, login_count_without_change, enabled 
FROM users 
WHERE username='您的用户名' 
LIMIT 1;"
```

### 2. 重置用户的密码修改状态（用于测试）

```bash
mysql -uxie080886 -pxie080886 xie080886 -e "
UPDATE users 
SET password_changed = 0, 
    login_count_without_change = 0 
WHERE username='您的用户名';"
```

### 3. 测试API返回（使用curl）

```bash
# 1. 获取验证码
CAPTCHA=$(curl -s http://localhost:8080/api/public/captcha)
TOKEN=$(echo "$CAPTCHA" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
CODE=$(echo "$CAPTCHA" | grep -o '"code":"[^"]*"' | cut -d'"' -f4)

echo "验证码Token: $TOKEN"
echo "验证码Code: $CODE"

# 2. 测试登录
curl -s -X POST http://localhost:8080/api/auth/role-login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"您的用户名\",\"password\":\"您的密码\",\"role\":\"MEMBER\",\"captchaToken\":\"$TOKEN\",\"captchaCode\":\"$CODE\"}" \
  | python3 -m json.tool | grep -A 2 "needPasswordChange"
```

---

## 🐛 可能的问题和解决方案

### 问题1: password_changed 字段为 NULL

**症状**: 数据库中 `password_changed` 字段显示为 `NULL`

**解决方案**:
```sql
UPDATE users 
SET password_changed = 0 
WHERE password_changed IS NULL 
AND role IN ('MEMBER', 'AGENT');
```

### 问题2: password_changed 字段为 1 (true)

**症状**: 用户已经被标记为修改过密码

**解决方案**:
```sql
-- 查看具体哪些用户
SELECT username, password_changed, login_count_without_change 
FROM users 
WHERE role IN ('MEMBER', 'AGENT');

-- 重置特定用户（如果需要测试）
UPDATE users 
SET password_changed = 0, 
    login_count_without_change = 0 
WHERE username='测试用户名';
```

### 问题3: 前端没有正确处理 needPasswordChange

**检查位置**: `frontend/src/views/MemberLogin.vue` 第 83-92 行

**当前逻辑**:
```typescript
if (result.success) {
  // 如果需要修改密码，强制跳转到修改密码页面
  if (result.needPasswordChange) {
    router.push('/change-password')
    return
  }
  // 已修改过密码：已同意过协议则直接进游戏，否则先去用户协议
  const accepted = localStorage.getItem('userAgreementAccepted') === 'true'
  router.push(accepted ? '/game' : '/user-agreement')
}
```

**前端调试**:
1. 打开浏览器开发者工具 (F12)
2. 切换到 Console 标签
3. 登录时查看网络请求和响应

---

## 📊 完整诊断流程

### 1. 检查后端日志
```bash
# 清空旧日志
echo "" > /www/wwwroot/www.bcbbs3.cn/backend/app.log

# 实时查看日志
tail -f /www/wwwroot/www.bcbbs3.cn/backend/app.log
```

### 2. 执行登录操作
访问 https://www.bcbbs3.cn/member/login 并登录

### 3. 分析日志输出
查找关键字段：
- `needPasswordChange`
- `passwordChanged`
- `loginCount`

### 4. 检查前端响应
打开浏览器开发者工具：
1. Network 标签
2. 找到 `role-login` 请求
3. 查看 Response 中的 `needPasswordChange` 字段

---

## 🎯 立即执行的测试

### 测试脚本
```bash
#!/bin/bash
cat > /tmp/test_login_flow.sh << 'SCRIPT'
#!/bin/bash

echo "======================================"
echo "   登录流程测试脚本"
echo "======================================"
echo ""

# 1. 检查后端状态
echo "1️⃣  检查后端服务状态..."
if curl -s http://localhost:8080/api/public/health > /dev/null; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务未运行！"
    exit 1
fi
echo ""

# 2. 查看测试用户状态
echo "2️⃣  查看数据库中用户状态..."
mysql -uxie080886 -pxie080886 xie080886 -e "
SELECT username, role, password_changed, login_count_without_change 
FROM users 
WHERE role IN ('MEMBER', 'AGENT') 
LIMIT 5;"
echo ""

# 3. 获取验证码
echo "3️⃣  获取验证码..."
CAPTCHA=$(curl -s http://localhost:8080/api/public/captcha)
TOKEN=$(echo "$CAPTCHA" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
CODE=$(echo "$CAPTCHA" | grep -o '"code":"[^"]*"' | cut -d'"' -f4)
echo "验证码Token: $TOKEN"
echo "验证码Code: $CODE"
echo ""

# 4. 提示用户
echo "4️⃣  请现在执行以下操作："
echo "   1. 打开新终端，执行: tail -f /www/wwwroot/www.bcbbs3.cn/backend/app.log"
echo "   2. 在浏览器访问 https://www.bcbbs3.cn/member/login"
echo "   3. 登录并观察日志输出"
echo ""
echo "======================================"

SCRIPT
chmod +x /tmp/test_login_flow.sh
/tmp/test_login_flow.sh
```

---

## 📝 预期的日志输出示例

### 正常情况（需要修改密码）
```
========== 角色登录请求开始 ==========
请求参数 - 用户名: test001, 角色: MEMBER
验证码验证结果: true
用户探测 - 用户名: test001, 是否启用: true, 角色: MEMBER, 密码已修改: false
开始Spring Security认证 - 用户名: test001
Spring Security认证成功 - 用户名: test001
========== 开始检查密码修改状态 ==========
【重要】用户 test001 登录 - 数据库原始值:
  - passwordChanged = false
  - loginCount = 0
【判断】passwordChanged = false, 是否需要修改密码？
✅ 【关键】用户未修改过密码，设置 needPasswordChange = true
【更新】登录次数从 0 增加到 1
【保存】用户数据已更新到数据库
========== 密码检查完成，needPasswordChange = true ==========
【最终响应】AuthResponse 构建完成:
  - 🔴 needPasswordChange: true
========== 角色登录请求完成，返回 200 OK ==========
```

### 异常情况（已修改过密码）
```
【判断】passwordChanged = true, 是否需要修改密码？
✅ 用户 test001 已修改过密码 (passwordChanged=true)，无需强制修改
========== 密码检查完成，needPasswordChange = false ==========
【最终响应】AuthResponse 构建完成:
  - 🔴 needPasswordChange: false
```

---

**下一步**: 请执行上述测试脚本，然后查看实时日志！
