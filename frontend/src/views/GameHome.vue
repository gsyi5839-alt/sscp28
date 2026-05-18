<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import GameHeader from '@/components/GameHeader.vue'
import MemberSidebar from '@/components/MemberSidebar.vue'
import './GameHome.css'

const DrawResults = defineAsyncComponent(loadDrawResults)
const RecentDrawsDialog = defineAsyncComponent(loadRecentDrawsDialog)
const SscTwoSidePanel = defineAsyncComponent(loadSscTwoSidePanel)
const SscBallPositionPanel = defineAsyncComponent(loadSscBallPositionPanel)
const SscSingleBallPanel = defineAsyncComponent(loadSscSingleBallPanel)
const SscNiuNiuPanel = defineAsyncComponent(loadSscNiuNiuPanel)
const SscNiuNiuStats = defineAsyncComponent(loadSscNiuNiuStats)
const NoticeDialog = defineAsyncComponent(loadNoticeDialog)
const BettingBalls = defineAsyncComponent(loadBettingBalls)
const NoticeSection = defineAsyncComponent(loadNoticeSection)
const Pc28TwoSidePanel = defineAsyncComponent(loadPc28TwoSidePanel)
const RacingTwoSidePanel = defineAsyncComponent(loadRacingTwoSidePanel)

import LotteryHeader from './game/components/LotteryHeader.vue'
import QuickBetBar from './game/components/QuickBetBar.vue'
import SummaryRoad from './game/components/SummaryRoad.vue'
import AnnounceSidebar from './game/components/AnnounceSidebar.vue'
import FooterBar from './game/components/FooterBar.vue'
import SystemClosedOverlay from './game/components/SystemClosedOverlay.vue'

import { useLotteryData } from './game/composables/useLotteryData'
import { useBetting } from './game/composables/useBetting'
import { useDragonLeaderboard } from './game/composables/useDragonLeaderboard'
import { useSummaryRoad } from './game/composables/useSummaryRoad'
import { useSscSummaryRoad } from './game/composables/useSscSummaryRoad'
import { useRecentDraws } from './game/composables/useRecentDraws'
import { useGameHomeState } from './game/composables/useGameHomeState'
import { racingSummaryTabs, type SummaryKey, type SscSummaryKey } from './game/constants/odds'
import type { RacingPanelMode } from './game/constants/racingOdds'

const route = useRoute()

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

function loadSscNiuNiuPanel() {
  return import('./game/components/ssc/SscNiuNiuPanel.vue')
}

