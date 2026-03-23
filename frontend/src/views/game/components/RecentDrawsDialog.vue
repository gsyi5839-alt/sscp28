<script setup lang="ts">
import type { RecentDialogRow, RecentTab } from '../composables/useRecentDraws'
import { getBallSrc } from '../composables/useOddsStyles'

interface Props {
  show: boolean
  gameName: string
  recentTab: RecentTab
  recentDialogRows: RecentDialogRow[]
  recentDialogStyle: { left: string; top: string }
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:recentTab': [tab: RecentTab]
  'close': []
  'titleMouseDown': [event: MouseEvent]
}>()

const closeIcon = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABIAAAASCAYAAABWzo5XAAAACXBIWXMAAAsSAAALEgHS3X78AAAAIGNIUk0AAHolAACAgwAA+f8AAIDpAAB1MAAA6mAAADqYAAAXb5JfxUYAAAO6SURBVHjaTJTNb1RlFMZ/7zt3Op1Oh+nQVj5KARWwxaQoCAkmunBjgkQSEUOaiCEQg7ggMdEQDcY1Gv0LDMTg10IS8B8gGA1gBVuaDlBpCW1pZzrTj/nqnXvve46LEcPZPL/NeXIW53nM8pH3NtlEyyUSdgcYTMyAgKry5JgnWK0BUXCKhm7Y+cEBzyS8y3Zb14BpbwEF9SNM3II1oMBjw8dqAKdgmyy1cIeMFy9bjccGTJsHIuhCFXd9EpebBSeoc2jkmuocahRXqBBdn0RKVVQE02rRuB2wiqJhhMxXkHkh8e4RyPYS3poEo6AOnMMYkIdF3EQZ75XX0EWDlmoQCaqKVQXCiCCXx2wfoOWN12k7dRLN9hLlZjAGjDFEj5aI5oW2kydIfnCM+L79BLk8uhKgBqw4AcDbnKV+9Qr+b39gu7tInzmNa1tH4+4s4XSRoKSkTn9C/OU9uGKJ+tXfIdOKAcQpVlAIHd7qJC1dMZbPnSe4P4ntyLDq9Mc0XJr6TIP0R6eIP7cFqdZYOPsN7sEoiY0ZVAQRwcwdP6rpTRkwFuPFaDycpTzj033mM5K7XiC4N446R6K/j/DRHPlPP6fVlUj1bUaiCJxQnqngiTFoGKHlJWSxiBc1SMzPMXHoMJu//47U3j0A1P+6xcP3P6Q9zJPY3kuYq2A6ujCrMqixeGG+gKuuoLUqIOB5rCzWkGwPtMT/f0IJQvxikXSngfoKEpShWMKk2gmCFLZ+J4eUl0EEi2V2dIrlNf1s+/UiqV0vUr4xRPn6n7Tv3cOzv/xEvpFmeWqRGDGIFF0sUx/LYcVYjFNMIEzcGGd5XT99P18gsX4ti9ducPvgYcaOn8B/NEv6pZ08c/5bpmpxFiYLxCLQUHHEsKqK+I779woEu17l+QvnSDzVxcLQTW4fPcaG1oCOpRluDh7BLxTp2L2Tp7/+ksmapVgoYwJFVLAqQlQPyS/UyB58i+SGHqYuXmLo0GF6owqdXavp6VnLqjvDXHv7HWpT06zdv4/M4CD/TC+gvkMjJXY8nfmiO9VKJuExMfQ3+bvj3D/7FVvVZ01nltCP0FDIptOURkZ5MJqjUatT+OFHtsQgaS3z9QBzrWej9q1uxxpDsVJneK5Ef3eW9ekUkVOalaCAwSiMFEoU/Aa7O7Nkk62IKGPVGubKmp7hre1tA6mYRRRElBgGRZv7QtNMms0SqRKJkrQWFKou4k7DH/HqYfTm2FLlUou1O4zSTPmTpfb4qP/QGrAYnCpiIBAZbogc+HcAo/AMwa270esAAAAASUVORK5CYII='

