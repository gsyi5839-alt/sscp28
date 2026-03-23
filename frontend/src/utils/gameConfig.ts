/**
 * Game configuration mapping - maps game keys to lottery codes and display names.
 * Used by GameHeader (tab navigation) and GameHome (data fetching / display).
 */

// Game configuration interface
export interface GameConfig {
  /** Internal game key used in GameHeader navigation */
  key: string
  /** Upstream lottery code for API requests */
  lotCode: number
  /** Chinese display name of the game */
  gameName: string
}

/**
 * Complete game key → lotCode / gameName mapping table.
 * Order matches the game navigation tabs in GameHeader.
 */
export const GAME_CONFIG_MAP: Record<string, GameConfig> = {
  caPc28:       { key: 'caPc28',       lotCode: 720, gameName: '加拿大pc28' },
  caSsc:        { key: 'caSsc',        lotCode: 719, gameName: '加拿大时时彩' },
  aus10:        { key: 'aus10',        lotCode: 797, gameName: '澳洲幸运10' },
  aus5:         { key: 'aus5',         lotCode: 795, gameName: '澳洲幸运5' },
  happyRacing:  { key: 'happyRacing',  lotCode: 763, gameName: '欢乐赛车' },
  happySsc:     { key: 'happySsc',     lotCode: 762, gameName: '欢乐时时彩' },
  luckyPlane:   { key: 'luckyPlane',   lotCode: 765, gameName: '幸运飞艇' },
  speedRacing:  { key: 'speedRacing',  lotCode: 768, gameName: '极速赛车' },
  speedSsc:     { key: 'speedSsc',     lotCode: 769, gameName: '极速时时彩' },
  lucky168:     { key: 'lucky168',     lotCode: 726, gameName: '168幸运飞艇' },
  lottery5:     { key: 'lottery5',     lotCode: 766, gameName: '体彩乐透5' },
  lottery10:    { key: 'lottery10',    lotCode: 767, gameName: '体彩乐透10' },
}

/** Default game key when no game is selected */
export const DEFAULT_GAME_KEY = 'caPc28'

/**
 * Get game config by key, falls back to default (加拿大pc28) if key not found.
 *
 * @param key the game key
 * @returns the game configuration
 */
export const getGameConfig = (key: string): GameConfig => {
  return GAME_CONFIG_MAP[key] ?? GAME_CONFIG_MAP[DEFAULT_GAME_KEY]
}
