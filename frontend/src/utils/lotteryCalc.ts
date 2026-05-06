/**
 * Lottery calculation utilities
 * Contains functions for dragon/tiger, three-pattern, and bull game calculations
 */

/**
 * Calculate dragon/tiger result from lottery numbers
 * Compare first and last number (supports any array length >= 2)
 * @param numbers - array of lottery numbers (at least 2 numbers)
 * @returns 'Dragon' if first > last, 'Tiger' if first < last, 'Tie' if equal
 */
export function calcDragonTiger(numbers: number[]): '龙' | '虎' | '和' {
  if (!numbers || numbers.length < 2) return '和'
  
  const first = numbers[0]!
  const last = numbers[numbers.length - 1]!  // Use last element, not hardcoded [4]
  
  if (first > last) return '龙'
  if (first < last) return '虎'
  return '和'
}

/**
 * Check if three numbers form a straight (sequential numbers)
 * Handles wrap-around: 8,9,0 and 9,0,1 are valid straights
 * @param nums - array of exactly 3 numbers
 * @returns true if numbers form a straight
 */
function isStraight(nums: number[]): boolean {
  if (nums.length !== 3) return false
  
  const sorted = [...nums].sort((a, b) => a - b)
  
  // Normal straight: consecutive numbers (e.g., 3,4,5)
  if (sorted[2]! - sorted[1]! === 1 && sorted[1]! - sorted[0]! === 1) {
    return true
  }
  
  // Wrap-around straights: 0,1,9 or 0,8,9
  if (sorted[0]! === 0 && sorted[1]! === 1 && sorted[2]! === 9) {
    return true
  }
  if (sorted[0]! === 0 && sorted[1]! === 8 && sorted[2]! === 9) {
    return true
  }
  
  return false
}

/**
 * Check if any two numbers in array are consecutive
 * Handles wrap-around: 0 and 9 are consecutive
 * @param nums - array of numbers
 * @returns true if any two numbers are consecutive
 */
function hasConsecutivePair(nums: number[]): boolean {
  if (nums.length < 2) return false
  
  for (let i = 0; i < nums.length; i++) {
    for (let j = i + 1; j < nums.length; j++) {
      const diff = Math.abs(nums[i]! - nums[j]!)
      // Normal consecutive: difference is 1
      // Wrap-around: 0 and 9 (difference is 9)
      if (diff === 1 || diff === 9) {
        return true
      }
    }
  }
  return false
}

/**
 * Calculate three-number pattern type
 * @param threeNums - array of exactly 3 numbers
 * @returns pattern type: 'Leopard' | 'Straight' | 'Pair' | 'Half-Straight' | 'Misc'
 */
export function calcThreePattern(threeNums: number[]): '豹子' | '顺子' | '对子' | '半顺' | '杂六' {
  if (!threeNums || threeNums.length !== 3) return '杂六'
  
  const [a, b, c] = threeNums
  
  // Leopard: all three numbers are the same (e.g., 3,3,3)
  if (a === b && b === c) {
    return '豹子'
  }
  
  // Straight: three consecutive numbers (e.g., 3,4,5 or 5,4,3, including wrap-around 8,9,0)
  if (isStraight(threeNums)) {
    return '顺子'
  }
  
  // Pair: exactly two numbers are the same (e.g., 3,3,5)
  if (a === b || b === c || a === c) {
    return '对子'
  }
  
  // Half-Straight: any two numbers are consecutive (including 0,9 wrap-around)
  if (hasConsecutivePair(threeNums)) {
    return '半顺'
  }
  
  // Misc: none of the above patterns
  return '杂六'
}

/**
 * Calculate bull/cow game result from 5 numbers
 * Find any 3 numbers whose sum is multiple of 10, then calculate remaining 2 numbers
 * @param numbers - array of exactly 5 numbers
 * @returns '牛牛' | '牛一'~'牛九' | '无牛'
 */
export function calcBullResult(numbers: number[]): string {
  if (!numbers || numbers.length !== 5) return '无牛'
  
  // Bull result names mapping (NiuNiu, Niu1-Niu9, No Bull)
  const bullNames = ['牛牛', '牛一', '牛二', '牛三', '牛四', '牛五', '牛六', '牛七', '牛八', '牛九']
  
  // Try all combinations of 3 numbers from 5
  // C(5,3) = 10 combinations
  for (let i = 0; i < 5; i++) {
    for (let j = i + 1; j < 5; j++) {
      for (let k = j + 1; k < 5; k++) {
        const sum3 = numbers[i]! + numbers[j]! + numbers[k]!
        
        // Check if sum of 3 numbers is multiple of 10
        if (sum3 % 10 === 0) {
          // Find remaining 2 numbers
          const remaining: number[] = []
          for (let m = 0; m < 5; m++) {
            if (m !== i && m !== j && m !== k) {
              remaining.push(numbers[m]!)
            }
          }
          
          // Calculate sum of remaining 2 numbers, get last digit
          const sum2 = remaining[0]! + remaining[1]!
          const lastDigit = sum2 % 10
          
          // Return bull result
          return bullNames[lastDigit] ?? '无牛'
        }
      }
    }
  }
  
  // No valid combination found
  return '无牛'
}

export type BullPokerResult = '高牌' | '一对' | '二对' | '三条' | '顺子' | '葫芦' | '四条' | '五条'

/**
 * Check whether five lottery digits form a circular straight.
 * Supports regular sequences and digit wrap-around such as 8,9,0,1,2.
 * @param numbers - array of exactly 5 numbers
 * @returns true when the numbers form a five-digit straight
 */
