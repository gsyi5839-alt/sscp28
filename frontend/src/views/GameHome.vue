<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import GameHeader from '../components/GameHeader.vue'
import MemberSidebar from '../components/MemberSidebar.vue'
import NoticeDialog from '../components/NoticeDialog.vue'
import DrawResults from './DrawResults.vue'
import { lotteryApi } from '../api/index'
import systemClosedBg from '@/assets/通用/bg.png'

const route = useRoute()

// ─── 公告弹窗 ────────────────────────────────────────────────────────────────
const NOTICE_SHOWN_KEY = 'bw-notice-shown-session'
const showNoticeDialog = ref(false)
const showNoticeList = ref(false)

const handleCloseNotice = () => {
  showNoticeDialog.value = false
  // Mark notice as shown in this session
  sessionStorage.setItem(NOTICE_SHOWN_KEY, 'true')
}

// ─── 内嵌公告列表 ──────────────────────────────────────────────────────────────
const activeNoticeTab = ref('特别通知')
const noticeTabItems = [
  { key: '特别通知', label: '特别通知' },
  { key: '通知', label: '通知' },
  { key: '安全通知', label: '安全通知' },
  { key: '站点通知', label: '站点通知' }
]
const noticeListData: Record<string, Array<{ id: number; time: string; content: string; isHighlight?: boolean }>> = {
  '特别通知': [
    { id: 1, time: '2026-02-27 12:03', content: '尊敬的会员您好！值此马年新春之际，谨向一直以来支持与信赖我们的广大用户朋友致以衷心感谢和新春祝福！新的一年，我们将持续提升系统稳定性与服务效率，优化产品体验，为您提供更加安全、便捷、优质的服务保障。感谢您一直以来对本系统的支持！请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)', isHighlight: true },
    { id: 2, time: '2026-01-20 05:11', content: '尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)' },
    { id: 3, time: '2024-09-14 06:59', content: '尊敬的会员，您好！为了公平公正的原则，以及更好的游戏氛围及体验，本系统新添加官方游戏，加拿大PC28和加拿大时时彩，开奖数据由加拿大官方提供(https://lotto.bclc.com)同时每个游戏添加新玩法（宝斗，牛牛，斗牛）' }
  ],
  '通知': [
    { id: 4, time: '2026-02-15 10:00', content: '请各位会员注意保管好自己的账号密码，不要向任何人透露您的账户信息。' }
  ],
  '安全通知': [
    { id: 5, time: '2026-02-10 08:30', content: '为了保障您的账户安全，建议定期修改密码，并开启双重验证。' }
  ],
  '站点通知': [
    { id: 6, time: '2026-02-01 09:00', content: '欢迎访问本站，祝您使用愉快！如有任何问题请联系在线客服。' }
  ]
}
const currentNoticeList = computed(() => noticeListData[activeNoticeTab.value] || [])
const onNoticeTabClick = (key: string) => { activeNoticeTab.value = key }

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

/** 系统封盘时间状态（中国时间每天 06:00-07:00） */
const currentTime = ref(new Date())
const isSystemClosed = computed(() => {
  // Convert to China time (UTC+8)
  const now = currentTime.value
  const chinaHour = (now.getUTCHours() + 8) % 24
  return chinaHour >= 6 && chinaHour < 7
})

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
// Fine-tune countdown horizontal alignment relative to the last ball position.
// Positive = move right, negative = move left.
const COUNTDOWN_EXTRA_SHIFT = -452

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

const handleWindowResize = () => {
  updateCountdownPosition()
  if (showRecentDialog.value) {
    recentDialogPos.value = clampRecentDialogPos(recentDialogPos.value.left, recentDialogPos.value.top)
  }
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

  // Update current time for system closed check (06:00-07:00)
  currentTime.value = new Date()

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

  // Handle tab parameter from URL query (for navigation from other pages)
  const tabFromQuery = route.query.tab as string
  if (tabFromQuery === 'twoSide' || tabFromQuery === 'balls') {
    activeBetTab.value = tabFromQuery
  }
  if (route.query.view === 'drawResults') {
    activeContentView.value = 'drawResults'
  }

  // Show notice after short delay (only once per session)
  const hasShownNotice = sessionStorage.getItem(NOTICE_SHOWN_KEY)
  if (!hasShownNotice) {
    setTimeout(() => { showNoticeDialog.value = true }, 500)
  }

  // Initial data load
  fetchLotteryInfo()
  fetchHistoryList()

  // Countdown tick every second; also handles auto-refresh when draw expires
  countdownTimer = setInterval(tickCountdown, 1000)
  nextTick(updateCountdownPosition)
  window.addEventListener('resize', handleWindowResize)
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  window.removeEventListener('resize', handleWindowResize)
  stopRecentDialogDrag()
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
  // From /root/sscp28/设计元素.md: "和值 0-27 赔率"
  { num: 0, odd: '850' },
  { num: 1, odd: '280' },
  { num: 2, odd: '135' },
  { num: 3, odd: '85' },
  { num: 4, odd: '46' },
  { num: 5, odd: '38' },
  { num: 6, odd: '33.7' },
  { num: 7, odd: '26.2' },
  { num: 8, odd: '21' },
  { num: 9, odd: '17.2' },
  { num: 10, odd: '15' },
  { num: 11, odd: '13.7' },
  { num: 12, odd: '13' },
  { num: 13, odd: '12.6' },
  { num: 14, odd: '12.6' },
  { num: 15, odd: '13' },
  { num: 16, odd: '13.7' },
  { num: 17, odd: '15' },
  { num: 18, odd: '17.2' },
  { num: 19, odd: '21' },
  { num: 20, odd: '26.2' },
  { num: 21, odd: '33.7' },
  { num: 22, odd: '38' },
  { num: 23, odd: '46' },
  { num: 24, odd: '85' },
  { num: 25, odd: '135' },
  { num: 26, odd: '280' },
  { num: 27, odd: '850' },
]