function loadSscNiuNiuStats() {
  return import('./game/components/ssc/SscNiuNiuStats.vue')
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

function loadPc28TwoSidePanel() {
  return import('./game/components/Pc28TwoSidePanel.vue')
}

function loadRacingTwoSidePanel() {
  return import('./game/components/racing/RacingTwoSidePanel.vue')
}

const {
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
} = useGameHomeState({
  route,
  loadDrawResults,
  loadRecentDrawsDialog,
  loadSscTwoSidePanel,
  loadSscBallPositionPanel,
  loadSscSingleBallPanel,
  loadSscNiuNiuPanel,
  loadSscNiuNiuStats,
  loadNoticeDialog,
  loadBettingBalls,
  loadNoticeSection,
  loadRacingTwoSidePanel,
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

const showRacingTwoSidePanel = computed(() => {
  if (activeGameCategory.value !== 'racing') {
    return false
  }

  return ['twoSide', 'positions', 'topTwo', 'baoDou', 'racingNiuNiu'].includes(activeBetTab.value)
})

const racingPanelMode = computed<RacingPanelMode>(() => {
  if (activeBetTab.value === 'topTwo') {
    return 'topSum'
  }

  if (activeBetTab.value === 'positions') {
    return 'position'
  }

  if (activeBetTab.value === 'baoDou') {
    return 'baoDou'
  }

  if (activeBetTab.value === 'racingNiuNiu') {
    return 'niuNiu'
  }

  return 'all'
})

const racingPanelPositionKey = computed(() => {
  return undefined
})

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

const handleRefresh = async () => {
  await Promise.all([fetchLotteryInfo(), fetchHistoryList()])
}

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

const { dragonList } = useDragonLeaderboard(historyIssues)

const { summaryTabs, activeSummaryKey, activeSummaryValues, onSummaryTabClick } = useSummaryRoad(
  historyIssues,
  getIssueSum,
  activeGameCategory
)
const {
  sscSummaryTabs,
  activeSscSummaryKey,
  activeSscSummaryValues,
  onSscSummaryTabClick,
} = useSscSummaryRoad(historyIssues, getIssueSum)

const isSscGame = computed(() => activeGameCategory.value === 'ssc')
const isRacingGame = computed(() => activeGameCategory.value === 'racing')
const currentSummaryTabs = computed(() => {
  if (isSscGame.value) return sscSummaryTabs
  if (isRacingGame.value && activeBetTab.value === 'baoDou') return [{ key: 'baoDou' as SummaryKey, label: '宝斗' }]
  if (isRacingGame.value) return racingSummaryTabs
  return summaryTabs
})
const currentSummaryKey = computed(() => isSscGame.value ? activeSscSummaryKey.value : activeSummaryKey.value)
const currentSummaryValues = computed(() => isSscGame.value ? activeSscSummaryValues.value : activeSummaryValues.value)
const handleSummaryTabClick = (key: string) => {
  if (isSscGame.value) {
    onSscSummaryTabClick(key as SscSummaryKey)
  } else {
    onSummaryTabClick(key as SummaryKey)
  }
}

watch(
  [activeGameCategory, activeBetTab],
  ([category, betTab]) => {
    if (category === 'racing' && betTab === 'baoDou') {
      activeSummaryKey.value = 'baoDou'
      return
    }
    if (category === 'racing' && activeSummaryKey.value === 'baoDou') {
      activeSummaryKey.value = 'sum'
    }
  },
  { immediate: true }
)

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

const handleWindowResize = () => {
  clampOnResize()
}

onMounted(() => {
  window.addEventListener('resize', handleWindowResize)
})

onUnmounted(() => {
  stopDrag()
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
              <SystemClosedOverlay v-if="isSystemClosed" />
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
                <div v-show="!tabLoading">
                  <Pc28TwoSidePanel
                    v-show="activeBetTab === 'twoSide' && activeGameCategory === 'pc28'"
                    v-model:sumAmounts="sumAmounts"
                    v-model:twoSideAmounts="twoSideAmounts"
                    v-model:colorAmounts="colorAmounts"
                    v-model:patternAmounts="patternAmounts"
                    :is-sealed="isSealed"
                    :quick-mode="quickMode"
                    :is-sum-selected="isSumSelected"
                    :is-two-side-selected="isTwoSideSelected"
                    :is-color-selected="isColorSelected"
                    :is-pattern-selected="isPatternSelected"
                    @toggle-sum="toggleSumSelect"
                    @ensure-sum="ensureSumSelected"
                    @toggle-two-side="toggleTwoSideSelect"
                    @ensure-two-side="ensureTwoSideSelected"
                    @toggle-color="toggleColorSelect"
                    @ensure-color="ensureColorSelected"
                    @toggle-pattern="togglePatternSelect"
                    @ensure-pattern="ensurePatternSelected"
                  />

                  <!-- Two-side betting panel: SSC version. -->
                  <SscTwoSidePanel
                    v-show="activeBetTab === 'twoSide' && activeGameCategory === 'ssc'"
                    :is-sealed="isSealed"
                    :quick-mode="quickMode"
                    :pre-draw-balls="preDrawBalls"
                    :show-header-ball="activeGameKey !== 'caSsc' && activeGameKey !== 'aus5'"
                    :variant="activeGameKey === 'aus5' ? 'aus5' : 'default'"
                  />

                  <!-- Two-side betting panel: racing version. -->
                  <RacingTwoSidePanel
                    v-show="showRacingTwoSidePanel"
                    v-model:amounts="twoSideAmounts"
                    :mode="racingPanelMode"
                    :position-key="racingPanelPositionKey"
                    :is-sealed="isSealed"
                    :quick-mode="quickMode"
                    :is-selected="isTwoSideSelected"
                    :pre-draw-balls="preDrawBalls"
                    @toggle="toggleTwoSideSelect"
                    @ensure="ensureTwoSideSelected"
                  />

                  <!-- Balls betting panel: PC28 and SSC. -->
                  <BettingBalls
                    v-show="activeBetTab === 'balls' && (activeGameCategory === 'pc28' || activeGameCategory === 'ssc')"
                    v-model:ballAmounts="ballAmounts"
                    :is-sealed="isSealed"
                    :quick-mode="quickMode"
                    :game-category="activeGameCategory"
                    :is-ball-selected="isBallSelected"
                    @toggle-ball="toggleBallSelect"
                    @ensure-ball="ensureBallSelected"
                  />

                  <!-- Single ball panels for SSC. -->
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

                  <!-- SSC ball position panel. -->
                  <SscBallPositionPanel
                    v-show="showSscBallPositionPanel"
                    v-model:ballAmounts="ballAmounts"
                    :title="activeSscBallPositionTitle"
                    :position-key="activeBetTab"
                    :is-sealed="isSealed"
                    :quick-mode="quickMode"
                    :is-ball-selected="isBallSelected"
                    @toggle-ball="toggleBallSelect"
                    @ensure-ball="ensureBallSelected"
                  />

                  <template v-if="activeBetTab === 'niuNiu' && activeGameCategory === 'ssc'">
                    <SscNiuNiuPanel
                      :is-sealed="isSealed"
                      :quick-mode="quickMode"
                    />
                  </template>
                </div>
              </div>

            <!-- Bottom quick bar (always visible) -->
            <QuickBetBar
              v-model:quickMode="quickMode"
              class="quick-bar-bottom"
            />

            <SscNiuNiuStats
              v-if="activeBetTab === 'niuNiu' && activeGameCategory === 'ssc'"
              :history-issues="historyIssues"
            />

            <!-- Summary road -->
            <SummaryRoad
              v-else
              :summary-tabs="currentSummaryTabs"
              :active-summary-key="currentSummaryKey"
              :active-summary-values="currentSummaryValues"
              :variant="activeGameCategory"
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
      :game-category="activeGameCategory"
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
