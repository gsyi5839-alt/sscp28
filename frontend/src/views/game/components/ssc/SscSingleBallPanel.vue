<script setup lang="ts">
/**
 * SscSingleBallPanel - Individual ball betting panel for SSC games.
 * Used for 第一球~第五球 tabs, showing:
 *   - Ball numbers 0-9 (5-column grid with blue ball icons)
 *   - 大/小/单/双 + disabled placeholder (5 columns)
 *   - 总和 section (总大/总小/总单/总双 + 龙/虎/和)
 *   - 前三/中三/后三 pattern sections (豹子/顺子/对子/半顺/杂六)
 */
import { reactive, ref } from 'vue'
import SscTwoSideSum from './SscTwoSideSum.vue'
import { getBlueBallSrc } from '../../composables/useOddsStyles'
import {
  SSC_SINGLE_BALL_NUMBERS,
  SSC_SINGLE_BALL_SIDES,
  SSC_SINGLE_SUM_ROW1,
  SSC_SINGLE_SUM_ROW2,
  SSC_PATTERN_ITEMS,
  SSC_PATTERN_TITLES,
  type SscBetItem,
} from '../../constants/sscOdds'

interface Props {
  /** Panel title (e.g., '第三球') */
  title: string
  /** Whether the current period is sealed */
  isSealed: boolean
  /** Quick mode: 'quick' hides input boxes */
  quickMode: 'quick' | 'normal'
  /** Key prefix for bet keys (e.g., 'sb3' for third ball) */
  keyPrefix: string
}

const props = defineProps<Props>()

// ─── Selection State ─────────────────────────────────────────────────
const selectedKeys = reactive(new Set<string>())

const isKeySelected = (key: string): boolean => selectedKeys.has(key)

const toggleSelect = (key: string) => {
  if (selectedKeys.has(key)) {
    selectedKeys.delete(key)
  } else {
    selectedKeys.add(key)
  }
}

const ensureSelected = (key: string) => {
  selectedKeys.add(key)
}

// ─── Bet Amounts ─────────────────────────────────────────────────────
const amounts = ref<Record<string, string>>({})

const updateAmounts = (newAmounts: Record<string, string>) => {
  amounts.value = newAmounts
}

// Selection checker ref for SscTwoSideSum
const isSelectedRef = ref(isKeySelected)

// ─── Helpers ─────────────────────────────────────────────────────────
/** Generate unique bet key */
const getBetKey = (section: string, label: string) =>
  `${props.keyPrefix}_${section}_${label}`

/** Handle input value change */
const onAmountChange = (section: string, label: string, val: number | undefined) => {
  const key = getBetKey(section, label)
  amounts.value = {
    ...amounts.value,
    [key]: val !== undefined && val !== null ? String(val) : '',
  }
}

/** Build bet key for sum section (compatible with SscTwoSideSum) */
const sumKeyPrefix = `${props.keyPrefix}_sum`
</script>

<template>
  <div class="sb-panel">
    <!-- ═══ Ball Numbers Section ═══ -->
    <div class="sb-section">
      <div class="sb-header sb-header--first">{{ title }}</div>
      <div class="sb-grid sb-grid--5col">
        <!-- Ball numbers 0-9 -->
        <div
          v-for="item in SSC_SINGLE_BALL_NUMBERS"
          :key="item.label"
          class="sb-cell"
          :class="{
            'sb-cell--selected': isKeySelected(getBetKey('num', item.label)),
            'sb-cell--sealed': isSealed,
          }"
          @click="toggleSelect(getBetKey('num', item.label))"
        >
          <div class="sb-cell-icon">
            <img :src="getBlueBallSrc(Number(item.label))" class="sb-ball-img" :alt="item.label" />
          </div>
          <div class="sb-cell-odd">
            <b class="text-red">{{ isSealed ? '--' : item.odd }}</b>
          </div>
          <div v-if="quickMode === 'normal'" class="sb-cell-input">
            <el-input-number
              :model-value="amounts[getBetKey('num', item.label)] ? Number(amounts[getBetKey('num', item.label)]) : undefined"
              class="sb-number-input"
              size="small"
              :controls="false"
              :disabled="isSealed"
              @click.stop
              @update:model-value="(val: number | undefined) => onAmountChange('num', item.label, val)"
              @focus="ensureSelected(getBetKey('num', item.label))"
            />
          </div>
        </div>

        <!-- 大/小/单/双 + placeholder -->
        <div
          v-for="item in SSC_SINGLE_BALL_SIDES"
          :key="'side_' + item.label"
          class="sb-cell"
          :class="{
            'sb-cell--selected': !item.disabled && isKeySelected(getBetKey('side', item.label)),
            'sb-cell--disabled': item.disabled,
            'sb-cell--sealed': isSealed,
          }"
          @click="!item.disabled && toggleSelect(getBetKey('side', item.label))"
        >
          <div class="sb-cell-label">
            <span>{{ item.label }}</span>
          </div>
          <div class="sb-cell-odd">
            <b class="text-red">{{ isSealed ? '--' : item.odd }}</b>
          </div>
          <div v-if="quickMode === 'normal' && !item.disabled" class="sb-cell-input">
            <el-input-number
              :model-value="amounts[getBetKey('side', item.label)] ? Number(amounts[getBetKey('side', item.label)]) : undefined"
              class="sb-number-input"
              size="small"
              :controls="false"
              :disabled="isSealed"
              @click.stop
              @update:model-value="(val: number | undefined) => onAmountChange('side', item.label, val)"
              @focus="ensureSelected(getBetKey('side', item.label))"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- ═══ Sum Section (reuse SscTwoSideSum) ═══ -->
    <SscTwoSideSum
      :row1="SSC_SINGLE_SUM_ROW1"
      :row2="SSC_SINGLE_SUM_ROW2"
      :is-sealed="isSealed"
      :quick-mode="quickMode"
      :amounts="amounts"
      :key-prefix="sumKeyPrefix"
      v-model:isSelected="isSelectedRef"
      @toggle="toggleSelect"
      @ensure="ensureSelected"
      @update:amounts="updateAmounts"
    />

    <!-- ═══ Pattern Sections: 前三 / 中三 / 后三 ═══ -->
    <div v-for="(pTitle, pIdx) in SSC_PATTERN_TITLES" :key="pTitle" class="sb-section">
      <div class="sb-header">{{ pTitle }}</div>
      <div class="sb-grid sb-grid--5col">
        <div
          v-for="item in SSC_PATTERN_ITEMS"
          :key="pTitle + '_' + item.label"
          class="sb-cell"
          :class="{
            'sb-cell--selected': isKeySelected(getBetKey(pTitle, item.label)),
            'sb-cell--sealed': isSealed,
          }"
          @click="toggleSelect(getBetKey(pTitle, item.label))"
        >
          <div class="sb-cell-plabel">{{ item.label }}</div>
          <div class="sb-cell-odd">
            <b class="text-red">{{ isSealed ? '--' : item.odd }}</b>
          </div>
          <div v-if="quickMode === 'normal'" class="sb-cell-input">
            <el-input-number
              :model-value="amounts[getBetKey(pTitle, item.label)] ? Number(amounts[getBetKey(pTitle, item.label)]) : undefined"
              class="sb-number-input"
              size="small"
              :controls="false"
              :disabled="isSealed"
              @click.stop
              @update:model-value="(val: number | undefined) => onAmountChange(pTitle, item.label, val)"
              @focus="ensureSelected(getBetKey(pTitle, item.label))"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sb-panel {
  width: 720px;
}

