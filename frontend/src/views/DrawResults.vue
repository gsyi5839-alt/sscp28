<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { lotteryApi } from '../api/index'
import {
  calcDragonTiger,
  calcThreePattern,
  calcBullResult,
  getDragonTigerColor,
  parseNumbers
} from '../utils/lotteryCalc'
import './DrawResults.css'

interface GameOption {
  lotCode: number
  lotName: string
  lotType?: number | null
  lotLabel?: number | null
  sort?: number | null
}

interface LotteryItem {
  preDrawIssue: string
  preDrawCode: string
  preDrawTime: string
  sumValue: string
  sizeLabel: string
  parityLabel: string
}

interface LotteryGamesResponse {
  code: number
  data: GameOption[]
}

interface LotteryListApiResponse {
  code: number
  data: {
    list: LotteryItem[]
    total: number
  }
}

// Fixed game display order for draw results page
const DRAW_RESULTS_GAME_ORDER = [
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
]

// Fallback game options when API fails
const FALLBACK_GAME_OPTIONS: GameOption[] = [
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
]

// Get today's date in YYYY-MM-DD format
const getTodayDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// Reactive state
const gameOptions = ref<GameOption[]>([])
const selectedLotCode = ref<number | null>(FALLBACK_GAME_OPTIONS[0]?.lotCode ?? null)
const selectedDate = ref(getTodayDate())
const tableData = ref<LotteryItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const ready = ref(false)

// Computed properties
const currentGame = computed(() => {
  return gameOptions.value.find((game) => game.lotCode === selectedLotCode.value) ?? FALLBACK_GAME_OPTIONS[0]
})

const currentGameLabel = computed(() => currentGame.value?.lotName || '加拿大pc28')

// Sort games by predefined order
const sortDrawResultsGames = (games: GameOption[]) => {
  const gameMap = new Map(games.map((item) => [item.lotName, item]))
  return DRAW_RESULTS_GAME_ORDER
    .map((name) => gameMap.get(name))
    .filter((item): item is GameOption => Boolean(item))
}

// Apply sorted game options
const applyGameOptions = (games: GameOption[]) => {
  const orderedGames = sortDrawResultsGames(games)
  gameOptions.value = orderedGames.length > 0 ? orderedGames : FALLBACK_GAME_OPTIONS

  if (!selectedLotCode.value || !gameOptions.value.some((game) => game.lotCode === selectedLotCode.value)) {
    selectedLotCode.value = gameOptions.value[0]?.lotCode ?? null
  }
}

// Fetch game options from API
const fetchGameOptions = async () => {
  try {
    const res = await lotteryApi.getGames() as unknown as LotteryGamesResponse
    if (res.code === 200 && Array.isArray(res.data) && res.data.length > 0) {
      applyGameOptions(res.data)
      return
    }
  } catch (_error) {
    // Fall back to the verified upstream mapping when the catalog request fails.
  }
  applyGameOptions(FALLBACK_GAME_OPTIONS)
}

