/**
 * Betting state management composable
 * Handles selection states for all betting types
 */
import { ref } from 'vue'

export type QuickMode = 'quick' | 'normal'

export function useBetting() {
  const quickMode = ref<QuickMode>('normal')

  // Selection states for quick mode
  const selectedSumNums = ref<Set<number>>(new Set())
  const selectedTwoSideKeys = ref<Set<string>>(new Set())
  const selectedColorKeys = ref<Set<string>>(new Set())
  const selectedPatternKeys = ref<Set<string>>(new Set())
  const selectedBallKeys = ref<Set<string>>(new Set())

  // Active states for normal mode
  const activeSumNum = ref<number | null>(null)
  const activeTwoSideKey = ref<string | null>(null)
  const activeColorKey = ref<string | null>(null)
  const activePatternKey = ref<string | null>(null)
  const activeBallKey = ref<string | null>(null)

  // Amount inputs
  const sumAmounts = ref<Record<number, string>>({})
  const twoSideAmounts = ref<Record<string, string>>({})
  const colorAmounts = ref<Record<string, string>>({})
  const patternAmounts = ref<Record<string, string>>({})
  const ballAmounts = ref<Record<string, string>>({})

  const setQuickMode = (mode: QuickMode) => {
    quickMode.value = mode
  }

  // Sum value selection
  const toggleSumSelect = (num: number) => {
    if (quickMode.value === 'quick') {
      const next = new Set(selectedSumNums.value)
      if (next.has(num)) next.delete(num)
      else next.add(num)
      selectedSumNums.value = next
      return
    }
    activeSumNum.value = activeSumNum.value === num ? null : num
  }

  const ensureSumSelected = (num: number) => {
    activeSumNum.value = num
  }

  const isSumSelected = (num: number) => {
    if (quickMode.value === 'quick') return selectedSumNums.value.has(num)
    const amount = sumAmounts.value[num]
    return (amount && amount.trim() !== '') || activeSumNum.value === num
  }

  // Two-side selection
  const toggleTwoSideSelect = (key: string) => {
    if (quickMode.value === 'quick') {
      const next = new Set(selectedTwoSideKeys.value)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      selectedTwoSideKeys.value = next
      return
    }
    activeTwoSideKey.value = activeTwoSideKey.value === key ? null : key
  }

  const ensureTwoSideSelected = (key: string) => {
    activeTwoSideKey.value = key
  }

  const isTwoSideSelected = (key: string) => {
    if (quickMode.value === 'quick') return selectedTwoSideKeys.value.has(key)
    const amount = twoSideAmounts.value[key]
    return (amount && amount.trim() !== '') || activeTwoSideKey.value === key
  }

  // Color selection
  const toggleColorSelect = (key: string) => {
    if (quickMode.value === 'quick') {
      const next = new Set(selectedColorKeys.value)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      selectedColorKeys.value = next
      return
    }
    activeColorKey.value = activeColorKey.value === key ? null : key
  }

  const ensureColorSelected = (key: string) => {
    activeColorKey.value = key
  }

  const isColorSelected = (key: string) => {
    if (quickMode.value === 'quick') return selectedColorKeys.value.has(key)
    const amount = colorAmounts.value[key]
    return (amount && amount.trim() !== '') || activeColorKey.value === key
  }

  // Pattern selection
  const togglePatternSelect = (key: string) => {
    if (quickMode.value === 'quick') {
      const next = new Set(selectedPatternKeys.value)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      selectedPatternKeys.value = next
      return
    }
    activePatternKey.value = activePatternKey.value === key ? null : key
  }

  const ensurePatternSelected = (key: string) => {
    activePatternKey.value = key
  }

  const isPatternSelected = (key: string) => {
    if (quickMode.value === 'quick') return selectedPatternKeys.value.has(key)
    const amount = patternAmounts.value[key]
    return (amount && amount.trim() !== '') || activePatternKey.value === key
  }

  // Ball (1-3 balls) selection
  const toggleBallSelect = (key: string) => {
    if (quickMode.value === 'quick') {
      const next = new Set(selectedBallKeys.value)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      selectedBallKeys.value = next
      return
    }
    activeBallKey.value = activeBallKey.value === key ? null : key
  }

  const ensureBallSelected = (key: string) => {
    activeBallKey.value = key
  }

  const isBallSelected = (key: string) => {
    if (quickMode.value === 'quick') return selectedBallKeys.value.has(key)
    const amount = ballAmounts.value[key]
    return (amount && amount.trim() !== '') || activeBallKey.value === key
  }

  return {
    quickMode,
    sumAmounts,
    twoSideAmounts,
    colorAmounts,
    patternAmounts,
    ballAmounts,
    setQuickMode,
    toggleSumSelect,
    ensureSumSelected,
    isSumSelected,
    toggleTwoSideSelect,
    ensureTwoSideSelected,
    isTwoSideSelected,
    toggleColorSelect,
    ensureColorSelected,
    isColorSelected,
    togglePatternSelect,
    ensurePatternSelected,
    isPatternSelected,
    toggleBallSelect,
    ensureBallSelected,
    isBallSelected,
  }
}
