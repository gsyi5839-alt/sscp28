/**
 * Lottery calculation utilities
 * Contains functions for dragon/tiger, three-pattern, and bull game calculations
 */

/**
 * Calculate dragon/tiger result from lottery numbers
 * Compare first and last number
 * @param numbers - array of lottery numbers (at least 5 numbers)
 * @returns '龙' if first > last, '虎' if first < last, '和' if equal
 */
export function calcDragonTiger(numbers: number[]): '龙' | '虎' | '和' {
  if (!numbers || numbers.length < 5) return '和'
  
  const first = numbers[0]!
  const last = numbers[4]!
  
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
 * @returns pattern type: '豹子' | '顺子' | '对子' | '半顺' | '杂六'
 */
export function calcThreePattern(threeNums: number[]): '豹子' | '顺子' | '对子' | '半顺' | '杂六' {
  if (!threeNums || threeNums.length !== 3) return '杂六'
  
  const [a, b, c] = threeNums
  
  // 豹子: all three numbers are the same (e.g., 3,3,3)
  if (a === b && b === c) {
    return '豹子'
  }
  
  // 顺子: three consecutive numbers (e.g., 3,4,5 or 5,4,3, including wrap-around 8,9,0)
  if (isStraight(threeNums)) {
    return '顺子'
  }
  
  // 对子: exactly two numbers are the same (e.g., 3,3,5)
  if (a === b || b === c || a === c) {
    return '对子'
  }
  
  // 半顺: any two numbers are consecutive (including 0,9 wrap-around)
  if (hasConsecutivePair(threeNums)) {
    return '半顺'
  }
  
  // 杂六: none of the above patterns
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
  
  // Bull result names mapping
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
      return ''             // Default black for tiger
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
