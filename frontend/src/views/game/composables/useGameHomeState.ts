import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { getGameHomeNoticeDelay } from '@/mobile/config/gameHome'
import { isGameMobileClient } from '@/mobile/utils/client'
import { getGameCategory, getSubNavForGame, type GameCategory } from '@/utils/gameSubNav'
import { DEFAULT_GAME_KEY, GAME_CONFIG_MAP, getGameConfig } from '@/utils/gameConfig'
import { NOTICE_SHOWN_KEY } from '../constants/notices'

type ContentView = 'game' | 'drawResults'
type PanelLoader = () => Promise<unknown>

interface UseGameHomeStateOptions {
  route: RouteLocationNormalizedLoaded
  loadDrawResults: PanelLoader
  loadRecentDrawsDialog: PanelLoader
  loadSscTwoSidePanel: PanelLoader
  loadSscBallPositionPanel: PanelLoader
  loadSscSingleBallPanel: PanelLoader
  loadSscNiuNiuPanel: PanelLoader
  loadSscNiuNiuStats: PanelLoader
  loadNoticeDialog: PanelLoader
  loadBettingBalls: PanelLoader
  loadNoticeSection: PanelLoader
  loadRacingTwoSidePanel: PanelLoader
}

const ACTIVE_GAME_STORAGE_KEY = 'bw-member-active-game-key'
const ACTIVE_BET_TAB_MAP_STORAGE_KEY = 'bw-member-active-bet-tab-map'
const TAB_LOADING_VISIBLE_MS = 200
const SSC_THIRD_LEVEL_TAB_KEYS = new Set([
  'twoSide',
  'balls',
  'ball1',
  'ball2',
  'ball3',
  'ball4',
  'ball5',
  'niuNiu',
])

const isValidBetTabForGame = (gameKey: string, tab: string): boolean => (
  getSubNavForGame(gameKey).some(item => item.key === tab)
)

