<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DragonLeaderboard from './DragonLeaderboard.vue'
import type { DragonItem } from '../composables/useDragonLeaderboard'
import type { PropType } from 'vue'
import { noticeApi } from '@/api/index'

defineProps({
  dragonList: {
    type: Array as PropType<DragonItem[]>,
    required: true
  }
})

const emit = defineEmits<{
  'moreClick': []
}>()

// Latest announce content fetched from API
const announceText = ref('')

/**
 * Fetch the latest notice from backend API.
 * Displays the first item from '特别通知' category.
 */
const fetchLatestNotice = async () => {
  try {
    const res = await noticeApi.getNotices() as any
    if (res?.code === 200 && res.data) {
      const specialNotices = res.data['特别通知'] || []
      if (specialNotices.length > 0) {
        announceText.value = specialNotices[0].content
      }
    }
  } catch (_e) {
    // Silently handle fetch errors
  }
}

onMounted(() => {
  fetchLatestNotice()
})
</script>

<template>
  <div class="right-sidebar">
    <!-- Announce header -->
    <div class="announce-header">
      <span class="announce-title">公告</span>
      <span class="more-link" @click="$emit('moreClick')">更多</span>
    </div>
    <!-- Content -->
    <div class="right-sidebar-scroll">
      <!-- Announce content -->
      <div class="announce-body">
        <p>{{ announceText }}</p>
      </div>
      <!-- Dragon leaderboard -->
      <DragonLeaderboard :dragon-list="dragonList" />
    </div>
  </div>
</template>

<style scoped>
.right-sidebar {
  width: 160px;
  flex-shrink: 0;
  margin-left: 10px;
  margin-top: 6px;
  align-self: flex-start;
}

.right-sidebar-scroll {
  overflow: visible;
}

.announce-header {
  height: 45px;
  line-height: 45px;
  padding: 0 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-linear-bg, linear-gradient(to bottom, #a6744d 0%, #351c0c 100%));
  color: #fff;
  font-weight: 400;
  font-size: 13px;
}

.announce-title {
  font-size: 13px;
  font-weight: 400;
  color: #fff;
}

.more-link {
  font-size: 13px;
  font-weight: 400;
  color: #fff;
  cursor: pointer;
}

.more-link:hover {
  text-decoration: underline;
}

.announce-body {
  padding: 10px;
  width: 160px;
  min-height: 50px;
  font-size: 13px;
  line-height: 26px;
  color: #000;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  box-sizing: border-box;
  overflow: hidden;
  text-align: left;
  word-break: break-word;
  white-space: normal;
}

.announce-body p {
  margin: 0;
}
</style>
