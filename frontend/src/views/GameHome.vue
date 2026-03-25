<script setup lang="ts">
import { ref, computed, defineAsyncComponent, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import GameHeader from '@/components/GameHeader.vue'
import MemberSidebar from '@/components/MemberSidebar.vue'

// Async-load heavy child components for faster initial render on mobile
const DrawResults = defineAsyncComponent(loadDrawResults)
const RecentDrawsDialog = defineAsyncComponent(loadRecentDrawsDialog)
const SscTwoSidePanel = defineAsyncComponent(loadSscTwoSidePanel)
const SscBallPositionPanel = defineAsyncComponent(loadSscBallPositionPanel)
const SscSingleBallPanel = defineAsyncComponent(loadSscSingleBallPanel)
const NoticeDialog = defineAsyncComponent(loadNoticeDialog)
const BettingBalls = defineAsyncComponent(loadBettingBalls)
const NoticeSection = defineAsyncComponent(loadNoticeSection)

// Child components (lightweight, loaded synchronously)
import LotteryHeader from './game/components/LotteryHeader.vue'
import QuickBetBar from './game/components/QuickBetBar.vue'
import SummaryRoad from './game/components/SummaryRoad.vue'
import AnnounceSidebar from './game/components/AnnounceSidebar.vue'
import FooterBar from './game/components/FooterBar.vue'

// Composables
import { useLotteryData } from './game/composables/useLotteryData'
import { useBetting } from './game/composables/useBetting'
import { useDragonLeaderboard } from './game/composables/useDragonLeaderboard'
import { useSummaryRoad } from './game/composables/useSummaryRoad'
import { useSscSummaryRoad } from './game/composables/useSscSummaryRoad'
import { useRecentDraws } from './game/composables/useRecentDraws'
import {
  sumGroups,
  twoSideRows,
  colorRows,
  patternRows,
  type SummaryKey,
  type SscSummaryKey,
} from './game/constants/odds'
import { getBallSrc, sumOddTextStyle, twoSideOddTextStyle, colorOddTextStyle, patternOddTextStyle } from './game/composables/useOddsStyles'
import { NOTICE_SHOWN_KEY } from './game/constants/notices'
import { getGameConfig, DEFAULT_GAME_KEY, GAME_CONFIG_MAP } from '@/utils/gameConfig'
import { getSubNavForGame, getGameCategory, type GameCategory } from '@/utils/gameSubNav'

const route = useRoute()
const isMobileClient = /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent)
const ACTIVE_GAME_STORAGE_KEY = 'bw-member-active-game-key'
const ACTIVE_BET_TAB_MAP_STORAGE_KEY = 'bw-member-active-bet-tab-map'

function loadDrawResults() {
  return import('@/views/DrawResults.vue')
}

function loadRecentDrawsDialog() {
  return import('./game/components/RecentDrawsDialog.vue')
}

function loadSscTwoSidePanel() {
  return import('./game/components/ssc/SscTwoSidePanel.vue')
}

function loadSscBallPositionPanel() {
  return import('./game/components/ssc/SscBallPositionPanel.vue')
}

function loadSscSingleBallPanel() {
  return import('./game/components/ssc/SscSingleBallPanel.vue')
}

function loadNoticeDialog() {
  return import('@/components/NoticeDialog.vue')
}

function loadBettingBalls() {
  return import('./game/components/BettingBalls.vue')
}

function loadNoticeSection() {
  return import('./game/components/NoticeSection.vue')
}

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
  if (!tab) return null
  return isValidBetTabForGame(gameKey, tab) ? tab : null
}

const saveBetTabForGame = (gameKey: string, tab: string) => {
  if (!isValidBetTabForGame(gameKey, tab)) return
  const map = readBetTabMap()
  map[gameKey] = tab
  writeBetTabMap(map)
}

// ─── Announcement Dialog ────────────────────────────────────────────────────────────────
const showNoticeDialog = ref(false)
const showNoticeList = ref(false)

