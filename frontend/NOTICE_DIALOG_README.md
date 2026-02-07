# 公告弹窗组件说明文档

## 📋 功能概述

在用户从"用户协议"页面点击"同意"进入游戏首页时，自动弹出公告通知对话框，展示系统公告信息。

## 📂 文件结构

```
frontend/src/
├── components/
│   └── NoticeDialog.vue          # 公告弹窗组件（新增）
├── assets/
│   ├── notice-header-red.svg     # 公告头部图标（已下载）
│   └── notice-header-red-bed63626.svg  # 原始文件名版本
└── views/
    ├── GameHome.vue              # 游戏首页（已更新）
    └── UserAgreement.vue         # 用户协议页面
```

## 🎨 组件特性

### NoticeDialog.vue 组件

**主要功能：**
1. ✅ 公告头部图标展示
2. ✅ 四种公告分类（特别通知、通知、安全通知、站点通知）
3. ✅ 左侧菜单切换
4. ✅ 右侧公告内容展示
5. ✅ 分页浏览（上一条/下一条）
6. ✅ 自定义滚动条样式
7. ✅ 响应式交互（点击、悬停效果）

**组件参数：**
```typescript
interface Props {
  visible: boolean  // 控制弹窗显示/隐藏
}

// 事件
emit('update:visible', value: boolean)  // 双向绑定
emit('close')  // 关闭事件
```

## 🚀 使用方式

### 基本用法

```vue
<script setup lang="ts">
import { ref } from 'vue'
import NoticeDialog from '@/components/NoticeDialog.vue'

const showNotice = ref(true)

const handleClose = () => {
  console.log('公告已关闭')
}
</script>

<template>
  <NoticeDialog 
    v-model:visible="showNotice"
    @close="handleClose"
  />
</template>
```

### 在游戏首页的集成

**GameHome.vue** 已经集成了公告弹窗：

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import NoticeDialog from '../components/NoticeDialog.vue'

const showNoticeDialog = ref(false)

onMounted(() => {
  // 页面加载后延迟500ms显示公告
  setTimeout(() => {
    showNoticeDialog.value = true
  }, 500)
})
</script>

<template>
  <NoticeDialog
    v-model:visible="showNoticeDialog"
    @close="handleCloseNotice"
  />
</template>
```

## 📊 公告数据结构

### 数据格式

```typescript
interface Notice {
  id: number
  title: string
  content: string
  createTime: string
}

// 公告分类数据
const noticeData = {
  '特别通知': [
    {
      id: 1,
      title: '特别通知',
      content: '尊敬的会员您好...',
      createTime: '2026-02-06'
    }
  ],
  '通知': [...],
  '安全通知': [...],
  '站点通知': [...]
}
```

### 后端 API 集成（待实现）

**建议的后端接口：**

```typescript
// 1. 获取公告列表
GET /api/public/notices?type=特别通知

// 2. 标记公告已读
POST /api/notices/{id}/read

// 3. 获取未读公告数量
GET /api/notices/unread-count
```

**集成示例：**

```typescript
// 在 src/api/index.ts 中添加
export const noticeApi = {
  // 获取公告列表
  getNotices: (type: string) => 
    api.get('/public/notices', { params: { type } }),
  
  // 标记已读
  markAsRead: (id: number) => 
    api.post(`/notices/${id}/read`)
}

// 在组件中使用
import { noticeApi } from '@/api'

const loadNotices = async (type: string) => {
  const response = await noticeApi.getNotices(type)
  return response.data
}
```

## 🎯 用户流程

1. **用户协议页面** (`UserAgreement.vue`)
   - 用户阅读协议
   - 点击"同意"按钮
   - 跳转到游戏首页 (`/game`)

2. **游戏首页** (`GameHome.vue`)
   - 页面加载完成
   - 延迟 500ms 后自动显示公告弹窗
   - 用户可以：
     - 切换不同公告分类
     - 浏览上一条/下一条公告
     - 点击关闭按钮或遮罩层关闭弹窗

## 🎨 样式定制

### 主要颜色

```css
/* 激活菜单项背景色 */
.notice-menu-item.active {
  background: #ff6600;
  color: #fff;
}

