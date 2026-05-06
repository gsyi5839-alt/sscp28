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
  /** Whether this cell should display as stopped betting */
  stopped?: boolean
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

/**
 * NiuNiu betting panel items.
 * The odds are taken from the reference design screenshot.
 */
export const SSC_NIUNIU_ROW1: SscBetItem[] = [
  { label: '牛牛', odd: '15.3' },
  { label: '牛九', odd: '15.4' },
  { label: '牛八', odd: '15.19' },
  { label: '牛七', odd: '15.4' },
  { label: '牛六', odd: '15.19' },
  { label: '牛五', odd: '15.4' },
  { label: '牛四', odd: '15.19' },
  { label: '牛三', odd: '15.4' },
  { label: '牛二', odd: '15.19' },
  { label: '牛一', odd: '15.4' },
  { label: '无牛', odd: '2.73' },
  { label: '', odd: '--', disabled: true },
]

export const SSC_NIUNIU_TWO_SIDE: SscBetItem[] = [
  { label: '牛单', odd: '3.08' },
  { label: '牛双', odd: '3.01' },
  { label: '牛大', odd: '3.04' },
  { label: '牛小', odd: '3.05' },
]

export const SSC_NIUNIU_SOHA: SscBetItem[] = [
  { label: '高牌', odd: '--', stopped: true },
  { label: '一对', odd: '--', stopped: true },
  { label: '二对', odd: '--', stopped: true },
  { label: '三条', odd: '--', stopped: true },
  { label: '顺子', odd: '--', stopped: true },
  { label: '葫芦', odd: '--', stopped: true },
  { label: '四条', odd: '--', stopped: true },
  { label: '五条', odd: '--', stopped: true },
]

export const SSC_NIUNIU_STATS_BULL = ['牛牛', '牛九', '牛八', '牛七', '牛六', '牛五', '牛四', '牛三', '牛二', '牛一', '无牛'] as const
export const SSC_NIUNIU_STATS_POKER = ['高牌', '一对', '二对', '三条', '顺子', '葫芦', '四条', '五条'] as const
