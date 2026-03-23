<script setup lang="ts">
import type { DragonItem } from '../composables/useDragonLeaderboard'

interface Props {
  dragonList: DragonItem[]
}

defineProps<Props>()
</script>

<template>
  <div class="dragon-header">两面长龙排行</div>
  <div class="dragon-list">
    <div v-for="item in dragonList" :key="item.label" class="dragon-row">
      <span class="dragon-label">{{ item.label }}</span>
      <span class="dragon-value">{{ item.value }}</span>
    </div>
  </div>
</template>

<style scoped>
.dragon-header {
  height: 45px;
  line-height: 45px;
  text-align: center;
  font-size: 14px;
  font-weight: 400;
  color: #fff;
  background: var(--bw-linear-bg, linear-gradient(to bottom, #a6744d 0%, #351c0c 100%));
  border-bottom: 1px solid var(--bw-border-color, #efba84);
}

.dragon-list {
  padding: 0;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
}

.dragon-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 26px;
  line-height: 26px;
  font-size: 13px;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
}

.dragon-row:last-child {
  border-bottom: none;
}

.dragon-label {
  width: 50%;
  height: 25px;
  line-height: 25px;
  padding-left: 10px;
  color: #000;
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  box-sizing: border-box;
}

.dragon-value {
  width: 50%;
  padding-left: 10px;
  color: red;
  font-weight: 400;
  box-sizing: border-box;
}
</style>
