<script setup lang="ts">
import { getBallSrc, getBlueBallSrc } from '../composables/useOddsStyles'
import type { GameCategory } from '@/utils/gameSubNav'
// Loading animation for draw-in-progress state (original design: 55x11 gif)
import loadingGif from '@/assets/游戏/loading.gif'

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
  /** Game category determines ball display style */
  gameCategory: GameCategory
}

const props = defineProps<Props>()
const emit = defineEmits<{
  refresh: []
}>()

const onGameNameClick = () => {
  emit('refresh')
}
</script>

<template>
  <div class="issue-bar">
    <div class="issue-row issue-row-top">
      <div class="issue-left">
        <span class="text-blue mr10 game-name-clickable" @click="onGameNameClick" title="点击刷新">{{ gameName }}</span>
        <span class="text-red mr10">今日输赢：<b class="win-amount">0</b></span>
      </div>
      <div class="issue-right">
        <b class="text-green mr10">{{ preDrawIssue }}</b>
        <span>期开奖：</span>
        <!-- PC28: 3 balls + sum (ball1 + ball2 + ball3 = sum) -->
        <template v-if="gameCategory === 'pc28' && preDrawBalls.length === 3">
          <img class="ball-img" :src="getBallSrc(preDrawBalls[0]!)" :alt="String(preDrawBalls[0])" />
          <span class="symbol">+</span>
          <img class="ball-img" :src="getBallSrc(preDrawBalls[1]!)" :alt="String(preDrawBalls[1])" />
          <span class="symbol">+</span>
          <img class="ball-img" :src="getBallSrc(preDrawBalls[2]!)" :alt="String(preDrawBalls[2])" />
          <span class="symbol">=</span>
          <img class="ball-img" :src="getBallSrc(preDrawSum)" :alt="String(preDrawSum)" />
        </template>
        <!-- SSC: 5 blue balls side by side (no sum) -->
        <template v-else-if="gameCategory === 'ssc'">
          <img
            v-for="(ball, idx) in preDrawBalls"
            :key="idx"
            class="ball-img-blue"
            :src="getBlueBallSrc(ball)"
            :alt="String(ball)"
          />
        </template>
      </div>
    </div>
    
    <div class="issue-row">
      <div class="issue-left issue-left--bottom">
        <b class="text-green">{{ drawIssue }}</b>
        <span class="ml10">期</span>
        <span class="bet-tab ml10 bet-tab-active bet-tab--display">{{ activeBetTabLabel }}</span>
      </div>
      <!-- Countdown / drawing status: flows naturally after left content -->
      <template v-if="isDrawing">
        <div class="drawing-row">
          <b class="text-red">{{ preDrawIssue }}</b>
          <span>期开奖：</span>
          <img class="loading-gif" :src="loadingGif" alt="loading" />
        </div>
      </template>
      <template v-else>
        <div class="countdown-group">
          <span class="ml40">距离封盘:</span>
          <b class="time-val time-red ml5">{{ sealCountdown }}</b>
          <span class="ml50">距离开奖:</span>
          <b class="time-val time-green ml5">{{ drawCountdown }}</b>
        </div>
      </template>
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

/* Bottom row left section offset */
.issue-left--bottom {
  position: relative;
  top: -7px;
}

/* Countdown container: padding-right 100px matching original design (pr100) */
.countdown-group {
  display: inline-flex;
  align-items: center;
  padding-right: 100px;
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

/* PC28 ball image: matches original design compact layout */
.ball-img {
  width: 27px;
  height: 27px;
  margin-left: 2px;
  display: inline-block;
  position: relative;
  top: 2px;
}

/* SSC blue balls: 26x26, no gap symbol */
.ball-img-blue {
  width: 26px;
  height: 26px;
  margin-left: 2px;
  display: inline-block;
}

/* + and = symbols: tight spacing matching original design */
.symbol {
  margin: 0 1px;
  font-size: 12px;
  color: #333;
}

/* Countdown time value: matches original design (single <b> block, font-size 13px) */
.time-val {
  font-size: 13px;
  border-radius: 3px;
}

.time-red {
  color: red;
}

.time-green {
  color: green;
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

/* Drawing in-progress row: issue number + loading animation */
.drawing-row {
  display: inline-flex;
  align-items: center;
  height: 25px;
  margin-left: 40px;
}

.loading-gif {
  width: 55px;
  height: 11px;
  margin-left: 4px;
}

.game-name-clickable {
  cursor: pointer;
  user-select: none;
}

/* Win amount: bold 13px badge matching original design (bg-[#fff] b-rd-3 font-size-13) */
.win-amount {
  font-size: 13px;
  background: #fff;
  border-radius: 3px;
  padding: 0 4px;
}
</style>