const readBetTabMap = (): Record<string, string> => {
  try {
    const raw = localStorage.getItem(ACTIVE_BET_TAB_MAP_STORAGE_KEY)
    if (!raw) return {}
    const parsed = JSON.parse(raw) as Record<string, string>
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

const writeBetTabMap = (map: Record<string, string>) => {
  localStorage.setItem(ACTIVE_BET_TAB_MAP_STORAGE_KEY, JSON.stringify(map))
}

const getSavedBetTabForGame = (gameKey: string): string | null => {
  const tab = readBetTabMap()[gameKey]
  return tab && isValidBetTabForGame(gameKey, tab) ? tab : null
}

const saveBetTabForGame = (gameKey: string, tab: string) => {
  if (!isValidBetTabForGame(gameKey, tab)) return
  const map = readBetTabMap()
  map[gameKey] = tab
  writeBetTabMap(map)
}

export function useGameHomeState(options: UseGameHomeStateOptions) {
  const isMobileClient = isGameMobileClient()
  const showNoticeDialog = ref(false)
  const showNoticeList = ref(false)
  const activeBetTab = ref<string>('twoSide')
  const activeContentView = ref<ContentView>('game')
  const tabLoading = ref(false)
  const activeGameKey = ref(DEFAULT_GAME_KEY)
  let tabLoadingTimer: ReturnType<typeof setTimeout> | null = null

  const activeLotCode = computed(() => getGameConfig(activeGameKey.value).lotCode)
  const activeGameName = computed(() => getGameConfig(activeGameKey.value).gameName)
  const activeGameCategory = computed(() => getGameCategory(activeGameKey.value))
  const activeBetTabLabel = computed(() => (
    getSubNavForGame(activeGameKey.value).find(item => item.key === activeBetTab.value)?.label ?? '两面盘'
  ))
  const centerContentClasses = computed(() => ({
    'center-content--wide': activeContentView.value === 'drawResults',
  }))
  const mainWrapperClasses = computed(() => ({
    'main-wrapper--draw-results': activeContentView.value === 'drawResults',
  }))

  const clearTabLoadingTimer = () => {
    if (tabLoadingTimer !== null) {
      clearTimeout(tabLoadingTimer)
      tabLoadingTimer = null
    }
  }

  const shouldShowTabLoading = (tab: string) => (
    activeGameCategory.value === 'ssc' && SSC_THIRD_LEVEL_TAB_KEYS.has(tab)
  )

  const preloadCommonPanels = () => {
    void Promise.all([
      options.loadDrawResults(),
      options.loadRecentDrawsDialog(),
      options.loadNoticeSection(),
      options.loadNoticeDialog(),
    ])
  }

  const preloadTabPanelsByCategory = (category: GameCategory) => {
    if (category === 'ssc') {
      void Promise.all([
        options.loadBettingBalls(),
        options.loadSscTwoSidePanel(),
        options.loadSscBallPositionPanel(),
        options.loadSscSingleBallPanel(),
        options.loadSscNiuNiuPanel(),
        options.loadSscNiuNiuStats(),
      ])
      return
    }
    void options.loadBettingBalls()
    if (category === 'racing') {
      void options.loadRacingTwoSidePanel()
    }
  }

  watch(activeBetTab, (tab) => {
    showNoticeList.value = false
    activeContentView.value = 'game'
    saveBetTabForGame(activeGameKey.value, tab)

    clearTabLoadingTimer()
    if (!shouldShowTabLoading(tab)) {
      tabLoading.value = false
      return
    }

    tabLoading.value = true
    tabLoadingTimer = setTimeout(() => {
      tabLoading.value = false
      tabLoadingTimer = null
    }, TAB_LOADING_VISIBLE_MS)
  }, { flush: 'sync' })

  watch(activeGameKey, (gameKey) => {
    activeBetTab.value = getSavedBetTabForGame(gameKey) ?? 'twoSide'
    localStorage.setItem(ACTIVE_GAME_STORAGE_KEY, gameKey)
  })

  watch(activeGameCategory, (category) => {
    preloadCommonPanels()
    preloadTabPanelsByCategory(category)
  }, { immediate: true })

  watch(activeContentView, (view) => {
    if (view === 'drawResults') {
      showNoticeList.value = false
    }
  })

  watch(() => options.route.query.view, (view) => {
    activeContentView.value = view === 'drawResults' ? 'drawResults' : 'game'
    if (view === 'drawResults') {
      showNoticeList.value = false
    }
  }, { immediate: true })

  const handleCloseNotice = () => {
    showNoticeDialog.value = false
    sessionStorage.setItem(NOTICE_SHOWN_KEY, 'true')
  }

  onMounted(() => {
    const savedGameKey = localStorage.getItem(ACTIVE_GAME_STORAGE_KEY)
    if (savedGameKey && Object.prototype.hasOwnProperty.call(GAME_CONFIG_MAP, savedGameKey)) {
      activeGameKey.value = savedGameKey
    }

    document.title = '游戏首页'
    const favicon = document.getElementById('favicon') as HTMLLinkElement
    if (favicon) favicon.href = '/favicon.png'

    const tabFromQuery = options.route.query.tab as string
    if (tabFromQuery && isValidBetTabForGame(activeGameKey.value, tabFromQuery)) {
      activeBetTab.value = tabFromQuery
      saveBetTabForGame(activeGameKey.value, tabFromQuery)
    } else {
      activeBetTab.value = getSavedBetTabForGame(activeGameKey.value) ?? 'twoSide'
    }
    if (options.route.query.view === 'drawResults') {
      activeContentView.value = 'drawResults'
    }

    if (!sessionStorage.getItem(NOTICE_SHOWN_KEY)) {
      const noticeDelayMs = getGameHomeNoticeDelay(isMobileClient)
      setTimeout(() => { showNoticeDialog.value = true }, noticeDelayMs)
    }
  })

  onUnmounted(() => {
    clearTabLoadingTimer()
  })

  return {
    activeBetTab,
    activeBetTabLabel,
    activeContentView,
    activeGameCategory,
    activeGameKey,
    activeGameName,
    activeLotCode,
    centerContentClasses,
    handleCloseNotice,
    mainWrapperClasses,
    showNoticeDialog,
    showNoticeList,
    tabLoading,
  }
}
