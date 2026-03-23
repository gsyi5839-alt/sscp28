/**
 * SSC (ShiShiCai) two-side betting options configuration.
 * Defines the betting structure for 5-ball games like 加拿大时时彩.
 */

// Betting cell item interface
export interface SscBetItem {
  /** Display label (e.g., '大', '小', '总大', '龙') */
  label: string
  /** Odds value displayed to user */
  odd: string
  /** Whether this cell is disabled (e.g., empty placeholder cell) */
  disabled?: boolean
}

/**
 * Per-ball betting options (大/小/单/双) - same for all 5 balls.
 * Each ball row has 4 cells in a single horizontal line.
 */
export const SSC_BALL_ITEMS: SscBetItem[] = [
  { label: '大', odd: '1.7776' },
  { label: '小', odd: '1.7776' },
  { label: '单', odd: '1.7776' },
  { label: '双', odd: '1.7776' },
]

/** Ball section headers for 5 balls */
export const SSC_BALL_TITLES = ['第一球', '第二球', '第三球', '第四球', '第五球']

/**
 * Sum section row 1: 总大/总小/总单/总双.
 * All odds are the same (1.7776).
 */
export const SSC_SUM_ROW1: SscBetItem[] = [
  { label: '总大', odd: '1.7776' },
  { label: '总小', odd: '1.7776' },
  { label: '总单', odd: '1.7776' },
  { label: '总双', odd: '1.7776' },
]

/**
 * Sum section row 2: 龙/虎/和/placeholder.
 * '和' has a different odd (8.8), last cell is disabled placeholder.
 */
export const SSC_SUM_ROW2: SscBetItem[] = [
  { label: '龙', odd: '1.7776' },
  { label: '虎', odd: '1.7776' },
  { label: '和', odd: '8.8' },
  { label: '', odd: '--', disabled: true },
]
