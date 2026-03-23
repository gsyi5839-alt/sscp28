<script setup lang="ts">
import { ref, computed } from 'vue'
import { noticeTabItems, noticeListData, NOTICE_SHOWN_KEY } from '../constants/notices'

const showNoticeDialog = defineModel<boolean>('showDialog', { default: false })
const showNoticeList = defineModel<boolean>('showList', { default: false })

const activeNoticeTab = ref('特别通知')

const currentNoticeList = computed(() => noticeListData[activeNoticeTab.value] || [])

const onNoticeTabClick = (key: string) => {
  activeNoticeTab.value = key
}

const handleCloseNotice = () => {
  showNoticeDialog.value = false
  sessionStorage.setItem(NOTICE_SHOWN_KEY, 'true')
}

defineExpose({
  handleCloseNotice
})
</script>

<template>
  <!-- Notice list panel (inline) -->
  <div v-if="showNoticeList" class="notice-list-panel">
    <div class="notice-tabs">
      <div
        v-for="item in noticeTabItems"
        :key="item.key"
        :class="['notice-tab', { active: item.key === activeNoticeTab }]"
        @click="onNoticeTabClick(item.key)"
      >
        {{ item.label }}
      </div>
    </div>
    <div class="notice-list-body">
      <div
        v-for="notice in currentNoticeList"
        :key="notice.id"
        :class="['notice-row', { highlight: notice.isHighlight }]"
      >
        <div class="notice-time-col">{{ notice.time }}</div>
        <div class="notice-content-col">{{ notice.content }}</div>
      </div>
      <div v-if="currentNoticeList.length === 0" class="notice-empty">
        暂无公告
      </div>
    </div>
  </div>
</template>

<style scoped>
.notice-list-panel {
  width: 600px;
  min-height: 247px;
  background: #fff;
  border: 1px solid var(--bw-border-color, #EFBA84);
  display: flex;
  flex-direction: column;
}

.notice-tabs {
  display: flex;
  border-bottom: 1px solid var(--bw-border-color, #EFBA84);
  background: var(--bw-bg-3, #fff7ef);
}

.notice-tab {
  flex: 1;
  text-align: center;
  padding: 6px 0;
  font-size: 13px;
  color: #000;
  cursor: pointer;
  border-right: 1px solid var(--bw-border-color, #EFBA84);
  transition: all 0.2s;
  user-select: none;
  height: 30px;
  line-height: 18px;
  box-sizing: border-box;
}

.notice-tab:last-child {
  border-right: none;
}

.notice-tab:hover {
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
}

.notice-tab.active {
  color: red;
  font-weight: bold;
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
}

.notice-list-body {
  flex: 1;
  overflow-y: auto;
}

.notice-row {
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid var(--bw-border-color, #EFBA84);
  line-height: 1.5;
}

.notice-row:last-child {
  border-bottom: none;
}

.notice-row.highlight {
  font-weight: bold;
  color: red;
}

.notice-time-col {
  flex-shrink: 0;
  width: 150px;
  padding: 8px 6px;
  text-align: center;
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  border-right: 1px solid var(--bw-border-color, #EFBA84);
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-content-col {
  flex: 1;
  width: 80%;
  padding: 8px;
  font-size: 13px;
  word-break: break-all;
  display: flex;
  align-items: center;
  line-height: 1.5;
}

.notice-empty {
  padding: 40px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.notice-list-body::-webkit-scrollbar {
  width: 6px;
}

.notice-list-body::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.notice-list-body::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.notice-list-body::-webkit-scrollbar-thumb:hover {
  background: #999;
}
</style>
