<script setup lang="ts">
import { computed } from 'vue'
import { calcBullPokerResult, calcBullResult, parseNumbers } from '@/utils/lotteryCalc'
import {
  SSC_NIUNIU_STATS_BULL,
  SSC_NIUNIU_STATS_POKER,
} from '../../constants/sscOdds'

interface Props {
  historyIssues: any[]
}

const props = defineProps<Props>()

const parseFiveBalls = (issue: any): number[] | null => {
  const code = issue?.preDrawCode ?? issue?.drawCode ?? ''
  const nums = parseNumbers(String(code)).slice(0, 5)
  return nums.length === 5 ? nums : null
}

const counts = computed(() => {
  const map = Object.fromEntries([
    ...SSC_NIUNIU_STATS_BULL,
    ...SSC_NIUNIU_STATS_POKER,
  ].map(label => [label, 0])) as Record<string, number>

  props.historyIssues.forEach(issue => {
    const nums = parseFiveBalls(issue)
    if (!nums) return

    const bullResult = calcBullResult(nums)
    map[bullResult] = (map[bullResult] ?? 0) + 1

    const pokerResult = calcBullPokerResult(nums)
    map[pokerResult] = (map[pokerResult] ?? 0) + 1
  })

  return map
})

const statLabels = computed(() => ([
  ...SSC_NIUNIU_STATS_BULL,
  ...SSC_NIUNIU_STATS_POKER,
]))
</script>

<template>
  <div class="ssc-niuniu-stats">
    <div class="stats-tab-bar">
      <div class="stats-tab">斗牛</div>
    </div>
    <div class="stats-shell">
      <div class="stats-grid">
        <div
          v-for="label in statLabels"
          :key="label"
          class="stats-item"
        >
          <b class="stats-label">{{ label }}</b>
          <span class="stats-value">{{ counts[label] ?? 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ssc-niuniu-stats {
  width: 720px;
  margin-top: 10px;
  font-size: 13px;
  color: #000;
}

.stats-tab-bar {
  display: flex;
  align-items: stretch;
  width: 100%;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  overflow: hidden;
}

.stats-tab {
  flex: 1;
  color: red;
  font-weight: 700;
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
}

.stats-shell {
  width: 100%;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  box-sizing: border-box;
}

.stats-grid {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  width: 100%;
}

.stats-item {
  flex: 0 0 calc(100% / 11);
  width: calc(100% / 11);
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.stats-item:nth-child(-n + 11) {
  border-bottom: 1px solid var(--bw-border-color, #efba84);
}

.stats-label,
.stats-value {
  display: block;
  height: 28px;
  line-height: 28px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.stats-label {
  font-weight: 700;
  background: #e0e0e0;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}
</style>
