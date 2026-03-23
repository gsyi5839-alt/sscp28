<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import noticeHeaderIcon from '@/assets/公告/notice-header-red.svg'
import { noticeApi } from '@/api/index'

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

// Currently active menu type
const activeMenu = ref('特别通知')

// Menu list
const menuItems = [
  { key: '特别通知', label: '特别通知' },
  { key: '通知', label: '通知' },
  { key: '安全通知', label: '安全通知' },
  { key: '站点通知', label: '站点通知' }
]

// Notice data fetched from backend API, grouped by category
interface NoticeItem {
  id: number
  category: string
  title: string
  content: string
  createTime: string
}
const noticeData = ref<Record<string, NoticeItem[]>>({})
const loading = ref(false)

/**
 * Fetch notices from backend API.
 * Called when dialog becomes visible.
 */
const fetchNotices = async () => {
  loading.value = true
  try {
    const res = await noticeApi.getNotices() as any
    if (res?.code === 200 && res.data) {
      noticeData.value = res.data
    }
  } catch (err) {
    console.error('Failed to fetch notices:', err)
  } finally {
    loading.value = false
  }
}

// Fetch notices when dialog becomes visible
watch(() => props.visible, (newVal) => {
  if (newVal) {
    fetchNotices()
  }
})

// Notice list for current menu
const currentNotices = computed(() => {
  return noticeData.value[activeMenu.value] || []
})

// Currently displayed notice index
const currentIndex = ref(0)

// Currently displayed notice
const currentNotice = computed(() => {
  return currentNotices.value[currentIndex.value] || null
})

// Total pages
const totalPages = computed(() => {
  return currentNotices.value.length
})

// Switch menu
const handleMenuClick = (menuKey: string) => {
  activeMenu.value = menuKey
  currentIndex.value = 0 // Reset to first item when switching menu
}

// Next item
const handleNext = () => {
  if (currentIndex.value < currentNotices.value.length - 1) {
    currentIndex.value++
  }
}

// Close dialog
const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

// Dialog internal visible state
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
/* Completely override Element Plus Dialog default styles */
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

/* Notice dialog content container - only place to control width */
.notice-dialog-content {
  width: 300px;
  margin: 0 auto;
  background: transparent;
}

/* Notice header icon - inherits parent container 300px width */
.notice-header {
  width: 100%;
  height: 155px;
  position: relative;
  z-index: 0;
  /* Align with @design.md: use SVG as background instead of <img>. */
  background-position: 50% calc(100% + 1px);
  background-size: 100%;
  background-repeat: no-repeat;
  margin-bottom: -10px;
}

/* Custom close button 42x42 circle */
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

/* White background content area - inherits parent 300px width, flush with SVG bottom */
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

/* Notice detail title */
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

/* Main content area */
.notice-main {
  display: flex;
  min-height: 360px;
  max-height: 360px;
}

/* Left menu */
.notice-menu {
  position: relative;
  width: 90px;
  height: 360px;
  margin-left: -10px; /* match @design.md */
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

/* Match @design.md: pseudo layers slide in */
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

/* Right content area */
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

/* Notice content */
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

/* Pagination control */
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

/* Hide all scrollbars (keep scroll functionality) */
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

/* header and body are children of .notice-dialog, use space selector */
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
