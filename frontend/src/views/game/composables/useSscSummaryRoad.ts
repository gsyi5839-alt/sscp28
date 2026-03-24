/**
 * SSC Summary Road (路珠) composable
 * Uses road bead pattern: consecutive same results grouped into columns.
 * Tabs: 总和大小, 总和单双, 龙虎和
 */
import { ref, computed } from 'vue'
import { SUMMARY_CELL_COUNT, type SscSummaryKey, sscSummaryTabs } from '../constants/odds'

/**
 * Encode flat labels into road bead runs.
 * e.g. ['大','大','小','大','大','大'] → [['大','大'], ['小'], ['大','大','大']]
 * Then pad/trim to SUMMARY_CELL_COUNT (30) columns.
 */
function toRoadBeads(labels: string[]): string[][] {
  if (!labels.length) return Array(SUMMARY_CELL_COUNT).fill([])
  const runs: string[][] = []
  let current = labels[0]
  let run: string[] = [current]
  for (let i = 1; i < labels.length; i++) {
    if (labels[i] === current && current !== '') {
      run.push(labels[i])
    } else {
      if (current !== '') runs.push(run)
      current = labels[i]
      run = [current]
    }
  }
  if (current !== '') runs.push(run)
  // Pad to 30 columns or take last 30
  if (runs.length >= SUMMARY_CELL_COUNT) {
    return runs.slice(runs.length - SUMMARY_CELL_COUNT)
  }
  const padded: string[][] = [...runs]
  while (padded.length < SUMMARY_CELL_COUNT) {
    padded.unshift([])
  }
  return padded
}

/**
 * Parse all 5 balls from SSC preDrawCode string.
 * @returns array of 5 numbers, or null if invalid
 */
function parseSscBalls(code: string | null | undefined): number[] | null {
  if (!code) return null
  const nums = code.split(',').map(Number).filter(n => !isNaN(n))
  if (nums.length < 5) return null
  return nums.slice(0, 5)
}

export function useSscSummaryRoad(
  historyIssues: { value: any[] },
  getIssueSum: (issue: any) => number | null
) {
  const activeSscSummaryKey = ref<SscSummaryKey>('size')

  // Compute size labels: 大 if sum >= 23, 小 if sum <= 22
  const sizeLabels = computed(() => {
    // Process oldest-first for correct road ordering
    const issues = [...historyIssues.value].reverse()
    return issues.map((issue: any) => {
      const sum = getIssueSum(issue)
      if (sum == null) return ''
      return sum >= 23 ? '大' : '小'
    })
  })

  // Compute parity labels: 单 if odd, 双 if even
  const parityLabels = computed(() => {
    const issues = [...historyIssues.value].reverse()
    return issues.map((issue: any) => {
      const sum = getIssueSum(issue)
      if (sum == null) return ''
      return sum % 2 === 1 ? '单' : '双'
    })
  })

  // Compute dragon/tiger labels: 龙(ball1>ball5), 虎(ball1<ball5), 和(ball1==ball5)
  const dragonTigerLabels = computed(() => {
    const issues = [...historyIssues.value].reverse()
    return issues.map((issue: any) => {
      const balls = parseSscBalls(issue?.preDrawCode)
      if (!balls) return ''
      const b1 = balls[0]
      const b5 = balls[4]
      if (b1 > b5) return '龙'
      if (b1 < b5) return '虎'
      return '和'
    })
  })

  // Road bead columns for each tab
  const sizeBeads = computed(() => toRoadBeads(sizeLabels.value))
  const parityBeads = computed(() => toRoadBeads(parityLabels.value))
  const dragonTigerBeads = computed(() => toRoadBeads(dragonTigerLabels.value))

  const activeSscSummaryValues = computed(() => {
    if (activeSscSummaryKey.value === 'parity') return parityBeads.value
    if (activeSscSummaryKey.value === 'dragonTiger') return dragonTigerBeads.value
    return sizeBeads.value
  })

  const onSscSummaryTabClick = (key: SscSummaryKey) => {
    activeSscSummaryKey.value = key
  }

  return {
    sscSummaryTabs,
    activeSscSummaryKey,
    activeSscSummaryValues,
    onSscSummaryTabClick,
  }
}
