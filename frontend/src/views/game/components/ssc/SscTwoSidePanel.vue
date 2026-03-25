<script setup lang="ts">
/**
 * SscTwoSidePanel - Main SSC two-side betting panel.
 * Composes 5 ball rows (第一球~第五球) + sum section (总和+龙虎和).
 * Manages selection state and bet amounts for all SSC two-side bets.
 */
import { reactive, ref } from 'vue'
import SscBettingRow from './SscBettingRow.vue'
import SscTwoSideSum from './SscTwoSideSum.vue'
import {
  SSC_BALL_ITEMS,
  SSC_BALL_TITLES,
  SSC_SUM_ROW1,
  SSC_SUM_ROW2,
} from '../../constants/sscOdds'

interface Props {
  /** Whether the current period is sealed (disables all inputs) */
  isSealed: boolean
  /** Quick mode: 'quick' hides input boxes, 'normal' shows them */
  quickMode: 'quick' | 'normal'
  /** Previous draw balls (5 numbers for SSC) */
  preDrawBalls: number[]
  /** Whether to show blue ball icon beside each row title */
  showHeaderBall?: boolean
}

defineProps<Props>()

// ─── Selection State ─────────────────────────────────────────────────────
// Track which bet cells are currently selected (for quick bet mode)
const selectedKeys = reactive(new Set<string>())

/** Check if a given bet key is selected */
const isKeySelected = (key: string): boolean => selectedKeys.has(key)

/** Toggle selection on a bet key */
const toggleSelect = (key: string) => {
  if (selectedKeys.has(key)) {
    selectedKeys.delete(key)
  } else {
    selectedKeys.add(key)
  }
}

/** Ensure a bet key is selected (called when input is focused) */
const ensureSelected = (key: string) => {
  selectedKeys.add(key)
}

// ─── Bet Amounts ─────────────────────────────────────────────────────────
// Store bet amounts for all cells, keyed by unique bet key
const amounts = ref<Record<string, string>>({})

/** Update amounts (called from child components) */
const updateAmounts = (newAmounts: Record<string, string>) => {
  amounts.value = newAmounts
}

// Selection checker ref for passing to child components
const isSelectedRef = ref(isKeySelected)
</script>

<template>
  <div class="ssc-two-side-panel">
    <!-- Ball rows: 第一球 ~ 第五球, each with 大/小/单/双 -->
    <SscBettingRow
      v-for="(title, idx) in SSC_BALL_TITLES"
      :key="title"
      :title="title"
      :items="SSC_BALL_ITEMS"
      :is-sealed="isSealed"
      :quick-mode="quickMode"
      :amounts="amounts"
      :key-prefix="`ball${idx + 1}`"
      :is-first="idx === 0"
      :ball-number="showHeaderBall ? (preDrawBalls[idx] ?? null) : null"
      v-model:isSelected="isSelectedRef"
      @toggle="toggleSelect"
      @ensure="ensureSelected"
      @update:amounts="updateAmounts"
    />

    <!-- Sum section: 总和(总大/总小/总单/总双) + 龙/虎/和 -->
    <SscTwoSideSum
      :row1="SSC_SUM_ROW1"
      :row2="SSC_SUM_ROW2"
      :is-sealed="isSealed"
      :quick-mode="quickMode"
      :amounts="amounts"
      key-prefix="sum"
      v-model:isSelected="isSelectedRef"
      @toggle="toggleSelect"
      @ensure="ensureSelected"
      @update:amounts="updateAmounts"
    />
  </div>
</template>

<style scoped>
.ssc-two-side-panel {
  width: 720px;
}
</style>
