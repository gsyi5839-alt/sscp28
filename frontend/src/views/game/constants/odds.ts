/**
 * Odds data and betting configurations
 * All values from design.md
 */

// Sum value odds (0-27)
export const sumOdds = [
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

// Group sum odds into 4 columns (7 items each)
export const sumGroups = Array.from({ length: 4 }, (_, col) =>
  sumOdds.slice(col * 7, col * 7 + 7)
)

// Two-side betting options
export const twoSideRows = [
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

// Color wave options
export const colorRows = [
  { label: '绿波', odd: '3' },
  { label: '蓝波', odd: '3' },
  { label: '红波', odd: '3' },
]

// Pattern options
export const patternRows = [
  { label: '豹子', odd: '65' },
  { label: '顺子', odd: '12' },
  { label: '对子', odd: '2.6' },
  { label: '半顺', odd: '2.05' },
  { label: '杂六', odd: '2.4' },
]

// Summary tabs for road display
export type SummaryKey = 'sum' | 'size' | 'parity' | 'baoDou'
export const summaryTabs: Array<{ key: SummaryKey; label: string }> = [
  { key: 'sum', label: '和值' },
  { key: 'size', label: '和值大小' },
  { key: 'parity', label: '和值单双' },
]

export const racingSummaryTabs: Array<{ key: SummaryKey; label: string }> = [
  { key: 'sum', label: '冠亚军和' },
  { key: 'size', label: '冠亚军和大小' },
  { key: 'parity', label: '冠亚军和单双' },
]

// SSC summary road tabs (road bead pattern with run-length grouping)
export type SscSummaryKey = 'size' | 'parity' | 'dragonTiger'
export const sscSummaryTabs: Array<{ key: SscSummaryKey; label: string }> = [
  { key: 'size', label: '总和大小' },
  { key: 'parity', label: '总和单双' },
  { key: 'dragonTiger', label: '龙虎和' },
]

export const SUMMARY_CELL_COUNT = 30
export const HISTORY_LIST_SIZE = 200

// Dragon definitions for leaderboard
export const dragonDefs = [
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
