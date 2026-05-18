<script setup lang="ts">
import { computed } from 'vue'
import { getBlueBallSrc } from '../../composables/useOddsStyles'

interface Props {
  title: string
  positionKey: string
  isSealed: boolean
  quickMode: 'quick' | 'normal'
  ballAmounts: Record<string, string>
  isBallSelected: (key: string) => boolean
}

interface PanelCell {
  key: string
  label: string
  odd: string
  labelType: 'ball' | 'text' | 'empty'
  labelWidth: number
  ballNumber?: number
  disabled?: boolean
}

interface PanelSection {
  title: string
  rows: PanelCell[][]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'toggleBall': [key: string]
  'ensureBall': [key: string]
  'update:ballAmounts': [val: Record<string, string>]
}>()

const BALL_ROWS = [
  [0, 1, 2, 3, 4],
  [5, 6, 7, 8, 9],
]

const NUMBER_ODDS: Record<number, string> = {
  0: '9.9',
  1: '9.9',
  2: '9.9',
  3: '9.9',
  4: '9.9',
  5: '9.9',
  6: '9.88',
  7: '9.9',
  8: '9.9',
  9: '9.9',
}

const SIDE_ITEMS = [
  { label: '大', odd: '1.9776' },
  { label: '小', odd: '1.9776' },
  { label: '单', odd: '1.9776' },
  { label: '双', odd: '1.9776' },
]

const SUM_ROW1 = [
  { label: '总大', odd: '1.9776' },
  { label: '总小', odd: '1.9776' },
  { label: '总单', odd: '1.9776' },
  { label: '总双', odd: '1.9776' },
]

const SUM_ROW2 = [
  { label: '龙', odd: '1.9776' },
  { label: '虎', odd: '1.9776' },
  { label: '和', odd: '9' },
]

const COMBO_ITEMS = [
  { label: '豹子', odd: '65' },
  { label: '顺子', odd: '12' },
  { label: '对子', odd: '2.6' },
  { label: '半顺', odd: '2.05' },
  { label: '杂六', odd: '2.4' },
]

const COMBO_GROUPS = [
  { key: 'front3', title: '前三' },
  { key: 'mid3', title: '中三' },
  { key: 'back3', title: '后三' },
]

const buildKey = (group: string, label: string) => `${props.positionKey}_${group}_${label}`

const sideItems = computed(() => SIDE_ITEMS)

const sumRow1Items = computed(() => SUM_ROW1)

const sumRow2Items = computed(() => SUM_ROW2)

const comboItems = computed(() => COMBO_ITEMS)

const getBallNumberOdd = (num: number): string => {
  return NUMBER_ODDS[num] ?? '9.9'
}

const buildBallCell = (num: number): PanelCell => ({
  key: buildKey('num', String(num)),
  label: String(num),
  odd: getBallNumberOdd(num),
  labelType: 'ball',
  labelWidth: 30,
  ballNumber: num,
})

const buildTextCell = (group: string, label: string, odd: string, labelWidth: number): PanelCell => ({
  key: buildKey(group, label),
  label,
  odd,
  labelType: 'text',
  labelWidth,
})

const buildEmptyCell = (group: string, labelWidth: number): PanelCell => ({
  key: buildKey(group, 'empty'),
  label: '',
  odd: '--',
  labelType: 'empty',
  labelWidth,
  disabled: true,
})

const topSectionRows = computed<PanelCell[][]>(() => ([
  BALL_ROWS[0].map(num => buildBallCell(num)),
  BALL_ROWS[1].map(num => buildBallCell(num)),
  [
    ...sideItems.value.map(item => buildTextCell('side', item.label, item.odd, 30)),
    buildEmptyCell('side', 30),
  ],
]))

const sumSectionRows = computed<PanelCell[][]>(() => ([
  sumRow1Items.value.map(item => buildTextCell('sum', item.label, item.odd, 60)),
  [
    ...sumRow2Items.value.map(item => buildTextCell('sum', item.label, item.odd, 60)),
    buildEmptyCell('sum', 60),
  ],
]))

const comboSections = computed<PanelSection[]>(() => COMBO_GROUPS.map(group => ({
  title: group.title,
  rows: [comboItems.value.map(item => buildTextCell(group.key, item.label, item.odd, 34))],
})))

const sections = computed<PanelSection[]>(() => ([
  { title: props.title, rows: topSectionRows.value },
  { title: '总和', rows: sumSectionRows.value },
  ...comboSections.value,
]))

const isCellSelected = (cell: PanelCell) => {
  if (cell.disabled) return false
  return props.isBallSelected(cell.key)
}

const onCellClick = (cell: PanelCell) => {
  if (cell.disabled) return
  emit('toggleBall', cell.key)
}

const onInputFocus = (cell: PanelCell) => {
  if (cell.disabled) return
  emit('ensureBall', cell.key)
}

const onAmountInput = (key: string, event: Event) => {
  const value = (event.target as HTMLInputElement).value
  emit('update:ballAmounts', {
    ...props.ballAmounts,
    [key]: value,
  })
}
</script>

