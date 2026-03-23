<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { lotteryApi } from '../api/index'
import {
  calcDragonTiger,
  calcThreePattern,
  calcBullResult,
  getDragonTigerColor,
  parseNumbers,
  calcDragonTigerPairs,
  calcNiuNiu10
} from '../utils/lotteryCalc'
import {
  DRAW_RESULTS_GAME_ORDER,
  FALLBACK_GAME_OPTIONS,
  SSC_LOT_CODES,
  RACING_LOT_CODES,
  niuNiuHeaders,
  type GameOption
} from '../utils/drawResultsConfig'
import './DrawResults.css'
import './DrawResultsAU10.css'

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

// Check if current game is SSC type (5 balls with all extended columns)
const isSSCType = computed(() => {
  return SSC_LOT_CODES.includes(selectedLotCode.value ?? 0)
})

// Check if current game is Racing type (10 balls with dragon/tiger only)
const isRacingType = computed(() => {
  return RACING_LOT_CODES.includes(selectedLotCode.value ?? 0)
})

// Check if current game is AU10 or Happy Racing type (full racing table with 7 columns)
// Includes: AU Lucky 10 (797), Happy Racing (763), Lucky Airship (765), Speed Racing (768)
const isAU10Type = computed(() => {
  return selectedLotCode.value === 797 || selectedLotCode.value === 763 || selectedLotCode.value === 765 || selectedLotCode.value === 768
})

// Check if current game has extended columns (any non-PC28 type)
const hasExtendedColumns = computed(() => {
  return isSSCType.value || isRacingType.value
})

// Dynamic summary column label: "Champion-Runner Sum" for racing, "Total Sum" for others
const summaryLabel = computed(() => {
  return isRacingType.value ? '冠亚合' : '总和'
})

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

