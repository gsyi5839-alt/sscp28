<script setup lang="ts">
import { ref, computed } from 'vue'
import noticeHeaderIcon from '@/assets/公告/notice-header-red.svg'

// Props
interface Props {
  visible: boolean
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'close'): void
}>()

// 当前激活的菜单类型
const activeMenu = ref('特别通知')

// 菜单列表
const menuItems = [
  { key: '特别通知', label: '特别通知' },
  { key: '通知', label: '通知' },
  { key: '安全通知', label: '安全通知' },
  { key: '站点通知', label: '站点通知' }
]

// 模拟公告数据（实际项目中应该从后端API获取）
const noticeData = {
  '特别通知': [
    {
      id: 1,
      title: '特别通知',
      content: '尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)',
      createTime: '2026-02-06'
    },
    {
      id: 2,
      title: '系统维护通知',
      content: '为了给您提供更好的服务体验，系统将于今晚23:00-24:00进行维护升级，期间可能无法访问，请您谅解。',
      createTime: '2026-02-05'
    }
  ],
  '通知': [
    {
      id: 3,
      title: '重要通知',
      content: '请各位会员注意保管好自己的账号密码，不要向任何人透露您的账户信息。',
      createTime: '2026-02-04'
    }
  ],
  '安全通知': [
    {
      id: 4,
      title: '账户安全提示',
      content: '为了保障您的账户安全，建议定期修改密码，并开启双重验证。',
      createTime: '2026-02-03'
    }
  ],
  '站点通知': [
    {
      id: 5,
      title: '站点公告',
      content: '欢迎访问本站，祝您使用愉快！',
      createTime: '2026-02-02'
    }
  ]
}

// 当前菜单对应的公告列表
const currentNotices = computed(() => {
  return noticeData[activeMenu.value as keyof typeof noticeData] || []
})

// 当前显示的公告索引
const currentIndex = ref(0)

// 当前显示的公告
const currentNotice = computed(() => {
  return currentNotices.value[currentIndex.value] || null
})

// 总页数
const totalPages = computed(() => {
  return currentNotices.value.length
})

// 切换菜单
const handleMenuClick = (menuKey: string) => {
  activeMenu.value = menuKey
  currentIndex.value = 0 // 切换菜单时重置到第一条
}

// 下一条
const handleNext = () => {
  if (currentIndex.value < currentNotices.value.length - 1) {
    currentIndex.value++
  }
}

// 关闭对话框
const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

// 对话框内部visible状态
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :show-close="false"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    width="300px"
    class="notice-dialog"
    @close="handleClose"
  >
    <template #header>
      <span style="display: none;"></span>
    </template>

    <div class="notice-dialog-content">
      <!-- 公告头部图标 + 自定义关闭按钮 -->
      <div class="notice-header" :style="{ backgroundImage: `url(${noticeHeaderIcon})` }">
        <span class="close-btn" @click="handleClose">&times;</span>
      </div>

      <!-- 白色背景内容区 -->
      <div class="notice-body">
        <!-- 公告详情标题 -->
        <div class="notice-title">公告详情</div>

        <!-- 主体内容区域 -->
        <div class="notice-main">
          <!-- 左侧菜单 -->
          <div class="notice-menu scroll-bar">
            <div
              v-for="item in menuItems"
              :key="item.key"
              :class="['notice-menu-item', { active: item.key === activeMenu }]"
              @click="handleMenuClick(item.key)"
            >
              {{ item.label }}
            </div>
          </div>

          <!-- 右侧内容 -->
          <div class="notice-content-wrapper">
            <div class="notice-content-inner">
              <!-- 公告内容 -->
              <div class="notice-content scroll-bar">
                <div v-if="currentNotice">
                  {{ currentNotice.content }}
                </div>
                <div v-else class="no-data">暂无公告</div>
              </div>

              <!-- 分页控制 -->
              <div class="notice-pagination">
                <span class="page-info">{{ currentIndex + 1 }} / {{ totalPages }}</span>
                <span 
                  class="page-action"
                  :class="{ disabled: currentIndex >= totalPages - 1 }"
                  @click="handleNext"
                >
                  下一条
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
/* 彻底覆盖 Element Plus Dialog 默认样式 */
:deep(.el-dialog) {
  border-radius: 0 !important;
  padding: 0 !important;
  overflow: visible !important;
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
}

:deep(.el-dialog__header) {
  display: none !important;
  padding: 0 !important;
  margin: 0 !important;
}

:deep(.el-dialog__body) {
  padding: 0 !important;
  overflow: visible !important;
  background: transparent !important;
}

/* 公告对话框内容容器 - 唯一控制宽度的地方 */
.notice-dialog-content {
  width: 300px;
  margin: 0 auto;
  background: transparent;
}

