/**
 * Summary road (统计路单) composable
 * Calculates sum/size/parity statistics for display
 */
import { ref, computed } from 'vue'
import { SUMMARY_CELL_COUNT, type SummaryKey, summaryTabs } from '../constants/odds'

export function useSummaryRoad(
  historyIssues: { value: any[] },
  getIssueSum: (issue: any) => number | null,
  gameCategory?: { value: string }
) {
  const activeSummaryKey = ref<SummaryKey>('sum')

  const padToCellCount = (values: any[]) => {
    if (values.length >= SUMMARY_CELL_COUNT) {
      return values.slice(0, SUMMARY_CELL_COUNT)
    }
    return [...values, ...Array(SUMMARY_CELL_COUNT - values.length).fill('')]
  }

  const toRoadColumns = (labels: string[], align: 'left' | 'right' = 'right') => {
    if (!labels.length) return Array(SUMMARY_CELL_COUNT).fill([])
    const columns: string[][] = []
    let current = labels[0]
    let column: string[] = [current]

    for (let i = 1; i < labels.length; i++) {
      const label = labels[i]
      if (label && label === current) {
        column.push(label)
        continue
      }
      if (current) columns.push(column)
      current = label
      column = [current]
    }

    if (current) columns.push(column)
    if (columns.length >= SUMMARY_CELL_COUNT) return columns.slice(columns.length - SUMMARY_CELL_COUNT)
    const emptyColumns = Array(SUMMARY_CELL_COUNT - columns.length).fill([])
    return align === 'left' ? [...columns, ...emptyColumns] : [...emptyColumns, ...columns]
  }

  const toLatestRoadColumns = (labels: string[]) => {
    if (!labels.length) return []
    const columns: string[][] = []

    for (const label of labels) {
      if (!label) continue
      const currentColumn = columns[columns.length - 1]
      if (currentColumn?.[0] === label) {
        currentColumn.push(label)
        continue
      }
      columns.push([label])
    }

    return columns.slice(0, SUMMARY_CELL_COUNT).reverse()
  }

  const parseRacingBalls = (issue: any): number[] => {
    const code = issue?.preDrawCode
    const raw = Array.isArray(code) ? code : String(code ?? '').split(',')
    return raw.map((item: any) => Number(item)).filter((num: number) => Number.isFinite(num))
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

  const summaryNumbers = computed(() => {
    const isRacing = gameCategory?.value === 'racing'
    const issues = isRacing ? [...historyIssues.value].reverse() : historyIssues.value.slice(0, SUMMARY_CELL_COUNT)
    const nums = issues.map((issue: any) => {
      const sum = getIssueSum(issue)
      return sum == null ? '' : String(sum)
    })
    if (isRacing) return toRoadColumns(nums)
    return padToCellCount(nums.reverse())
  })

  const summarySize = computed(() => {
    const category = gameCategory?.value ?? 'pc28'
    const isPC28 = category === 'pc28'
    const isRacing = category === 'racing'
    const issues = isRacing ? [...historyIssues.value].reverse() : historyIssues.value.slice(0, SUMMARY_CELL_COUNT)
    const labels = issues.map((issue: any) => {
      const label = issue?.sizeLabel
      if (label === '大' || label === '小' || label === '和') return label
      const sum = getIssueSum(issue)
      if (sum == null) return ''
      // PC28: 13/14 = 和, >=15 = 大, <=12 = 小; Racing top-two sum: >=12 = 大; SSC: >=23 = 大.
      if (isPC28 && (sum === 13 || sum === 14)) return '和'
      if (isRacing) return sum >= 12 ? '大' : '小'
      return isPC28 ? (sum >= 15 ? '大' : '小') : (sum >= 23 ? '大' : '小')
    })
    if (isRacing) return toRoadColumns(labels)
    return padToCellCount(labels.reverse())
  })

  const summaryParity = computed(() => {
    const isPC28 = !gameCategory || gameCategory.value === 'pc28'
    const isRacing = gameCategory?.value === 'racing'
    const issues = isRacing ? [...historyIssues.value].reverse() : historyIssues.value.slice(0, SUMMARY_CELL_COUNT)
    const labels = issues.map((issue: any) => {
      const label = issue?.parityLabel
      if (label === '单' || label === '双' || label === '和') return label
      const sum = getIssueSum(issue)
      if (sum == null || sum === 27) return ''
      // PC28: 13/14 = 和; otherwise even = 双, odd = 单
      if (isPC28 && (sum === 13 || sum === 14)) return '和'
      return sum % 2 === 0 ? '双' : '单'
    })
    if (isRacing) return toRoadColumns(labels)
    return padToCellCount(labels.reverse())
  })

  const summaryBaoDou = computed(() => {
    const isRacing = gameCategory?.value === 'racing'
    if (!isRacing) return []
    const issues = historyIssues.value.slice()
    const labels = issues.map((issue: any) => {
      const balls = parseRacingBalls(issue)
      return balls.length >= 10 ? calcRacingBaoDou(balls) : ''
    })
    return toLatestRoadColumns(labels)
  })

  const activeSummaryValues = computed(() => {
    if (activeSummaryKey.value === 'baoDou') return summaryBaoDou.value
    if (activeSummaryKey.value === 'size') return summarySize.value
    if (activeSummaryKey.value === 'parity') return summaryParity.value
    return summaryNumbers.value
  })

  const onSummaryTabClick = (key: SummaryKey) => {
    activeSummaryKey.value = key
  }

  return {
    summaryTabs,
    activeSummaryKey,
    activeSummaryValues,
    onSummaryTabClick,
  }
}