// /root/sscp28/设计元素.md provides the measured size of odds text:
// - 3 digits (e.g. 850/280/135): 24.06 x 30
// - 2 digits (e.g. 85/46/38/21/15/13): 16.05 x 30
// - decimals (e.g. 33.7/26.2/17.2/12.6/13.7): 27.77 x 30
const sumOddTextStyle = (odd: string) => {
  const v = String(odd ?? '')
  if (!v) return {}
  let width = '16.05px'
  if (v.includes('.')) width = '27.77px'
  else if (v.length >= 3) width = '24.06px'
  return { width, height: '30px', lineHeight: '30px' }
}

// /root/sscp28/设计元素.md provides the measured size of two-side odds text:
// - 2.15 / 17.5: 27.77 x 30
// - 4.3: 19.75 x 30
const twoSideOddTextStyle = (odd: string) => {
  const v = String(odd ?? '')
  if (!v) return {}
  const width = v === '4.3' ? '19.75px' : '27.77px'
  return { width, height: '30px', lineHeight: '30px' }
}

// /root/sscp28/设计元素.md provides the measured size of "色波" odds text:
// - 3: 8.03 x 30
const colorOddTextStyle = (odd: string) => {
  const v = String(odd ?? '')
  if (!v) return {}
  const width = v === '3' ? '8.03px' : '27.77px'
  return { width, height: '30px', lineHeight: '30px' }
}

// /root/sscp28/设计元素.md provides the measured size of "豹子/顺子/对子/半顺/杂六" odds text:
// - 65 / 12: 16.05 x 30
// - 2.6 / 2.4: 19.75 x 30
// - 2.05: 27.77 x 30
const patternOddTextStyle = (odd: string) => {
  const v = String(odd ?? '')
  if (!v) return {}
  let width = '27.77px'
  if (v === '65' || v === '12') width = '16.05px'
  else if (v === '2.6' || v === '2.4') width = '19.75px'
  return { width, height: '30px', lineHeight: '30px' }
}

const sumGroups = Array.from({ length: 4 }, (_, col) =>
  sumOdds.slice(col * 7, col * 7 + 7)
)

const twoSideRows = [
  [
    { label: '大', odd: '2.15' },
    { label: '单', odd: '2.15' },
    { label: '极大', odd: '17.5' },
    { label: '大单', odd: '4.3' },
    { label: '大双', odd: '4.3' },
  ],
  [
    { label: '小', odd: '2.15' },
    { label: '双', odd: '2.15' },
    { label: '极小', odd: '17.5' },
    { label: '小单', odd: '4.3' },
    { label: '小双', odd: '4.3' },
  ],
]

const colorRows = [
  { label: '绿波', odd: '3' },
  { label: '蓝波', odd: '3' },
  { label: '红波', odd: '3' },
]

const patternRows = [
  { label: '豹子', odd: '65' },
  { label: '顺子', odd: '12' },
  { label: '对子', odd: '2.6' },
  { label: '半顺', odd: '2.05' },
  { label: '杂六', odd: '2.4' },
]

type SummaryKey = 'sum' | 'size' | 'parity'
type QuickMode = 'quick' | 'normal'
type RecentTab = 'number' | 'size' | 'parity' | 'misc'

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
    // Skip tie results ('和') — sum=27 in PC28 is neither 单 nor 双
    if (label && label !== '和') return String(label)
    const sum = getIssueSum(issue)
    if (sum == null || sum === 27) return ''
    return sum % 2 === 0 ? '双' : '单'
  }))
)

const activeSummaryKey = ref<SummaryKey>('sum')
const quickMode = ref<QuickMode>('normal')
// Active betting tab: 'twoSide' = 两面盘, 'balls' = 1-3球
const activeBetTab = ref<'twoSide' | 'balls'>('twoSide')
// Active content view: 'game' = default game panel, 'drawResults' = lottery results
const activeContentView = ref<'game' | 'drawResults'>('game')
const centerContentClasses = computed(() => ({
  'center-content--wide': activeContentView.value === 'drawResults'
}))
const mainWrapperClasses = computed(() => ({
  'main-wrapper--draw-results': activeContentView.value === 'drawResults'
}))
// 当切换游戏标签时，自动返回游戏内容
watch(activeBetTab, () => {
  showNoticeList.value = false
  activeContentView.value = 'game'
})

watch(activeContentView, (view) => {
  if (view === 'drawResults') {
    showNoticeList.value = false
  }
})

watch(
  () => route.query.view,
  (view) => {
    activeContentView.value = view === 'drawResults' ? 'drawResults' : 'game'
    if (view === 'drawResults') {
      showNoticeList.value = false
    }
  },
  { immediate: true }
)
// Ball amounts for 1-3球 panel: flat map with key "${colIdx}_${ballKey}" (e.g. "0_3", "1_大")
const ballAmounts = ref<Record<string, string>>({})
// 1-3球选中状态
const selectedBallKeys = ref<Set<string>>(new Set())
const activeBallKey = ref<string | null>(null)
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