/* 公告头部图标 - 继承父容器300px宽度 */
.notice-header {
  width: 100%;
  height: 155px;
  position: relative;
  z-index: 0;
  /* Align with @设计样式.md: use SVG as background instead of <img>. */
  background-position: 50% calc(100% + 1px);
  background-size: 100%;
  background-repeat: no-repeat;
  margin-bottom: -10px;
}

/* 自定义关闭按钮 42x42 圆形 */
.close-btn {
  position: absolute;
  top: 8px;
  right: 10px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  border: 2px solid rgba(255, 255, 255, 0.9);
  line-height: 1;
  transition: background 0.3s;
  user-select: none;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.75);
}

/* 白色背景内容区 - 继承父容器300px宽度，紧贴SVG底部 */
.notice-body {
  width: 100%;
  background: #fff;
  padding: 0;
  border-radius: 0 0 14px 14px;
  box-shadow: none;
  margin-top: 0;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
}

/* 公告详情标题 */
.notice-title {
  font-size: 20px;
  height: 40px;
  line-height: 40px;
  font-weight: bold;
  text-align: center;
  padding-top: 10px;
  margin-bottom: 10px;
  color: #333;
}

/* 主体内容区域 */
.notice-main {
  display: flex;
  min-height: 360px;
  max-height: 360px;
}

/* 左侧菜单 */
.notice-menu {
  position: relative;
  width: 90px;
  height: 360px;
  margin-left: -10px; /* match @设计样式.md */
  flex-basis: 90px;
  flex-shrink: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 10px;
  scrollbar-width: none;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.notice-menu-item {
  width: 90px;
  text-align: center;
  font-size: 12px;
  color: #adadad;
  cursor: pointer;
  transition: color 0.2s ease-in-out;
  user-select: none;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 0; /* create stacking context for pseudo elements */
  overflow: hidden;
  padding: 12px 0 12px 10px;
  box-sizing: border-box;
}

.notice-menu-item:hover {
  color: #f28c1d;
}

.notice-menu-item.active {
  color: #fff;
}

/* Match @设计样式.md: pseudo layers slide in */
.notice-menu-item::before {
  content: '';
  display: block;
  width: 100%;
  height: 100%;
  background: #f28c1d;
  position: absolute;
  right: 0;
  top: 0;
  z-index: -1;
  box-shadow: 0 2px 2px #0000001a;
  transform: translate(-100px);
  transition: all 0.3s ease-in-out;
}

.notice-menu-item.active::before {
  transform: translateY(0);
  opacity: 1;
}

/* 右侧内容区域 */
.notice-content-wrapper {
  flex: 1;
  padding-left: 10px;
  padding-right: 10px;
  line-height: 20px;
  position: relative;
  word-break: break-all;
}

.notice-content-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 公告内容 */
.notice-content {
  flex: 1;
  overflow-y: auto;
  font-size: 14px;
  color: #333;
  line-height: 20px;
  word-break: break-all;
  padding: 0;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

/* 分页控制 */
.notice-pagination {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 20px;
  padding-right: 20px;
  font-size: 12px;
  color: #adadad;
  border-top: none;
}

.page-info {
  font-size: 12px;
}

.page-action {
  cursor: pointer;
  transition: color 0.3s;
  user-select: none;
}

.page-action:hover:not(.disabled) {
  color: var(--bw-default-color, #351c0c);
}

.page-action.disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* 隐藏所有滚动条（保留滚动功能） */
.scroll-bar {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.scroll-bar::-webkit-scrollbar {
  display: none; /* Chrome/Safari */
}
</style>

<!-- 全局样式：彻底覆盖 Element Plus Dialog -->
<style>
/*
 * 关键：Element Plus 把 class="notice-dialog" 加在 .el-dialog 同一个元素上
 * 所以选择器必须是 .el-dialog.notice-dialog（无空格），不是 .notice-dialog .el-dialog
 */
.el-dialog.notice-dialog {
  --el-dialog-bg-color: transparent;
  --el-dialog-box-shadow: none;
  --el-dialog-border-radius: 0px;
  --el-dialog-padding-primary: 0px;
  --el-dialog-width: 300px;
  --el-bg-color: transparent;
  width: 300px !important;
  max-width: 300px !important;
  min-width: 300px !important;
  background: transparent !important;
  background-color: transparent !important;
  box-shadow: none !important;
  border: none !important;
  border-radius: 0 !important;
  overflow: visible !important;
  padding: 0 !important;
}

/* header 和 body 是 .notice-dialog 的子元素，用空格选择器 */
.el-dialog.notice-dialog .el-dialog__header {
  display: none !important;
  padding: 0 !important;
  margin: 0 !important;
  height: 0 !important;
  min-height: 0 !important;
  background: transparent !important;
}

.el-dialog.notice-dialog .el-dialog__body {
  padding: 0 !important;
  background: transparent !important;
  background-color: transparent !important;
  overflow: visible !important;
}
</style>
