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
      <!-- First priority per your request: implement top navigation bar.
           Later you provide "home page main body" DOM/screenshot, I'll continue to complete the layout. -->
      <div class="placeholder">
        Game Home (Main content to be implemented)
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

