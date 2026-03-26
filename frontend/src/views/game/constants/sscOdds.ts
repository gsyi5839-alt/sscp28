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

// ─── Single Ball Panel Constants (第一球~第五球 individual panels) ────────────

/**
 * Ball number odds for single-ball panel (0-9).
 * Most balls have odds 9.9, except ball #5 which is 9.88.
 */
export const SSC_SINGLE_BALL_NUMBERS: SscBetItem[] = [
  { label: '0', odd: '9.9' },
  { label: '1', odd: '9.9' },
  { label: '2', odd: '9.9' },
  { label: '3', odd: '9.9' },
  { label: '4', odd: '9.9' },
  { label: '5', odd: '9.88' },
  { label: '6', odd: '9.9' },
  { label: '7', odd: '9.9' },
  { label: '8', odd: '9.9' },
  { label: '9', odd: '9.9' },
]

/**
 * Big/Small/Odd/Even options for single-ball panel + disabled placeholder.
 * 5 columns: 大/小/单/双 + empty cell.
 */
export const SSC_SINGLE_BALL_SIDES: SscBetItem[] = [
  { label: '大', odd: '1.9776' },
  { label: '小', odd: '1.9776' },
  { label: '单', odd: '1.9776' },
  { label: '双', odd: '1.9776' },
  { label: '', odd: '--', disabled: true },
]

/**
 * Sum section row 1 for single-ball panel: 总大/总小/总单/总双.
 * Odds: 1.9776 (differs from two-side panel's 1.7776).
 */
export const SSC_SINGLE_SUM_ROW1: SscBetItem[] = [
  { label: '总大', odd: '1.9776' },
  { label: '总小', odd: '1.9776' },
  { label: '总单', odd: '1.9776' },
  { label: '总双', odd: '1.9776' },
]

/**
 * Sum section row 2 for single-ball panel: 龙/虎/和/placeholder.
 * '和' has odds 9, last cell is disabled.
 */
export const SSC_SINGLE_SUM_ROW2: SscBetItem[] = [
  { label: '龙', odd: '1.9776' },
  { label: '虎', odd: '1.9776' },
  { label: '和', odd: '9' },
  { label: '', odd: '--', disabled: true },
]

/**
 * Pattern section items for 前三/中三/后三.
 * 5 columns: 豹子/顺子/对子/半顺/杂六.
 */
export const SSC_PATTERN_ITEMS: SscBetItem[] = [
  { label: '豹子', odd: '65' },
  { label: '顺子', odd: '12' },
  { label: '对子', odd: '2.6' },
  { label: '半顺', odd: '2.05' },
  { label: '杂六', odd: '2.4' },
]

/** Pattern section titles */
export const SSC_PATTERN_TITLES = ['前三', '中三', '后三']
