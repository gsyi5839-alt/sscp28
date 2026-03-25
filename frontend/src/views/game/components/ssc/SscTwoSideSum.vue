<script setup lang="ts">
/**
 * SscTwoSideSum - SSC two-side sum betting section.
 * Renders the "总和" section with two rows:
 *   Row 1: 总大 | 总小 | 总单 | 总双
 *   Row 2: 龙 | 虎 | 和(8.8) | empty(--)
 * Data format differs from ball rows (different labels, mixed odds, disabled cell).
 */
import type { SscBetItem } from '../../constants/sscOdds'

interface Props {
  /** First row items (总大/总小/总单/总双) */
  row1: SscBetItem[]
  /** Second row items (龙/虎/和/placeholder) */
  row2: SscBetItem[]
  /** Whether the current period is sealed */
  isSealed: boolean
  /** Quick mode: 'quick' hides input boxes */
  quickMode: 'quick' | 'normal'
  /** Bet amounts keyed by unique bet key */
  amounts: Record<string, string>
  /** Prefix for generating unique bet keys (e.g., 'sum') */
  keyPrefix: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'toggle': [betKey: string]
  'ensure': [betKey: string]
  'update:amounts': [val: Record<string, string>]
}>()

/** Check if a bet key is selected (delegated from parent) */
const isSelected = defineModel<(key: string) => boolean>('isSelected', {
  default: () => () => false,
})

/** Generate unique bet key */
const getBetKey = (label: string) => `${props.keyPrefix}_${label}`

/** Handle el-input-number value change */
const onAmountChange = (label: string, val: number | undefined) => {
  emit('update:amounts', {
    ...props.amounts,
    [getBetKey(label)]: val !== undefined && val !== null ? String(val) : '',
  })
}
</script>

<template>
  <div class="ssc-sum-section">
    <!-- Section header -->
    <div class="ssc-sum-header">总和</div>

    <!-- Row 1: 总大/总小/总单/总双 -->
    <div class="ssc-sum-row">
      <div
        v-for="(item, idx) in row1"
        :key="item.label"
        class="ssc-sum-cell"
        :class="{
          'ssc-sum-cell--selected': isSelected(getBetKey(item.label)),
          'ssc-sum-cell--sealed': isSealed,
          'ssc-sum-cell--last': idx === row1.length - 1,
        }"
        @click="$emit('toggle', getBetKey(item.label))"
      >
        <div class="ssc-sum-label">{{ item.label }}</div>
        <div class="ssc-sum-odd">
          <b class="text-red">{{ isSealed ? '--' : item.odd }}</b>
        </div>
        <!-- Input area: only render in normal mode to avoid empty flex space in quick mode -->
        <div v-if="quickMode === 'normal'" class="ssc-sum-input">
          <el-input-number
            :model-value="amounts[getBetKey(item.label)] ? Number(amounts[getBetKey(item.label)]) : undefined"
            class="ssc-number-input"
            size="small"
            :controls="false"
            :disabled="isSealed"
            @click.stop
            @update:model-value="(val: number | undefined) => onAmountChange(item.label, val)"
            @focus="$emit('ensure', getBetKey(item.label))"
          />
        </div>
      </div>
    </div>

    <!-- Row 2: 龙/虎/和/empty -->
    <div class="ssc-sum-row ssc-sum-row--last">
      <div
        v-for="(item, idx) in row2"
        :key="idx"
        class="ssc-sum-cell"
        :class="{
          'ssc-sum-cell--selected': !item.disabled && isSelected(getBetKey(item.label)),
          'ssc-sum-cell--disabled': item.disabled,
          'ssc-sum-cell--sealed': isSealed,
          'ssc-sum-cell--last': idx === row2.length - 1,
        }"
        @click="!item.disabled && $emit('toggle', getBetKey(item.label))"
      >
        <div class="ssc-sum-label">{{ item.label }}</div>
        <div class="ssc-sum-odd">
          <b class="text-red">{{ isSealed ? '--' : item.odd }}</b>
        </div>
        <!-- Input area: only render in normal mode to avoid empty flex space in quick mode -->
        <div v-if="quickMode === 'normal' && !item.disabled" class="ssc-sum-input">
          <el-input-number
            :model-value="amounts[getBetKey(item.label)] ? Number(amounts[getBetKey(item.label)]) : undefined"
            class="ssc-number-input"
            size="small"
            :controls="false"
            :disabled="isSealed"
            @click.stop
            @update:model-value="(val: number | undefined) => onAmountChange(item.label, val)"
            @focus="$emit('ensure', getBetKey(item.label))"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ssc-sum-section {
  width: 100%;
}