// Get ball image URL by number
// PC28 uses colorful balls (ball_cols_split: 00-27)
// SSC uses blue balls (ball_blue_split: 0-9, single digit)
const getBallImageUrl = (num: number): string => {
  if (isSSCType.value) {
    // SSC type (ShiShiCai): blue ball icons, single digit (0-9)
    return new URL(`../assets/游戏/ball_blue_split/ball_${num}.png`, import.meta.url).href
  } else if (isRacingType.value) {
    // Racing type (AU Lucky 10, etc.): colored square icons (1-10)
    return new URL(`../assets/游戏/ball_racing_split/ball_${num}.png`, import.meta.url).href
  } else {
    // PC28 type: colorful ball icons, padded to 2 digits (00-27)
    const padded = String(num).padStart(2, '0')
    return new URL(`../assets/游戏/ball_cols_split/ball_${padded}.png`, import.meta.url).href
  }
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
          <el-option v-for="game in gameOptions" :key="game.lotCode" :label="game.lotName" :value="game.lotCode" />
        </el-select>
        <div class="toolbar-title">历史开奖结果（{{ currentGameLabel }}）</div>
        <div class="toolbar-spacer" />
        <el-date-picker v-model="selectedDate" type="date" class="date-picker" placeholder="选择日期" size="small" format="YYYY-MM-DD" value-format="YYYY-MM-DD" :editable="false" />
      </div>

      <!-- AU10: Scroll container wraps table-shell for correct scrollbar position -->
      <div v-if="isAU10Type" class="au10-scroll-container">
        <div class="table-shell table-shell--au10">
          <el-table v-loading="loading" :data="tableData" border fit style="width: 100%" class="results-table au10-results-table" empty-text="暂无数据">
          <!-- Period column: proportional width (original 132px) -->
          <el-table-column prop="preDrawIssue" label="期数" align="center" width="79">
            <template #header><b class="header-text">期数</b></template>
          </el-table-column>
          <!-- Draw time column: proportional width (original 281px) -->
          <el-table-column prop="preDrawTime" label="开奖日期" align="center" width="168">
            <template #header><b class="header-text">开奖日期</b></template>
          </el-table-column>
          <!-- AU10 ball results column: uses racing ball images (25x25px) -->
          <el-table-column label="开奖结果" align="center" width="362">
            <template #header><b class="header-text">开奖结果</b></template>
            <template #default="{ row }">
              <div class="uno-flex-center">
                <img v-for="(ball, index) in parseBalls(row.preDrawCode)" :key="`${row.preDrawIssue}-ball-${index}`" :src="getBallImageUrl(parseInt(ball, 10))" :alt="`ball-${ball}`" class="ball-img" />
              </div>
            </template>
          </el-table-column>
          <!-- Summary column (Champion-Runner Sum): proportional width (original 150px), 3 subdivisions -->
          <el-table-column :label="summaryLabel" align="center" width="90">
            <template #header><b class="header-text">{{ summaryLabel }}</b></template>
            <template #default="{ row }">
              <div class="au10-summary-row">
                <div class="uno-b-r">{{ row.sumValue || '--' }}</div>
                <div class="uno-b-r" :style="{ color: row.sizeLabel === '大' ? 'red' : '' }">{{ row.sizeLabel || '--' }}</div>
                <div :style="{ color: row.parityLabel === '双' ? 'red' : '' }">{{ row.parityLabel || '--' }}</div>
              </div>
            </template>
          </el-table-column>
          <!-- AU10 1-5 Dragon/Tiger column: proportional width (original 200px), 5 subdivisions -->
          <el-table-column label="1-5龙虎" align="center" width="120">
            <template #header><b class="header-text">1-5龙虎</b></template>
            <template #default="{ row }">
              <div class="au10-dragon-tiger-row">
                <div v-for="(result, idx) in calcDragonTigerPairs(parseNumbers(row.preDrawCode))" :key="`dt-${idx}`" :class="['uno-b-r', idx === 4 ? 'b-r-none' : '', result === '龙' ? 'color-blue' : '']">{{ result }}</div>
              </div>
            </template>
          </el-table-column>
          <!-- AU10 BaoDou column: proportional width (original 40px), Dragon/Tiger comparing 1st vs 10th ball -->
          <el-table-column label="宝斗" align="center" width="39" class-name="baodou-column">
            <template #header><b class="header-text">宝斗</b></template>
            <template #default="{ row }">
              <div class="au10-summary-row baodou-cell">
                <!-- Safely parse numbers and compare 1st vs 10th ball for BaoDou result -->
                <div :class="(parseNumbers(row.preDrawCode)[0] ?? 0) > (parseNumbers(row.preDrawCode)[9] ?? 0) ? 'color-blue' : ''">{{ (parseNumbers(row.preDrawCode)[0] ?? 0) > (parseNumbers(row.preDrawCode)[9] ?? 0) ? '龙' : '虎' }}</div>
              </div>
            </template>
          </el-table-column>
          <!-- AU10 NiuNiu column: proportional width (original 240px), 6 sub-columns -->
          <el-table-column label="牛牛" align="center" width="143" class-name="niuniu-column">
            <template #header>
              <div class="au10-niuniu-header-container">
                <div class="au10-niuniu-main-header">牛牛</div>
                <div class="au10-niuniu-sub-header">
                  <div v-for="(header, idx) in niuNiuHeaders" :key="`nn-header-${idx}`" class="au10-niuniu-sub-item">{{ header }}</div>
                </div>
              </div>
            </template>
            <template #default="{ row }">
              <div class="au10-niuniu-row">
                <div v-for="(result, idx) in calcNiuNiu10(parseNumbers(row.preDrawCode))" :key="`nn-${idx}`" class="au10-niuniu-cell">{{ result }}</div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <!-- Pagination inside table-shell -->
        <div class="pagination-container">
          <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[20, 30, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper" background small @current-change="handlePageChange" @size-change="handleSizeChange" />
        </div>
        </div>
      </div>

      <!-- Non-AU10: Standard table layout -->
      <div v-else class="table-shell" :class="{ 'table-shell--ssc': hasExtendedColumns }">
        <el-table v-loading="loading" :data="tableData" border fit style="width: 100%" class="results-table" empty-text="暂无数据">
          <!-- Period column -->
          <el-table-column prop="preDrawIssue" label="期数" align="center" :width="hasExtendedColumns ? 70 : 120">
            <template #header><b class="header-text">期数</b></template>
          </el-table-column>

          <!-- Draw time column -->
          <el-table-column prop="preDrawTime" label="开奖日期" align="center" :width="hasExtendedColumns ? 170 : 180">
            <template #header><b class="header-text">开奖日期</b></template>
          </el-table-column>

          <!-- Ball results column -->
          <el-table-column label="开奖结果" align="center" :width="hasExtendedColumns ? 208 : 250">
            <template #header><b class="header-text">开奖结果</b></template>
            <template #default="{ row }">
              <div class="ball-list">
                <img v-for="(ball, index) in parseBalls(row.preDrawCode)" :key="`${row.preDrawIssue}-${index}`" :src="getBallImageUrl(parseInt(ball, 10))" :alt="`ball-${ball}`" class="ball-img" />
              </div>
            </template>
          </el-table-column>

          <!-- Summary column -->
          <el-table-column :label="summaryLabel" align="center" width="150">
            <template #header><b class="header-text">{{ summaryLabel }}</b></template>
            <template #default="{ row }">
              <div class="summary-row">
                <div class="summary-cell summary-cell--border">{{ row.sumValue || '--' }}</div>
                <div class="summary-cell summary-cell--border" :style="{ color: row.sizeLabel === '大' ? 'red' : '' }">{{ row.sizeLabel || '--' }}</div>
                <div class="summary-cell" :style="{ color: row.parityLabel === '双' ? 'red' : '' }">{{ row.parityLabel || '--' }}</div>
              </div>
            </template>
          </el-table-column>

          <!-- Dragon/Tiger column for non-PC28 types -->
          <el-table-column v-if="hasExtendedColumns" label="龙虎" align="center" width="60">
            <template #header><b class="header-text">龙虎</b></template>
            <template #default="{ row }">
              <div class="calc-cell">
                <span :class="getDragonTigerColor(calcDragonTiger(parseNumbers(row.preDrawCode)))">{{ calcDragonTiger(parseNumbers(row.preDrawCode)) }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- SSC-only extended columns -->
          <template v-if="isSSCType">
            <el-table-column label="前三" align="center" width="60">
              <template #header><b class="header-text">前三</b></template>
              <template #default="{ row }"><div class="calc-cell"><span>{{ calcThreePattern(parseNumbers(row.preDrawCode).slice(0, 3)) }}</span></div></template>
            </el-table-column>
            <el-table-column label="中三" align="center" width="60">
              <template #header><b class="header-text">中三</b></template>
              <template #default="{ row }"><div class="calc-cell"><span>{{ calcThreePattern(parseNumbers(row.preDrawCode).slice(1, 4)) }}</span></div></template>
            </el-table-column>
            <el-table-column label="后三" align="center" width="60">
              <template #header><b class="header-text">后三</b></template>
              <template #default="{ row }"><div class="calc-cell"><span>{{ calcThreePattern(parseNumbers(row.preDrawCode).slice(2, 5)) }}</span></div></template>
            </el-table-column>
            <el-table-column label="斗牛" align="center" width="60">
              <template #header><b class="header-text">斗牛</b></template>
              <template #default="{ row }"><div class="calc-cell"><span>{{ calcBullResult(parseNumbers(row.preDrawCode)) }}</span></div></template>
            </el-table-column>
          </template>
        </el-table>

        <!-- Pagination -->
        <div class="pagination-container">
          <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[20, 30, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper" background small @current-change="handlePageChange" @size-change="handleSizeChange" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* All table styles moved to DrawResults.css for better maintainability */
/* Scoped styles retained here only if Vue-specific deep selectors are required */

/* Date picker outer container: 130x24 per design spec */
:deep(.date-picker) {
  width: 130px !important;
  height: 24px !important;
}

:deep(.date-picker .el-input__wrapper) {
  height: 24px !important;
  line-height: 24px !important;
  padding: 0 8px !important;
}

:deep(.date-picker .el-input__inner) {
  height: 24px !important;
  line-height: 24px !important;
}
</style>