// Fetch lottery data from API
const fetchData = async () => {
  if (!selectedLotCode.value) return

  loading.value = true
  try {
    const res = await lotteryApi.getList(
      selectedLotCode.value,
      currentPage.value,
      pageSize.value,
      selectedDate.value || undefined
    ) as unknown as LotteryListApiResponse

    if (res.code === 200 && res.data) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
      return
    }

    tableData.value = []
    total.value = 0
  } catch (_error) {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// Parse ball numbers from comma-separated string
const parseBalls = (code: string) => {
  if (!code) return []
  return code
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

// Get ball image URL by number (for PC28: 0-9, for others: 0-27)
const getBallImageUrl = (num: number): string => {
  const padded = String(num).padStart(2, '0')
  return new URL(`../assets/游戏/ball_cols_split/ball_${padded}.png`, import.meta.url).href
}

// Pagination handlers
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

// Watch for game selection changes
watch(selectedLotCode, () => {
  if (!ready.value || !selectedLotCode.value) return
  currentPage.value = 1
  fetchData()
})

// Watch for date selection changes
watch(selectedDate, () => {
  if (!ready.value || !selectedLotCode.value) return
  currentPage.value = 1
  fetchData()
})

// Initialize on mount
onMounted(async () => {
  await fetchGameOptions()
  ready.value = true
  await fetchData()
})
</script>

<template>
  <div class="draw-results">
    <div class="draw-results-card">
      <!-- Toolbar: game selector, title, date picker -->
      <div class="toolbar">
        <el-select v-model="selectedLotCode" class="game-select" placeholder="请选择游戏" size="small">
          <el-option
            v-for="game in gameOptions"
            :key="game.lotCode"
            :label="game.lotName"
            :value="game.lotCode"
          />
        </el-select>

        <div class="toolbar-title">历史开奖结果（{{ currentGameLabel }}）</div>

        <div class="toolbar-spacer" />

        <el-date-picker
          v-model="selectedDate"
          type="date"
          class="date-picker"
          placeholder="选择日期"
          size="small"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :editable="false"
        />
      </div>

      <!-- Results table + pagination as one block -->
      <div class="table-shell">
        <el-table
          v-loading="loading"
          :data="tableData"
          border
          fit
          class="results-table"
          empty-text="暂无数据"
        >
          <!-- Period column -->
          <el-table-column prop="preDrawIssue" label="期数" align="center" min-width="120">
            <template #header>
              <b class="header-text">期数</b>
            </template>
          </el-table-column>

          <!-- Draw time column -->
          <el-table-column prop="preDrawTime" label="开奖日期" align="center" min-width="180">
            <template #header>
              <b class="header-text">开奖日期</b>
            </template>
          </el-table-column>

          <!-- Ball results column with PNG images -->
          <el-table-column label="开奖结果" align="center" min-width="250">
            <template #header>
              <b class="header-text">开奖结果</b>
            </template>
            <template #default="{ row }">
              <div class="ball-list">
                <img
                  v-for="(ball, index) in parseBalls(row.preDrawCode)"
                  :key="`${row.preDrawIssue}-${index}`"
                  :src="getBallImageUrl(parseInt(ball, 10))"
                  :alt="`ball-${ball}`"
                  class="ball-img"
                />
              </div>
            </template>
          </el-table-column>

          <!-- Summary column: sum value, size label, parity label -->
          <el-table-column label="总和" align="center" min-width="150">
            <template #header>
              <b class="header-text">总和</b>
            </template>
            <template #default="{ row }">
              <div class="summary-row">
                <div class="summary-cell summary-cell--border">{{ row.sumValue || '--' }}</div>
                <div
                  class="summary-cell summary-cell--border"
                  :style="{ color: row.sizeLabel === '大' ? 'red' : '' }"
                >{{ row.sizeLabel || '--' }}</div>
                <div
                  class="summary-cell"
                  :style="{ color: row.parityLabel === '双' ? 'red' : '' }"
                >{{ row.parityLabel || '--' }}</div>
              </div>
            </template>
          </el-table-column>

          <!-- Dragon/Tiger column: compare first and last number -->
          <el-table-column label="龙虎" align="center" width="60">
            <template #header>
              <b class="header-text">龙虎</b>
            </template>
            <template #default="{ row }">
              <div class="calc-cell">
                <span :class="getDragonTigerColor(calcDragonTiger(parseNumbers(row.preDrawCode)))">
                  {{ calcDragonTiger(parseNumbers(row.preDrawCode)) }}
                </span>
              </div>
            </template>
          </el-table-column>

          <!-- Front three column: pattern of first 3 numbers -->
          <el-table-column label="前三" align="center" width="60">
            <template #header>
              <b class="header-text">前三</b>
            </template>
            <template #default="{ row }">
              <div class="calc-cell">
                <span>{{ calcThreePattern(parseNumbers(row.preDrawCode).slice(0, 3)) }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- Middle three column: pattern of middle 3 numbers (index 1,2,3) -->
          <el-table-column label="中三" align="center" width="60">
            <template #header>
              <b class="header-text">中三</b>
            </template>
            <template #default="{ row }">
              <div class="calc-cell">
                <span>{{ calcThreePattern(parseNumbers(row.preDrawCode).slice(1, 4)) }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- Back three column: pattern of last 3 numbers -->
          <el-table-column label="后三" align="center" width="60">
            <template #header>
              <b class="header-text">后三</b>
            </template>
            <template #default="{ row }">
              <div class="calc-cell">
                <span>{{ calcThreePattern(parseNumbers(row.preDrawCode).slice(2, 5)) }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- Bull game column: calculate bull result from 5 numbers -->
          <el-table-column label="斗牛" align="center" width="60">
            <template #header>
              <b class="header-text">斗牛</b>
            </template>
            <template #default="{ row }">
              <div class="calc-cell">
                <span>{{ calcBullResult(parseNumbers(row.preDrawCode)) }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination inside table-shell -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[20, 30, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            small
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Scoped deep selectors for Element Plus table customization */
:deep(.results-table.el-table--border::before),
:deep(.results-table.el-table--border::after) {
  display: none;
}
:deep(.results-table.el-table--border .el-table__inner-wrapper) {
  border: none;
}
:deep(.results-table .el-scrollbar__bar) {
  display: none;
}
:deep(.results-table .el-table__body-wrapper) {
  overflow: hidden;
}
:deep(.results-table table.el-table__body),
:deep(.results-table table.el-table__header) {
  table-layout: auto;
}
:deep(.results-table .el-table__header-wrapper th.el-table__cell) {
  height: 40px;
  padding: 0;
  background: var(--bw-table-header-bg-color, linear-gradient(to bottom, #fff 0%, #fff1e4 100%));
}
:deep(.results-table .el-table__row td.el-table__cell) {
  height: 30px;
  padding: 0;
}
:deep(.results-table .el-table__body .cell) {
  line-height: 30px;
  padding: 0 8px;
}
</style>