function isFiveNumberStraight(numbers: number[]): boolean {
  const uniqueNums = [...new Set(numbers)]
  if (uniqueNums.length !== 5) return false

  const numSet = new Set(uniqueNums)
  for (let start = 0; start <= 9; start++) {
    let matched = true
    for (let offset = 0; offset < 5; offset++) {
      if (!numSet.has((start + offset) % 10)) {
        matched = false
        break
      }
    }
    if (matched) return true
  }

  return false
}

/**
 * Calculate the poker-style bull category from 5 numbers.
 * @param numbers - array of exactly 5 numbers
 * @returns poker category for the NiuNiu/Suoha statistics panel
 */
export function calcBullPokerResult(numbers: number[]): BullPokerResult {
  if (!numbers || numbers.length !== 5) return '高牌'

  const counts = new Map<number, number>()
  numbers.forEach(num => {
    counts.set(num, (counts.get(num) ?? 0) + 1)
  })

  const countValues = [...counts.values()].sort((a, b) => b - a)
  const pairCount = countValues.filter(count => count === 2).length

  if (countValues[0] === 5) return '五条'
  if (countValues[0] === 4) return '四条'
  if (countValues[0] === 3 && countValues[1] === 2) return '葫芦'
  if (isFiveNumberStraight(numbers)) return '顺子'
  if (countValues[0] === 3) return '三条'
  if (pairCount === 2) return '二对'
  if (pairCount === 1) return '一对'
  return '高牌'
}

/**
 * Get color class for dragon/tiger result
 * @param result - dragon/tiger result string
 * @returns CSS color class string
 */
export function getDragonTigerColor(result: string): string {
  switch (result) {
    case '龙':
      return 'color-red'    // Red for dragon
    case '和':
      return 'color-green'  // Green for tie
    case '虎':
    default:
      return ''             // Default color (black) for tiger
  }
}

/**
 * Parse lottery code string to number array
 * @param code - comma-separated lottery code (e.g., "9,7,4,5,9")
 * @returns array of numbers
 */
export function parseNumbers(code: string): number[] {
  if (!code) return []
  return code
    .split(',')
    .map(item => parseInt(item.trim(), 10))
    .filter(num => !isNaN(num))
}

/**
 * Calculate dragon/tiger pairs for 10-ball racing games (Australian Lucky 10)
 * Compares positions: 1 vs 10, 2 vs 9, 3 vs 8, 4 vs 7, 5 vs 6
 * @param numbers - array of 10 lottery numbers
 * @returns array of 5 results: '龙' (dragon) if front > back, '虎' (tiger) if front < back, '和' (tie) if equal
 */
export function calcDragonTigerPairs(numbers: number[]): string[] {
  if (!numbers || numbers.length < 10) {
    return ['和', '和', '和', '和', '和']
  }
  
  // Comparison pairs: [front position index, back position index]
  // Position 1 vs 10, 2 vs 9, 3 vs 8, 4 vs 7, 5 vs 6
  const pairs: [number, number][] = [
    [0, 9],  // Position 1 vs Position 10
    [1, 8],  // Position 2 vs Position 9
    [2, 7],  // Position 3 vs Position 8
    [3, 6],  // Position 4 vs Position 7
    [4, 5],  // Position 5 vs Position 6
  ]
  
  return pairs.map(([frontIdx, backIdx]) => {
    const front = numbers[frontIdx]!
    const back = numbers[backIdx]!
    if (front > back) return '龙'
    if (front < back) return '虎'
    return '和'
  })
}

/**
 * Calculate BaoDou (宝斗) result for racing games
 * Based on champion-runner sum (冠亚合) value range
 * Rule: sum in range 9-12 = '入' (in), otherwise = '出' (out)
 * @param numbers - array of lottery numbers (at least 2)
 * @returns '入' or '出'
 */
export function calcBaoDou(numbers: number[]): string {
  if (!numbers || numbers.length < 2) return '出'
  
  // Calculate champion (1st) + runner-up (2nd) sum
  const sum = numbers[0]! + numbers[1]!
  
  // Sum in range 9-12 is 'In', otherwise 'Out'
  return (sum >= 9 && sum <= 12) ? '入' : '出'
}

/**
 * Calculate NiuNiu (牛牛) results for 10-ball racing games using sliding window
 * Returns 6 results for: 庄(Banker), 闲一(Player1), 闲二(Player2), 闲三(Player3), 闲四(Player4), 闲五(Player5)
 * Window positions: balls[0..4], balls[1..5], balls[2..6], balls[3..7], balls[4..8], balls[5..9]
 * @param numbers - array of 10 lottery numbers
 * @returns array of 6 bull results
 */
export function calcNiuNiu10(numbers: number[]): string[] {
  if (!numbers || numbers.length < 10) {
    return ['无牛', '无牛', '无牛', '无牛', '无牛', '无牛']
  }
  
  // Sliding window starting positions for 6 groups
  // Banker: [0-4], Player1: [1-5], Player2: [2-6], Player3: [3-7], Player4: [4-8], Player5: [5-9]
  const windowStarts = [0, 1, 2, 3, 4, 5]
  
  return windowStarts.map(start => {
    // Extract 5 balls for current window
    const fiveBalls = numbers.slice(start, start + 5)
    // Use existing calcBullResult function for 5-ball bull calculation
    return calcBullResult(fiveBalls)
  })
}
