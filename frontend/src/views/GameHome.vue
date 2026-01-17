<script setup lang="ts">
import { onMounted } from 'vue'
import GameHeader from '../components/GameHeader.vue'

onMounted(() => {
  document.title = '游戏首页'
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) {
    favicon.href = '/favicon.png'
  }
})
</script>

<template>
  <div class="page">
    <GameHeader />
    <div class="content">
      <!-- 先按你的要求：优先把顶部导航栏做出来。
           后续你再给“首页主体区域”的 DOM/截图，我继续补齐布局。 -->
      <div class="placeholder">
        游戏首页（主体内容待接入）
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #ffffff;
}

.content {
  width: 1418px;
  margin: 0 auto;
  min-height: calc(100vh - 70px);
  box-sizing: border-box;
  padding: 16px 12px;
}

.placeholder {
  border: 1px dashed #ddd;
  color: #666;
  padding: 24px;
  font-size: 14px;
}
</style>

