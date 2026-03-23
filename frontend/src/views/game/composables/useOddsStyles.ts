/**
 * Odds text styling helpers
 * Based on design.md measurements
 */

// Sum odds text style
export const sumOddTextStyle = (odd: string) => {
  const v = String(odd ?? '')
  if (!v) return {}
  let width = '16.05px'
  if (v.includes('.')) width = '27.77px'
  else if (v.length >= 3) width = '24.06px'
  return { width, height: '30px', lineHeight: '30px' }
}

// Two-side odds text style
export const twoSideOddTextStyle = (odd: string) => {
  const v = String(odd ?? '')
  if (!v) return {}
  const width = v === '4.3' ? '19.75px' : '27.77px'
  return { width, height: '30px', lineHeight: '30px' }
}

// Color wave odds text style
export const colorOddTextStyle = (odd: string) => {
  const v = String(odd ?? '')
  if (!v) return {}
  const width = v === '3' ? '8.03px' : '27.77px'
  return { width, height: '30px', lineHeight: '30px' }
}

// Pattern odds text style
export const patternOddTextStyle = (odd: string) => {
  const v = String(odd ?? '')
  if (!v) return {}
  let width = '27.77px'
  if (v === '65' || v === '12') width = '16.05px'
  else if (v === '2.6' || v === '2.4') width = '19.75px'
  return { width, height: '30px', lineHeight: '30px' }
}

// Get ball image source (PC28: ball_cols_split, 0-27)
export const getBallSrc = (num: number) => {
  const safe = Math.max(0, Math.min(27, num))
  const name = String(safe).padStart(2, '0')
  return new URL(`../../../assets/游戏/ball_cols_split/ball_${name}.png`, import.meta.url).href
}

// Get blue ball image source (SSC: ball_blue_split, 0-9, 26x26)
export const getBlueBallSrc = (num: number) => {
  const safe = Math.max(0, Math.min(9, num))
  return new URL(`../../../assets/游戏/ball_blue_split/ball_${safe}.png`, import.meta.url).href
}