// 1-3球选中状态函数
const toggleBallSelect = (key: string) => {
  if (quickMode.value === 'quick') {
    const next = new Set(selectedBallKeys.value)
    if (next.has(key)) {
      next.delete(key)
    } else {
      next.add(key)
    }
    selectedBallKeys.value = next
    return
  }
  activeBallKey.value = key
}

const ensureBallSelected = (key: string) => {
  activeBallKey.value = key
}

const isBallSelected = (key: string) => {
  if (quickMode.value === 'quick') return selectedBallKeys.value.has(key)
  const amount = ballAmounts.value[key]
  return (amount && amount.trim() !== '') || activeBallKey.value === key
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

// Display-only label near the issue number (switching is handled by GameHeader sub-nav).
const activeBetTabLabel = computed(() => (activeBetTab.value === 'balls' ? '1-3球' : '两面盘'))

// ─── 最近开奖弹窗 ──────────────────────────────────────────────────────────────
interface RecentDialogRow {
  issue: string
  time: string
  balls: number[]
  sum: number
  size: '大' | '小'
  parity: '单' | '双'
  sizeParity: string
  misc: '豹子' | '顺子' | '对子' | '半顺' | '杂六'
}

const showRecentDialog = ref(false)
const recentTab = ref<RecentTab>('number')
const recentDialogRef = ref<HTMLDivElement | null>(null)
const recentDialogPos = ref({ left: 0, top: 0 })

const RECENT_DIALOG_WIDTH = 520
const RECENT_DIALOG_HEIGHT = 338

const recentCloseIcon =
  'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABIAAAASCAYAAABWzo5XAAAACXBIWXMAAAsSAAALEgHS3X78AAAAIGNIUk0AAHolAACAgwAA+f8AAIDpAAB1MAAA6mAAADqYAAAXb5JfxUYAAAO6SURBVHjaTJTNb1RlFMZ/7zt3Op1Oh+nQVj5KARWwxaQoCAkmunBjgkQSEUOaiCEQg7ggMdEQDcY1Gv0LDMTg10IS8B8gGA1gBVuaDlBpCW1pZzrTj/nqnXvve46LEcPZPL/NeXIW53nM8pH3NtlEyyUSdgcYTMyAgKry5JgnWK0BUXCKhm7Y+cEBzyS8y3Zb14BpbwEF9SNM3II1oMBjw8dqAKdgmyy1cIeMFy9bjccGTJsHIuhCFXd9EpebBSeoc2jkmuocahRXqBBdn0RKVVQE02rRuB2wiqJhhMxXkHkh8e4RyPYS3poEo6AOnMMYkIdF3EQZ75XX0EWDlmoQCaqKVQXCiCCXx2wfoOWN12k7dRLN9hLlZjAGjDFEj5aI5oW2kydIfnCM+L79BLk8uhKgBqw4AcDbnKV+9Qr+b39gu7tInzmNa1tH4+4s4XSRoKSkTn9C/OU9uGKJ+tXfIdOKAcQpVlAIHd7qJC1dMZbPnSe4P4ntyLDq9Mc0XJr6TIP0R6eIP7cFqdZYOPsN7sEoiY0ZVAQRwcwdP6rpTRkwFuPFaDycpTzj033mM5K7XiC4N446R6K/j/DRHPlPP6fVlUj1bUaiCJxQnqngiTFoGKHlJWSxiBc1SMzPMXHoMJu//47U3j0A1P+6xcP3P6Q9zJPY3kuYq2A6ujCrMqixeGG+gKuuoLUqIOB5rCzWkGwPtMT/f0IJQvxikXSngfoKEpShWMKk2gmCFLZ+J4eUl0EEi2V2dIrlNf1s+/UiqV0vUr4xRPn6n7Tv3cOzv/xEvpFmeWqRGDGIFF0sUx/LYcVYjFNMIEzcGGd5XT99P18gsX4ti9ducPvgYcaOn8B/NEv6pZ08c/5bpmpxFiYLxCLQUHHEsKqK+I779woEu17l+QvnSDzVxcLQTW4fPcaG1oCOpRluDh7BLxTp2L2Tp7/+ksmapVgoYwJFVLAqQlQPyS/UyB58i+SGHqYuXmLo0GF6owqdXavp6VnLqjvDXHv7HWpT06zdv4/M4CD/TC+gvkMjJXY8nfmiO9VKJuExMfQ3+bvj3D/7FVvVZ01nltCP0FDIptOURkZ5MJqjUatT+OFHtsQgaS3z9QBzrWej9q1uxxpDsVJneK5Ef3eW9ekUkVOalaCAwSiMFEoU/Aa7O7Nkk62IKGPVGubKmp7hre1tA6mYRRRElBgGRZv7QtNMms0SqRKJkrQWFKou4k7DH/HqYfTm2FLlUou1O4zSTPmTpfb4qP/QGrAYnCpiIBAZbogc+HcAo/AMwa270esAAAAASUVORK5CYII='

const clampRecentDialogPos = (left: number, top: number) => {
  const maxLeft = Math.max(0, window.innerWidth - RECENT_DIALOG_WIDTH)
  const maxTop = Math.max(0, window.innerHeight - RECENT_DIALOG_HEIGHT)
  return {
    left: Math.min(Math.max(0, left), maxLeft),
    top: Math.min(Math.max(0, top), maxTop),
  }
}

const getRecentDialogDefaultPos = () =>
  clampRecentDialogPos(window.innerWidth - 750, 200)

const recentDialogStyle = computed(() => ({
  left: `${recentDialogPos.value.left}px`,
  top: `${recentDialogPos.value.top}px`,
}))

const getIssueTimeLabel = (raw: string | null | undefined) => {
  if (!raw) return '--:--'
  const m = String(raw).match(/(\d{2}):(\d{2})/)
  if (!m) return '--:--'
  return `${m[1]}:${m[2]}`
}

const classifyMisc = (balls: number[]): RecentDialogRow['misc'] => {
  const sorted = [...balls].sort((a, b) => a - b)
  const [b0 = 0, b1 = 0, b2 = 0] = sorted
  const uniq = new Set(sorted).size
  if (uniq === 1) return '豹子'
  if (uniq === 2) return '对子'
  const isStraight = b0 + 1 === b1 && b1 + 1 === b2
  if (isStraight) return '顺子'
  const isHalfStraight = b0 + 1 === b1 || b1 + 1 === b2
  if (isHalfStraight) return '半顺'
  return '杂六'
}

const getBallSizeLabels = (row: RecentDialogRow) => {
  const [b0 = 0, b1 = 0, b2 = 0] = row.balls
  return [b0 >= 5 ? '大' : '小', b1 >= 5 ? '大' : '小', b2 >= 5 ? '大' : '小'] as const
}

const getBallParityLabels = (row: RecentDialogRow) => {
  const [b0 = 0, b1 = 0, b2 = 0] = row.balls
  return [b0 % 2 === 0 ? '双' : '单', b1 % 2 === 0 ? '双' : '单', b2 % 2 === 0 ? '双' : '单'] as const
}

const recentDialogRows = computed<RecentDialogRow[]>(() =>
  historyIssues.value
    .slice(0, HISTORY_LIST_SIZE)
    .map((issue: any) => {
      const balls = parseBalls(issue?.preDrawCode)
      if (!balls) return null
      const sum = getIssueSum(issue)
      if (sum == null) return null
      const size: '大' | '小' = sum >= 14 ? '大' : '小'
      const parity: '单' | '双' = sum % 2 === 0 ? '双' : '单'
      return {
        issue: String(issue?.preDrawIssue ?? '--'),
        time: getIssueTimeLabel(issue?.preDrawTime),
        balls,
        sum,
        size,
        parity,
        sizeParity: `${size}${parity}`,
        misc: classifyMisc(balls),
      }
    })
    .filter(Boolean) as RecentDialogRow[]
)

const openRecentDialog = () => {
  // Keep the same behavior as the upstream page: close notice first, then open history.
  showNoticeDialog.value = false
  recentTab.value = 'number'
  recentDialogPos.value = getRecentDialogDefaultPos()
  showRecentDialog.value = true
}

const closeRecentDialog = () => {
  showRecentDialog.value = false
  stopRecentDialogDrag()
}

let isRecentDialogDragging = false
let recentDragOffsetX = 0
let recentDragOffsetY = 0

const onRecentDialogDragMove = (event: MouseEvent) => {
  if (!isRecentDialogDragging) return
  const nextLeft = event.clientX - recentDragOffsetX
  const nextTop = event.clientY - recentDragOffsetY
  recentDialogPos.value = clampRecentDialogPos(nextLeft, nextTop)
}

const stopRecentDialogDrag = () => {
  if (!isRecentDialogDragging) return
  isRecentDialogDragging = false
  window.removeEventListener('mousemove', onRecentDialogDragMove)
  window.removeEventListener('mouseup', stopRecentDialogDrag)
}

const onRecentTitleMouseDown = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (target.closest('.recent-dialog-close')) return
  if (!recentDialogRef.value) return
  isRecentDialogDragging = true
  recentDragOffsetX = event.clientX - recentDialogPos.value.left
  recentDragOffsetY = event.clientY - recentDialogPos.value.top
  window.addEventListener('mousemove', onRecentDialogDragMove)
  window.addEventListener('mouseup', stopRecentDialogDrag)
  event.preventDefault()
}

