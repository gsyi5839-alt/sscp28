<script setup lang="ts">
import type { SummaryKey, SscSummaryKey } from '../constants/odds'

type SummaryVariant = 'pc28' | 'ssc' | 'racing'

interface Props {
  summaryTabs: Array<{ key: SummaryKey | SscSummaryKey; label: string }>
  activeSummaryKey: SummaryKey | SscSummaryKey
  activeSummaryValues: any[]
  variant?: SummaryVariant
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'pc28',
})

const emit = defineEmits<{
  'tabClick': [key: SummaryKey | SscSummaryKey | string]
}>()
</script>

<template>
  <div
    class="summary-road mt10"
    :class="[`summary-road--${activeSummaryKey}`, `summary-road--${variant}`]"
  >
    <div class="summary-bar">
      <span
        v-for="tab in summaryTabs"
        :key="tab.key"
        class="summary-item"
        :class="{
          active: activeSummaryKey === tab.key,
          'summary-item-danger': activeSummaryKey === tab.key,
        }"
        @click="$emit('tabClick', tab.key)"
      >
        {{ tab.label }}
      </span>
    </div>

    <div class="summary-values-shell">
      <div class="summary-values" :class="`summary-values--${activeSummaryKey}`">
        <div
          v-for="(value, idx) in activeSummaryValues"
          :key="idx"
          class="summary-value"
        >
          <div
            class="text-center uno-b-r wfull summary-cell-inner summary-cell-inner--road"
            :class="{ 'bg-primary5': idx % 2 === 0 }"
          >
            <div v-if="Array.isArray(value)" class="multi-row">
              <span
                v-for="(item, itemIdx) in value"
                :key="itemIdx"
                class="value-text-multi"
              >
                {{ item }}
              </span>
            </div>
            <div v-else>
              <span class="pt5 block value-text">{{ value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mt10 {
  margin-top: 10px;
}

.summary-road {
  --summary-tab-height: 30px;
  --summary-road-height: 148.73px;
  --summary-body-height: 110.73px;
  position: relative;
  z-index: 1;
  width: 720px;
  height: auto;
  max-height: none;
  min-height: auto;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  contain: layout paint;
}

.summary-road--sum {
  --summary-road-height: 79.89px;
  --summary-body-height: 49.89px;
}

.summary-road--size {
  --summary-road-height: 148.73px;
  --summary-body-height: 110.73px;
}

.summary-road--parity {
  --summary-road-height: 148.73px;
  --summary-body-height: 110.73px;
}

.summary-road--baoDou {
  --summary-road-height: 100px;
  --summary-body-height: 70px;
}

.summary-bar {
  flex: 0 0 var(--summary-tab-height);
  width: 720px;
  height: var(--summary-tab-height);
  line-height: var(--summary-tab-height);
  display: flex;
  align-items: stretch;
  border: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-bg-3, #fff7ef);
  box-sizing: border-box;
  text-align: center;
  cursor: pointer;
  overflow: hidden;
}

.summary-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
  text-align: center;
  color: #000;
  border-right-width: 1px;
  border-right-style: solid;
  border-color: var(--el-border-color, var(--bw-border-color, #efba84));
  user-select: none;
}

.summary-item:last-child {
  border-right: none;
}

.summary-item.active {
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
  font-weight: 700;
}

.summary-item-danger {
  color: red;
  font-weight: 700;
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
}

.summary-values-shell {
  position: relative;
  flex: 0 0 var(--summary-body-height);
  width: 720px;
  height: var(--summary-body-height);
  min-height: var(--summary-body-height);
  max-height: var(--summary-body-height);
  box-sizing: border-box;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  overflow: hidden;
  overflow: clip;
  contain: strict;
  isolation: isolate;
}

.summary-values {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-columns: repeat(30, minmax(0, 1fr));
  grid-template-rows: minmax(0, 1fr);
  box-sizing: border-box;
  overflow: hidden;
  contain: strict;
}

.summary-values--sum {
  height: var(--summary-body-height, 49.89px);
  max-height: var(--summary-body-height, 49.89px);
}

.summary-values--size {
  height: var(--summary-body-height, 110.73px);
  max-height: var(--summary-body-height, 110.73px);
}

.summary-values--parity {
  height: var(--summary-body-height, 110.73px);
  max-height: var(--summary-body-height, 110.73px);
}

.summary-values--baoDou {
  display: flex;
  grid-template-columns: none;
}

.summary-value {
  display: block;
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
}

.summary-road--baoDou .summary-value {
  flex: 1 1 0;
}

.text-center {
  text-align: center;
}

.wfull {
  width: 100%;
}

.uno-b-r {
  border-right-width: 1px;
  border-right-style: solid;
  border-color: var(--el-border-color, var(--bw-border-color, #efba84));
}

.summary-cell-inner {
  height: 100%;
  min-height: 0;
  max-height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  overflow: clip;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.summary-cell-inner--road {
  padding-bottom: 2px;
  padding-top: 0;
}

.summary-cell-inner--road .pt5 {
  padding-top: 0;
}

.summary-road--racing .summary-cell-inner--road {
  padding-bottom: 10px;
}

.summary-road--racing .summary-cell-inner--road .pt5 {
  padding-top: 5px;
}

.summary-road--racing .multi-row {
  padding: 5px 0 10px;
  gap: 5px;
}

.summary-road--racing .value-text-multi {
  line-height: 1.2;
  padding: 0;
}

.block {
  display: block;
}

.pt5 {
  padding-top: 5px;
}

.bg-primary5 {
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
}

.value-text {
  display: block;
  line-height: 1.2;
  font-size: 13px;
  white-space: nowrap;
  word-break: normal;
  overflow-wrap: normal;
  padding-top: 0;
}

.multi-row {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  padding: 2px 0;
  flex: 1 1 auto;
  min-height: 0;
  max-height: 100%;
  overflow: hidden;
  overflow: clip;
}

.value-text-multi {
  display: block;
  line-height: 1;
  font-size: 13px;
  padding: 1px 0;
}

.summary-value:last-child .uno-b-r {
  border-right: none;
}
</style>