/* 悬停效果 */
.notice-menu-item:hover {
  background: #fff9f0;
  color: #ff6600;
}
```

### 尺寸参数

```css
/* 对话框宽度 */
width: 500px;

/* 公告头部图标 */
width: 300px;
height: 155px;

/* 左侧菜单 */
width: 90px;

/* 内容区域高度 */
min-height: 360px;
max-height: 360px;
```

## 🔧 高级配置

### 1. 控制首次访问才显示

```typescript
onMounted(() => {
  // 检查是否已查看公告
  const hasViewedNotice = localStorage.getItem('noticeViewed')
  
  if (!hasViewedNotice) {
    setTimeout(() => {
      showNoticeDialog.value = true
    }, 500)
  }
})

const handleCloseNotice = () => {
  showNoticeDialog.value = false
  // 标记已查看
  localStorage.setItem('noticeViewed', 'true')
}
```

### 2. 按日期控制显示

```typescript
const shouldShowNotice = () => {
  const lastViewDate = localStorage.getItem('lastNoticeViewDate')
  const today = new Date().toDateString()
  
  // 每天首次访问才显示
  return lastViewDate !== today
}

onMounted(() => {
  if (shouldShowNotice()) {
    setTimeout(() => {
      showNoticeDialog.value = true
    }, 500)
  }
})

const handleCloseNotice = () => {
  showNoticeDialog.value = false
  localStorage.setItem('lastNoticeViewDate', new Date().toDateString())
}
```

### 3. 强制阅读（必须查看所有公告才能关闭）

```typescript
// 在 NoticeDialog.vue 中
const hasReadAll = ref(false)
const readNotices = ref<Set<number>>(new Set())

const handleNext = () => {
  if (currentNotice.value) {
    readNotices.value.add(currentNotice.value.id)
  }
  
  if (currentIndex.value < currentNotices.value.length - 1) {
    currentIndex.value++
  } else {
    hasReadAll.value = true
  }
}

// 只有阅读完所有公告才能关闭
const canClose = computed(() => hasReadAll.value)
```

## 📱 响应式适配

组件已包含自定义滚动条样式，适配不同屏幕尺寸。如需移动端适配，可添加：

```css
@media screen and (max-width: 768px) {
  :deep(.el-dialog) {
    width: 90% !important;
  }
  
  .notice-header {
    width: 100%;
    height: auto;
  }
  
  .notice-main {
    flex-direction: column;
  }
  
  .notice-menu {
    width: 100%;
    height: auto;
    display: flex;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .notice-menu-item {
    flex: 1;
  }
}
```

## 🐛 常见问题

### 1. 公告弹窗不显示

**检查清单：**
- ✅ 确认 `showNoticeDialog` 初始值为 `false`
- ✅ 确认 `onMounted` 中有设置 `showNoticeDialog.value = true`
- ✅ 确认 Element Plus 已正确安装
- ✅ 检查浏览器控制台是否有错误

### 2. 图标不显示

**解决方案：**
```typescript
// 确认 SVG 文件路径正确
import noticeHeaderIcon from '@/assets/notice-header-red.svg'

// 或使用完整路径
import noticeHeaderIcon from '@/assets/notice-header-red-bed63626.svg'
```

### 3. 样式异常

**原因：**Element Plus 全局样式覆盖

**解决：**
```css
/* 使用 :deep() 穿透 */
:deep(.el-dialog) {
  /* 自定义样式 */
}
```

## 📝 待优化项

1. [ ] 接入后端 API 获取真实公告数据
2. [ ] 添加公告图片支持
3. [ ] 添加公告富文本编辑器支持
4. [ ] 添加公告已读/未读状态
5. [ ] 添加公告搜索功能
6. [ ] 添加公告置顶功能
7. [ ] 添加公告优先级排序

## 🔗 相关文件

- **组件源码**: `/frontend/src/components/NoticeDialog.vue`
- **使用页面**: `/frontend/src/views/GameHome.vue`
- **图标资源**: `/frontend/src/assets/notice-header-red.svg`
- **路由配置**: `/frontend/src/router/index.ts`

## 📞 技术支持

如有问题，请查看：
1. Element Plus Dialog 官方文档
2. Vue 3 Composition API 文档
3. TypeScript 类型定义

---

**最后更新时间**: 2026-02-06  
**组件版本**: v1.0.0
