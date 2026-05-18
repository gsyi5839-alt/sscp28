/**
 * Game sub-navigation configuration.
 * Maps game types to their third-level navigation items.
 * Different game categories have different betting panels.
 */

// Sub-navigation item interface
export interface SubNavItem {
  /** Unique key identifying the sub-nav tab */
  key: string
  /** Display label in Chinese */
  label: string
}

/**
 * Game type categories - each category shares the same sub-navigation layout.
 * - pc28: 28-type games (3 balls, sum 0-27)
 * - ssc: ShiShiCai-type games (5 balls)
 * - racing: Racing-type games (10 balls)
 */
export type GameCategory = 'pc28' | 'ssc' | 'racing'

/**
 * PC28-type sub-navigation items (e.g., 加拿大pc28).
 * Panels: 两面盘 (big/small/odd/even), 1-3球 (individual balls)
 */
export const PC28_SUB_NAV: SubNavItem[] = [
  { key: 'twoSide', label: '两面盘' },
  { key: 'balls', label: '1-3球' },
]

/**
 * SSC-type sub-navigation items (e.g., 加拿大时时彩, 欢乐时时彩).
 * Panels: 两面盘, 1-5球, 第一球~第五球, 斗牛
 */
export const SSC_SUB_NAV: SubNavItem[] = [
  { key: 'twoSide', label: '两面盘' },
  { key: 'balls', label: '1-5球' },
  { key: 'ball1', label: '第一球' },
  { key: 'ball2', label: '第二球' },
  { key: 'ball3', label: '第三球' },
  { key: 'ball4', label: '第四球' },
  { key: 'ball5', label: '第五球' },
  { key: 'niuNiu', label: '斗牛' },
]

/**
 * Racing-type sub-navigation items (e.g., 澳洲幸运10, 欢乐赛车).
 * Panels: 两面盘, 1-10名, 冠亚军组合, 宝斗, 牛牛
 */
export const RACING_SUB_NAV: SubNavItem[] = [
  { key: 'twoSide', label: '两面盘' },
  { key: 'positions', label: '1-10名' },
  { key: 'topTwo', label: '冠亚军组合' },
  { key: 'baoDou', label: '宝斗' },
  { key: 'racingNiuNiu', label: '牛牛' },
]

/**
 * Map game keys to their game category.
 * Used to determine which sub-navigation to display.
 */
export const GAME_CATEGORY_MAP: Record<string, GameCategory> = {
  // PC28 type
  caPc28: 'pc28',

  // SSC (ShiShiCai) type - 5 balls
  caSsc: 'ssc',
  happySsc: 'ssc',
  speedSsc: 'ssc',
  aus5: 'ssc',
  lottery5: 'ssc',

  // Racing type - 10 balls
  aus10: 'racing',
  happyRacing: 'racing',
  luckyPlane: 'racing',
  speedRacing: 'racing',
  lucky168: 'racing',
  lottery10: 'racing',
}

/**
 * Map game category to its sub-navigation items.
 */
const CATEGORY_SUB_NAV_MAP: Record<GameCategory, SubNavItem[]> = {
  pc28: PC28_SUB_NAV,
  ssc: SSC_SUB_NAV,
  racing: RACING_SUB_NAV,
}

/**
 * Get the sub-navigation items for a given game key.
 * Falls back to PC28 sub-nav if game key is not recognized.
 *
 * @param gameKey the active game key (e.g., 'caPc28', 'caSsc')
 * @returns array of sub-navigation items for the game
 */
export const getSubNavForGame = (gameKey: string): SubNavItem[] => {
  const category = GAME_CATEGORY_MAP[gameKey] ?? 'pc28'
  return CATEGORY_SUB_NAV_MAP[category]
}

/**
 * Get the game category for a given game key.
 *
 * @param gameKey the active game key
 * @returns the game category
 */
export const getGameCategory = (gameKey: string): GameCategory => {
  return GAME_CATEGORY_MAP[gameKey] ?? 'pc28'
}