const getTagColorClass = (label: '大' | '小' | '单' | '双') =>
  label === '大' || label === '双' ? 'recent-pill--orange' : 'recent-pill--blue'

const getBallSrc = (num: number) => {
  const safe = Math.max(0, Math.min(27, num))
  const name = String(safe).padStart(2, '0')
  return new URL(`../assets/游戏/ball_cols_split/ball_${name}.png`, import.meta.url).href
}
</script>

<template>
  <div class="page">
    <GameHeader v-model:betTab="activeBetTab" v-model:contentView="activeContentView" />

    <!-- main wrapper: 92% width centered, white bg, no bottom border -->
    <div class="main-wrapper" :class="mainWrapperClasses">
      <!-- 三栏布局：左侧栏 + 主内容 + 右侧栏 -->
      <div class="main-body">
        <!-- 左侧：会员信息 -->
        <MemberSidebar />

        <!-- 中间：主内容区域 -->
        <div class="center-content" :class="centerContentClasses">
          <!-- Draw Results panel -->
          <div v-if="activeContentView === 'drawResults'" key="draw-results-view" class="draw-results-view">
            <DrawResults />
          </div>

          <!-- 游戏面板（默认显示） -->
          <div v-else-if="!showNoticeList" key="game-panel-view" class="game-panel">
            <!-- 系统封盘遮罩层（中国时间每天 06:00-07:00） -->
            <div v-if="isSystemClosed" class="system-closed-overlay">
              <img :src="systemClosedBg" alt="系统封盘" class="system-closed-bg" />
            </div>
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
                <div class="issue-left issue-left--bottom">
                  <b class="text-green">{{ drawIssue }}</b>
                  <span class="ml10">期</span>
                  <!-- Display only (not clickable). Use GameHeader sub-nav to switch the panel. -->
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
              <button class="btn btn-recent" @click="openRecentDialog">最近开奖</button>
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
                    <!-- /root/sscp28/设计元素.md: odds text should not be bold; keep #ff0000 color via .text-red -->
                    <span class="text-red sum-odd-text" :style="sumOddTextStyle(item.odd)">{{ item.odd }}</span>
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
                  <span class="odd text-red">
                    <span class="two-side-odd-text" :style="twoSideOddTextStyle(item.odd)">{{ item.odd }}</span>
                  </span>
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
                <span class="odd text-red">
                  <span class="color-odd-text" :style="colorOddTextStyle(item.odd)">{{ item.odd }}</span>
                </span>
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
                <span class="odd text-red">
                  <span class="pattern-odd-text" :style="patternOddTextStyle(item.odd)">{{ item.odd }}</span>
                </span>
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
                    :class="{ 'balls-row-selected': isBallSelected(`${colIdx}_${n - 1}`) }"
                    @click="toggleBallSelect(`${colIdx}_${n - 1}`)"
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
                        @focus="ensureBallSelected(`${colIdx}_${n - 1}`)"
                      />
                    </div>
                  </div>

                  <!-- Two-side rows: 大 小 单 双, odds 1.9776 -->
                  <div
                    v-for="label in ['大', '小', '单', '双']"
                    :key="label"
                    class="balls-row"
                    :class="{ 'balls-row-selected': isBallSelected(`${colIdx}_${label}`) }"
                    @click="toggleBallSelect(`${colIdx}_${label}`)"
                  >
                    <div class="balls-label-cell">{{ label }}</div>
                    <div class="balls-odd-cell"><b class="text-red">1.9776</b></div>
                    <div class="balls-input-cell">
                      <input
                        v-model="ballAmounts[`${colIdx}_${label}`]"
                        class="balls-cell-input"
                        type="text"
                        @focus="ensureBallSelected(`${colIdx}_${label}`)"
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

            <!-- Layout: height varies by active tab; sum=79.89px, size/parity=148.73px -->
            <div class="summary-road mt10" :class="`summary-road--${activeSummaryKey}`">
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
          </div>

          <!-- 公告列表（点击"更多"显示, only in game view） -->
          <div v-else key="notice-list-view" class="notice-list-panel">
            <!-- 标签页菜单 -->
            <div class="notice-tabs">
              <div
                v-for="item in noticeTabItems"
                :key="item.key"
                :class="['notice-tab', { active: item.key === activeNoticeTab }]"
                @click="onNoticeTabClick(item.key)"
              >
                {{ item.label }}
              </div>
            </div>
            <!-- 公告列表 -->
            <div class="notice-list-body">
              <div
                v-for="notice in currentNoticeList"
                :key="notice.id"
                :class="['notice-row', { highlight: notice.isHighlight }]"
              >
                <div class="notice-time-col">{{ notice.time }}</div>
                <div class="notice-content-col">{{ notice.content }}</div>
              </div>
              <div v-if="currentNoticeList.length === 0" class="notice-empty">
                暂无公告
              </div>
            </div>
          </div>
        </div>
        <!-- 右侧：公告 + 长龙排行（公告列表显示时或开奖结果页面时隐藏） -->
        <div v-if="!showNoticeList && activeContentView === 'game'" class="right-sidebar">
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

    <!-- Bottom marquee bar (shown for game and draw results views) -->
    <div v-if="activeContentView === 'game' || activeContentView === 'drawResults'" class="footer-bar">
      <div class="marquee-box">
        <div class="marquee-content">
          <b class="footer-marquee-text">尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)</b>
        </div>
      </div>
      <div class="footer-more" @click="showNoticeList = !showNoticeList">{{ showNoticeList ? '返回' : '更多' }}</div>
    </div>

    <!-- 最近开奖弹窗（无蒙层，支持拖动） -->
    <div
      v-if="showRecentDialog"
      ref="recentDialogRef"
      class="recent-dialog"
      :style="recentDialogStyle"
    >
      <div class="recent-dialog-title" @mousedown="onRecentTitleMouseDown">
        <h4 class="recent-dialog-title-text">
          加拿大pc28
          <span class="recent-dialog-drag-tip">弹窗可拖动</span>
        </h4>
        <img
          class="recent-dialog-close cursor-pointer"
          :src="recentCloseIcon"
          alt="关闭"
          @click="closeRecentDialog"
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
                      @click="recentTab = 'number'"
                    >
                      号码
                    </button>
                    <button
                      class="recent-switch-btn"
                      :class="{ active: recentTab === 'size' }"
                      @click="recentTab = 'size'"
                    >
                      大小
                    </button>
                    <button
                      class="recent-switch-btn"
                      :class="{ active: recentTab === 'parity' }"
                      @click="recentTab = 'parity'"
                    >
                      单双
                    </button>
                    <button
                      class="recent-switch-btn"
                      :class="{ active: recentTab === 'misc' }"
                      @click="recentTab = 'misc'"
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
                    <div class="recent-misc-item">{{ row.size }}</div>
                    <div class="recent-misc-item">{{ row.parity }}</div>
                    <div class="recent-misc-item">{{ row.sizeParity }}</div>
                    <div class="recent-misc-item">{{ row.misc }}</div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
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
  /* Prevent horizontal overflow from causing visible background color bars on the right side */
  overflow-x: hidden;
}