const handleCloseNotice = () => {
  showNoticeDialog.value = false
  sessionStorage.setItem(NOTICE_SHOWN_KEY, 'true')
}

// ─── Tab States ─────────────────────────────────────────────────────────────────────────
const activeBetTab = ref<string>('twoSide')
const activeContentView = ref<'game' | 'drawResults'>('game')
const tabLoading = ref(false)
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
let tabLoadingTimer: ReturnType<typeof setTimeout> | null = null

// ─── Active Game Selection ───────────────────────────────────────────────────────────────
// Synced with GameHeader via v-model:activeGameKey
const activeGameKey = ref(DEFAULT_GAME_KEY)

// Compute lotCode and gameName from the active game key
const activeLotCode = computed(() => getGameConfig(activeGameKey.value).lotCode)
const activeGameName = computed(() => getGameConfig(activeGameKey.value).gameName)
const activeGameCategory = computed(() => getGameCategory(activeGameKey.value))

const preloadCommonPanels = () => {
  void Promise.all([
    loadDrawResults(),
    loadRecentDrawsDialog(),
    loadNoticeSection(),
    loadNoticeDialog(),
  ])
}

const preloadTabPanelsByCategory = (category: GameCategory) => {
  if (category === 'ssc') {
    void Promise.all([
      loadBettingBalls(),
      loadSscTwoSidePanel(),
      loadSscBallPositionPanel(),
      loadSscSingleBallPanel(),
    ])
    return
  }

  // PC28 and racing both use the balls panel.
  void loadBettingBalls()
}

const preloadPanelsForCurrentGame = (category: GameCategory) => {
  preloadCommonPanels()
  preloadTabPanelsByCategory(category)
}

const activeBetTabLabel = computed(() => {
  // Find the matching sub-nav label for the current bet tab
  const subNavItems = getSubNavForGame(activeGameKey.value)
  const found = subNavItems.find(item => item.key === activeBetTab.value)
  return found ? found.label : '两面盘'
})

const SSC_BALL_TAB_TITLE_MAP: Record<string, string> = {
  ball1: '第一球',
  ball4: '第四球',
  ball5: '第五球',
}

const showSscBallPositionPanel = computed(() => (
  activeGameCategory.value === 'ssc'
  && Object.prototype.hasOwnProperty.call(SSC_BALL_TAB_TITLE_MAP, activeBetTab.value)
))

const activeSscBallPositionTitle = computed(() => (
  SSC_BALL_TAB_TITLE_MAP[activeBetTab.value] ?? '第一球'
))

const centerContentClasses = computed(() => ({
  'center-content--wide': activeContentView.value === 'drawResults'
}))

