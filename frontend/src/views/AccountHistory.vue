<script setup lang="ts">
// Account History Page - displays transaction records for last two weeks
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import GameHeader from '../components/GameHeader.vue'
import MemberSidebar from '../components/MemberSidebar.vue'

const router = useRouter()

// Data interface for account history record
interface HistoryRecord {
  date: string
  weekday: string
  orderCount: number
  betAmount: number
  validAmount: number
  rebate: number
  winLoss: number
}

// Table data arrays - will be populated by API
const lastWeekData = ref<HistoryRecord[]>([])
const thisWeekData = ref<HistoryRecord[]>([])

// TODO: fetch data from API

// Weekday mapping
const weekdayMap = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// Format date as YYYY-MM-DD
const formatDate = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// Get weekday name from date
const getWeekday = (date: Date): string => {
  return weekdayMap[date.getDay()] ?? ''
}

// Compute last week dates (Monday to Sunday)
const lastWeekDates = computed(() => {
  const today = new Date()
  const currentDay = today.getDay() || 7 // Convert Sunday from 0 to 7
  
  // Last week Monday
  const lastMonday = new Date(today)
  lastMonday.setDate(today.getDate() - currentDay - 6)
  
  const dates: { date: string; weekday: string }[] = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(lastMonday)
    d.setDate(lastMonday.getDate() + i)
    dates.push({
      date: formatDate(d),
      weekday: getWeekday(d)
    })
  }
  return dates
})

// Compute this week dates (Monday to Sunday)
const thisWeekDates = computed(() => {
  const today = new Date()
  const currentDay = today.getDay() || 7 // Convert Sunday from 0 to 7
  
  // This week Monday
  const thisMonday = new Date(today)
  thisMonday.setDate(today.getDate() - currentDay + 1)
  
  const dates: { date: string; weekday: string }[] = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(thisMonday)
    d.setDate(thisMonday.getDate() + i)
    dates.push({
      date: formatDate(d),
      weekday: getWeekday(d)
    })
  }
  return dates
})

// Generate table data with dates (using API data or zeros)
const lastWeekTableData = computed(() => {
  return lastWeekDates.value.map((dateInfo, index) => {
    const record = lastWeekData.value[index]
    return {
      date: dateInfo.date,
      weekday: dateInfo.weekday,
      orderCount: record?.orderCount ?? 0,
      betAmount: record?.betAmount ?? 0,
      validAmount: record?.validAmount ?? 0,
      rebate: record?.rebate ?? 0,
      winLoss: record?.winLoss ?? 0
    }
  })
})

const thisWeekTableData = computed(() => {
  return thisWeekDates.value.map((dateInfo, index) => {
    const record = thisWeekData.value[index]
    return {
      date: dateInfo.date,
      weekday: dateInfo.weekday,
      orderCount: record?.orderCount ?? 0,
      betAmount: record?.betAmount ?? 0,
      validAmount: record?.validAmount ?? 0,
      rebate: record?.rebate ?? 0,
      winLoss: record?.winLoss ?? 0
    }
  })
})

// Summary method for last week table
const getLastWeekSummary = () => {
  const data = lastWeekTableData.value
  return [
'上周总计:',
    String(data.reduce((sum, item) => sum + item.orderCount, 0)),
    String(data.reduce((sum, item) => sum + item.betAmount, 0)),
    String(data.reduce((sum, item) => sum + item.validAmount, 0)),
    String(data.reduce((sum, item) => sum + item.rebate, 0)),
    String(data.reduce((sum, item) => sum + item.winLoss, 0))
  ]
}

// Summary method for this week table
const getThisWeekSummary = () => {
  const data = thisWeekTableData.value
  return [
'本周总计:',
    String(data.reduce((sum, item) => sum + item.orderCount, 0)),
    String(data.reduce((sum, item) => sum + item.betAmount, 0)),
    String(data.reduce((sum, item) => sum + item.validAmount, 0)),
    String(data.reduce((sum, item) => sum + item.rebate, 0)),
    String(data.reduce((sum, item) => sum + item.winLoss, 0))
  ]
}

