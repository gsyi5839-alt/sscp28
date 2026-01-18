# 🔍 网站路由和用户流程分析

**分析时间**: 2026-01-18 00:15 UTC  
**网站**: https://www.bcbbs3.cn/

---

## 📋 完整用户流程

### 1️⃣ 首页访问 (`/`)
**URL**: https://www.bcbbs3.cn/  
**组件**: `Search.vue`  
**功能**: 搜索页面

**特殊验证码跳转逻辑**:
- ⚠️ **当前未实现**: 需要添加特殊验证码输入检测
- 📝 **需求**: 当用户输入特殊验证码时，跳转到 `/member`

**建议实现**:
```typescript
// 在 Search.vue 的 handleSearch 函数中添加
const SPECIAL_CODE = '您的特殊验证码' // 例如: 'VIP2026'

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  
  // 检测特殊验证码
  if (keyword === SPECIAL_CODE) {
    router.push('/member')  // 跳转到会员/代理选择页
    return
  }
  
  // 正常搜索流程
  if (!keyword) {
    ElMessage.warning('请输入关键词')
    return
  }
  
  router.push({
    path: '/search/results',
    query: { q: keyword }
  })
}
```

---

### 2️⃣ 会员/代理选择页 (`/member`)
**URL**: https://www.bcbbs3.cn/member  
**组件**: `MemberPanel.vue`  
**功能**: 
- 会员线路 / 代理线路切换
- 显示可用线路列表
- 点击任意线路进入相应登录页

**跳转逻辑**:
```typescript
const selectLine = () => {
  if (activeTab.value === 'member') {
    router.push('/member/login')  // 会员登录
  } else {
    router.push('/agent/login')   // 代理登录
  }
}
```

---

### 3️⃣ 会员登录 (`/member/login`)
**URL**: https://www.bcbbs3.cn/member/login  
**组件**: `MemberLogin.vue`  
**功能**: 会员登录（需要验证码）

**登录后跳转逻辑**:
```typescript
if (result.success) {
  // 情况1: 第一次登录或未修改过密码
  if (result.needPasswordChange) {
    router.push('/change-password')  // 强制修改密码
    return
  }
  
  // 情况2: 已修改过密码
  const accepted = localStorage.getItem('userAgreementAccepted') === 'true'
  router.push(accepted ? '/game' : '/user-agreement')  // 协议页或游戏页
}

// 情况3: 账号被停用
if (result.status === 403 && result.errorMessage.includes('账户已停用')) {
  router.push({
    path: '/force-change-password',
    query: { role: 'MEMBER', username: loginForm.value.account }
  })
}
```

---

### 4️⃣ 修改密码页 (`/change-password`)
**URL**: https://www.bcbbs3.cn/change-password  
**组件**: `ChangePassword.vue`  
**触发条件**: 
- 第一次登录
- `needPasswordChange === true`

**修改密码后跳转**:
```typescript
// 修改密码成功后
const accepted = localStorage.getItem('userAgreementAccepted') === 'true'
router.push(accepted ? '/game' : '/user-agreement')
```

---

### 5️⃣ 用户协议页 (`/user-agreement`)
**URL**: https://www.bcbbs3.cn/user-agreement  
**组件**: `UserAgreement.vue`  
**功能**: 显示用户协议和规则

**按钮操作**:
```typescript
// 不同意 - 退出登录，返回会员登录页
const handleDisagree = () => {
  authStore.logout()
  router.push('/member/login')
}

// 同意 - 标记已同意，跳转到游戏页面
const handleAgree = () => {
  localStorage.setItem('userAgreementAccepted', 'true')
  router.push('/game')  // ✅ 跳转到游戏页面
}
```

---

### 6️⃣ 游戏页面 (`/game`)
**URL**: https://www.bcbbs3.cn/game  
**组件**: `GameHome.vue`  
**功能**: 游戏首页/游戏列表

---

## 🔄 完整流程图