/* ─── Section Header (gradient bar) ─── */
.sb-header {
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

.sb-header--first {
  border-top: 1px solid var(--bw-border-color, #efba84);
}

/* ─── 5-column Grid ─── */
.sb-grid {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
}

.sb-grid--5col > .sb-cell {
  width: 20%;
  box-sizing: border-box;
}

/* ─── Individual Cell ─── */
.sb-cell {
  display: flex;
  align-items: center;
  height: 30px;
  line-height: 30px;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  border-right: none;
  box-sizing: border-box;
  cursor: pointer;
}

/* Right border for every 5th column (last in row) */
.sb-grid--5col > .sb-cell:nth-child(5n) {
  border-right: 1px solid var(--bw-border-color, #efba84);
}

/* Sealed */
.sb-cell--sealed {
  background: var(--el-disabled-bg-color, #f5f7fa);
}

/* Hover */
.sb-cell:not(.sb-cell--disabled):not(.sb-cell--sealed):hover {
  background: var(--bw-header-color, #be9d76);
}

.sb-cell:not(.sb-cell--disabled):hover .sb-cell-icon,
.sb-cell:not(.sb-cell--disabled):hover .sb-cell-label,
.sb-cell:not(.sb-cell--disabled):hover .sb-cell-plabel,
.sb-cell:not(.sb-cell--disabled):hover .sb-cell-input {
  background: transparent;
}

/* Selected */
.sb-cell--selected {
  background: #ffc214;
}

.sb-cell--selected .sb-cell-icon,
.sb-cell--selected .sb-cell-label,
.sb-cell--selected .sb-cell-plabel,
.sb-cell--selected .sb-cell-odd,
.sb-cell--selected .sb-cell-input {
  background: #ffc214;
}

/* Disabled */
.sb-cell--disabled {
  cursor: not-allowed;
}

/* ─── Ball Icon Area ─── */
.sb-cell-icon {
  width: 30px;
  flex: 0 0 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.sb-ball-img {
  width: 22px;
  height: 22px;
}

/* ─── Text Label Area (大/小/单/双) ─── */
.sb-cell-label {
  min-width: 30px;
  flex: 0 0 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  color: var(--bw-default-color, #351c0c);
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  font-size: 13px;
}

/* ─── Pattern Label Area (豹子/顺子 etc.) ─── */
.sb-cell-plabel {
  width: 34px;
  flex: 0 0 34px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  color: var(--bw-default-color, #351c0c);
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  font-size: 13px;
}

/* ─── Odds Area ─── */
.sb-cell-odd {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.sb-cell-odd b {
  font-weight: 500;
}

.text-red {
  color: red;
}

/* ─── Input Area ─── */
.sb-cell-input {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  box-sizing: border-box;
}

/* ─── el-input-number Styles ─── */
.sb-number-input {
  width: 50px !important;
  --el-input-border-color: #abb2c5;
  --el-input-border-radius: 4px;
}

.sb-number-input :deep(.el-input__wrapper) {
  height: 22px;
  min-height: 22px;
  padding: 0 4px;
  border-radius: 4px;
  box-shadow: 0 0 0 1px #abb2c5 inset;
}

.sb-number-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary, #5c2e0d) inset;
}

.sb-number-input :deep(.el-input__inner) {
  text-align: center;
  font-size: 12px;
  height: 22px;
  line-height: 22px;
}
</style>
