<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import {
  SSC_NIUNIU_ROW1,
  SSC_NIUNIU_SOHA,
  SSC_NIUNIU_TWO_SIDE,
} from '../../constants/sscOdds'

interface Props {
  isSealed: boolean
  quickMode: 'quick' | 'normal'
}

defineProps<Props>()

interface NiuNiuSection {
  title: string
  items: Array<{
    label: string
    odd: string
    disabled?: boolean
    stopped?: boolean
  }>
}

const sections = computed<NiuNiuSection[]>(() => ([
  {
    title: '斗牛',
    items: SSC_NIUNIU_ROW1,
  },
  {
    title: '斗牛两面',
    items: SSC_NIUNIU_TWO_SIDE,
  },
  {
    title: '斗牛梭哈',
    items: SSC_NIUNIU_SOHA,
  },
]))

const selectedKeys = reactive(new Set<string>())
const getCellKey = (sectionTitle: string, label: string) => `${sectionTitle}_${label}`
const isCellSelected = (sectionTitle: string, label: string) => selectedKeys.has(getCellKey(sectionTitle, label))
const toggleCell = (sectionTitle: string, item: { label: string; disabled?: boolean; stopped?: boolean }) => {
  if (item.disabled || item.stopped) return
  const key = getCellKey(sectionTitle, item.label)
  if (selectedKeys.has(key)) {
    selectedKeys.delete(key)
  } else {
    selectedKeys.add(key)
  }
}

const amounts = ref<Record<string, string>>({})

const onAmountChange = (label: string, val: number | undefined) => {
  amounts.value = {
    ...amounts.value,
    [label]: val !== undefined && val !== null ? String(val) : '',
  }
}
</script>

<template>
  <div class="ssc-niuniu-panel">
    <section
      v-for="(section, sectionIndex) in sections"
      :key="section.title"
      class="niuniu-section"
    >
      <div
        class="niuniu-section-title"
        :class="{ 'niuniu-section-title--first': sectionIndex === 0 }"
      >
        {{ section.title }}
      </div>

      <div class="niuniu-grid">
        <div
          v-for="(item, itemIndex) in section.items"
          :key="`${section.title}-${itemIndex}-${item.label || 'blank'}`"
          class="niuniu-cell"
          :class="{
            'niuniu-cell--selected': isCellSelected(section.title, item.label),
            'niuniu-cell--blank': item.disabled,
            'niuniu-cell--sealed': isSealed || item.disabled || item.stopped,
            'niuniu-cell--clickable': !item.disabled && !item.stopped,
            'niuniu-cell--quick': quickMode === 'quick',
          }"
          @click="toggleCell(section.title, item)"
        >
          <div class="niuniu-label">{{ item.label }}</div>
          <div class="niuniu-odd">
            <b>{{ isSealed ? '--' : item.odd }}</b>
          </div>
          <div v-if="quickMode === 'normal'" class="niuniu-input-area">
            <span v-if="item.stopped" class="niuniu-stopped">停押</span>
            <el-input-number
              v-else-if="!item.disabled"
              :model-value="amounts[item.label] ? Number(amounts[item.label]) : undefined"
              class="niuniu-number-input"
              size="small"
              :controls="false"
              :disabled="isSealed"
              @update:model-value="(val: number | undefined) => onAmountChange(item.label, val)"
            />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.ssc-niuniu-panel {
  width: 720px;
  font-size: 13px;
  text-align: center;
}

.niuniu-section {
  width: 100%;
}

.niuniu-section-title {
  height: 35px;
  line-height: 35px;
  font-size: 14px;
  font-weight: 700;
  color: #000;
  background: #e0e0e0;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  border-bottom: none;
  box-sizing: border-box;
}

.niuniu-section-title--first {
  border-top: 1px solid var(--bw-border-color, #efba84);
}

.niuniu-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  width: 100%;
}

.niuniu-cell {
  display: flex;
  align-items: center;
  width: 100%;
  height: 30px;
  line-height: 30px;
  background: var(--el-disabled-bg-color, #f5f7fa);
  border: 1px solid var(--bw-border-color, #efba84);
  border-left: none;
  border-bottom: none;
  box-sizing: border-box;
  cursor: default;
}

.niuniu-cell:nth-child(4n + 1) {
  border-left: 1px solid var(--bw-border-color, #efba84);
}

.niuniu-section .niuniu-cell:nth-last-child(-n + 4) {
  border-bottom: 1px solid var(--bw-border-color, #efba84);
}

.niuniu-cell--clickable {
  cursor: pointer;
}

.niuniu-cell--clickable:not(.niuniu-cell--sealed):hover {
  background: var(--bw-header-color, #be9d76);
}

.niuniu-cell--clickable:not(.niuniu-cell--sealed):hover .niuniu-label,
.niuniu-cell--clickable:not(.niuniu-cell--sealed):hover .niuniu-input-area {
  background: transparent;
}

.niuniu-cell--selected {
  background: #ffc214;
}

.niuniu-cell--selected .niuniu-label,
.niuniu-cell--selected .niuniu-odd,
.niuniu-cell--selected .niuniu-input-area {
  background: #ffc214;
}

.niuniu-label {
  flex: 0 0 60px;
  width: 60px;
  height: 30px;
  line-height: 30px;
  color: var(--bw-default-color, #351c0c);
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.niuniu-odd {
  flex: 1 1 0;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  height: 30px;
  color: red;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.niuniu-odd b {
  font-weight: 700;
}

.niuniu-input-area {
  flex: 1 1 0;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  height: 30px;
}

.niuniu-number-input {
  width: 50px;
}

.niuniu-stopped {
  display: inline-block;
  width: 50px;
  color: #000;
  text-align: center;
}

.niuniu-cell--quick .niuniu-odd {
  border-right: none;
}

:deep(.niuniu-number-input.el-input-number--small) {
  width: 50px;
}

:deep(.niuniu-number-input .el-input__wrapper) {
  height: 22px;
  min-height: 22px;
  padding: 0 4px;
  box-shadow: 0 0 0 1px #abb2c5 inset;
  background: #fff;
}

:deep(.niuniu-number-input.is-disabled .el-input__wrapper) {
  opacity: 1;
  background: #fff;
}

:deep(.niuniu-number-input .el-input__inner) {
  height: 22px;
  line-height: 22px;
  color: #000;
  font-size: 12px;
  text-align: center;
}
</style>