const mainWrapperClasses = computed(() => ({
  'main-wrapper--draw-results': activeContentView.value === 'drawResults'
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

// Watch tab changes to reset content view and show loading for SSC third-level tabs
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

// Restore tab from game-specific history when switching games
watch(activeGameKey, (gameKey) => {
  activeBetTab.value = getSavedBetTabForGame(gameKey) ?? 'twoSide'
  localStorage.setItem(ACTIVE_GAME_STORAGE_KEY, gameKey)
})

watch(
  activeGameCategory,
  (category) => {
    preloadPanelsForCurrentGame(category)
  },
  { immediate: true }
)

watch(activeContentView, (view) => {
  if (view === 'drawResults') {
    showNoticeList.value = false
  }
})

watch(
  () => route.query.view,
  (view) => {
    activeContentView.value = view === 'drawResults' ? 'drawResults' : 'game'
    if (view === 'drawResults') {
      showNoticeList.value = false
    }
  },
  { immediate: true }
)

// ─── Lottery Data ───────────────────────────────────────────────────────────────────────
// Pass reactive lotCode ref so data refetches automatically when game switches
const lotCodeRef = computed(() => activeLotCode.value)
const {
  preDrawIssue,
  preDrawBalls,
  preDrawSum,
  drawIssue,
  sealCountdown,
  drawCountdown,
  isDrawing,
  isSystemClosed,
  historyIssues,
  isSealed,
  parseBalls,
  getIssueSum,
  fetchLotteryInfo,
  fetchHistoryList,
} = useLotteryData(lotCodeRef)

// Handle refresh when clicking game name
const handleRefresh = async () => {
  await Promise.all([fetchLotteryInfo(), fetchHistoryList()])
}

// ─── Betting ────────────────────────────────────────────────────────────────────────────
const {
  quickMode,
  sumAmounts,
  twoSideAmounts,
  colorAmounts,
  patternAmounts,
  ballAmounts,
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
} = useBetting()

// ─── Dragon Leaderboard ─────────────────────────────────────────────────────────────────
const { dragonList } = useDragonLeaderboard(historyIssues)

// ─── Summary Road ───────────────────────────────────────────────────────────────────────
// PC28 summary road (和值 / 和值大小 / 和值单双)
const { summaryTabs, activeSummaryKey, activeSummaryValues, onSummaryTabClick } = useSummaryRoad(
  historyIssues,
  getIssueSum,
  activeGameCategory
)
// SSC summary road (总和大小 / 总和单双 / 龙虎和) with road bead pattern
const {
  sscSummaryTabs,
  activeSscSummaryKey,
  activeSscSummaryValues,
  onSscSummaryTabClick,
} = useSscSummaryRoad(historyIssues, getIssueSum)

// Unified summary props that switch based on game category
const isSscGame = computed(() => activeGameCategory.value === 'ssc')
const currentSummaryTabs = computed(() => isSscGame.value ? sscSummaryTabs : summaryTabs)
const currentSummaryKey = computed(() => isSscGame.value ? activeSscSummaryKey.value : activeSummaryKey.value)
const currentSummaryValues = computed(() => isSscGame.value ? activeSscSummaryValues.value : activeSummaryValues.value)
const handleSummaryTabClick = (key: string) => {
  if (isSscGame.value) {
    onSscSummaryTabClick(key as SscSummaryKey)
  } else {
    onSummaryTabClick(key as SummaryKey)
  }
}

// ─── Recent Draws Dialog ────────────────────────────────────────────────────────────────
const {
  showRecentDialog,
  recentTab,
  recentDialogStyle,
  recentDialogRows,
  openRecentDialog,
  closeRecentDialog,
  onTitleMouseDown,
  stopDrag,
  clampOnResize,
} = useRecentDraws(historyIssues, parseBalls, getIssueSum, activeGameCategory)

// ─── Lifecycle ──────────────────────────────────────────────────────────────────────────
onMounted(() => {
  const savedGameKey = localStorage.getItem(ACTIVE_GAME_STORAGE_KEY)
  if (savedGameKey && Object.prototype.hasOwnProperty.call(GAME_CONFIG_MAP, savedGameKey)) {
    activeGameKey.value = savedGameKey
  }

  document.title = '游戏首页'
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) favicon.href = '/favicon.png'

  // Handle tab parameter from URL
  const tabFromQuery = route.query.tab as string
  if (tabFromQuery && isValidBetTabForGame(activeGameKey.value, tabFromQuery)) {
    activeBetTab.value = tabFromQuery
    saveBetTabForGame(activeGameKey.value, tabFromQuery)
  } else {
    activeBetTab.value = getSavedBetTabForGame(activeGameKey.value) ?? 'twoSide'
  }
  if (route.query.view === 'drawResults') {
    activeContentView.value = 'drawResults'
  }

  // Show notice after short delay (only once per session)
  const hasShownNotice = sessionStorage.getItem(NOTICE_SHOWN_KEY)
  if (!hasShownNotice) {
    const noticeDelayMs = isMobileClient ? 1800 : 500
    setTimeout(() => { showNoticeDialog.value = true }, noticeDelayMs)
  }
})

onUnmounted(() => {
  clearTabLoadingTimer()
  stopDrag()
})

// ─── Window Resize Handler ──────────────────────────────────────────────────────────────
const handleWindowResize = () => {
  clampOnResize()
}

onMounted(() => {
  window.addEventListener('resize', handleWindowResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleWindowResize)
})
</script>

<template>
  <div class="page">
    <GameHeader v-model:betTab="activeBetTab" v-model:contentView="activeContentView" v-model:activeGameKey="activeGameKey" />

    <div class="page-scroll">
      <div class="main-wrapper" :class="mainWrapperClasses">
        <div class="main-body">
          <!-- Left sidebar -->
          <MemberSidebar />

          <!-- Center content -->
          <div class="center-content" :class="centerContentClasses">
            <!-- Draw Results panel -->
            <div v-if="activeContentView === 'drawResults'" key="draw-results-view" class="draw-results-view">
              <DrawResults />
            </div>

            <!-- Game panel -->
            <div
              v-else-if="!showNoticeList"
              key="game-panel-view"
              class="game-panel"
            >
              <!-- System closed overlay (local time daily 06:00-07:00) -->
              <div v-if="isSystemClosed" class="system-closed-overlay">
                <img src="@/assets/通用/bg.png" alt="系统封盘" class="system-closed-bg" />
              </div>
              <!-- Lottery header with countdown -->
              <LotteryHeader
                :game-name="activeGameName"
                :pre-draw-issue="preDrawIssue"
                :pre-draw-balls="preDrawBalls"
                :pre-draw-sum="preDrawSum"
                :draw-issue="drawIssue"
                :seal-countdown="sealCountdown"
                :draw-countdown="drawCountdown"
                :is-drawing="isDrawing"
                :active-bet-tab-label="activeBetTabLabel"
                :game-category="activeGameCategory"
                @refresh="handleRefresh"
              />

              <!-- Top quick bar -->
              <QuickBetBar
                v-model:quickMode="quickMode"
                show-recent-button
                @recent="openRecentDialog(() => { showNoticeDialog = false })"
              />

              <!-- Betting panels loading region -->
              <div
                class="betting-content-area"
                v-loading="tabLoading"
                element-loading-text="加载中，请稍后！！！"
              >
            <!-- Two-side betting panel: PC28 version -->
            <div v-show="!tabLoading">
            <div v-show="activeBetTab === 'twoSide' && activeGameCategory === 'pc28'">
              <!-- Sum values -->
              <h5 class="section-title">和值</h5>
              <div class="sum-grid" :class="{ 'sum-grid--quick': quickMode === 'quick' }">
                <div v-for="(group, groupIndex) in sumGroups" :key="groupIndex" class="sum-col">
                  <div class="sum-head">
                    <div class="sum-head-cell">和值</div>
                    <div class="sum-head-cell">赔率</div>
                    <div v-if="quickMode === 'normal'" class="sum-head-cell">金额</div>
                  </div>
                  <div
                    v-for="item in group"
                    :key="item.num"
                    class="sum-row"
                    :class="{ 'sum-row-selected': isSumSelected(item.num) }"
                    @click="toggleSumSelect(item.num)"
                  >
                    <div class="sum-cell ball-cell">
                      <img class="ball-img" :src="getBallSrc(item.num)" :alt="String(item.num)" />
                    </div>
                    <div class="sum-cell odd-cell">
                      <span class="text-red sum-odd-text" :style="sumOddTextStyle(item.odd)">{{ isSealed ? '--' : item.odd }}</span>
                    </div>
                    <div v-if="quickMode === 'normal'" class="sum-cell input-cell">
                      <input
                        v-model="sumAmounts[item.num]"
                        class="cell-input"
                        type="text"
                        :disabled="isSealed"
                        @click.stop
                        @focus="ensureSumSelected(item.num)"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <!-- Two side -->
              <h5 class="section-title two-side-title">两面</h5>
              <div class="two-side-grid">
                <div v-for="(row, index) in twoSideRows" :key="index" class="two-side-row">
                  <div
                    v-for="item in row"
                    :key="item.label"
                    class="two-side-item"
                    :class="{ 'bet-item-selected': isTwoSideSelected(item.label) }"
                    @click="toggleTwoSideSelect(item.label)"
                  >
                    <span class="label">{{ item.label }}</span>
                    <span class="odd text-red">
                      <span class="two-side-odd-text" :style="twoSideOddTextStyle(item.odd)">{{ isSealed ? '--' : item.odd }}</span>
                    </span>
                    <div class="input-box">
                      <input
                        v-if="quickMode === 'normal'"
                        v-model="twoSideAmounts[item.label]"
                        class="cell-input"
                        type="text"
                        :disabled="isSealed"
                        @click.stop
                        @focus="ensureTwoSideSelected(item.label)"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <!-- Color wave -->
              <h5 class="section-title color-title">色波</h5>
              <div class="color-grid">
                <div
                  v-for="item in colorRows"
                  :key="item.label"
                  class="color-item"
                  :class="{ 'bet-item-selected': isColorSelected(item.label) }"
                  @click="toggleColorSelect(item.label)"
                >
                  <span class="label" :class="`label-${item.label}`">{{ item.label }}</span>
                  <span class="odd text-red">
                    <span class="color-odd-text" :style="colorOddTextStyle(item.odd)">{{ isSealed ? '--' : item.odd }}</span>
                  </span>
                  <div class="input-box">
                    <input
                      v-if="quickMode === 'normal'"
                      v-model="colorAmounts[item.label]"
                      class="cell-input"
                      type="text"
                      :disabled="isSealed"
                      @click.stop
                      @focus="ensureColorSelected(item.label)"
                    />
                  </div>
                </div>
              </div>

              <!-- Patterns -->
              <h5 class="section-title pattern-title">豹子/顺子/对子</h5>
              <div class="pattern-grid">
                <div
                  v-for="item in patternRows"
                  :key="item.label"
                  class="pattern-item"
                  :class="{ 'bet-item-selected': isPatternSelected(item.label) }"
                  @click="togglePatternSelect(item.label)"
                >
                  <span class="label">{{ item.label }}</span>
                  <span class="odd text-red">
                    <span class="pattern-odd-text" :style="patternOddTextStyle(item.odd)">{{ isSealed ? '--' : item.odd }}</span>
                  </span>
                  <div class="input-box">
                    <input
                      v-if="quickMode === 'normal'"
                      v-model="patternAmounts[item.label]"
                      class="cell-input"
                      type="text"
                      :disabled="isSealed"
                      @click.stop
                      @focus="ensurePatternSelected(item.label)"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Two-side betting panel: SSC version (加拿大时时彩 etc.) -->
            <SscTwoSidePanel
              v-show="activeBetTab === 'twoSide' && activeGameCategory === 'ssc'"
              :is-sealed="isSealed"
              :quick-mode="quickMode"
              :pre-draw-balls="preDrawBalls"
              :show-header-ball="activeGameKey !== 'caSsc'"
            />

            <!-- Balls betting panel: PC28 (1-3球) / SSC (1-5球) -->
            <BettingBalls
              v-show="activeBetTab === 'balls' && (activeGameCategory === 'pc28' || activeGameCategory === 'ssc')"
              :is-sealed="isSealed"
              :quick-mode="quickMode"
              :game-category="activeGameCategory"
              v-model:ballAmounts="ballAmounts"
              :is-ball-selected="isBallSelected"
              @toggle-ball="toggleBallSelect"
              @ensure-ball="ensureBallSelected"
            />

            <!-- Single ball panels: 第一球~第五球 (SSC only) -->
            <SscSingleBallPanel
              v-show="activeBetTab === 'ball2' && activeGameCategory === 'ssc'"
              title="第二球"
              :is-sealed="isSealed"
              :quick-mode="quickMode"
              key-prefix="sb2"
            />

            <SscSingleBallPanel
              v-show="activeBetTab === 'ball3' && activeGameCategory === 'ssc'"
              title="第三球"
              :is-sealed="isSealed"
              :quick-mode="quickMode"
              key-prefix="sb3"
            />

            <!-- SSC ball position panel: 第一球 ~ 第五球 -->
            <SscBallPositionPanel
              v-show="showSscBallPositionPanel"
              :title="activeSscBallPositionTitle"
              :position-key="activeBetTab"
              :is-sealed="isSealed"
              :quick-mode="quickMode"
              v-model:ballAmounts="ballAmounts"
              :is-ball-selected="isBallSelected"
              @toggle-ball="toggleBallSelect"
              @ensure-ball="ensureBallSelected"
            />
            </div>
              </div>

            <!-- Bottom quick bar (always visible) -->
            <QuickBetBar
              v-model:quickMode="quickMode"
              class="quick-bar-bottom"
            />

            <!-- Summary road -->
            <SummaryRoad
              :summary-tabs="currentSummaryTabs"
              :active-summary-key="currentSummaryKey"
              :active-summary-values="currentSummaryValues"
              @tab-click="handleSummaryTabClick"
            />
            </div>

            <!-- Notice list panel -->
            <NoticeSection v-else v-model:show-list="showNoticeList" />
          </div>

          <!-- Right sidebar -->
          <AnnounceSidebar
            v-if="!showNoticeList && activeContentView === 'game'"
            :dragon-list="dragonList"
            @more-click="showNoticeList = true"
          />
        </div>
      </div>
    </div>

    <!-- Footer bar -->
    <FooterBar
      :show-more="showNoticeList"
      @toggle-more="showNoticeList = !showNoticeList"
    />

    <!-- Recent draws dialog -->
    <RecentDrawsDialog
      v-model:show="showRecentDialog"
      v-model:recent-tab="recentTab"
      :game-name="activeGameName"
      :recent-dialog-rows="recentDialogRows"
      :recent-dialog-style="recentDialogStyle"
      @close="closeRecentDialog"
      @title-mouse-down="onTitleMouseDown"
    />

    <!-- Notice dialog -->
    <NoticeDialog
      v-if="showNoticeDialog"
      v-model:visible="showNoticeDialog"
      @close="handleCloseNotice"
    />
  </div>
</template>

<style scoped>
.page {
  height: 100vh;
  min-height: 100vh;
  background: #fff;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  overflow: hidden;
}

.page-scroll {
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  background: #fff;
}

.main-wrapper {
  width: 92%;
  margin: 5px auto 0;
  position: relative;
  left: -67px;
  background: #fff;
  border-bottom: none;
}

.main-wrapper--draw-results {
  width: calc(100% - 7px);
  left: 0;
  margin-left: 0;
  margin-right: 7px;
  box-sizing: border-box;
}

.main-body {
  display: flex;
  align-items: flex-start;
  width: 100%;
}

.center-content {
  flex: 0 0 720px;
  width: 720px;
  min-height: 500px;
  border-left: none;
  border-right: none;
}

.center-content.center-content--wide {
  flex: 1 1 0 !important;
  width: auto !important;
  min-width: 0;
  max-width: none;
  overflow-x: auto;
  overflow-y: visible;
}

.draw-results-view {
  display: block;
  width: 100%;
  box-sizing: border-box;
  min-width: 0;
  max-width: 100%;
  overflow: visible;
}

.game-panel {
  width: 720px;
  min-height: 733px;
  margin: 5px 0 30px;
  position: relative;
}

.betting-content-loading-region {
  min-height: 200px;
  position: relative;
}

.betting-content-panels {
  min-height: 200px;
}

.betting-content-loading-region :deep(.el-loading-mask) {
  align-items: flex-start;
  justify-content: center;
  padding-top: 40px;
}

.betting-content-loading-region :deep(.el-loading-spinner) {
  top: 0;
  margin-top: 0;
}

/* Section titles */
.section-title {
  width: 720px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  font-size: 14px;
  font-weight: 400;
  color: #000;
  border: 1px solid var(--bw-border-color, #efba84);
  border-bottom: none;
  background: var(--bw-bg-3, #fff7ef);
  margin: 0;
  box-sizing: border-box;
}

.two-side-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid var(--bw-border-color, #efba84);
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-bg-3, #fff7ef);
}

.color-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid var(--bw-border-color, #efba84);
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-bg-3, #fff7ef);
}

.pattern-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid var(--bw-border-color, #efba84);
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-bg-3, #fff7ef);
}

/* Sum grid styles */
.sum-grid {
  width: 720px;
  display: flex;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.sum-col {
  width: 25%;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.sum-col:last-child {
  border-right: none;
}

.sum-head {
  display: flex;
  height: 30px;
  line-height: 30px;
  background: var(--bw-table-header-bg-color);
  border-bottom: 1px solid var(--bw-border-color, #efba84);
}

.sum-head-cell {
  flex: 1;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  background: var(--bw-table-header-bg-color);
  box-sizing: border-box;
}

.sum-head-cell:first-child {
  width: 30px;
  flex: 0 0 30px;
}

.sum-head-cell:nth-child(2) {
  width: 75px;
  flex: 0 0 75px;
}

.sum-head-cell:nth-child(3) {
  width: 74px;
  flex: 0 0 74px;
}

.sum-head-cell:last-child {
  border-right: none;
}

.sum-row {
  display: flex;
  height: 30px;
  line-height: 30px;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  cursor: pointer;
}

.sum-row:hover {
  background: var(--bw-header-color, #be9d76);
}

.sum-col .sum-row:last-child {
  border-bottom: none;
}

.sum-row-selected .sum-cell,
.sum-row-selected:hover .sum-cell,
.sum-row-selected:focus-within .sum-cell {
  background: #ffc214 !important;
}

.sum-cell {
  flex: 1;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sum-cell:nth-child(1) {
  width: 30px;
  flex: 0 0 30px;
}

.sum-cell:nth-child(2) {
  width: 75px;
  flex: 0 0 75px;
}

.sum-cell:nth-child(3) {
  width: 74px;
  flex: 0 0 74px;
}

.sum-grid--quick .sum-head-cell:nth-child(1),
.sum-grid--quick .sum-cell:nth-child(1) {
  width: 30px;
  flex: 0 0 30px;
}

.sum-grid--quick .sum-head-cell:nth-child(2),
.sum-grid--quick .sum-cell:nth-child(2) {
  width: auto;
  flex: 1 1 auto;
}

.sum-cell:last-child {
  border-right: none;
}

.sum-odd-text {
  display: inline-block;
  text-align: center;
  font-weight: 500;
  font-size: 13px;
}

.ball-img {
  width: 27px;
  height: 27px;
  margin-left: 6px;
  display: inline-block;
}

.text-red {
  color: red;
}

.cell-input {
  width: 50px;
  height: 22px;
  padding: 0 4px;
  border: 1px solid #abb2c5;
  border-radius: 4px;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: textfield;
  background: #ffffff;
  color: #000;
  font-size: 12px;
  line-height: 22px;
  text-align: center;
  box-sizing: border-box;
}

.cell-input:disabled {
  opacity: 1;
  background: #ffffff;
  color: #000;
  border-color: #abb2c5;
}

.cell-input:focus {
  outline: none;
  box-shadow: none;
  border-color: var(--el-color-primary, #5c2e0d);
}

/* Two side grid */
.two-side-grid {
  width: 720px;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.two-side-row {
  display: flex;
  height: 30px;
  line-height: 30px;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.two-side-row:last-child {
  border-bottom: none;
}

.two-side-item {
  width: 20%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  cursor: pointer;
}

.two-side-item:hover {
  background: var(--bw-header-color, #be9d76);
}

.two-side-item:hover .label,
.two-side-item:hover .input-box {
  background: transparent;
}

.two-side-item:last-child {
  border-right: none;
}

.two-side-item .label {
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  color: #000;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  display: inline-block;
  font-size: 13px;
  background: var(--bw-form-item-label-bg-color, #fff1e4);
}

.two-side-item:hover .label,
.two-side-item:focus-within .label {
  background: transparent;
}

.two-side-item .odd {
  width: 56.83px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  background: transparent;
}

.two-side-odd-text {
  display: inline-block;
  text-align: center;
}

.two-side-item .input-box {
  width: 56px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.two-side-item:hover .input-box,
.two-side-item:focus-within .input-box {
  background: transparent;
}

/* Color and pattern grid */
.color-grid,
.pattern-grid {
  width: 720px;
  display: flex;
  border: 1px solid var(--bw-border-color, #efba84);
  border-top: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.color-item {
  width: 33.33%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  cursor: pointer;
}

.color-item:hover {
  background: var(--bw-header-color, #be9d76);
}

.color-item:last-child {
  border-right: none;
}

.color-item .label {
  width: 60px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  font-weight: 700;
  font-size: 12px;
  box-sizing: border-box;
}

.label-绿波 {
  color: green;
}

.label-蓝波 {
  color: blue;
}

.label-红波 {
  color: red;
}

.color-item .odd {
  width: 90px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  background: transparent;
}

.color-odd-text {
  display: inline-block;
  height: 30px;
  line-height: 30px;
  text-align: center;
}

.color-item .input-box {
  width: 89px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

/* Pattern */
.pattern-item {
  width: 20%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  cursor: pointer;
}

.pattern-item:hover {
  background: var(--bw-header-color, #be9d76);
}

.bet-item-selected,
.bet-item-selected:hover,
.bet-item-selected:focus-within {
  background: #ffc214;
}

.bet-item-selected .label,
.bet-item-selected:hover .label,
.bet-item-selected:focus-within .label,
.bet-item-selected .odd,
.bet-item-selected:hover .odd,
.bet-item-selected:focus-within .odd,
.bet-item-selected .input-box,
.bet-item-selected:hover .input-box,
.bet-item-selected:focus-within .input-box {
  background: #ffc214;
}

.pattern-item:last-child {
  border-right: none;
}

.pattern-item .label {
  width: 32px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  font-weight: 700;
  font-size: 12px;
  box-sizing: border-box;
  background: transparent;
}

.pattern-item .odd {
  width: 56px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-right: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  background: transparent;
}

.pattern-odd-text {
  display: inline-block;
  height: 30px;
  line-height: 30px;
  text-align: center;
}

.pattern-item .input-box {
  width: 55px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.pattern-item .cell-input {
  width: 50px;
  height: 22px;
}

/* Quick bar top - right-align */
:deep(.quick-bar:not(.quick-bar-bottom)) {
  justify-content: flex-end;
  padding: 0 10px;
}

/* Quick bar bottom adjustment */
:deep(.quick-bar-bottom) {
  height: 48px;
  margin-top: 10px;
  background: transparent;
  border-color: transparent;
}

/* Sealed overlay (rgba(53,28,12,.6), 830x732.84, z-index 2001) */
.sealed-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 830px;
  height: 732.84px;
  background: var(--bw-header-bg-opacity, rgba(53, 28, 12, .6));
  z-index: 2001;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sealed-overlay-bg {
  width: 500px;
  max-width: 90%;
  opacity: 0.8;
}

/* System closed overlay */
.system-closed-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
}

.system-closed-bg {
  width: 500px;
  max-width: 90%;
  opacity: 0.9;
}

/* Betting content loading area */
.betting-content-area {
  position: relative;
  min-height: 200px;
}

/* Spacing */
.mt10 {
  margin-top: 10px;
}
</style>