/* Sum section gradient header */
.ssc-sum-header {
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-weight: bold;
  font-size: 13px;
  color: #000;
  background: var(--bw-table-header-bg-color, linear-gradient(to bottom, #fff 0%, #fff1e4 100%));
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  box-sizing: border-box;
}

/* Sum row container */
.ssc-sum-row {
  display: flex;
  width: 100%;
}

/* Individual sum cell: min-width 25% */
.ssc-sum-cell {
  flex: 1;
  min-width: 25%;
  display: flex;
  align-items: center;
  height: 30px;
  line-height: 30px;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  border-left: none;
  box-sizing: border-box;
  cursor: pointer;
}

/* First cell in row has left border */
.ssc-sum-cell:first-child {
  border-left: 1px solid var(--bw-border-color, #efba84);
}

/* Sealed state: disabled background */
.ssc-sum-cell--sealed {
  background: var(--el-disabled-bg-color, #f5f7fa);
}

/* Hover state (not when sealed or disabled) */
.ssc-sum-cell:not(.ssc-sum-cell--disabled):not(.ssc-sum-cell--sealed):hover {
  background: var(--bw-header-color, #be9d76);
}

.ssc-sum-cell:not(.ssc-sum-cell--disabled):hover .ssc-sum-label,
.ssc-sum-cell:not(.ssc-sum-cell--disabled):hover .ssc-sum-input {
  background: transparent;
}

/* Selected state */
.ssc-sum-cell--selected,
.ssc-sum-cell--selected:hover {
  background: #ffc214;
}

.ssc-sum-cell--selected .ssc-sum-label,
.ssc-sum-cell--selected:hover .ssc-sum-label,
.ssc-sum-cell--selected .ssc-sum-odd,
.ssc-sum-cell--selected:hover .ssc-sum-odd,
.ssc-sum-cell--selected .ssc-sum-input,
.ssc-sum-cell--selected:hover .ssc-sum-input {
  background: #ffc214;
}

/* Disabled cell */
.ssc-sum-cell--disabled {
  cursor: not-allowed;
}

/* Label area */
.ssc-sum-label {
  width: 60px;
  flex: 0 0 60px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  color: var(--bw-default-color, #351c0c);
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  font-size: 13px;
}

/* Odds area */
.ssc-sum-odd {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.ssc-sum-odd b {
  font-weight: 500;
}

.text-red {
  color: red;
}

/* Input area */
.ssc-sum-input {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  box-sizing: border-box;
}

/* el-input-number: match original design width 50px, small size, no controls */
.ssc-number-input {
  width: 50px !important;
  --el-input-border-color: #abb2c5;
  --el-input-border-radius: 4px;
}

/* Override el-input-number inner input styles */
.ssc-number-input :deep(.el-input__wrapper) {
  height: 22px;
  min-height: 22px;
  padding: 0 4px;
  border-radius: 4px;
  box-shadow: 0 0 0 1px #abb2c5 inset;
}

.ssc-number-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary, #5c2e0d) inset;
}

.ssc-number-input :deep(.el-input__inner) {
  text-align: center;
  font-size: 12px;
  height: 22px;
  line-height: 22px;
}
</style>