const getBallSizeLabels = (row: RecentDialogRow) => {
  const [b0 = 0, b1 = 0, b2 = 0] = row.balls
  return [b0 >= 5 ? '大' : '小', b1 >= 5 ? '大' : '小', b2 >= 5 ? '大' : '小'] as const
}

const getBallParityLabels = (row: RecentDialogRow) => {
  const [b0 = 0, b1 = 0, b2 = 0] = row.balls
  return [b0 % 2 === 0 ? '双' : '单', b1 % 2 === 0 ? '双' : '单', b2 % 2 === 0 ? '双' : '单'] as const
}

const getTagColorClass = (label: '大' | '小' | '单' | '双') =>
  label === '大' || label === '双' ? 'recent-pill--orange' : 'recent-pill--blue'
</script>

<template>
  <div
    v-if="show"
    class="recent-dialog"
    :style="recentDialogStyle"
  >
    <div class="recent-dialog-title" @mousedown="$emit('titleMouseDown', $event)">
      <h4 class="recent-dialog-title-text">
        {{ gameName }}
        <span class="recent-dialog-drag-tip">弹窗可拖动</span>
      </h4>
      <img
        class="recent-dialog-close cursor-pointer"
        :src="closeIcon"
        alt="关闭"
        @click="$emit('close')"
      />
    </div>

    <div class="recent-dialog-body">
      <div class="recent-dialog-scroll">
        <table class="recent-dialog-table">
          <thead>
            <tr>
              <th class="recent-col-issue">期数</th>
              <th class="recent-col-time">时间</th>
              <th class="recent-col-main">
                <div class="recent-col-main-wrap">
                  <button
                    class="recent-switch-btn"
                    :class="{ active: recentTab === 'number' }"
                    @click="$emit('update:recentTab', 'number')"
                  >
                    号码
                  </button>
                  <button
                    class="recent-switch-btn"
                    :class="{ active: recentTab === 'size' }"
                    @click="$emit('update:recentTab', 'size')"
                  >
                    大小
                  </button>
                  <button
                    class="recent-switch-btn"
                    :class="{ active: recentTab === 'parity' }"
                    @click="$emit('update:recentTab', 'parity')"
                  >
                    单双
                  </button>
                  <button
                    class="recent-switch-btn"
                    :class="{ active: recentTab === 'misc' }"
                    @click="$emit('update:recentTab', 'misc')"
                  >
                    杂项
                  </button>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in recentDialogRows" :key="row.issue">
              <td>{{ row.issue }}</td>
              <td>{{ row.time }}</td>
              <td>
                <div v-if="recentTab === 'number'" class="recent-number-cell">
                  <img
                    v-for="(ball, idx) in row.balls"
                    :key="`${row.issue}-${idx}-${ball}`"
                    class="recent-number-ball"
                    :src="getBallSrc(ball)"
                    :alt="String(ball)"
                  />
                </div>
                <div v-else-if="recentTab === 'size'" class="recent-pill-row">
                  <span
                    v-for="(label, idx) in getBallSizeLabels(row)"
                    :key="`${row.issue}-size-${idx}`"
                    class="recent-pill"
                    :class="getTagColorClass(label as '大' | '小' | '单' | '双')"
                  >
                    {{ label }}
                  </span>
                </div>
                <div v-else-if="recentTab === 'parity'" class="recent-pill-row">
                  <span
                    v-for="(label, idx) in getBallParityLabels(row)"
                    :key="`${row.issue}-parity-${idx}`"
                    class="recent-pill"
                    :class="getTagColorClass(label as '大' | '小' | '单' | '双')"
                  >
                    {{ label }}
                  </span>
                </div>
                <div v-else class="recent-misc-row">
                  <div class="recent-misc-item">{{ row.sum }}</div>
                  <div class="recent-misc-item" :style="{ color: row.size === '大' ? 'red' : '' }">{{ row.size }}</div>
                  <div class="recent-misc-item" :style="{ color: row.parity === '双' ? 'red' : '' }">{{ row.parity }}</div>
                  <div class="recent-misc-item" :style="{ color: row.sizeParity === '大双' ? 'red' : '' }">{{ row.sizeParity }}</div>
                  <div class="recent-misc-item">{{ row.misc }}</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.recent-dialog {
  position: fixed;
  width: 520px;
  height: 338px;
  background: #fff;
  border: 1px solid var(--bw-border-color, #efba84);
  border-radius: 2px;
  box-shadow: 0 12px 32px 4px rgba(0, 0, 0, 0.04), 0 8px 20px rgba(0, 0, 0, 0.08);
  z-index: 2006;
}

.recent-dialog-title {
  width: 100%;
  height: 26px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 5px;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-table-header-bg-color, linear-gradient(to bottom, #fff 0%, #fff1e4 100%));
  cursor: move;
  user-select: none;
}

.recent-dialog-title-text {
  margin: 0;
  display: flex;
  align-items: center;
  height: 30px;
  line-height: 30px;
  font-size: 14px;
  font-weight: 700;
  color: #000;
}

.recent-dialog-drag-tip {
  margin-left: 8px;
  font-size: 12px;
  font-weight: 400;
  color: #ff0000;
}

.recent-dialog-close {
  width: 18px;
  height: 18px;
  display: block;
  flex-shrink: 0;
}

.recent-dialog-body {
  padding: 0 6px 6px;
  box-sizing: border-box;
}

.recent-dialog-scroll {
  width: 506px;
  height: 300px;
  max-height: 300px;
  overflow: auto;
}

.recent-dialog-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 14px;
  color: #333;
}

.recent-dialog-table th,
.recent-dialog-table td {
  padding: 0;
  text-align: center;
  box-sizing: border-box;
  border: 1px solid var(--bw-border-color, #efba84);
}

.recent-dialog-table th {
  height: 26px;
  line-height: 26px;
  font-size: 14px;
  font-weight: 400;
  color: #333;
}

.recent-dialog-table td {
  height: 26px;
  line-height: 26px;
  font-size: 14px;
  font-weight: 400;
  color: #333;
}

.recent-col-issue {
  width: 128px;
}

.recent-col-time {
  width: 60px;
}

.recent-col-main {
  width: 318px;
}

.recent-col-main-wrap {
  display: inline-flex;
  align-items: center;
}

.recent-switch-btn {
  min-width: 47px;
  height: 24px;
  line-height: 12px;
  padding: 5px 11px;
  border: 1px solid var(--bw-border-color, #efba84);
  background: #fff;
  color: #000;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
}

.recent-switch-btn:first-child {
  min-width: 48px;
  border-radius: 4px 0 0 4px;
}

.recent-switch-btn:last-child {
  border-radius: 0 4px 4px 0;
}

.recent-switch-btn + .recent-switch-btn {
  border-left: none;
}

.recent-switch-btn.active {
  background: var(--el-color-primary, #5c2e0d);
  border-color: var(--el-color-primary, #5c2e0d);
  color: #fff;
}

.recent-number-cell {
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recent-number-ball {
  width: 27px;
  height: 27px;
  margin-left: 2px;
  position: relative;
  top: 2px;
}

.recent-number-ball:first-child {
  margin-left: 0;
}

.recent-pill-row {
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recent-pill {
  width: 22px;
  height: 22px;
  line-height: 20px;
  margin: 0 2px;
  border-radius: 50%;
  font-size: 12px;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.recent-pill--orange {
  background: #ff7302;
}

.recent-pill--blue {
  background: #0089ff;
}

.recent-misc-row {
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recent-misc-item {
  width: 40px;
  text-align: center;
}

.cursor-pointer {
  cursor: pointer;
}
</style>