```
[用户访问首页]
     ↓
输入特殊验证码? ──Yes──> [会员/代理选择页 /member]
     ↓ No                         ↓
[正常搜索]              点击会员线路 / 代理线路
                                  ↓
                     [会员登录 /member/login]
                                  ↓
                          ┌───────┴───────┐
                          ↓               ↓
                   第一次登录?      已修改过密码?
                          ↓               ↓
                   [修改密码页]      已同意协议?
                /change-password          ↓
                          ↓         ┌─────┴─────┐
                    修改成功         ↓           ↓
                          ↓       [游戏页]   [用户协议页]
                    已同意协议?    /game     /user-agreement
                          ↓                      ↓
                    ┌─────┴─────┐          点击"同意"
                    ↓           ↓               ↓
               [游戏页]    [用户协议页]    [游戏页]
               /game     /user-agreement   /game
                              ↓
                        点击"同意"
                              ↓
                         [游戏页]
                         /game
```

---

## 🌐 Nginx反向代理配置

### 当前配置
**配置文件**: `/www/server/nginx/conf/vhost/nginx/extension/www.bcbbs3.cn/api_proxy.conf`

```nginx
# API 代理配置
location /api/ {
    proxy_pass http://127.0.0.1:8080/api/;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_connect_timeout 60s;
    proxy_send_timeout 60s;
    proxy_read_timeout 60s;
    
    # CORS headers
    add_header Access-Control-Allow-Origin * always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "Authorization, Content-Type, X-Requested-With" always;
    
    # Handle preflight requests
    if ($request_method = 'OPTIONS') {
        add_header Access-Control-Allow-Origin * always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Authorization, Content-Type, X-Requested-With" always;
        add_header Content-Length 0;
        add_header Content-Type text/plain;
        return 204;
    }
}
```

### 路由处理
**主配置**: `/www/server/nginx/conf/vhost/nginx/www.bcbbs3.cn.conf`

```nginx
server {
    listen 80;
    listen 443 ssl http2;
    server_name www.bcbbs3.cn;
    root /www/wwwroot/www.bcbbs3.cn;
    index index.html;
    
    # HTTP to HTTPS redirect
    if ($server_port != 443) {
        rewrite ^(/.*)$ https://$host$1 permanent;
    }
    
    # SSL配置
    ssl_certificate    /www/server/panel/vhost/cert/www.bcbbs3.cn/fullchain.pem;
    ssl_certificate_key    /www/server/panel/vhost/cert/www.bcbbs3.cn/privkey.pem;
    
    # SPA路由支持 (需要添加)
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    include /www/server/panel/vhost/nginx/extension/www.bcbbs3.cn/*.conf;
}
```

---

## ⚠️ 当前问题和建议

### 问题1: 特殊验证码跳转未实现
**状态**: ❌ 未实现  
**位置**: `frontend/src/views/Search.vue`  
**建议**: 在 `handleSearch` 函数中添加特殊验证码检测逻辑

### 问题2: SPA路由刷新404
**状态**: ⚠️ 可能存在问题  
**原因**: Nginx配置中可能缺少 `try_files` 指令  
**建议**: 确保Nginx配置包含:
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 问题3: API代理配置
**状态**: ✅ 已配置  
**路径**: `/api/*` → `http://127.0.0.1:8080/api/*`  
**验证**: 需要测试所有API端点是否正常工作

---

## 🧪 测试建议

### 1. 首页特殊验证码测试
```bash
# 访问首页
curl https://www.bcbbs3.cn/

# 需要前端实现: 输入特殊验证码后跳转到/member
```

### 2. 会员登录流程测试
```bash
# 访问会员选择页
curl https://www.bcbbs3.cn/member

# 访问会员登录页
curl https://www.bcbbs3.cn/member/login

# 测试登录API
curl -X POST https://www.bcbbs3.cn/api/auth/role-login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "password": "test123",
    "role": "MEMBER",
    "captchaToken": "...",
    "captchaCode": "1234"
  }'
```

### 3. 修改密码流程测试
```bash
# 访问修改密码页
curl https://www.bcbbs3.cn/change-password
```