// Navigate to game page on "more" click
const onMoreClick = () => {
  router.push('/game')
}
</script>

<template>
  <div class="account-history-page">
    <!-- Top navigation header with hidden sub-nav -->
    <GameHeader hide-sub-nav />

    <!-- Main content area -->
    <div class="main-wrapper">
      <div class="main-body">
        <!-- Left: Member sidebar -->
        <MemberSidebar />

        <!-- Center: Main content -->
        <div class="center-content">
          <!-- Title bar -->
          <h3 class="page-title">账户历史 - 近两周</h3>

          <!-- Last week table -->
          <div class="table-wrapper">
            <el-table
              :data="lastWeekTableData"
              border
              show-summary
              :summary-method="getLastWeekSummary"
              class="history-table"
            >
              <el-table-column prop="date" align="center" min-width="196">
                <template #header>
                  <b class="table-header-text">交易日期</b>
                </template>
                <template #default="{ row }">
                  <span>{{ row.date }} (<span class="weekday-text">{{ row.weekday }}</span>)</span>
                </template>
              </el-table-column>
              <el-table-column prop="orderCount" align="center" width="196">
                <template #header>
                  <b class="table-header-text">注单笔数</b>
                </template>
              </el-table-column>
              <el-table-column prop="betAmount" align="center" width="196">
                <template #header>
                  <b class="table-header-text">投注金额</b>
                </template>
              </el-table-column>
              <el-table-column prop="validAmount" align="center" width="196">
                <template #header>
                  <b class="table-header-text">有效金额</b>
                </template>
                <template #default="{ row }">
                  <span class="valid-amount">{{ row.validAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="rebate" align="center" width="196">
                <template #header>
                  <b class="table-header-text">退水</b>
                </template>
              </el-table-column>
              <el-table-column prop="winLoss" align="center" width="196">
                <template #header>
                  <b class="table-header-text">输赢</b>
                </template>
                <template #default="{ row }">
                  <span class="win-loss">{{ row.winLoss }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- This week table -->
          <div class="table-wrapper this-week-table">
            <el-table
              :data="thisWeekTableData"
              border
              show-summary
              :summary-method="getThisWeekSummary"
              class="history-table"
            >
              <el-table-column prop="date" align="center" min-width="196">
                <template #header>
                  <b class="table-header-text">交易日期</b>
                </template>
                <template #default="{ row }">
                  <span>{{ row.date }} (<span class="weekday-text">{{ row.weekday }}</span>)</span>
                </template>
              </el-table-column>
              <el-table-column prop="orderCount" align="center" width="196">
                <template #header>
                  <b class="table-header-text">注单笔数</b>
                </template>
              </el-table-column>
              <el-table-column prop="betAmount" align="center" width="196">
                <template #header>
                  <b class="table-header-text">投注金额</b>
                </template>
              </el-table-column>
              <el-table-column prop="validAmount" align="center" width="196">
                <template #header>
                  <b class="table-header-text">有效金额</b>
                </template>
                <template #default="{ row }">
                  <span class="valid-amount">{{ row.validAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="rebate" align="center" width="196">
                <template #header>
                  <b class="table-header-text">退水</b>
                </template>
              </el-table-column>
              <el-table-column prop="winLoss" align="center" width="196">
                <template #header>
                  <b class="table-header-text">输赢</b>
                </template>
                <template #default="{ row }">
                  <span class="win-loss">{{ row.winLoss }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom fixed announcement bar -->
    <div class="footer-bar">
      <div class="marquee-box">
        <div class="marquee-content">
          <span class="footer-marquee-text">尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)</span>
        </div>
      </div>
      <div class="footer-more" @click="onMoreClick">更多</div>
    </div>
  </div>
</template>

<style scoped>
/* Page container - use white bg to avoid exposing theme gradient on the right side */
.account-history-page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  padding-bottom: 34px;
  overflow-x: hidden;
}

/* Main wrapper - matches BetStatus/GameHome layout */
.main-wrapper {
  width: 100%;
  margin: 5px 0 0;
  padding-right: 28px;
  background: #fff;
  flex: 1;
  box-sizing: border-box;
}

.main-body {
  display: flex;
  align-items: flex-start;
}

/* Content area - flex fill remaining space, match design mt5 pb20 */
.center-content {
  flex: 1;
  min-width: 0;
  margin-top: 5px;
  padding-bottom: 20px;
  box-sizing: border-box;
}

/* Title bar - bg-primary4 gradient with border, 36px height (includes 1px top border) */
.page-title {
  width: 100%;
  font-size: 16px;
  font-weight: bold;
  background: var(--bw-bg-4, linear-gradient(to bottom, #fff1e4 0%, #ffddc0 100%));
  text-align: center;
  height: 35px;
  line-height: 35px;
  border: 1px solid var(--bw-border-color, #EFBA84);
  border-bottom: none;
  margin: 0;
  box-sizing: border-box;
}

/* Table container - width 100% since parent is 1176px */
.table-wrapper {
  width: 100%;
  border: 1px solid var(--bw-border-color, #EFBA84);
  border-top: none;
  border-bottom: none;
  box-sizing: border-box;
}

.this-week-table {
  margin-top: 5px;
  border-top: 1px solid var(--bw-border-color, #EFBA84);
}

/* Table styles */
.history-table {
  width: 100%;
  --el-table-border-color: var(--bw-border-color, #EFBA84);
}

.table-header-text {
  font-size: 14px;
  color: #000;
  font-weight: bold;
}

/* Table header with theme gradient */
:deep(.el-table__header-wrapper) {
  background: var(--bw-table-header-bg-color, linear-gradient(to bottom, #f2f8fc 0%, #bae0fd 100%));
}

:deep(.el-table__header th.el-table__cell) {
  background: var(--bw-table-header-bg-color, linear-gradient(to bottom, #f2f8fc 0%, #bae0fd 100%));
  height: 34px;
  padding: 0;
}

/* Table body row height */
:deep(.el-table__body tr) {
  height: 34px;
}

:deep(.el-table__body td.el-table__cell) {
  height: 34px;
  padding: 0;
}

/* Table footer background */
:deep(.el-table__footer-wrapper) {
  background: var(--bw-table-tfooter-bg-color, #ffe3ec);
}

:deep(.el-table__footer td.el-table__cell) {
  background: var(--bw-table-tfooter-bg-color, #ffe3ec);
  height: 27px;
  line-height: 27px;
  padding: 0;
}

/* Weekday text color - blue */
.weekday-text {
  color: blue;
}

/* Valid amount column - green */
.valid-amount {
  color: green;
}

/* Win/Loss column - red */
.win-loss {
  color: red;
}

/* Summary row valid amount and win/loss colors */
:deep(.el-table__footer td:nth-child(4) .cell) {
  color: green;
}

:deep(.el-table__footer td:nth-child(6) .cell) {
  color: red;
}

/* Bottom announcement bar */
.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  width: 100%;
  height: 34px;
  line-height: 34px;
  background-color: var(--el-bg-color, #fff);
  border-top: 1px solid var(--bw-border-color, #EFBA84);
  overflow: hidden;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}

.marquee-box {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  position: relative;
}

.marquee-content {
  display: inline-block;
  white-space: nowrap;
  line-height: 34px;
  padding-left: 100%;
  animation: marquee-scroll 35s linear infinite;
}

@keyframes marquee-scroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-100%);
  }
}

.footer-marquee-text {
  font-size: 12px;
  font-weight: 400;
  color: var(--el-text-color-regular, #333);
}

.footer-more {
  flex-shrink: 0;
  padding: 0 12px;
  font-size: 12px;
  color: var(--el-color-danger, red);
  cursor: pointer;
  user-select: none;
}

.footer-more:hover {
  text-decoration: underline;
}
</style>
