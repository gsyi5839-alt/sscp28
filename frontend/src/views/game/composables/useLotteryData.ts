/**
 * Lottery data management composable
 * Handles fetching lottery info, countdown timers, and history data
 */
import { ref, computed, onMounted, onUnmounted, watch, type Ref } from 'vue'
import { lotteryApi } from '../../../api/index'
import { HISTORY_LIST_SIZE } from '../constants/odds'

/**
 * Lottery data management composable.
 * Accepts a reactive lotCode ref to support dynamic game switching.
 *
 * @param lotCodeRef reactive ref holding the current game's lottery code
 */
export function useLotteryData(lotCodeRef: Ref<number>) {
  const isMobileClient = /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent)

  // Previous draw data
  const preDrawIssue = ref('--')
  const preDrawBalls = ref<number[]>([0, 0, 0])
  const preDrawSum = ref(0)

  // Current draw data
  const drawIssue = ref('--')
  const sealCountdown = ref('--:--:--')
  const drawCountdown = ref('--:--:--')
  const isDrawing = ref(false)

  // System closed status (local time daily 06:00-07:00)
  const currentTime = ref(new Date())
  const isSystemClosed = computed(() => {
    const now = currentTime.value
    const minutesOfDay = now.getHours() * 60 + now.getMinutes()
    return minutesOfDay >= 6 * 60 && minutesOfDay < 7 * 60
  })

  // History data
  const historyIssues = ref<any[]>([])
  const historyNums = ref<number[]>([])

  // Mobile-first polling strategy: keep data fresh while reducing network pressure.
  const INFO_POLL_INTERVAL = isMobileClient ? 6000 : 4000
  const HISTORY_POLL_INTERVAL = isMobileClient ? 12000 : 5000
  const FULL_HISTORY_POLL_INTERVAL = 60000
  const FAST_HISTORY_LIST_SIZE = isMobileClient ? 80 : 120

  // Internal state
  let countdownTimer: ReturnType<typeof setInterval> | null = null
  let infoPollTimer: ReturnType<typeof setInterval> | null = null
  let historyPollTimer: ReturnType<typeof setInterval> | null = null
  let fullHistoryPollTimer: ReturnType<typeof setInterval> | null = null
  let fullHistoryBootstrapTimer: ReturnType<typeof setTimeout> | null = null
  let isFetching = false
  let drawTimestamp = 0
  let sealTimestamp = 0
  let currentDrawIssue = ''
  let currentPreDrawIssue = ''

  // Check if betting is sealed.
  // Only treat as sealed in the real window: [sealTimestamp, drawTimestamp).
  // This avoids "stuck sealed overlay" after draw time has passed.
  const isSealed = computed(() => {
    const now = currentTime.value.getTime()
    if (sealTimestamp <= 0 || drawTimestamp <= 0) return false
    return now >= sealTimestamp && now < drawTimestamp
  })

  // Helper functions
  const parseTimestamp = (value: unknown): number => {
    if (value == null) return 0

    if (typeof value === 'number') {
      if (!Number.isFinite(value)) return 0
      // Accept both milliseconds and seconds epoch values.
      return value > 1e12 ? value : value * 1000
    }

    if (typeof value !== 'string') return 0
    const trimmed = value.trim()
    if (!trimmed) return 0

    // Accept numeric timestamp string.
    if (/^\d+$/.test(trimmed)) {
      const numeric = Number(trimmed)
      if (!Number.isFinite(numeric)) return 0
      return numeric > 1e12 ? numeric : numeric * 1000
    }

    const normalized = trimmed.includes('T') ? trimmed : trimmed.replace(' ', 'T')
    const hasTimezone = /([zZ]|[+-]\d{2}:\d{2})$/.test(normalized)
    const candidate = hasTimezone ? normalized : `${normalized}+08:00`
    const ts = Date.parse(candidate)
    return Number.isNaN(ts) ? 0 : ts
  }

  const fmtCountdown = (diffMs: number): string => {
    if (diffMs <= 0) return '00:00:00'
    const totalSec = Math.floor(diffMs / 1000)
    const h = Math.floor(totalSec / 3600)
    const m = Math.floor((totalSec % 3600) / 60)
    const s = totalSec % 60
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${pad(h)}:${pad(m)}:${pad(s)}`
  }

  // Fetch lottery info from API
  const fetchLotteryInfo = async () => {
    if (isFetching) return
    isFetching = true
    try {
      const res: any = await lotteryApi.getInfo(lotCodeRef.value)
      if (res?.code === 200 && res?.data) {
        const d = res.data
        const newIssue = d.drawIssue || ''

        // Previous draw result
        const newPreDrawIssue = d.preDrawIssue || ''
        preDrawIssue.value = newPreDrawIssue || '--'
        if (d.preDrawCode) {
          preDrawBalls.value = d.preDrawCode.split(',').map(Number)
          preDrawSum.value = preDrawBalls.value.reduce((a: number, b: number) => a + b, 0)
        }

        // Current draw period
        drawIssue.value = newIssue
        drawTimestamp = parseTimestamp(d.drawTime)
        sealTimestamp = drawTimestamp - 10 * 1000

        // New period detection
        if (newIssue && newIssue !== currentDrawIssue) {
          currentDrawIssue = newIssue
          isDrawing.value = false
          fetchHistoryList()
        }

        // New result detection
        if (newPreDrawIssue && newPreDrawIssue !== currentPreDrawIssue) {
          currentPreDrawIssue = newPreDrawIssue
          fetchHistoryList()
        }
      }
    } catch (_e) {
      // fail silently
    } finally {
      isFetching = false
    }
  }

  // Fetch history list
  const fetchHistoryList = async (size = FAST_HISTORY_LIST_SIZE) => {
    try {
      const res: any = await lotteryApi.getList(lotCodeRef.value, 1, size)
      if (res?.code === 200 && res?.data?.list) {
        const rawList = res.data.list || []
        const sortedList = [...rawList].sort((a: any, b: any) => {
          const ai = Number(a?.preDrawIssue)
          const bi = Number(b?.preDrawIssue)
          if (Number.isFinite(ai) && Number.isFinite(bi)) return bi - ai
          return String(b?.preDrawIssue || '').localeCompare(String(a?.preDrawIssue || ''))
        })
        historyIssues.value = sortedList
        historyNums.value = sortedList.map((item: any) => Number(item.sumValue)).filter((n: number) => !isNaN(n))
      }
    } catch (_e) {
      // fail silently
    }
  }

  // Countdown ticker (runs every 1s for smooth UI countdown)
  const tickCountdown = () => {
    const now = Date.now()
    const drawDiff = drawTimestamp - now
    const sealDiff = sealTimestamp - now

    currentTime.value = new Date()

    if (drawDiff <= 0 && drawTimestamp > 0) {
      // Draw time reached: show loading animation
      isDrawing.value = true
      drawCountdown.value = '00:00:00'
      sealCountdown.value = '00:00:00'
    } else {
      isDrawing.value = false
      drawCountdown.value = fmtCountdown(drawDiff)
      sealCountdown.value = fmtCountdown(sealDiff)
    }
  }

  // Parse balls from code string
  const parseBalls = (code: string | null | undefined): number[] | null => {
    if (!code) return null
    const nums = code.split(',').map(Number).filter(n => !isNaN(n))
    if (nums.length < 3) return null
    return nums.slice(0, 3)
  }

  // Get issue data helper
  const getIssueData = (issue: any) => {
    const balls = parseBalls(issue?.preDrawCode)
    if (!balls) return null
    const sum = Number(issue?.sumValue)
    const sumValue = Number.isFinite(sum) ? sum : balls.reduce((a, b) => a + b, 0)
    return { balls, sum: sumValue }
  }

  // Get issue sum helper
  const getIssueSum = (issue: any): number | null => {
    const rawSum = Number(issue?.sumValue)
    if (Number.isFinite(rawSum)) return rawSum
    const balls = parseBalls(issue?.preDrawCode)
    if (!balls) return null
    return balls.reduce((a, b) => a + b, 0)
  }

  // Lifecycle
  let isPageVisible = true

  const handleVisibilityChange = () => {
    isPageVisible = !document.hidden
    if (isPageVisible) {
      // Page became visible: fetch immediately and restart polling
      fetchLotteryInfo()
      fetchHistoryList()
      if (!isMobileClient) {
        fetchHistoryList(HISTORY_LIST_SIZE)
      }
      startPolling()
    } else {
      // Page hidden (mobile tab switch / screen off): pause polling to save battery & data
      stopPolling()
    }
  }

  const startPolling = () => {
    stopPolling()
    countdownTimer = setInterval(tickCountdown, 1000)
    infoPollTimer = setInterval(fetchLotteryInfo, INFO_POLL_INTERVAL)
    historyPollTimer = setInterval(fetchHistoryList, HISTORY_POLL_INTERVAL)
    fullHistoryPollTimer = setInterval(() => {
      fetchHistoryList(HISTORY_LIST_SIZE)
    }, FULL_HISTORY_POLL_INTERVAL)
  }

  const stopPolling = () => {
    if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
    if (infoPollTimer) { clearInterval(infoPollTimer); infoPollTimer = null }
    if (historyPollTimer) { clearInterval(historyPollTimer); historyPollTimer = null }
    if (fullHistoryPollTimer) { clearInterval(fullHistoryPollTimer); fullHistoryPollTimer = null }
    if (fullHistoryBootstrapTimer) { clearTimeout(fullHistoryBootstrapTimer); fullHistoryBootstrapTimer = null }
  }

  onMounted(() => {
    fetchLotteryInfo()
    fetchHistoryList()
    if (isMobileClient) {
      fullHistoryBootstrapTimer = setTimeout(() => {
        fetchHistoryList(HISTORY_LIST_SIZE)
      }, 8000)
    } else {
      fetchHistoryList(HISTORY_LIST_SIZE)
    }
    startPolling()
    // Pause polling when page is hidden (mobile screen off / tab switch)
    document.addEventListener('visibilitychange', handleVisibilityChange)
  })

  onUnmounted(() => {
    stopPolling()
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  })

  // Watch for lotCode changes to reload data for the new game
  watch(lotCodeRef, () => {
    // Reset all state for the new game
    preDrawIssue.value = '--'
    preDrawBalls.value = [0, 0, 0]
    preDrawSum.value = 0
    drawIssue.value = '--'
    sealCountdown.value = '--:--:--'
    drawCountdown.value = '--:--:--'
    isDrawing.value = false
    historyIssues.value = []
    historyNums.value = []
    drawTimestamp = 0
    sealTimestamp = 0
    currentDrawIssue = ''
    currentPreDrawIssue = ''
    isFetching = false
    // Fetch data for the new game immediately
    fetchLotteryInfo()
    fetchHistoryList()
    if (isMobileClient) {
      if (fullHistoryBootstrapTimer) {
        clearTimeout(fullHistoryBootstrapTimer)
      }
      fullHistoryBootstrapTimer = setTimeout(() => {
        fetchHistoryList(HISTORY_LIST_SIZE)
      }, 6000)
    } else {
      fetchHistoryList(HISTORY_LIST_SIZE)
    }
  })

  return {
    // State
    preDrawIssue,
    preDrawBalls,
    preDrawSum,
    drawIssue,
    sealCountdown,
    drawCountdown,
    isDrawing,
    isSystemClosed,
    historyIssues,
    historyNums,
    isSealed,
    // Methods
    parseBalls,
    getIssueData,
    getIssueSum,
    fetchLotteryInfo,
    fetchHistoryList,
  }
}
