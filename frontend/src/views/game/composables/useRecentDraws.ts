/**
 * Recent draws dialog composable
 * Manages dialog state, position, and data
 */
import { ref, computed } from 'vue'
import {
  calcBullResult,
  calcDragonTiger,
  calcDragonTigerPairs,
  calcThreePattern,
} from '@/utils/lotteryCalc'

export type RecentTab = 'number' | 'size' | 'parity' | 'misc'
export type RecentPattern = '豹子' | '顺子' | '对子' | '半顺' | '杂六'

export interface RecentDialogRow {
  issue: string
  time: string
  balls: number[]
  sum: number
  size: '大' | '小' | '和'
  parity: '单' | '双' | '和'
  sizeParity: string
  misc: RecentPattern
  dragonTiger: '龙' | '虎' | '和'
  frontPattern: RecentPattern
  midPattern: RecentPattern
  backPattern: RecentPattern
  bull: string
  racingDragonTigerPairs: string[]
  baoDou: string
}

const RECENT_DIALOG_WIDTH = 520
const RECENT_RACING_DIALOG_WIDTH = 560
const RECENT_DIALOG_HEIGHT = 338

export function useRecentDraws(
  historyIssues: { value: any[] },
  _parseBalls: (code: string | null | undefined) => number[] | null,
  getIssueSum: (issue: any) => number | null,
  gameCategory?: { value: string }
) {
  const showRecentDialog = ref(false)
  const recentTab = ref<RecentTab>('number')
  const recentDialogRef = ref<HTMLDivElement | null>(null)
  const recentDialogPos = ref({ left: 0, top: 0 })

  const getRecentDialogWidth = () => (
    gameCategory?.value === 'racing' ? RECENT_RACING_DIALOG_WIDTH : RECENT_DIALOG_WIDTH
  )

  const clampRecentDialogPos = (left: number, top: number) => {
    const maxLeft = Math.max(0, window.innerWidth - getRecentDialogWidth())
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

  const parseRecentBalls = (code: string | null | undefined): number[] | null => {
    if (!code) return null
    const nums = code.split(',').map(Number).filter(n => !isNaN(n))
    const category = gameCategory?.value ?? 'pc28'
    if (category === 'ssc') return nums.length >= 5 ? nums.slice(0, 5) : null
    if (category === 'racing') return nums.length >= 10 ? nums.slice(0, 10) : null
    return nums.length >= 3 ? nums.slice(0, 3) : null
  }

  const getBallSizeLabels = (row: RecentDialogRow) => {
    const isRacing = gameCategory?.value === 'racing'
    return row.balls.map(ball => ball >= (isRacing ? 6 : 5) ? '大' : '小')
  }

  const getBallParityLabels = (row: RecentDialogRow) => {
    return row.balls.map(ball => ball % 2 === 0 ? '双' : '单')
  }

  const calcRacingBaoDou = (balls: number[]): '龙' | '虎' | '出' | '入' => {
    const labels: Record<number, '龙' | '虎' | '出' | '入'> = {
      1: '入',
      2: '龙',
      3: '出',
      4: '虎',
    }
    let winner = 3
    let winnerIndex = Number.POSITIVE_INFINITY
    for (const ball of [1, 2, 3, 4]) {
      const index = balls.indexOf(ball)
      if (index !== -1 && index < winnerIndex) {
        winner = ball
        winnerIndex = index
      }
    }
    return labels[winner] ?? '出'
  }

  const recentDialogRows = computed<RecentDialogRow[]>(() =>
    historyIssues.value
      .slice(0, 200)
      .map((issue: any) => {
        const balls = parseRecentBalls(issue?.preDrawCode)
        if (!balls) return null
        const sum = getIssueSum(issue)
        if (sum == null) return null
        const category = gameCategory?.value ?? 'pc28'
        const isPC28 = category === 'pc28'
        const isRacing = category === 'racing'
        // PC28: sum 13/14 = 和; Racing top-two sum: >=12 = 大; SSC: sum >= 23 = 大.
        const size: '大' | '小' | '和' = isPC28 && (sum === 13 || sum === 14)
          ? '和'
          : (isPC28 ? (sum >= 15 ? '大' : '小') : (isRacing ? (sum >= 12 ? '大' : '小') : (sum >= 23 ? '大' : '小')))
        const parity: '单' | '双' | '和' = isPC28 && (sum === 13 || sum === 14) ? '和' : (sum % 2 === 0 ? '双' : '单')
        return {
          issue: String(issue?.preDrawIssue ?? '--'),
          time: getIssueTimeLabel(issue?.preDrawTime),
          balls,
          sum,
          size,
          parity,
          sizeParity: `${size}${parity}`,
          misc: calcThreePattern(balls.slice(0, 3)),
          dragonTiger: calcDragonTiger(balls),
          frontPattern: calcThreePattern(balls.slice(0, 3)),
          midPattern: calcThreePattern(balls.slice(1, 4)),
          backPattern: calcThreePattern(balls.slice(2, 5)),
          bull: calcBullResult(balls),
          racingDragonTigerPairs: calcDragonTigerPairs(balls),
          baoDou: isRacing ? calcRacingBaoDou(balls) : '出',
        }
      })
      .filter(Boolean) as RecentDialogRow[]
  )

  const openRecentDialog = (dismissNotice?: () => void) => {
    if (dismissNotice) dismissNotice()
    recentTab.value = gameCategory?.value === 'ssc' ? 'misc' : 'number'
    recentDialogPos.value = getRecentDialogDefaultPos()
    showRecentDialog.value = true
  }

  const clampOnResize = () => {
    if (showRecentDialog.value) {
      recentDialogPos.value = clampRecentDialogPos(recentDialogPos.value.left, recentDialogPos.value.top)
    }
  }

  const closeRecentDialog = () => {
    showRecentDialog.value = false
    stopDrag()
  }

  // Drag handling
  let isDragging = false
  let dragOffsetX = 0
  let dragOffsetY = 0

  const onDragMove = (event: MouseEvent) => {
    if (!isDragging) return
    const nextLeft = event.clientX - dragOffsetX
    const nextTop = event.clientY - dragOffsetY
    recentDialogPos.value = clampRecentDialogPos(nextLeft, nextTop)
  }

  const stopDrag = () => {
    if (!isDragging) return
    isDragging = false
    window.removeEventListener('mousemove', onDragMove)
    window.removeEventListener('mouseup', stopDrag)
  }

  const onTitleMouseDown = (event: MouseEvent) => {
    const target = event.target as HTMLElement
    if (target.closest('.recent-dialog-close')) return
    isDragging = true
    dragOffsetX = event.clientX - recentDialogPos.value.left
    dragOffsetY = event.clientY - recentDialogPos.value.top
    window.addEventListener('mousemove', onDragMove)
    window.addEventListener('mouseup', stopDrag)
    event.preventDefault()
  }

  const getTagColorClass = (label: '大' | '小' | '单' | '双' | '和') =>
    label === '大' || label === '双' ? 'recent-pill--orange' : 'recent-pill--blue'

  return {
    showRecentDialog,
    recentTab,
    recentDialogRef,
    recentDialogStyle,
    recentDialogRows,
    openRecentDialog,
    closeRecentDialog,
    onTitleMouseDown,
    getBallSizeLabels,
    getBallParityLabels,
    getTagColorClass,
    stopDrag,
    clampOnResize,
  }
}
