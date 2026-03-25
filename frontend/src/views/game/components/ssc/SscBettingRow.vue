<script setup lang="ts">
/**
 * SscBettingRow - Reusable SSC two-side betting row component.
 * Renders a gradient header + a row of 4 betting cells (label | odd | input).
 * Used for 第一球~第五球 sections, each with 大/小/单/双 options.
 */
import type { SscBetItem } from '../../constants/sscOdds'
import { getBlueBallSrc } from '../../composables/useOddsStyles'

interface Props {
  /** Section title displayed in the gradient header (e.g., '第一球') */
  title: string
  /** Betting items to render (typically 4: 大/小/单/双) */
  items: SscBetItem[]
  /** Whether the current period is sealed (disables input) */
  isSealed: boolean
  /** Quick mode: 'quick' hides input boxes, 'normal' shows them */
  quickMode: 'quick' | 'normal'
  /** Bet amounts keyed by unique bet key (e.g., 'ball1_大') */
  amounts: Record<string, string>
  /** Prefix for generating unique bet keys (e.g., 'ball1') */
  keyPrefix: string
  /** Whether this is the first row (controls top border) */
  isFirst?: boolean
  /** Previous draw ball number for this position (0-9), shown as blue ball icon */
  ballNumber?: number | null
}

const props = withDefaults(defineProps<Props>(), {
  isFirst: false,
})

const emit = defineEmits<{
  /** Toggle selection state for a bet cell */
  'toggle': [betKey: string]
  /** Ensure a cell is selected when input is focused */
  'ensure': [betKey: string]
  /** Update the amounts record */
  'update:amounts': [val: Record<string, string>]
}>()

/**
 * Check if a specific bet key is currently selected.
 * Delegated from parent via prop function.
 */
const isSelected = defineModel<(key: string) => boolean>('isSelected', {
  default: () => () => false,
})

/** Generate the unique bet key for a given item label */
const getBetKey = (label: string) => `${props.keyPrefix}_${label}`

/** Handle input value change */
/** Handle el-input-number value change */
const onAmountChange = (label: string, val: number | undefined) => {
  emit('update:amounts', {
    ...props.amounts,
    [getBetKey(label)]: val !== undefined && val !== null ? String(val) : '',
  })
}
</script>

<template>
  <div class="ssc-row-section">
    <!-- Gradient header bar -->
    <div
      class="ssc-row-header"
      :class="{ 'ssc-row-header--first': isFirst }"
    >
      {{ title }}
      <img
        v-if="ballNumber != null"
        :src="getBlueBallSrc(ballNumber)"
        class="ssc-header-ball"
        :alt="String(ballNumber)"
      />
    </div>

    <!-- Betting cells row: 4 items in a horizontal line -->
    <div class="ssc-row-cells">
      <div
        v-for="(item, idx) in items"
        :key="item.label"
        class="ssc-cell"
        :class="{
          'ssc-cell--selected': isSelected(getBetKey(item.label)),
          'ssc-cell--disabled': item.disabled,
          'ssc-cell--sealed': isSealed,
          'ssc-cell--last': idx === items.length - 1,
        }"
        @click="!item.disabled && $emit('toggle', getBetKey(item.label))"
      >
        <!-- Label area (bg color) -->
        <div class="ssc-cell-label">{{ item.label }}</div>
        <!-- Odds area -->
        <div class="ssc-cell-odd">
          <b class="text-red">{{ isSealed ? '--' : item.odd }}</b>
        </div>
        <!-- Input area: only render in normal mode to avoid empty flex space in quick mode -->
        <div v-if="quickMode === 'normal'" class="ssc-cell-input">
          <el-input-number
            v-if="!item.disabled"
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
.ssc-row-section {
  width: 100%;
}

/* Gradient header bar matching the design */
.ssc-row-header {
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

/* First header has top border */
.ssc-row-header--first {
  border-top: 1px solid var(--bw-border-color, #efba84);
}

/* Blue ball icon in header */
.ssc-header-ball {
  width: 26px;
  height: 26px;
  margin-left: 6px;
  vertical-align: middle;
}

/* Row of 4 betting cells */
.ssc-row-cells {
  display: flex;
  width: 100%;
}

/* Individual betting cell: label | odd | input */
.ssc-cell {
  flex: 1;
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
.ssc-cell:first-child {
  border-left: 1px solid var(--bw-border-color, #efba84);
}

/* Last cell has no right border (outer border is from first-child left + last right) */
.ssc-cell--last {
  /* Keep right border from base .ssc-cell */
}

/* Sealed state: disabled background matching original design */
.ssc-cell--sealed {
  background: var(--el-disabled-bg-color, #f5f7fa);
}

/* Hover state (not when sealed or disabled) */
.ssc-cell:not(.ssc-cell--disabled):not(.ssc-cell--sealed):hover {
  background: var(--bw-header-color, #be9d76);
}

.ssc-cell:not(.ssc-cell--disabled):hover .ssc-cell-label,
.ssc-cell:not(.ssc-cell--disabled):hover .ssc-cell-input {
  background: transparent;
}

/* Selected state (golden highlight) */
.ssc-cell--selected,
.ssc-cell--selected:hover {
  background: #ffc214;
}

.ssc-cell--selected .ssc-cell-label,
.ssc-cell--selected:hover .ssc-cell-label,
.ssc-cell--selected .ssc-cell-odd,
.ssc-cell--selected:hover .ssc-cell-odd,
.ssc-cell--selected .ssc-cell-input,
.ssc-cell--selected:hover .ssc-cell-input {
  background: #ffc214;
}

/* Disabled cell (placeholder) */
.ssc-cell--disabled {
  cursor: not-allowed;
}

/* Label area */
.ssc-cell-label {
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
.ssc-cell-odd {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.ssc-cell-odd b {
  font-weight: 500;
}

.text-red {
  color: red;
}

/* Input area */
.ssc-cell-input {
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
