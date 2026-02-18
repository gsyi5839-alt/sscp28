<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import GameHeader from '../components/GameHeader.vue'
import MemberSidebar from '../components/MemberSidebar.vue'
import NoticeDialog from '../components/NoticeDialog.vue'
import { lotteryApi } from '../api/index'

// ─── 公告弹窗 ────────────────────────────────────────────────────────────────
const showNoticeDialog = ref(false)

const handleCloseNotice = () => {
  showNoticeDialog.value = false
}

// ─── 开奖 API 数据 ────────────────────────────────────────────────────────────
/** 当前彩种 code，720 = 加拿大PC28 */
const LOT_CODE = 720

/** 上一期开奖：期号 */
const preDrawIssue = ref('--')
/** 上一期开奖：三个球号码 [b1, b2, b3] */
const preDrawBalls = ref<number[]>([0, 0, 0])
/** 上一期开奖：和值 */
const preDrawSum = ref(0)

/** 当前期（下期）期号 */
const drawIssue = ref('--')
/** 距离封盘倒计时字符串 HH:MM:SS */
const sealCountdown = ref('--:--:--')
/** 距离开奖倒计时字符串 HH:MM:SS */
const drawCountdown = ref('--:--:--')
/** 是否处于开奖中状态（倒计时已过，等待新期数据） */
const isDrawing = ref(false)

/** 历史统计：最近30期和值列表（来自 API） */
const historyNums = ref<number[]>([])

let countdownTimer: ReturnType<typeof setInterval> | null = null
/** 是否正在执行自动刷新（防止重复请求） */
let isFetching = false

/** drawTime 目标时间戳（ms，UTC） */
let drawTimestamp = 0
/** sealTime = drawTime - 10s（封盘提前10秒） */
let sealTimestamp = 0
/** 当前已知的期号，用于检测新期是否到来 */
let currentDrawIssue = ''
/** 当前已知的上一期开奖期号 */
let currentPreDrawIssue = ''
const lastBallRef = ref<HTMLImageElement | null>(null)
const countdownRef = ref<HTMLDivElement | null>(null)
const countdownShift = ref(0)
const COUNTDOWN_EXTRA_SHIFT = -455

const parseTimestamp = (str: string | null | undefined): number => {
  if (!str) return 0
  // Upstream API always returns Beijing time (CST, UTC+8).
  // Append timezone offset so Date.parse() treats it correctly regardless of browser locale.
  // format: "2026-02-18 03:19:00" → "2026-02-18T03:19:00+08:00"
  return new Date(str.replace(' ', 'T') + '+08:00').getTime()
}

