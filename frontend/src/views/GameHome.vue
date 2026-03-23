<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import GameHeader from '@/components/GameHeader.vue'
import MemberSidebar from '@/components/MemberSidebar.vue'
import NoticeDialog from '@/components/NoticeDialog.vue'
import DrawResults from '@/views/DrawResults.vue'

// Child components
import LotteryHeader from './game/components/LotteryHeader.vue'
import QuickBetBar from './game/components/QuickBetBar.vue'
import BettingBalls from './game/components/BettingBalls.vue'
import SummaryRoad from './game/components/SummaryRoad.vue'
import AnnounceSidebar from './game/components/AnnounceSidebar.vue'
import RecentDrawsDialog from './game/components/RecentDrawsDialog.vue'
import FooterBar from './game/components/FooterBar.vue'
import NoticeSection from './game/components/NoticeSection.vue'
import SscTwoSidePanel from './game/components/ssc/SscTwoSidePanel.vue'

// Composables
import { useLotteryData } from './game/composables/useLotteryData'
import { useBetting } from './game/composables/useBetting'
import { useDragonLeaderboard } from './game/composables/useDragonLeaderboard'
import { useSummaryRoad } from './game/composables/useSummaryRoad'
import { useRecentDraws } from './game/composables/useRecentDraws'
import {
  sumGroups,
  twoSideRows,
  colorRows,
  patternRows,
  type SummaryKey,
} from './game/constants/odds'
import { getBallSrc, sumOddTextStyle, twoSideOddTextStyle, colorOddTextStyle, patternOddTextStyle } from './game/composables/useOddsStyles'
import { NOTICE_SHOWN_KEY } from './game/constants/notices'
import { getGameConfig, DEFAULT_GAME_KEY } from '@/utils/gameConfig'
import { getSubNavForGame, getGameCategory } from '@/utils/gameSubNav'

const route = useRoute()

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

// ─── Active Game Selection ───────────────────────────────────────────────────────────────
// Synced with GameHeader via v-model:activeGameKey
const activeGameKey = ref(DEFAULT_GAME_KEY)

// Compute lotCode and gameName from the active game key
const activeLotCode = computed(() => getGameConfig(activeGameKey.value).lotCode)
const activeGameName = computed(() => getGameConfig(activeGameKey.value).gameName)
const activeGameCategory = computed(() => getGameCategory(activeGameKey.value))

const activeBetTabLabel = computed(() => {
  // Find the matching sub-nav label for the current bet tab
  const subNavItems = getSubNavForGame(activeGameKey.value)
  const found = subNavItems.find(item => item.key === activeBetTab.value)
  return found ? found.label : '两面盘'
})

const centerContentClasses = computed(() => ({
  'center-content--wide': activeContentView.value === 'drawResults'
}))

const mainWrapperClasses = computed(() => ({
  'main-wrapper--draw-results': activeContentView.value === 'drawResults'
}))

// Watch tab changes to reset content view
watch(activeBetTab, () => {
  showNoticeList.value = false
  activeContentView.value = 'game'
})

// Reset bet tab to default when switching games
watch(activeGameKey, () => {
  activeBetTab.value = 'twoSide'
})

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
const { summaryTabs, activeSummaryKey, activeSummaryValues, onSummaryTabClick } = useSummaryRoad(
  historyIssues,
  getIssueSum,
  activeGameCategory
)

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
  document.title = '游戏首页'
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) favicon.href = '/favicon.png'

  // Handle tab parameter from URL
  const tabFromQuery = route.query.tab as string
  if (tabFromQuery) {
    activeBetTab.value = tabFromQuery
  }
  if (route.query.view === 'drawResults') {
    activeContentView.value = 'drawResults'
  }

  // Show notice after short delay (only once per session)
  const hasShownNotice = sessionStorage.getItem(NOTICE_SHOWN_KEY)
  if (!hasShownNotice) {
    setTimeout(() => { showNoticeDialog.value = true }, 500)
  }
})

onUnmounted(() => {
  stopDrag()
})

// ─── Window Resize Handler ──────────────────────────────────────────────────────────────
const lotteryHeaderRef = ref<InstanceType<typeof LotteryHeader> | null>(null)

const handleWindowResize = () => {
  lotteryHeaderRef.value?.updateCountdownPosition()
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
          <div v-else-if="!showNoticeList" key="game-panel-view" class="game-panel">
            <!-- System closed overlay (China time daily 06:00-07:00) -->
            <div v-if="isSystemClosed" class="system-closed-overlay">
              <img src="@/assets/通用/bg.png" alt="系统封盘" class="system-closed-bg" />
            </div>

            <!-- Lottery header with countdown -->
            <LotteryHeader
              ref="lotteryHeaderRef"
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

            <!-- Two-side betting panel: PC28 version -->
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
            />

            <!-- 1-3 balls betting panel (PC28 only) -->
            <BettingBalls
              v-show="activeBetTab === 'balls'"
              :is-sealed="isSealed"
              :quick-mode="quickMode"
              v-model:ballAmounts="ballAmounts"
              :is-ball-selected="isBallSelected"
              @toggle-ball="toggleBallSelect"
              @ensure-ball="ensureBallSelected"
            />

            <!-- Bottom quick bar -->
            <QuickBetBar
              v-model:quickMode="quickMode"
              class="quick-bar-bottom"
            />

            <!-- Summary road -->
            <SummaryRoad
              :summary-tabs="summaryTabs"
              :active-summary-key="activeSummaryKey"
              :active-summary-values="activeSummaryValues"
              @tab-click="onSummaryTabClick"
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
      v-model:visible="showNoticeDialog"
      @close="handleCloseNotice"
    />
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
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
  overflow: hidden;
  contain: paint;
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
  width: 45px;
  height: 20px;
  border: 1px solid #a0b4d8;
  border-radius: 0.5px;
  box-sizing: border-box;
}

.cell-input:focus {
  outline: none;
  box-shadow: none;
  border-color: #000;
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
  height: 20px;
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

/* Spacing */
.mt10 {
  margin-top: 10px;
}
</style>
