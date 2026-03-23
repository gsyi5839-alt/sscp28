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

  const summaryNumbers = computed(() => {
    const nums = historyIssues.value.slice(0, SUMMARY_CELL_COUNT).map((issue: any) => {
      const sum = getIssueSum(issue)
      return sum == null ? '' : String(sum)
    })
    return padToCellCount(nums.reverse())
  })

  const summarySize = computed(() => {
    const isPC28 = !gameCategory || gameCategory.value === 'pc28'
    const labels = historyIssues.value.slice(0, SUMMARY_CELL_COUNT).map((issue: any) => {
      const label = issue?.sizeLabel
      if (label === '大' || label === '小' || label === '和') return label
      const sum = getIssueSum(issue)
      if (sum == null) return ''
      // PC28: 13/14 = 和, >=15 = 大, <=12 = 小; SSC: >=23 = 大, <=22 = 小
      if (isPC28 && (sum === 13 || sum === 14)) return '和'
      return isPC28 ? (sum >= 15 ? '大' : '小') : (sum >= 23 ? '大' : '小')
    })
    return padToCellCount(labels.reverse())
  })

  const summaryParity = computed(() => {
    const isPC28 = !gameCategory || gameCategory.value === 'pc28'
    const labels = historyIssues.value.slice(0, SUMMARY_CELL_COUNT).map((issue: any) => {
      const label = issue?.parityLabel
      if (label === '单' || label === '双' || label === '和') return label
      const sum = getIssueSum(issue)
      if (sum == null || sum === 27) return ''
      // PC28: 13/14 = 和; otherwise even = 双, odd = 单
      if (isPC28 && (sum === 13 || sum === 14)) return '和'
      return sum % 2 === 0 ? '双' : '单'
    })
    return padToCellCount(labels.reverse())
  })

  const activeSummaryValues = computed(() => {
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