const fmtCountdown = (diffMs: number): string => {
  if (diffMs <= 0) return '00:00:00'
  const totalSec = Math.floor(diffMs / 1000)
  const h = Math.floor(totalSec / 3600)
  const m = Math.floor((totalSec % 3600) / 60)
  const s = totalSec % 60
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(h)}:${pad(m)}:${pad(s)}`
}

const fetchLotteryInfo = async () => {
  if (isFetching) return
  isFetching = true
  try {
    const res: any = await lotteryApi.getInfo(LOT_CODE)
    if (res?.code === 200 && res?.data) {
      const d = res.data
      const newIssue = d.drawIssue || ''

      // Previous draw result
      const newPreDrawIssue = d.preDrawIssue || ''
      preDrawIssue.value = newPreDrawIssue || '--'
      if (d.preDrawCode) {
        preDrawBalls.value = d.preDrawCode.split(',').map(Number)
        preDrawSum.value = preDrawBalls.value.reduce((a: number, b: number) => a + b, 0)
        nextTick(updateCountdownPosition)
      }

      // Current draw period
      drawIssue.value = newIssue
      drawTimestamp = parseTimestamp(d.drawTime)
      // Seal = drawTime - 10s (closes 10 seconds before draw, matching the upstream site design)
      sealTimestamp = drawTimestamp - 10 * 1000

      // If we have a new issue, clear the "drawing" state
      if (newIssue && newIssue !== currentDrawIssue) {
        currentDrawIssue = newIssue
        isDrawing.value = false
        // Also refresh history when a new period starts
        fetchHistoryList()
      }

      // Refresh history when new result is published
      if (newPreDrawIssue && newPreDrawIssue !== currentPreDrawIssue) {
        currentPreDrawIssue = newPreDrawIssue
        fetchHistoryList()
      }
    }
  } catch (_e) {
    // fail silently
  } finally {
    isFetching = false
  }
}

const fetchHistoryList = async () => {
  try {
    const res: any = await lotteryApi.getList(LOT_CODE, 1, HISTORY_LIST_SIZE)
    if (res?.code === 200 && res?.data?.list) {
      const rawList = res.data.list || []
      const sortedList = [...rawList].sort((a: any, b: any) => {
        const ai = Number(a?.preDrawIssue)
        const bi = Number(b?.preDrawIssue)
        if (Number.isFinite(ai) && Number.isFinite(bi)) return bi - ai
        return String(b?.preDrawIssue || '').localeCompare(String(a?.preDrawIssue || ''))
      })
      historyIssues.value = sortedList
      historyNums.value = sortedList.map((item: any) => Number(item.sumValue)).filter((n: number) => !isNaN(n))
    }
  } catch (_e) {
    // fail silently
  }
}

const updateCountdownPosition = () => {
  const ballEl = lastBallRef.value
  const countEl = countdownRef.value
  if (!ballEl || !countEl) return
  const ballRect = ballEl.getBoundingClientRect()
  // Subtract current shift to recover natural (pre-transform) left position,
  // so repeated calls remain stable and don't reset to 0.
  const naturalLeft = countEl.getBoundingClientRect().left - countdownShift.value
  countdownShift.value = Math.round(ballRect.left - naturalLeft) + COUNTDOWN_EXTRA_SHIFT
}

// Re-position countdown after lottery data loads and balls are rendered
watch(preDrawBalls, () => {
  nextTick(updateCountdownPosition)
})

/** Called every second to tick the countdown display */
const tickCountdown = () => {
  const now = Date.now()
  const drawDiff = drawTimestamp - now
  const sealDiff = sealTimestamp - now

  if (drawDiff <= 0 && drawTimestamp > 0) {
    // Draw time has passed — enter "drawing" state and keep polling for the new period
    isDrawing.value = true
    drawCountdown.value = '00:00:00'
    sealCountdown.value = '00:00:00'
    // Aggressively re-fetch until the API returns a new period
    fetchLotteryInfo()
  } else {
    isDrawing.value = false
    drawCountdown.value = fmtCountdown(drawDiff)
    sealCountdown.value = fmtCountdown(sealDiff)
  }
}

onMounted(() => {
  document.title = '游戏首页'
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) favicon.href = '/favicon.png'

  // Show notice after short delay
  setTimeout(() => { showNoticeDialog.value = true }, 500)

  // Initial data load
  fetchLotteryInfo()
  fetchHistoryList()

  // Countdown tick every second; also handles auto-refresh when draw expires
  countdownTimer = setInterval(tickCountdown, 1000)
  nextTick(updateCountdownPosition)
  window.addEventListener('resize', updateCountdownPosition)
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  window.removeEventListener('resize', updateCountdownPosition)
})

/* ============ 两面长龙排行（基于开奖历史） ============ */
const historyIssues = ref<any[]>([])

const parseBalls = (code: string | null | undefined): number[] | null => {
  if (!code) return null
  const nums = code.split(',').map(Number).filter(n => !isNaN(n))
  if (nums.length < 3) return null
  return nums.slice(0, 3)
}

const getIssueData = (issue: any) => {
  const balls = parseBalls(issue?.preDrawCode)
  if (!balls) return null
  const sum = Number(issue?.sumValue)
  const sumValue = Number.isFinite(sum) ? sum : balls.reduce((a, b) => a + b, 0)
  return { balls, sum: sumValue }
}

const dragonDefs = [
  { key: 'b1_big', label: '第1球-大', test: (d: any) => d.balls[0] >= 5 },
  { key: 'b1_small', label: '第1球-小', test: (d: any) => d.balls[0] <= 4 },
  { key: 'b1_odd', label: '第1球-单', test: (d: any) => d.balls[0] % 2 === 1 },
  { key: 'b1_even', label: '第1球-双', test: (d: any) => d.balls[0] % 2 === 0 },
  { key: 'b2_big', label: '第2球-大', test: (d: any) => d.balls[1] >= 5 },
  { key: 'b2_small', label: '第2球-小', test: (d: any) => d.balls[1] <= 4 },
  { key: 'b2_odd', label: '第2球-单', test: (d: any) => d.balls[1] % 2 === 1 },
  { key: 'b2_even', label: '第2球-双', test: (d: any) => d.balls[1] % 2 === 0 },
  { key: 'b3_big', label: '第3球-大', test: (d: any) => d.balls[2] >= 5 },
  { key: 'b3_small', label: '第3球-小', test: (d: any) => d.balls[2] <= 4 },
  { key: 'b3_odd', label: '第3球-单', test: (d: any) => d.balls[2] % 2 === 1 },
  { key: 'b3_even', label: '第3球-双', test: (d: any) => d.balls[2] % 2 === 0 },
  { key: 'sum_big', label: '和值-大', test: (d: any) => d.sum >= 14 },
  { key: 'sum_small', label: '和值-小', test: (d: any) => d.sum <= 13 },
  { key: 'sum_odd', label: '和值-单', test: (d: any) => d.sum % 2 === 1 },
  { key: 'sum_even', label: '和值-双', test: (d: any) => d.sum % 2 === 0 },
]

const dragonList = computed(() => {
  const issues = historyIssues.value
  if (!issues.length) return []

  const dataList = issues
    .map((issue) => getIssueData(issue))
    .filter(Boolean) as Array<{ balls: number[]; sum: number }>

  if (!dataList.length) return []

  const stats = dragonDefs.map((def, idx) => {
    let count = 0
    for (const d of dataList) {
      if (def.test(d)) count += 1
      else break
    }
    return { label: def.label, count, order: idx }
  })

  const DRAGON_MIN_COUNT = 2
  const DRAGON_MAX_COUNT = 5

  return stats
    .filter((item) => item.count >= DRAGON_MIN_COUNT)
    .sort((a, b) => (b.count - a.count) || (a.order - b.order))
    .slice(0, DRAGON_MAX_COUNT)
    .map((item) => ({ label: item.label, value: `${item.count}期` }))
})

const sumOdds = [
  { num: 0, odd: '399.88' },
  { num: 1, odd: '99.88' },
  { num: 2, odd: '79.88' },
  { num: 3, odd: '39.88' },
  { num: 4, odd: '29.88' },
  { num: 5, odd: '24.88' },
  { num: 6, odd: '19.88' },
  { num: 7, odd: '15.88' },
  { num: 8, odd: '12.88' },
  { num: 9, odd: '10.88' },
  { num: 10, odd: '9.88' },
  { num: 11, odd: '8.88' },
  { num: 12, odd: '6.89' },
  { num: 13, odd: '6.48' },
  { num: 14, odd: '6.48' },
  { num: 15, odd: '6.89' },
  { num: 16, odd: '8.88' },
  { num: 17, odd: '9.88' },
  { num: 18, odd: '10.88' },
  { num: 19, odd: '12.88' },
  { num: 20, odd: '15.88' },
  { num: 21, odd: '19.88' },
  { num: 22, odd: '24.88' },
  { num: 23, odd: '29.875' },
  { num: 24, odd: '39.88' },
  { num: 25, odd: '79.88' },
  { num: 26, odd: '99.88' },
  { num: 27, odd: '399.88' },
]

const sumGroups = Array.from({ length: 4 }, (_, col) =>
  sumOdds.slice(col * 7, col * 7 + 7)
)

const twoSideRows = [
  [
    { label: '大', odd: '1.44' },
    { label: '单', odd: '1.44' },
    { label: '极大', odd: '15.49' },
    { label: '大单', odd: '2.88' },
    { label: '大双', odd: '2.88' },
  ],
  [
    { label: '小', odd: '1.44' },
    { label: '双', odd: '1.44' },
    { label: '极小', odd: '15.49' },
    { label: '小单', odd: '2.88' },
    { label: '小双', odd: '2.88' },
  ],
]

const colorRows = [
  { label: '绿波', odd: '1.58' },
  { label: '蓝波', odd: '1.58' },
  { label: '红波', odd: '1.58' },
]

const patternRows = [
  { label: '豹子', odd: '49.88' },
  { label: '顺子', odd: '6.88' },
  { label: '对子', odd: '1.44' },
  { label: '半顺', odd: '1.34' },
  { label: '杂六', odd: '1.08' },
]

type SummaryKey = 'sum' | 'size' | 'parity'
type QuickMode = 'quick' | 'normal'

const summaryTabs: Array<{ key: SummaryKey; label: string }> = [
  { key: 'sum', label: '和值' },
  { key: 'size', label: '和值大小' },
  { key: 'parity', label: '和值单双' },
]

const SUMMARY_CELL_COUNT = 30
const HISTORY_LIST_SIZE = 200

const padToCellCount = (values: any[]) => {
  if (values.length >= SUMMARY_CELL_COUNT) {
    return values.slice(0, SUMMARY_CELL_COUNT)
  }
  return [...values, ...Array(SUMMARY_CELL_COUNT - values.length).fill('')]
}

const buildRoadColumns = (labels: string[]) => {
  const cols: string[][] = []
  for (const label of labels) {
    if (!label) continue
    const last = cols[cols.length - 1]
    if (!last || last[0] !== label) {
      cols.push([label])
    } else {
      last.push(label)
    }
    if (cols.length >= SUMMARY_CELL_COUNT) break
  }
  return padToCellCount(cols)
}

const getIssueSum = (issue: any): number | null => {
  const rawSum = Number(issue?.sumValue)
  if (Number.isFinite(rawSum)) return rawSum
  const balls = parseBalls(issue?.preDrawCode)
  if (!balls) return null
  return balls.reduce((a, b) => a + b, 0)
}

/** Derive display values from historyIssues (API data, reactive) */
const summaryNumbers = computed(() =>
  padToCellCount(historyIssues.value.slice(0, SUMMARY_CELL_COUNT).map((issue: any) => {
    const sum = getIssueSum(issue)
    return sum == null ? '' : String(sum)
  }))
)
const summarySize = computed(() =>
  buildRoadColumns(historyIssues.value.map((issue: any) => {
    const label = issue?.sizeLabel
    if (label) return String(label)
    const sum = getIssueSum(issue)
    if (sum == null) return ''
    return sum >= 14 ? '大' : '小'
  }))
)
const summaryParity = computed(() =>
  buildRoadColumns(historyIssues.value.map((issue: any) => {
    const label = issue?.parityLabel
    if (label) return String(label)
    const sum = getIssueSum(issue)
    if (sum == null) return ''
    return sum % 2 === 0 ? '双' : '单'
  }))
)

const activeSummaryKey = ref<SummaryKey>('sum')
const quickMode = ref<QuickMode>('normal')
// Active betting tab: 'twoSide' = 两面盘, 'balls' = 1-3球
const activeBetTab = ref<'twoSide' | 'balls'>('twoSide')
// Ball amounts for 1-3球 panel: flat map with key "${colIdx}_${ballKey}" (e.g. "0_3", "1_大")
const ballAmounts = ref<Record<string, string>>({})
const selectedSumNums = ref<Set<number>>(new Set())
const sumAmounts = ref<Record<number, string>>({})
const activeSumNum = ref<number | null>(null)
const selectedTwoSideKeys = ref<Set<string>>(new Set())
const twoSideAmounts = ref<Record<string, string>>({})
const activeTwoSideKey = ref<string | null>(null)
const selectedColorKeys = ref<Set<string>>(new Set())
const colorAmounts = ref<Record<string, string>>({})
const activeColorKey = ref<string | null>(null)
const selectedPatternKeys = ref<Set<string>>(new Set())
const patternAmounts = ref<Record<string, string>>({})
const activePatternKey = ref<string | null>(null)

const activeSummaryValues = computed(() => {
  if (activeSummaryKey.value === 'size') return summarySize.value
  if (activeSummaryKey.value === 'parity') return summaryParity.value
  return summaryNumbers.value
})

const onSummaryTabClick = (key: SummaryKey) => {
  activeSummaryKey.value = key
}

const setQuickMode = (mode: QuickMode) => {
  quickMode.value = mode
}

const toggleSumSelect = (num: number) => {
  if (quickMode.value === 'quick') {
    const next = new Set(selectedSumNums.value)
    if (next.has(num)) {
      next.delete(num)
    } else {
      next.add(num)
    }
    selectedSumNums.value = next
    return
  }
  activeSumNum.value = num
}

const ensureSumSelected = (num: number) => {
  activeSumNum.value = num
}

const isSumSelected = (num: number) => {
  if (quickMode.value === 'quick') return selectedSumNums.value.has(num)
  const amount = sumAmounts.value[num]
  return (amount && amount.trim() !== '') || activeSumNum.value === num
}

const toggleTwoSideSelect = (key: string) => {
  if (quickMode.value === 'quick') {
    const next = new Set(selectedTwoSideKeys.value)
    if (next.has(key)) {
      next.delete(key)
    } else {
      next.add(key)
    }
    selectedTwoSideKeys.value = next
    return
  }
  activeTwoSideKey.value = key
}

const ensureTwoSideSelected = (key: string) => {
  activeTwoSideKey.value = key
}

const isTwoSideSelected = (key: string) => {
  if (quickMode.value === 'quick') return selectedTwoSideKeys.value.has(key)
  const amount = twoSideAmounts.value[key]
  return (amount && amount.trim() !== '') || activeTwoSideKey.value === key
}

const toggleColorSelect = (key: string) => {
  if (quickMode.value === 'quick') {
    const next = new Set(selectedColorKeys.value)
    if (next.has(key)) {
      next.delete(key)
    } else {
      next.add(key)
    }
    selectedColorKeys.value = next
    return
  }
  activeColorKey.value = key
}

const ensureColorSelected = (key: string) => {
  activeColorKey.value = key
}

const isColorSelected = (key: string) => {
  if (quickMode.value === 'quick') return selectedColorKeys.value.has(key)
  const amount = colorAmounts.value[key]
  return (amount && amount.trim() !== '') || activeColorKey.value === key
}

const togglePatternSelect = (key: string) => {
  if (quickMode.value === 'quick') {
    const next = new Set(selectedPatternKeys.value)
    if (next.has(key)) {
      next.delete(key)
    } else {
      next.add(key)
    }
    selectedPatternKeys.value = next
    return
  }
  activePatternKey.value = key
}

const ensurePatternSelected = (key: string) => {
  activePatternKey.value = key
}

const isPatternSelected = (key: string) => {
  if (quickMode.value === 'quick') return selectedPatternKeys.value.has(key)
  const amount = patternAmounts.value[key]
  return (amount && amount.trim() !== '') || activePatternKey.value === key
}

const onExplainClick = () => {
  ElMessageBox.alert(
    '保存可以将金额保存為常用筹码，最多可以保存三个。',
    'xxobudi.gl7f25n0.com显示',
    {
      confirmButtonText: '确定',
      showClose: false,
      closeOnClickModal: true,
      customClass: 'explain-messagebox',
    }
  )
}

const getBallSrc = (num: number) => {
  const safe = Math.max(0, Math.min(27, num))
  const name = String(safe).padStart(2, '0')
  return new URL(`../assets/游戏/ball_cols_split/ball_${name}.png`, import.meta.url).href
}
</script>

<template>
  <div class="page">
    <GameHeader />

    <!-- 主体：92%宽度居中，白色背景，无底边框 -->
    <div class="main-wrapper">
      <!-- 三栏布局：左侧栏 + 主内容 + 右侧栏 -->
      <div class="main-body">
        <!-- 左侧：会员信息 -->
        <MemberSidebar />

        <!-- 中间：主内容区域 -->
        <div class="center-content">
          <div class="game-panel">
            <div class="issue-bar">
              <div class="issue-row issue-row-top">
                <div class="issue-left">
                  <span class="text-blue mr10">加拿大pc28</span>
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
                <div class="issue-left">
                  <b class="text-green">{{ drawIssue }}</b>
                  <span class="ml10">期</span>
                  <span
                    class="bet-tab ml10"
                    :class="{ 'bet-tab-active': activeBetTab === 'twoSide' }"
                    @click="activeBetTab = 'twoSide'"
                  >两面盘</span>
                  <span
                    class="bet-tab ml10"
                    :class="{ 'bet-tab-active': activeBetTab === 'balls' }"
                    @click="activeBetTab = 'balls'"
                  >1-3球</span>
                </div>
                <div class="issue-right">
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
                      <span class="ml40">距离开奖:</span>
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

            <div class="quick-bar quick-bar-top">
              <span
                class="quick-tab"
                :class="{ active: quickMode === 'quick' }"
                @click="setQuickMode('quick')"
              >
                快捷
              </span>
              <span
                class="quick-tab"
                :class="{ active: quickMode === 'normal' }"
                @click="setQuickMode('normal')"
              >
                一般
              </span>
              <span class="text-blue ml10">金额</span>
              <input class="amount-input" type="text" />
              <button class="btn btn-ok">确定</button>
              <button class="btn btn-clear">清空</button>
              <button class="btn btn-save">保存</button>
              <span class="ml10" role="button" tabindex="0" @click="onExplainClick">（说明）</span>
              <button class="btn btn-recent">最近开奖</button>
            </div>

            <!-- 两面盘 content (hidden when 1-3球 tab is active) -->
            <div v-show="activeBetTab === 'twoSide'">
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
                  @click="toggleSumSelect(item.num)"
                >
                  <div class="sum-cell ball-cell">
                    <img class="ball-img" :src="getBallSrc(item.num)" :alt="String(item.num)" />
                  </div>
                  <div class="sum-cell odd-cell">
                    <b class="text-red">{{ item.odd }}</b>
                  </div>
                  <div v-if="quickMode === 'normal'" class="sum-cell input-cell">
                    <input
                      v-model="sumAmounts[item.num]"
                      class="cell-input"
                      type="text"
                      @click.stop
                      @focus="ensureSumSelected(item.num)"
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
                  @click="toggleTwoSideSelect(item.label)"
                >
                  <span class="label">{{ item.label }}</span>
                  <span class="odd text-red">{{ item.odd }}</span>
                  <div class="input-box">
                    <input
                      v-if="quickMode === 'normal'"
                      v-model="twoSideAmounts[item.label]"
                      class="cell-input"
                      type="text"
                      @click.stop
                      @focus="ensureTwoSideSelected(item.label)"
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
                @click="toggleColorSelect(item.label)"
              >
                <span class="label" :class="`label-${item.label}`">{{ item.label }}</span>
                <span class="odd text-red">{{ item.odd }}</span>
                <div class="input-box">
                  <input
                    v-if="quickMode === 'normal'"
                    v-model="colorAmounts[item.label]"
                    class="cell-input"
                    type="text"
                    @click.stop
                    @focus="ensureColorSelected(item.label)"
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
                @click="togglePatternSelect(item.label)"
              >
                <span class="label">{{ item.label }}</span>
                <span class="odd text-red">{{ item.odd }}</span>
                <div class="input-box">
                  <input
                    v-if="quickMode === 'normal'"
                    v-model="patternAmounts[item.label]"
                    class="cell-input"
                    type="text"
                    @click.stop
                    @focus="ensurePatternSelected(item.label)"
                  />
                </div>
              </div>
            </div>
            </div><!-- end v-show twoSide -->

            <!-- 1-3球 betting panel -->
            <div v-show="activeBetTab === 'balls'" class="balls-panel">
              <div class="balls-grid">
                <div
                  v-for="(colName, colIdx) in ['第一球', '第二球', '第三球']"
                  :key="colIdx"
                  class="balls-col"
                >
                  <!-- Column header with gradient background -->
                  <div class="balls-col-header">{{ colName }}</div>

                  <!-- Ball rows 0-9, odds 9.9 -->
                  <div
                    v-for="n in 10"
                    :key="n - 1"
                    class="balls-row"
                  >
                    <div class="balls-ball-cell">
                      <img :src="getBallSrc(n - 1)" class="ball-img balls-ball-img" :alt="String(n - 1)" />
                    </div>
                    <div class="balls-odd-cell"><b class="text-red">9.9</b></div>
                    <div class="balls-input-cell">
                      <input
                        v-model="ballAmounts[`${colIdx}_${n - 1}`]"
                        class="balls-cell-input"
                        type="text"
                      />
                    </div>
                  </div>

                  <!-- Two-side rows: 大 小 单 双, odds 1.9776 -->
                  <div
                    v-for="label in ['大', '小', '单', '双']"
                    :key="label"
                    class="balls-row"
                  >
                    <div class="balls-label-cell">{{ label }}</div>
                    <div class="balls-odd-cell"><b class="text-red">1.9776</b></div>
                    <div class="balls-input-cell">
                      <input
                        v-model="ballAmounts[`${colIdx}_${label}`]"
                        class="balls-cell-input"
                        type="text"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div><!-- end 1-3球 panel -->

            <div class="quick-bar quick-bar-bottom">
              <span
                class="quick-tab"
                :class="{ active: quickMode === 'quick' }"
                @click="setQuickMode('quick')"
              >
                快捷
              </span>
              <span
                class="quick-tab"
                :class="{ active: quickMode === 'normal' }"
                @click="setQuickMode('normal')"
              >
                一般
              </span>
              <span class="text-blue ml10">金额</span>
              <input class="amount-input" type="text" />
              <button class="btn btn-ok">确定</button>
              <button class="btn btn-clear">清空</button>
              <button class="btn btn-save">保存</button>
              <span class="ml10" role="button" tabindex="0" @click="onExplainClick">（说明）</span>
            </div>

            <div class="summary-bar">
              <span
                v-for="tab in summaryTabs"
                :key="tab.key"
                class="summary-item"
                :class="{
                  active: activeSummaryKey === tab.key,
                  'summary-item-danger': activeSummaryKey === tab.key,
                }"
                @click="onSummaryTabClick(tab.key)"
              >
                {{ tab.label }}
              </span>
            </div>

            <div class="summary-values" :class="`summary-values--${activeSummaryKey}`">
              <div
                v-for="(value, idx) in activeSummaryValues"
                :key="idx"
                class="summary-value"
              >
                <div
                  class="text-center pb10 uno-b-r wfull summary-cell-inner"
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

        <!-- 右侧：公告 + 长龙排行 -->
        <div class="right-sidebar">
          <!-- 公告标题 -->
          <div class="announce-header">
            <span class="announce-title">公告</span>
            <span class="more-link">更多</span>
          </div>
          <!-- 公告内容 -->
          <div class="announce-body">
            <p>尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)</p>
          </div>
          <!-- 两面长龙排行 -->
          <div class="dragon-header">两面长龙排行</div>
          <div class="dragon-list">
            <div v-for="item in dragonList" :key="item.label" class="dragon-row">
              <span class="dragon-label">{{ item.label }}</span>
              <span class="dragon-value">{{ item.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部滚动公告栏 -->
    <div class="footer-bar">
      <marquee
        behavior="scroll"
        direction="left"
        scrollamount="3"
        class="footer-marquee"
      >
        尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)
      </marquee>
    </div>

    <!-- 公告弹窗 -->
    <NoticeDialog
      v-model:visible="showNoticeDialog"
      @close="handleCloseNotice"
    />
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}

/* ==================== 主体容器 ==================== */
/* 原版：w-92% m-auto bg-[#fff] mt5 b-b-none */
/* 不使用 flex:1，白色区域只占内容需要的高度 */
.main-wrapper {
  width: 92%;
  /* Shift entire panel 102px to the left */
  margin: 5px auto 0;
  position: relative;
  left: -102px;
  background: #fff;
  border-bottom: none;
}

/* 三栏 flex 布局 —— 不强制 min-height，高度由内容决定 */
.main-body {
  display: flex;
  align-items: flex-start;
}

/* ==================== 中间内容区域 ==================== */
.center-content {
  flex: 0 0 720px;
  width: 720px;
  min-height: 500px; /* 中间内容区合理最小高度，后续有真实内容后可去除 */
  border-left: none;
  border-right: none;
}

.placeholder {
  padding: 24px;
  color: #999;
  font-size: 14px;
  text-align: center;
}

.game-panel {
  width: 720px;
  height: 733px;
  margin: 5px 0 30px;
  position: relative;
}

.issue-bar {
  width: 720px;
  height: 56px;
  padding: 5px 20px;
  border: 1px solid #efba84;
  border-bottom: 1px solid #efba84;
  border-radius: 4px 4px 0 0;
  font-size: 12px;
  box-sizing: border-box;
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

.countdown-group {
  display: inline-flex;
  align-items: center;
  pointer-events: none; /* prevent transformed element from intercepting tab button clicks */
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

.ml5 {
  margin-left: 5px;
}

.ml10 {
  margin-left: 10px;
}

.mr10 {
  margin-right: 10px;
}

.ml40 {
  margin-left: 40px;
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

.quick-bar {
  width: 720px;
  height: 49px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #efba84;
  border-top: none;
  border-bottom: none;
  background: #fff1e4;
  box-sizing: border-box;
  font-size: 12px;
  gap: 6px;
}

.quick-bar-top {
  justify-content: flex-end;
  padding: 0 10px;
}

.quick-bar-bottom {
  height: 48px;
  margin-top: 10px;
  background: transparent;
  border-color: transparent;
}

.quick-tab {
  width: 35px;
  text-align: center;
  cursor: pointer;
  color: #ff0000;
}

.quick-tab.active {
  background: #ffffbf;
  border: 1px solid #efba84;
  height: 25px;
  line-height: 25px;
  color: #ff0000;
}

.amount-input {
  width: 55px;
  height: 24px;
  border: 1px solid #a0b4d8;
  box-sizing: border-box;
}

.btn {
  width: 46px;
  height: 20px;
  border: none;
  font-size: 12px;
  cursor: pointer;
  color: #fff;
}

.btn-ok {
  background: #63a35c;
}

.btn-clear {
  background: #4a90e2;
}

.btn-save {
  background: #f5a623;
}

.btn-recent {
  width: auto;
  padding: 0 10px;
  line-height: 20px;
  border-radius: 2px;
  background: linear-gradient(180deg, #ff9c00, #ff5100);
}

.section-title {
  width: 720px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  font-size: 14px;
  font-weight: 400;
  color: #000;
  border: 1px solid #efba84;
  border-bottom: none;
  background: #fff1e4;
  margin: 0;
  box-sizing: border-box;
}

.two-side-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid #efba84;
  border-right: 1px solid #efba84;
  background: #fff1e4;
}

.color-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid #efba84;
  border-right: 1px solid #efba84;
  background: #fff1e4;
}

.sum-grid {
  width: 720px;
  display: flex;
  border: 1px solid #efba84;
  border-top: 1px solid #efba84;
  box-sizing: border-box;
}

.sum-col {
  width: 25%;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
}

.sum-col:last-child {
  border-right: none;
}

.sum-head {
  display: flex;
  height: 30px;
  line-height: 30px;
  background: #fff1e4;
  border-bottom: 1px solid #efba84;
}

.sum-head-cell {
  flex: 1;
  text-align: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
}

.sum-head-cell:first-child {
  width: 30px;
  flex: 0 0 30px;
}

.sum-head-cell:nth-child(2) {
  width: 75px;
  flex: 0 0 75px;
}

.sum-head-cell:nth-child(3) {
  width: 74px;
  flex: 0 0 74px;
}

.sum-head-cell:last-child {
  border-right: none;
}

.sum-row {
  display: flex;
  height: 30px;
  line-height: 30px;
  border-bottom: 1px solid #efba84;
}

.sum-row {
  cursor: pointer;
}

.sum-col .sum-row:last-child {
  border-bottom: none;
}

.sum-row:hover .sum-cell,
.sum-row:focus-within .sum-cell {
  background: #be9d76;
}

.sum-row-selected .sum-cell,
.sum-row-selected:hover .sum-cell,
.sum-row-selected:focus-within .sum-cell {
  background: #ffc214;
}

.sum-cell {
  flex: 1;
  text-align: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sum-cell:nth-child(1) {
  width: 30px;
  flex: 0 0 30px;
}

.sum-cell:nth-child(2) {
  width: 75px;
  flex: 0 0 75px;
}

.sum-cell:nth-child(3) {
  width: 74px;
  flex: 0 0 74px;
}

/* Quick mode: hide amount column and let odds stretch */
.sum-grid--quick .sum-head-cell:nth-child(1),
.sum-grid--quick .sum-cell:nth-child(1) {
  width: 30px;
  flex: 0 0 30px;
}

.sum-grid--quick .sum-head-cell:nth-child(2),
.sum-grid--quick .sum-cell:nth-child(2) {
  width: auto;
  flex: 1 1 auto;
}

.sum-cell:last-child {
  border-right: none;
}

.cell-input {
  width: 45px;
  height: 20px;
  border: 1px solid #a0b4d8;
  border-radius: 0.5px;
  box-sizing: border-box;
}

.cell-input:focus {
  outline: none;
  box-shadow: none;
  border-color: #000;
}

.two-side-grid {
  width: 720px;
  border: 1px solid #efba84;
  border-top: 1px solid #efba84;
  box-sizing: border-box;
}

.two-side-row {
  display: flex;
  height: 30px;
  line-height: 30px;
  border-bottom: 1px solid #efba84;
}

.two-side-row:last-child {
  border-bottom: none;
}

.two-side-item {
  width: 20%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  cursor: pointer;
}

.two-side-item:hover,
.two-side-item:focus-within {
  background: #be9d76;
}

.two-side-item:last-child {
  border-right: none;
}

.two-side-item .label {
  width: 30px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  background: #fff1e4;
  color: #000;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  display: inline-block;
  font-size: 13px;
}

.two-side-item:hover .label,
.two-side-item:focus-within .label {
  background: transparent;
}

.two-side-item .odd {
  width: 56.83px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.two-side-item .odd b {
  display: inline-block;
  width: 56.83px;
  height: 28px;
  line-height: 28px;
  text-align: center;
}

.two-side-item .input-box {
  width: 56px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.color-grid,
.pattern-grid {
  width: 720px;
  display: flex;
  border: 1px solid #efba84;
  border-top: 1px solid #efba84;
  box-sizing: border-box;
}

.color-item {
  width: 33.33%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  cursor: pointer;
}

.color-item:hover,
.color-item:focus-within {
  background: #be9d76;
}

.color-item:last-child {
  border-right: none;
}

.color-item .label {
  width: 60px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
  font-weight: 700;
  font-size: 12px;
  box-sizing: border-box;
}

.label-绿波 {
  color: green;
}

.label-蓝波 {
  color: blue;
}

.label-红波 {
  color: red;
}

.color-item .odd {
  width: 90px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-item .odd b {
  display: inline-block;
  width: 27.77px;
  height: 30px;
  line-height: 30px;
  text-align: center;
}

.color-item .input-box {
  width: 89px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.pattern-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid #efba84;
  border-right: 1px solid #efba84;
  background: #fff1e4;
}

.pattern-item {
  width: 20%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  cursor: pointer;
}

.bet-item-selected,
.bet-item-selected:hover,
.bet-item-selected:focus-within {
  background: #ffc214;
}

.bet-item-selected .label,
.bet-item-selected:hover .label,
.bet-item-selected:focus-within .label,
.bet-item-selected .odd,
.bet-item-selected:hover .odd,
.bet-item-selected:focus-within .odd,
.bet-item-selected .input-box,
.bet-item-selected:hover .input-box,
.bet-item-selected:focus-within .input-box {
  background: #ffc214;
}

.pattern-item:hover,
.pattern-item:focus-within {
  background: #be9d76;
}

.pattern-item:last-child {
  border-right: none;
}

.pattern-item .label {
  width: 32px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
  font-weight: 700;
  font-size: 12px;
  box-sizing: border-box;
}

.pattern-item .odd {
  width: 56px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pattern-item .input-box {
  width: 55px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.pattern-item .cell-input {
  width: 50px;
  height: 20px;
}

.summary-bar {
  width: 720px;
  height: 30px;
  line-height: 30px;
  display: flex;
  align-items: center;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  background: var(--bw-bg-3, #fff7ef);
  box-sizing: border-box;
  text-align: center;
  cursor: pointer;
  overflow: hidden;
}

.summary-item {
  flex: 1;
  text-align: center;
  color: #000;
  border-right-width: 1px;
  border-right-style: solid;
  border-color: var(--el-border-color, #efba84);
  border-top-color: rgb(239, 186, 132);
  border-right-color: rgb(239, 186, 132);
  border-bottom-color: rgb(239, 186, 132);
  border-left-color: rgb(239, 186, 132);
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
  border-right: none !important;
}

.summary-values {
  width: 719.1px;
  display: grid;
  grid-template-columns: repeat(30, 23.97px);
  /* Single row should fill the container height */
  grid-auto-rows: 1fr;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  box-sizing: border-box;
  overflow: hidden;
}

/* Per-tab heights (3 different sizes in the design) */
.summary-values--sum {
  height: 49.88px;
}

.summary-values--size {
  /* Match design inspection: 23.97 × 129.63 per cell */
  height: 129.63px;
}

.summary-values--parity {
  /* Match design inspection screenshot: 23.95 × 149.56 (inner cell) */
  height: 149.56px;
}

.summary-value {
  display: block;
  width: 23.97px;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.text-center {
  text-align: center;
}

.pb10 {
  padding-bottom: 10px;
}

.wfull {
  width: 100%;
}

.block {
  display: block;
}

.pt5 {
  padding-top: 5px;
}

.summary-cell-inner {
  height: 100%;
  box-sizing: border-box;
}

.bg-primary5 {
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
}

.uno-b-r {
  border-right-width: 1px;
  border-right-style: solid;
  border-color: var(--el-border-color, #efba84);
  border-top-color: rgb(239, 186, 132);
  border-right-color: rgb(239, 186, 132);
  border-bottom-color: rgb(239, 186, 132);
  border-left-color: rgb(239, 186, 132);
}

.summary-value:last-child .uno-b-r {
  border-right: none;
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
  padding: 4px 0;
  height: 100%;
}

.value-text-multi {
  display: block;
  line-height: 1;
  font-size: 13px;
  padding: 1px 0;
}

/* ==================== 右侧公告栏 ==================== */
.right-sidebar {
  width: 160px;
  flex-shrink: 0;
  margin-left: 10px;
  /* Align top with game-panel top border (game-panel has margin-top: 5px) */
  margin-top: 5px;
}

/* 公告标题行 */
.announce-header {
  height: 45px;
  line-height: 45px;
  padding: 0 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #efba84;
  background: linear-gradient(to bottom, #ab6939 0%, #3a1c04 100%);
  color: #fff;
  font-weight: 400;
  font-size: 13px;
}

.announce-title {
  font-size: 13px;
  font-weight: 400;
  color: #fff;
}

.more-link {
  font-size: 13px;
  font-weight: 400;
  color: #fff;
  cursor: pointer;
}

.more-link:hover {
  text-decoration: underline;
}

/* 公告内容 */
.announce-body {
  padding: 10px;
  width: 160px;
  height: 177px;
  font-size: 13px;
  line-height: 26px;
  color: #000;
  border: 1px solid #efba84;
  border-top: none;
  box-sizing: border-box;
  overflow: hidden;
  text-align: left;
  word-break: break-word;
  white-space: normal;
}

.announce-body p {
  margin: 0;
}

/* 两面长龙排行标题 */
.dragon-header {
  height: 45px;
  line-height: 45px;
  text-align: center;
  font-size: 14px;
  font-weight: 400;
  color: #fff;
  background: linear-gradient(to bottom, #ab6939 0%, #3a1c04 100%);
  border-bottom: 1px solid #efba84;
}

/* 长龙列表 */
.dragon-list {
  padding: 0;
  border: 1px solid #efba84;
  border-top: none;
}

.dragon-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 26px;
  line-height: 26px;
  font-size: 13px;
  border-bottom: 1px solid #efba84;
}

.dragon-row:last-child {
  border-bottom: none;
}

.dragon-label {
  width: 50%;
  height: 25px;
  line-height: 25px;
  padding-left: 10px;
  color: #000;
  border-right: 1px solid #efba84;
  background: #fff1e4;
  box-sizing: border-box;
}

.dragon-value {
  width: 50%;
  padding-left: 10px;
  color: red;
  font-weight: 400;
  box-sizing: border-box;
}

/* ==================== 底部滚动公告栏 ==================== */
/* margin-top: auto 配合 page 的 flex-column，始终推到页面最底部 */
.footer-bar {
  width: 92%;
  margin: 0 auto;
  margin-top: auto;
  height: 30px;
  line-height: 30px;
  background: #2b1204;
  overflow: hidden;
}

.footer-marquee {
  color: red;
  font-size: 13px;
  font-weight: 400;
  line-height: 30px;
}

/* ==================== 下注 tab 按钮 ==================== */
.bet-tab {
  cursor: pointer;
  color: blue;
  padding: 1px 4px;
  font-size: 12px;
}

.bet-tab-active {
  background: #ffffbf;
  border: 1px solid #efba84;
  color: #c00;
}

/* ==================== 1-3球 面板 ==================== */
.balls-panel {
  width: 720px;
}

.balls-grid {
  width: 720px;
  display: flex;
  border: 1px solid #efba84;
  box-sizing: border-box;
}

/* Each column: 1/3 width */
.balls-col {
  flex: 0 0 33.333%;
  width: 33.333%;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
}

.balls-col:last-child {
  border-right: none;
}

/* Gradient header (第一球/第二球/第三球) */
.balls-col-header {
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-weight: bold;
  font-size: 13px;
  color: #fff;
  background: linear-gradient(to bottom, #ab6939 0%, #3a1c04 100%);
}

/* Each data row */
.balls-row {
  display: flex;
  height: 30px;
  line-height: 30px;
  border-top: 1px solid #efba84;
  cursor: pointer;
  box-sizing: border-box;
}

.balls-row:hover .balls-ball-cell,
.balls-row:hover .balls-odd-cell,
.balls-row:hover .balls-input-cell,
.balls-row:hover .balls-label-cell {
  background: #be9d76;
}

/* Ball image cell (60px) */
.balls-ball-cell {
  width: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
}

/* Override default ball-img margin for this context */
.balls-ball-img {
  margin-left: 0 !important;
}

/* Odds cell (flex-1) */
.balls-odd-cell {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
}

/* Amount input cell (flex-1) */
.balls-input-cell {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

/* Amount text input */
.balls-cell-input {
  width: 50px;
  height: 20px;
  border: 1px solid #a0b4d8;
  text-align: center;
  font-size: 12px;
  box-sizing: border-box;
}

/* Two-side label cell (大/小/单/双) with warm background */
.balls-label-cell {
  width: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #efba84;
  font-weight: bold;
  background: #fff1e4;
  box-sizing: border-box;
}
</style>
