<script setup lang="ts">
import {
  colorRows,
  patternRows,
  sumGroups,
  twoSideRows,
} from '../constants/odds'
import {
  colorOddTextStyle,
  getBallSrc,
  patternOddTextStyle,
  sumOddTextStyle,
  twoSideOddTextStyle,
} from '../composables/useOddsStyles'

interface Props {
  isSealed: boolean
  quickMode: 'quick' | 'normal'
  sumAmounts: Record<number, string>
  twoSideAmounts: Record<string, string>
  colorAmounts: Record<string, string>
  patternAmounts: Record<string, string>
  isSumSelected: (num: number) => boolean
  isTwoSideSelected: (key: string) => boolean
  isColorSelected: (key: string) => boolean
  isPatternSelected: (key: string) => boolean
}

defineProps<Props>()

const emit = defineEmits<{
  'toggle-sum': [num: number]
  'ensure-sum': [num: number]
  'toggle-two-side': [key: string]
  'ensure-two-side': [key: string]
  'toggle-color': [key: string]
  'ensure-color': [key: string]
  'toggle-pattern': [key: string]
  'ensure-pattern': [key: string]
  'update:sumAmounts': [value: Record<number, string>]
  'update:twoSideAmounts': [value: Record<string, string>]
  'update:colorAmounts': [value: Record<string, string>]
  'update:patternAmounts': [value: Record<string, string>]
}>()

const updateSumAmount = (amounts: Record<number, string>, num: number, value: string) => {
  emit('update:sumAmounts', { ...amounts, [num]: value })
}

const updateTwoSideAmount = (amounts: Record<string, string>, key: string, value: string) => {
  emit('update:twoSideAmounts', { ...amounts, [key]: value })
}

const updateColorAmount = (amounts: Record<string, string>, key: string, value: string) => {
  emit('update:colorAmounts', { ...amounts, [key]: value })
}

const updatePatternAmount = (amounts: Record<string, string>, key: string, value: string) => {
  emit('update:patternAmounts', { ...amounts, [key]: value })
}
</script>

<template>
  <div>
    <h5 class="section-title">和值</h5>
    <div class="sum-grid" :class="{ 'sum-grid--quick': quickMode === 'quick' }">
      <div v-for="(group, groupIndex) in sumGroups" :key="groupIndex" class="sum-col">
        <div class="sum-head">
          <div class="sum-head-cell">和值</div>
          <div class="sum-head-cell">赔率</div>
          <div v-if="quickMode === 'normal'" class="sum-head-cell">金额</div>
        </div>
        <div
          v-for="item in group"
          :key="item.num"
          class="sum-row"
          :class="{ 'sum-row-selected': isSumSelected(item.num) }"
          @click="$emit('toggle-sum', item.num)"
        >
          <div class="sum-cell ball-cell">
            <img class="ball-img" :src="getBallSrc(item.num)" :alt="String(item.num)" />
          </div>
          <div class="sum-cell odd-cell">
            <span class="text-red sum-odd-text" :style="sumOddTextStyle(item.odd)">{{ isSealed ? '--' : item.odd }}</span>
          </div>
          <div v-if="quickMode === 'normal'" class="sum-cell input-cell">
            <input
              :value="sumAmounts[item.num]"
              class="cell-input"
              type="text"
              :disabled="isSealed"
              @input="updateSumAmount(sumAmounts, item.num, ($event.target as HTMLInputElement).value)"
              @click.stop
              @focus="$emit('ensure-sum', item.num)"
            />
          </div>
        </div>
      </div>
    </div>

    <h5 class="section-title two-side-title">两面</h5>
    <div class="two-side-grid">
      <div v-for="(row, index) in twoSideRows" :key="index" class="two-side-row">
        <div
          v-for="item in row"
          :key="item.label"
          class="two-side-item"
          :class="{ 'bet-item-selected': isTwoSideSelected(item.label) }"
          @click="$emit('toggle-two-side', item.label)"
        >
          <span class="label">{{ item.label }}</span>
          <span class="odd text-red">
            <span class="two-side-odd-text" :style="twoSideOddTextStyle(item.odd)">{{ isSealed ? '--' : item.odd }}</span>
          </span>
          <div class="input-box">
            <input
              v-if="quickMode === 'normal'"
              :value="twoSideAmounts[item.label]"
              class="cell-input"
              type="text"
              :disabled="isSealed"
              @input="updateTwoSideAmount(twoSideAmounts, item.label, ($event.target as HTMLInputElement).value)"
              @click.stop
              @focus="$emit('ensure-two-side', item.label)"
            />
          </div>
        </div>
      </div>
    </div>

    <h5 class="section-title color-title">色波</h5>
    <div class="color-grid">
      <div
        v-for="item in colorRows"
        :key="item.label"
        class="color-item"
        :class="{ 'bet-item-selected': isColorSelected(item.label) }"
        @click="$emit('toggle-color', item.label)"
      >
        <span class="label" :class="`label-${item.label}`">{{ item.label }}</span>
        <span class="odd text-red">
          <span class="color-odd-text" :style="colorOddTextStyle(item.odd)">{{ isSealed ? '--' : item.odd }}</span>
        </span>
        <div class="input-box">
          <input
            v-if="quickMode === 'normal'"
            :value="colorAmounts[item.label]"
            class="cell-input"
            type="text"
            :disabled="isSealed"
            @input="updateColorAmount(colorAmounts, item.label, ($event.target as HTMLInputElement).value)"
            @click.stop
            @focus="$emit('ensure-color', item.label)"
          />
        </div>
      </div>
    </div>

    <h5 class="section-title pattern-title">豹子/顺子/对子</h5>
    <div class="pattern-grid">
      <div
        v-for="item in patternRows"
        :key="item.label"
        class="pattern-item"
        :class="{ 'bet-item-selected': isPatternSelected(item.label) }"
        @click="$emit('toggle-pattern', item.label)"
      >
        <span class="label">{{ item.label }}</span>
        <span class="odd text-red">
          <span class="pattern-odd-text" :style="patternOddTextStyle(item.odd)">{{ isSealed ? '--' : item.odd }}</span>
        </span>
        <div class="input-box">
          <input
            v-if="quickMode === 'normal'"
            :value="patternAmounts[item.label]"
            class="cell-input"
            type="text"
            :disabled="isSealed"
            @input="updatePatternAmount(patternAmounts, item.label, ($event.target as HTMLInputElement).value)"
            @click.stop
            @focus="$emit('ensure-pattern', item.label)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped src="./Pc28TwoSidePanel.css"></style>
