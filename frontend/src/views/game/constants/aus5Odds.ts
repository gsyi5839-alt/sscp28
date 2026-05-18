import type { SscBetItem } from './sscOdds'

/**
 * Australian Lucky 5 two-side odds.
 * Kept separate from SSC odds to avoid changing Canada/Happy/Speed SSC panels.
 */
export const AUS5_BALL_TITLES = ['第一球', '第二球', '第三球', '第四球', '第五球']

export const AUS5_BALL_ITEMS: SscBetItem[] = [
  { label: '大', odd: '1.9806' },
  { label: '小', odd: '1.9806' },
  { label: '单', odd: '1.9806' },
  { label: '双', odd: '1.9806' },
]

export const AUS5_SUM_ROW1: SscBetItem[] = [
  { label: '总大', odd: '1.9806' },
  { label: '总小', odd: '1.9806' },
  { label: '总单', odd: '1.9806' },
  { label: '总双', odd: '1.9806' },
]

export const AUS5_SUM_ROW2: SscBetItem[] = [
  { label: '龙', odd: '1.9806' },
  { label: '虎', odd: '1.9806' },
  { label: '和', odd: '9' },
  { label: '', odd: '--', disabled: true },
]
