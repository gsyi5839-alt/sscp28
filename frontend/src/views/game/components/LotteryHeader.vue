<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { getBallSrc } from '../composables/useOddsStyles'

interface Props {
  gameName: string
  preDrawIssue: string
  preDrawBalls: number[]
  preDrawSum: number
  drawIssue: string
  sealCountdown: string
  drawCountdown: string
  isDrawing: boolean
  activeBetTabLabel: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  refresh: []
}>()

const onGameNameClick = () => {
  emit('refresh')
}

const lastBallRef = ref<HTMLImageElement | null>(null)
const countdownRef = ref<HTMLDivElement | null>(null)
const countdownShift = ref(0)
const COUNTDOWN_EXTRA_SHIFT = -452

const updateCountdownPosition = () => {
  const ballEl = lastBallRef.value
  const countEl = countdownRef.value
  if (!ballEl || !countEl) return
  const ballRect = ballEl.getBoundingClientRect()
  const naturalLeft = countEl.getBoundingClientRect().left - countdownShift.value
  countdownShift.value = Math.round(ballRect.left - naturalLeft) + COUNTDOWN_EXTRA_SHIFT
}

watch(() => props.preDrawBalls, () => {
  nextTick(updateCountdownPosition)
}, { immediate: true })

defineExpose({ updateCountdownPosition })
</script>

<template>
  <div class="issue-bar">
    <div class="issue-row issue-row-top">
      <div class="issue-left">
        <span class="text-blue mr10 game-name-clickable" @click="onGameNameClick" title="点击刷新">{{ gameName }}</span>
        <span class="text-red">今日输赢：0</span>
      </div>
      <div class="issue-right">
        <b class="text-green mr10">{{ preDrawIssue }}</b>
        <span>期开奖：</span>
        <template v-if="preDrawBalls.length === 3">
          <img class="ball-img" :src="getBallSrc(preDrawBalls[0]!)" :alt="String(preDrawBalls[0])" />
          <span class="symbol">+</span>
          <img class="ball-img" :src="getBallSrc(preDrawBalls[1]!)" :alt="String(preDrawBalls[1])" />
          <span class="symbol">+</span>
          <img class="ball-img" :src="getBallSrc(preDrawBalls[2]!)" :alt="String(preDrawBalls[2])" />
          <span class="symbol">=</span>
          <img ref="lastBallRef" class="ball-img" :src="getBallSrc(preDrawSum)" :alt="String(preDrawSum)" />
        </template>
      </div>
    </div>
    
    <div class="issue-row">
      <div class="issue-left issue-left--bottom">
        <b class="text-green">{{ drawIssue }}</b>
        <span class="ml10">期</span>
        <span class="bet-tab ml10 bet-tab-active bet-tab--display">{{ activeBetTabLabel }}</span>
      </div>
      <div class="issue-right issue-right--bottom">
        <template v-if="isDrawing">
          <span class="text-red ml40" style="font-weight:bold;">正在开奖...</span>
        </template>
        <template v-else>
          <div
            ref="countdownRef"
            class="countdown-group"
            :style="{ transform: `translateX(${countdownShift}px)` }"
          >
            <span class="ml40">距离封盘:</span>
            <b class="time-box time-red ml5">{{ sealCountdown.split(':')[0] }}</b>
            <span class="time-sep">:</span>
            <b class="time-box time-red">{{ sealCountdown.split(':')[1] }}</b>
            <span class="time-sep">:</span>
            <b class="time-box time-red">{{ sealCountdown.split(':')[2] }}</b>
            <span class="ml50">距离开奖:</span>
            <b class="time-box time-green ml5">{{ drawCountdown.split(':')[0] }}</b>
            <span class="time-sep">:</span>
            <b class="time-box time-green">{{ drawCountdown.split(':')[1] }}</b>
            <span class="time-sep">:</span>
            <b class="time-box time-green">{{ drawCountdown.split(':')[2] }}</b>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.issue-bar {
  width: 720px;
  height: 56px;
  padding: 5px 20px;
  border: 1px solid var(--bw-border-color, #efba84);
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  font-size: 12px;
  box-sizing: border-box;
  position: relative;
}

.issue-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 25px;
}

.issue-row-top {
  margin-bottom: 5px;
}

.issue-left,
.issue-right {
  display: flex;
  align-items: center;
}

.issue-left--bottom,
.issue-right--bottom {
  position: relative;
  top: -7px;
}

.countdown-group {
  display: inline-flex;
  align-items: center;
  pointer-events: none;
}

.text-blue {
  color: blue;
}

.text-red {
  color: red;
}

.text-green {
  color: green;
}

.ml10 {
  margin-left: 10px;
}

.ml40 {
  margin-left: 40px;
}

.ml50 {
  margin-left: 50px;
}

.mr10 {
  margin-right: 10px;
}

.ml5 {
  margin-left: 5px;
}

.ball-img {
  width: 27px;
  height: 27px;
  margin-left: 6px;
  display: inline-block;
}

.symbol {
  margin: 0 6px;
  font-size: 12px;
  color: #333;
}

.time-box {
  display: inline-block;
  width: 16px;
  height: 17px;
  line-height: 17px;
  text-align: center;
  border-radius: 3px;
  font-size: 13px;
}

.time-red {
  color: red;
}

.time-green {
  color: green;
}

.time-sep {
  margin: 0 4px;
  color: #333;
}

.bet-tab {
  cursor: pointer;
  color: blue;
  padding: 1px 4px;
  font-size: 12px;
}

.bet-tab-active {
  background: #ffffbf;
  border: 1px solid var(--bw-border-color, #efba84);
  color: #c00;
}

.bet-tab--display {
  cursor: default;
  user-select: none;
}

.bet-tab--display.bet-tab-active {
  color: #00f !important;
  background: transparent !important;
  border: none !important;
}

.game-name-clickable {
  cursor: pointer;
  user-select: none;
}
</style>