/* ==================== Main Container ==================== */
/* Original: w-92% m-auto bg-[#fff] mt5 b-b-none */
/* No flex:1, white area only takes content height */
.main-wrapper {
  width: 92%;
  /* Shift entire panel 67px to the left */
  margin: 5px auto 0;
  position: relative;
  left: -67px;
  background: #fff;
  border-bottom: none;
}

/* Draw results mode: expand to fill screen width with 7px gap to scrollbar */
.main-wrapper--draw-results {
  width: calc(100% - 7px);
  left: 0;
  margin-left: 0;
  margin-right: 7px;
  box-sizing: border-box;
}

/* 三栏 flex 布局 —— 不强制 min-height，高度由内容决定 */
.main-body {
  display: flex;
  align-items: flex-start;
  width: 100%;
}

/* ==================== 中间内容区域 ==================== */
.center-content {
  flex: 0 0 720px;
  width: 720px;
  min-height: 500px; /* 中间内容区合理最小高度，后续有真实内容后可去除 */
  border-left: none;
  border-right: none;
  /* PC28 表格背景色 - 继承主题变量 (--pc28-cell-bg) */
}

/* Draw results mode: center-content expands to fill available space */
/* Using compound selector for higher specificity to override base .center-content styles */
.center-content.center-content--wide {
  flex: 1 1 0 !important;
  width: auto !important;
  min-width: 0;
  max-width: none;
}

