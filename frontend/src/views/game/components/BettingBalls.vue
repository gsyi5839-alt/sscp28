<script setup lang="ts">
import { getBallSrc } from '../composables/useOddsStyles'

interface Props {
  isSealed: boolean
  quickMode: 'quick' | 'normal'
  ballAmounts: Record<string, string>
  isBallSelected: (key: string) => boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'toggleBall': [key: string]
  'ensureBall': [key: string]
  'update:ballAmounts': [val: Record<string, string>]
}>()

const colNames = ['第一球', '第二球', '第三球']
const ballLabels = ['大', '小', '单', '双']
</script>

<template>
  <div class="balls-panel">
    <div class="balls-grid">
      <div
        v-for="(colName, colIdx) in colNames"
        :key="colIdx"
        class="balls-col"
      >
        <!-- Column header -->
        <div class="balls-col-header">{{ colName }}</div>

        <!-- Ball rows 0-9, odds 9.9 -->
        <div
          v-for="n in 10"
          :key="n - 1"
          class="balls-row"
          :class="{ 'balls-row-selected': isBallSelected(`${colIdx}_${n - 1}`) }"
          @click="$emit('toggleBall', `${colIdx}_${n - 1}`)"
        >
          <div class="balls-ball-cell">
            <img :src="getBallSrc(n - 1)" class="ball-img balls-ball-img" :alt="String(n - 1)" />
          </div>
          <div class="balls-odd-cell"><b class="text-red">{{ isSealed ? '--' : '9.7' }}</b></div>
          <!-- Input area: only render in normal mode to avoid empty flex space in quick mode -->
          <div v-if="quickMode === 'normal'" class="balls-input-cell">
            <input
              :value="ballAmounts[`${colIdx}_${n - 1}`]"
              @input="$emit('update:ballAmounts', { ...ballAmounts, [`${colIdx}_${n - 1}`]: ($event.target as HTMLInputElement).value })"
              class="balls-cell-input"
              type="text"
              :disabled="isSealed"
              @focus="$emit('ensureBall', `${colIdx}_${n - 1}`)"
            />
          </div>
        </div>

        <!-- Two-side rows -->
        <div
          v-for="label in ballLabels"
          :key="label"
          class="balls-row"
          :class="{ 'balls-row-selected': isBallSelected(`${colIdx}_${label}`) }"
          @click="$emit('toggleBall', `${colIdx}_${label}`)"
        >
          <div class="balls-label-cell">{{ label }}</div>
          <div class="balls-odd-cell"><b class="text-red">{{ isSealed ? '--' : '1.7776' }}</b></div>
          <!-- Input area: only render in normal mode -->
          <div v-if="quickMode === 'normal'" class="balls-input-cell">
            <input
              :value="ballAmounts[`${colIdx}_${label}`]"
              @input="$emit('update:ballAmounts', { ...ballAmounts, [`${colIdx}_${label}`]: ($event.target as HTMLInputElement).value })"
              class="balls-cell-input"
              type="text"
              :disabled="isSealed"
              @focus="$emit('ensureBall', `${colIdx}_${label}`)"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.balls-panel {
  width: 720px;
}

.balls-grid {
  width: 720px;
  display: flex;
  border: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.balls-col {
  flex: 0 0 33.333%;
  width: 33.333%;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.balls-col:last-child {
  border-right: none;
}

.balls-col-header {
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-weight: bold;
  font-size: 13px;
  color: #000;
  background: var(--bw-table-header-bg-color, linear-gradient(to bottom, #fff 0%, #fff1e4 100%));
}

.balls-row {
  display: flex;
  height: 30px;
  line-height: 30px;
  border-top: 1px solid var(--bw-border-color, #efba84);
  cursor: pointer;
  box-sizing: border-box;
}

.balls-row:hover {
  background: var(--bw-header-color, #be9d76);
}

.balls-row-selected .balls-ball-cell,
.balls-row-selected .balls-odd-cell,
.balls-row-selected .balls-input-cell,
.balls-row-selected .balls-label-cell,
.balls-row-selected:hover .balls-ball-cell,
.balls-row-selected:hover .balls-odd-cell,
.balls-row-selected:hover .balls-input-cell,
.balls-row-selected:hover .balls-label-cell {
  background: #ffc214;
}

.balls-ball-cell {
  width: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.ball-img {
  width: 27px;
  height: 27px;
  margin-left: 6px;
  display: inline-block;
}

.balls-ball-img {
  margin-left: 0 !important;
}

.balls-odd-cell {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.balls-odd-cell b {
  font-weight: 500;
}

.text-red {
  color: red;
}

.balls-input-cell {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.balls-cell-input {
  width: 50px;
  height: 20px;
  border: 1px solid #a0b4d8;
  border-radius: 6px;
  background: #ffffff;
  text-align: center;
  font-size: 12px;
  box-sizing: border-box;
}

.balls-label-cell {
  width: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  font-weight: bold;
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  box-sizing: border-box;
}
</style>