<template>
  <div class="ssc-pos-panel">
    <section
      v-for="(section, sectionIdx) in sections"
      :key="section.title"
      class="ssc-pos-section"
    >
      <div
        class="ssc-pos-header"
        :class="{ 'ssc-pos-header--first': sectionIdx === 0 }"
      >
        {{ section.title }}
      </div>

      <div
        v-for="(row, rowIdx) in section.rows"
        :key="`${section.title}-${rowIdx}`"
        class="ssc-pos-row"
      >
        <div
          v-for="(cell, cellIdx) in row"
          :key="cell.key"
          class="ssc-pos-cell"
          :class="{
            'ssc-pos-cell--selected': isCellSelected(cell),
            'ssc-pos-cell--disabled': cell.disabled,
            'ssc-pos-cell--sealed': isSealed,
            'ssc-pos-cell--first': cellIdx === 0,
          }"
          @click="onCellClick(cell)"
        >
          <div
            class="ssc-pos-label"
            :style="{ width: `${cell.labelWidth}px`, flex: `0 0 ${cell.labelWidth}px` }"
          >
            <img
              v-if="cell.labelType === 'ball' && cell.ballNumber !== undefined"
              :src="getBlueBallSrc(cell.ballNumber)"
              class="ssc-pos-ball"
              :alt="cell.label"
            />
            <span v-else>{{ cell.label }}</span>
          </div>
          <div class="ssc-pos-odd">
            <b class="text-red">{{ isSealed && !cell.disabled ? '--' : cell.odd }}</b>
          </div>
          <div v-if="quickMode === 'normal' && !cell.disabled" class="ssc-pos-input-wrap">
            <input
              :value="ballAmounts[cell.key] ?? ''"
              class="ssc-pos-input"
              type="text"
              :disabled="isSealed"
              @input="onAmountInput(cell.key, $event)"
              @focus="onInputFocus(cell)"
              @click.stop
            />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.ssc-pos-panel {
  width: 720px;
}

.ssc-pos-section {
  width: 100%;
}

.ssc-pos-header {
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-weight: 700;
  font-size: 13px;
  color: #000;
  background: var(--bw-table-header-bg-color, linear-gradient(to bottom, #fff 0%, #fff1e4 100%));
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  box-sizing: border-box;
}

.ssc-pos-header--first {
  border-top: 1px solid var(--bw-border-color, #efba84);
}

.ssc-pos-row {
  display: flex;
  width: 100%;
}

/* Keep row separators visually continuous across column borders. */
.ssc-pos-row + .ssc-pos-row {
  margin-top: -1px;
}

.ssc-pos-row + .ssc-pos-row .ssc-pos-cell {
  border-top: 1px solid var(--bw-border-color, #efba84);
}

.ssc-pos-cell {
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

.ssc-pos-cell--first {
  border-left: 1px solid var(--bw-border-color, #efba84);
}

.ssc-pos-cell--disabled {
  cursor: not-allowed;
}

.ssc-pos-cell--sealed {
  background: var(--el-disabled-bg-color, #f5f7fa);
}

.ssc-pos-cell:not(.ssc-pos-cell--disabled):not(.ssc-pos-cell--sealed):hover {
  background: var(--bw-header-color, #be9d76);
}

.ssc-pos-cell:not(.ssc-pos-cell--disabled):hover .ssc-pos-label,
.ssc-pos-cell:not(.ssc-pos-cell--disabled):hover .ssc-pos-odd,
.ssc-pos-cell:not(.ssc-pos-cell--disabled):hover .ssc-pos-input-wrap {
  background: transparent;
}

.ssc-pos-cell--selected {
  background: #ffc214;
}

.ssc-pos-cell--selected .ssc-pos-label,
.ssc-pos-cell--selected .ssc-pos-odd,
.ssc-pos-cell--selected .ssc-pos-input-wrap {
  background: #ffc214;
}

.ssc-pos-label {
  height: 30px;
  line-height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--bw-default-color, #351c0c);
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  font-size: 13px;
}

.ssc-pos-ball {
  width: 27px;
  height: 27px;
}

.ssc-pos-odd {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.ssc-pos-odd b {
  font-weight: 500;
}

.ssc-pos-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.ssc-pos-input {
  width: 50px;
  height: 22px;
  padding: 0 4px;
  border: 1px solid #abb2c5;
  border-radius: 4px;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: textfield;
  background: #ffffff;
  color: #000;
  font-size: 12px;
  line-height: 22px;
  text-align: center;
  box-sizing: border-box;
}

.ssc-pos-input:disabled {
  opacity: 1;
  background: #ffffff;
  color: #000;
  border-color: #abb2c5;
}

.ssc-pos-input:focus,
.ssc-pos-input:focus-visible {
  outline: none;
  box-shadow: none;
  border-color: var(--el-color-primary, #5c2e0d);
}

.text-red {
  color: red;
}
</style>