/* Draw results view container - ensure full width inheritance */
.draw-results-view {
  display: block;
  width: 100%;
  box-sizing: border-box;
}

.placeholder {
  padding: 24px;
  color: #999;
  font-size: 14px;
  text-align: center;
}

.game-panel {
  width: 720px;
  /* Do not set a fixed height: content is taller than legacy estimates and would overflow onto the footer. */
  min-height: 733px;
  margin: 5px 0 30px;
  position: relative;
  overflow: hidden;
  /* Extra paint containment: clips descendants that still paint outside overflow:hidden in some engines */
  contain: paint;
}

.issue-bar {
  width: 720px;
  height: 56px;
  padding: 5px 20px;
  border: 1px solid var(--bw-border-color, #efba84);
  border-bottom: none;
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

.issue-left--bottom,
.issue-right--bottom {
  /* Match screenshot: move the "期 + 盘面" label and countdown line up a bit. */
  position: relative;
  top: -7px;
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

.ml50 {
  margin-left: 50px;
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
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
  border-bottom: none;
  background: var(--pc28-cell-bg, #f2eae0);
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
  border: 1px solid var(--bw-border-color, #efba84);
  height: 25px;
  line-height: 25px;
  color: #ff0000;
}

.amount-input {
  width: 55px;
  height: 24px;
  border: 1px solid #a0b4d8;
  border-radius: 0.5px;
  box-sizing: border-box;
}

.amount-input:focus,
.amount-input:focus-visible {
  outline: none;
  box-shadow: none;
  /* Match .cell-input focus: thin border only (no heavy browser focus ring). */
  border-color: #000;
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
  min-width: 72px;
  height: 20px;
  padding: 0 10px;
  line-height: 20px;
  border-radius: 2px;
  font-size: 13px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #ff9c00, #ff5100);
}

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

.section-title {
  width: 720px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  font-size: 14px;
  font-weight: 400;
  color: #000;
  border: 1px solid var(--bw-border-color, #efba84);
  border-bottom: none;
  background: var(--pc28-cell-bg, #f2eae0);
  margin: 0;
  box-sizing: border-box;
}

.two-side-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid var(--bw-border-color, #efba84);
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--pc28-cell-bg, #f2eae0);
}

.color-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid var(--bw-border-color, #efba84);
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--pc28-cell-bg, #f2eae0);
}

.sum-grid {
  width: 720px;
  display: flex;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.sum-col {
  width: 25%;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.sum-col:last-child {
  border-right: none;
}

.sum-head {
  display: flex;
  height: 30px;
  line-height: 30px;
  background: var(--pc28-cell-bg, #f2eae0);
  border-bottom: 1px solid var(--bw-border-color, #efba84);
}

.sum-head-cell {
  flex: 1;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--pc28-cell-bg, #f2eae0);
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
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  background: var(--pc28-cell-bg, #f2eae0);
}

.sum-row {
  cursor: pointer;
}

.sum-col .sum-row:last-child {
  border-bottom: none;
}

.sum-row:hover .sum-cell,
.sum-row:focus-within .sum-cell {
  background: var(--bw-header-color, #be9d76);
}

.sum-row-selected .sum-cell,
.sum-row-selected:hover .sum-cell,
.sum-row-selected:focus-within .sum-cell {
  background: #ffc214;
}

.sum-cell {
  flex: 1;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--pc28-cell-bg, #f2eae0);
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

.sum-odd-text {
  display: inline-block;
  text-align: center;
  /* Slightly bold for readability (avoid default <b> heavy bold). */
  font-weight: 500;
  font-size: 13px;
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
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.two-side-row {
  display: flex;
  height: 30px;
  line-height: 30px;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
}

.two-side-row:last-child {
  border-bottom: none;
}

.two-side-item {
  width: 20%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  cursor: pointer;
}

.two-side-item:hover,
.two-side-item:focus-within {
  background: var(--bw-header-color, #be9d76);
}

.two-side-item:last-child {
  border-right: none;
}

.two-side-item .label {
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  color: #000;
  border-right: 1px solid var(--bw-border-color, #efba84);
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
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  /* Odds digits: slightly bold, but not heavy like <b> default. */
  font-weight: 500;
}

.two-side-odd-text {
  display: inline-block;
  text-align: center;
}

.two-side-item .input-box {
  width: 56px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.color-grid,
.pattern-grid {
  width: 720px;
  display: flex;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.color-item {
  width: 33.33%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  cursor: pointer;
}

.color-item:hover,
.color-item:focus-within {
  background: var(--bw-header-color, #be9d76);
}

.color-item:last-child {
  border-right: none;
}

.color-item .label {
  width: 60px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
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
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  /* Odds digits: slightly bold, but not heavy like <b> default. */
  font-weight: 500;
}

.color-odd-text {
  display: inline-block;
  height: 30px;
  line-height: 30px;
  text-align: center;
}

.color-item .input-box {
  width: 89px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.pattern-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid var(--bw-border-color, #efba84);
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-form-item-label-bg-color, #fff1e4);
}

.pattern-item {
  width: 20%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
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
  background: var(--bw-header-color, #be9d76);
}

.pattern-item:last-child {
  border-right: none;
}

.pattern-item .label {
  width: 32px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  font-weight: 700;
  font-size: 12px;
  box-sizing: border-box;
}

.pattern-item .odd {
  width: 56px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  /* Odds digits: slightly bold, but not heavy like <b> default. */
  font-weight: 500;
}

.pattern-odd-text {
  display: inline-block;
  height: 30px;
  line-height: 30px;
  text-align: center;
}

.pattern-item .input-box {
  width: 55px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.pattern-item .cell-input {
  width: 50px;
  height: 20px;
}

/* Spacing above road block (matches design class div.mt10 in screenshots) */
.mt10 {
  margin-top: 10px;
}

/* Summary road: height varies by active tab; tab=30px, body adapts to content */
.summary-road {
  --summary-tab-height: 30px;
  /* Default for size/parity: 30px tabs + 110.73px body; total ~148.73px with border */
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
  /* CRITICAL: clip overflow to prevent content from bleeding into footer */
  overflow: hidden;
  contain: layout paint;
}

/* Sum tab: 30px tabs + 49.89px body */
.summary-road--sum {
  --summary-road-height: 79.89px;
  --summary-body-height: 49.89px;
}

/* Size tab: 30px tabs + 110.73px body */
.summary-road--size {
  --summary-road-height: 148.73px;
  --summary-body-height: 110.73px;
}

/* Parity tab: 30px tabs + 110.73px body */
.summary-road--parity {
  --summary-road-height: 148.73px;
  --summary-body-height: 110.73px;
}

.summary-bar {
  flex: 0 0 var(--summary-tab-height);
  width: 720px;
  height: var(--summary-tab-height);
  line-height: var(--summary-tab-height);
  display: flex;
  align-items: stretch;
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
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
  text-align: center;
  color: #000;
  border-right-width: 1px;
  border-right-style: solid;
  border-color: var(--el-border-color, var(--bw-border-color, #efba84));
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
}

/* Road body: adapts to active tab height via CSS variable */
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
  background: var(--pc28-cell-bg, #f2eae0);
  /* Force clip all overflow */
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
  /* Explicit height prevents grid content from expanding beyond container */
  height: 100%;
  display: grid;
  grid-template-columns: repeat(30, minmax(0, 1fr));
  grid-template-rows: minmax(0, 1fr);
  box-sizing: border-box;
  overflow: hidden;
  contain: strict;
}

/* Each mode adapts to shell height; content auto-fits within the container */
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

.summary-value {
  display: block;
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
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
  min-height: 0;
  max-height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  overflow: clip;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

/* Road cells: drop pb10 so padding cannot expand past the fixed shell height */
.summary-cell-inner--road {
  padding-bottom: 2px;
  padding-top: 0;
}

.summary-cell-inner--road .pt5 {
  padding-top: 0;
}

.bg-primary5 {
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
}

.uno-b-r {
  border-right-width: 1px;
  border-right-style: solid;
  border-color: var(--el-border-color, var(--bw-border-color, #efba84));
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
  padding: 2px 0;
  flex: 1 1 auto;
  min-height: 0;
  max-height: 100%;
  overflow: hidden;
  /* Ensure text clips within cell */
  overflow: clip;
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
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-linear-bg, linear-gradient(to bottom, #a6744d 0%, #351c0c 100%));
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
  border: 1px solid var(--bw-border-color, #efba84);
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
  background: var(--bw-linear-bg, linear-gradient(to bottom, #a6744d 0%, #351c0c 100%));
  border-bottom: 1px solid var(--bw-border-color, #efba84);
}

/* 长龙列表 */
.dragon-list {
  padding: 0;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: none;
}

.dragon-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 26px;
  line-height: 26px;
  font-size: 13px;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
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
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-form-item-label-bg-color, #fff1e4);
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
  position: relative;
  z-index: 100;
  width: 100%;
  height: 34px;
  margin: 0 auto;
  margin-top: auto;
  line-height: 30px;
  background-color: #fdfdfd;
  border-top: 1px solid var(--el-border-color, #EFBA84);
  overflow: hidden;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}

.marquee-box {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  position: relative;
}

.marquee-content {
  display: inline-block;
  white-space: nowrap;
  line-height: 34px;
  padding-left: 100%;
  animation: marquee-scroll 35s linear infinite;
}

@keyframes marquee-scroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-100%);
  }
}

.footer-marquee-text {
  color: var(--bw-default-color, #351c0c);
  font-size: 13px;
  font-weight: bolder;
  font-family: "Microsoft YaHei", Tahoma, "HelveticaNeue-Light", "Helvetica Neue Light", "Helvetica Neue", Helvetica, Arial, sans-serif;
}

.footer-more {
  flex-shrink: 0;
  padding: 0 15px;
  color: #ff0000;
  font-size: 13px;
  cursor: pointer;
}

.footer-more:hover {
  text-decoration: underline;
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
  border: 1px solid var(--bw-border-color, #efba84);
  color: #c00;
}

.bet-tab--display {
  cursor: default;
  user-select: none;
}

.bet-tab--display.bet-tab-active {
  /* Display-only: use design blue font, remove background/border highlight. */
  color: #00f !important;
  background: transparent !important;
  border: none !important;
}

/* ==================== 1-3球 面板 ==================== */
.balls-panel {
  width: 720px;
}

.balls-grid {
  width: 720px;
  display: flex;
  border: 1px solid var(--bw-border-color, #efba84);
  background: var(--pc28-cell-bg, #f2eae0);
  box-sizing: border-box;
}

/* Each column: 1/3 width */
.balls-col {
  flex: 0 0 33.333%;
  width: 33.333%;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.balls-col:last-child {
  border-right: none;
}

/* Match the requested style: light beige headers and body, no dark brown. */
.balls-col-header {
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-weight: bold;
  font-size: 13px;
  color: #000;
  background: var(--bw-table-header-bg-color, linear-gradient(to bottom, #fff 0%, #fff1e4 100%));
}

/* Each data row */
.balls-row {
  display: flex;
  height: 30px;
  line-height: 30px;
  border-top: 1px solid var(--bw-border-color, #efba84);
  cursor: pointer;
  background: var(--pc28-cell-bg, #f2eae0);
  box-sizing: border-box;
}

.balls-row:hover .balls-ball-cell,
.balls-row:hover .balls-odd-cell,
.balls-row:hover .balls-input-cell,
.balls-row:hover .balls-label-cell {
  background: var(--bw-header-color, #be9d76);
}

/* 1-3球选中状态 - 黄色背景 */
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

/* Ball image cell (60px) */
.balls-ball-cell {
  width: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--pc28-cell-bg, #f2eae0);
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
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--pc28-cell-bg, #f2eae0);
  box-sizing: border-box;
}

.balls-odd-cell b {
  /* Odds digits: slightly bold, but not heavy like <b> default. */
  font-weight: 500;
}

/* Amount input cell (flex-1) */
.balls-input-cell {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--pc28-cell-bg, #f2eae0);
  box-sizing: border-box;
}

/* Amount text input */
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

/* Two-side label cell (大/小/单/双) with warm background */
.balls-label-cell {
  width: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  font-weight: bold;
  background: var(--pc28-cell-bg, #f2eae0);
  box-sizing: border-box;
}

/* ─── 内嵌公告列表样式 ─────────────────────────────────────────────────────── */
.notice-list-panel {
  width: 600px;
  min-height: 247px;
  background: #fff;
  border: 1px solid var(--bw-border-color, #EFBA84);
  display: flex;
  flex-direction: column;
}

.notice-tabs {
  display: flex;
  border-bottom: 1px solid var(--bw-border-color, #EFBA84);
  background: var(--bw-bg-3, #fff7ef);
}

.notice-tab {
  flex: 1;
  text-align: center;
  padding: 6px 0;
  font-size: 13px;
  color: #000;
  cursor: pointer;
  border-right: 1px solid var(--bw-border-color, #EFBA84);
  transition: all 0.2s;
  user-select: none;
  height: 30px;
  line-height: 18px;
  box-sizing: border-box;
}

.notice-tab:last-child {
  border-right: none;
}

.notice-tab:hover {
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
}

.notice-tab.active {
  color: red;
  font-weight: bold;
  background: var(--bw-bg-4, linear-gradient(to bottom, #fdeadb 0%, #f4c7a9 100%));
}

.notice-list-body {
  flex: 1;
  overflow-y: auto;
}

.notice-row {
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid var(--bw-border-color, #EFBA84);
  line-height: 1.5;
}

.notice-row:last-child {
  border-bottom: none;
}

.notice-row.highlight {
  font-weight: bold;
  color: red;
}

.notice-time-col {
  flex-shrink: 0;
  width: 150px;
  padding: 8px 6px;
  text-align: center;
  background: var(--bw-form-item-label-bg-color, #fff1e4);
  border-right: 1px solid var(--bw-border-color, #EFBA84);
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-content-col {
  flex: 1;
  width: 80%;
  padding: 8px;
  font-size: 13px;
  word-break: break-all;
  display: flex;
  align-items: center;
  line-height: 1.5;
}

.notice-empty {
  padding: 40px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

/* 滚动条 */
.notice-list-body::-webkit-scrollbar {
  width: 6px;
}

.notice-list-body::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.notice-list-body::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.notice-list-body::-webkit-scrollbar-thumb:hover {
  background: #999;
}

/* ============ 系统封盘遮罩层（每天 06:00-07:00） ============ */
.system-closed-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
}

.system-closed-bg {
  width: 500px;
  max-width: 90%;
  opacity: 0.9;
}

/* 确保 game-panel 有相对定位以便遮罩层定位 */
.game-panel {
  position: relative;
}
</style>
