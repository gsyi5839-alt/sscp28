/**
 * Recent draws dialog composable
 * Manages dialog state, position, and data
 */
import { ref, computed } from 'vue'

export type RecentTab = 'number' | 'size' | 'parity' | 'misc'

export interface RecentDialogRow {
  issue: string
  time: string
  balls: number[]
  sum: number
  size: '大' | '小' | '和'
  parity: '单' | '双' | '和'
  sizeParity: string
  misc: '豹子' | '顺子' | '对子' | '半顺' | '杂六'
}

const RECENT_DIALOG_WIDTH = 520
const RECENT_DIALOG_HEIGHT = 338

export function useRecentDraws(
  historyIssues: { value: any[] },
  parseBalls: (code: string | null | undefined) => number[] | null,
  getIssueSum: (issue: any) => number | null,
  gameCategory?: { value: string }
) {
  const showRecentDialog = ref(false)
  const recentTab = ref<RecentTab>('number')
  const recentDialogRef = ref<HTMLDivElement | null>(null)
  const recentDialogPos = ref({ left: 0, top: 0 })

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
      .slice(0, 200)
      .map((issue: any) => {
        const balls = parseBalls(issue?.preDrawCode)
        if (!balls) return null
        const sum = getIssueSum(issue)
        if (sum == null) return null
        // PC28: sum 13/14 = 和, >=15 = 大, <=12 = 小; SSC: sum >= 23 = 大, <=22 = 小
        const isPC28 = !gameCategory || gameCategory.value === 'pc28'
        const size: '大' | '小' | '和' = isPC28 && (sum === 13 || sum === 14) ? '和' : (isPC28 ? (sum >= 15 ? '大' : '小') : (sum >= 23 ? '大' : '小'))
        const parity: '单' | '双' | '和' = isPC28 && (sum === 13 || sum === 14) ? '和' : (sum % 2 === 0 ? '双' : '单')
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

  const openRecentDialog = (dismissNotice?: () => void) => {
    if (dismissNotice) dismissNotice()
    recentTab.value = 'number'
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
