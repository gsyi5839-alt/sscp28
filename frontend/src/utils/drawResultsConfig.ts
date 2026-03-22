/**
 * Configuration constants for DrawResults component
 * Extracted to keep DrawResults.vue under 500 lines
 */

// TypeScript interface for game option
export interface GameOption {
  lotCode: number
  lotName: string
  lotType?: number | null
  lotLabel?: number | null
  sort?: number | null
}

// Fixed game display order for draw results page
export const DRAW_RESULTS_GAME_ORDER = [
  '加拿大pc28',
  '加拿大时时彩',
  '澳洲幸运10',
  '澳洲幸运5',
  '欢乐赛车',
  '欢乐时时彩',
  '幸运飞艇',
  '极速赛车',
  '极速时时彩',
  '168幸运飞艇',
  '体彩乐透5',   // Sports lottery 5 (lotCode=766)
  '体彩乐透10',  // Sports lottery 10 (lotCode=767)
]

// Fallback game options when API fails
export const FALLBACK_GAME_OPTIONS: GameOption[] = [
  { lotCode: 720, lotName: '加拿大pc28' },
  { lotCode: 719, lotName: '加拿大时时彩' },
  { lotCode: 797, lotName: '澳洲幸运10' },
  { lotCode: 795, lotName: '澳洲幸运5' },
  { lotCode: 763, lotName: '欢乐赛车' },
  { lotCode: 762, lotName: '欢乐时时彩' },
  { lotCode: 765, lotName: '幸运飞艇' },
  { lotCode: 768, lotName: '极速赛车' },
  { lotCode: 769, lotName: '极速时时彩' },
  { lotCode: 726, lotName: '168幸运飞艇' },
  { lotCode: 766, lotName: '体彩乐透5' },   // Sports lottery 5
  { lotCode: 767, lotName: '体彩乐透10' },  // Sports lottery 10
]

// SSC (ShiShiCai) type codes: 5 balls, show all extended columns (dragon/tiger, front3/mid3/back3, bull)
export const SSC_LOT_CODES = [719, 795, 762, 769, 766]  // Canada SSC, AU Lucky 5, Happy SSC, Speed SSC, Sports Lotto 5

// Racing type codes: 10 balls, show dragon/tiger column only
export const RACING_LOT_CODES = [797, 763, 765, 768, 726, 767]  // AU Lucky 10, Happy Racing, Lucky Airship, Speed Racing, 168 Lucky Airship, Sports Lotto 10

// NiuNiu column headers for AU10 (Banker, Player1-5)
export const niuNiuHeaders = ['庄', '闲一', '闲二', '闲三', '闲四', '闲五']