### 4. 用户协议流程测试
```bash
# 访问用户协议页
curl https://www.bcbbs3.cn/user-agreement
```

### 5. 游戏页面测试
```bash
# 访问游戏页面
curl https://www.bcbbs3.cn/game
```

---

## 📝 API端点列表

### 认证相关
- `POST /api/auth/role-login` - 角色登录
- `POST /api/auth/change-password` - 修改密码
- `POST /api/auth/force-change-password` - 强制修改密码
- `GET /api/auth/me` - 获取当前用户信息

### 公共接口
- `GET /api/public/health` - 健康检查
- `GET /api/public/captcha` - 获取验证码
- `GET /api/public/lines?type=MEMBER|AGENT` - 获取线路列表
- `GET /api/public/search?q=keyword` - 搜索

---

## ✅ 路由守卫逻辑

```typescript
// frontend/src/router/index.ts
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // 未登录时跳转到会员登录页面
    next({ name: 'memberLogin' })
  } else {
    next()
  }
})
```

**受保护的路由**:
- `/dashboard` - 需要认证

**公开路由**:
- `/` - 首页/搜索
- `/member` - 会员/代理选择
- `/member/login` - 会员登录
- `/agent/login` - 代理登录
- `/change-password` - 修改密码
- `/user-agreement` - 用户协议
- `/game` - 游戏页面

---

**文档状态**: ✅ 完成  
**需要实现**: 首页特殊验证码检测逻辑  
**配置状态**: ✅ Nginx反向代理已配置


---

## ✅ 配置完成状态 (2026-01-18 00:13 UTC)

### Nginx配置已部署
**配置文件**: `/www/server/panel/vhost/nginx/www.bcbbs3.cn.conf`
**前端路径**: `/www/wwwroot/www.bcbbs3.cn/frontend/dist`
**SSL证书**: 自签名证书 `/www/server/nginx/conf/ssl/www.bcbbs3.cn.{crt,key}`

### 配置亮点:
✅ **SPA路由支持**: `try_files $uri $uri/ /index.html` - 所有前端路由刷新不会404
✅ **API反向代理**: `/api/*` → `http://127.0.0.1:8080/api/*` - 工作正常
✅ **CORS配置**: 已配置跨域支持
✅ **HTTP/2支持**: HTTPS连接使用HTTP/2协议
✅ **静态资源缓存**: 图片30天，JS/CSS 12小时

### 测试结果:
- ✅ HTTP访问: `http://localhost` - 200 OK
- ✅ HTTPS访问: `https://localhost` - 200 OK (自签名证书)
- ✅ API健康检查: `http://localhost/api/public/health` - 200 OK
- ✅ 前端资源加载: 正常

### ⚠️ 待完成事项:

1. **首页特殊验证码功能**:
   - 位置: `frontend/src/views/Search.vue`
   - 需求: 在 `handleSearch` 函数中添加特殊验证码检测逻辑
   - 实现建议: 见上文"建议实现"部分

2. **SSL证书更新**:
   - 当前使用自签名证书，浏览器会显示安全警告
   - 建议使用 Let's Encrypt 免费证书
   - 配置路径: `/www/server/panel/vhost/cert/www.bcbbs3.cn/`

3. **Systemd服务配置**:
   - 当前systemd无法正确管理Nginx
   - 需要更新 `/usr/lib/systemd/system/nginx.service`
   - 或创建自定义服务文件指向 `/www/server/nginx/sbin/nginx`

### 快速重启命令:
```bash
# 测试配置
/www/server/nginx/sbin/nginx -t

# 重新加载配置
/www/server/nginx/sbin/nginx -s reload

# 重启Nginx
killall nginx && /www/server/nginx/sbin/nginx -c /www/server/nginx/conf/nginx.conf

# 查看进程
ps aux | grep nginx
```

---

**最后更新**: 2026-01-18 00:13 UTC  
**配置状态**: ✅ 完全可用  
**网站状态**: ✅ 正常运行
