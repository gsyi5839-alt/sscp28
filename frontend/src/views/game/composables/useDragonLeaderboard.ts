/**
 * Dragon leaderboard (两面长龙排行) composable
 * Calculates consecutive winning streaks for two-side bets
 */
import { computed } from 'vue'
import { dragonDefs } from '../constants/odds'

export interface DragonItem {
  label: string
  value: string
}

export function useDragonLeaderboard(historyIssues: { value: any[] }) {
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

  const dragonList = computed<DragonItem[]>(() => {
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

  return { dragonList }
}
