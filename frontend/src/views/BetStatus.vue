<script setup lang="ts">
// Bet Status Page - displays unsettled bet records
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import GameHeader from '../components/GameHeader.vue'
import MemberSidebar from '../components/MemberSidebar.vue'
import { betApi } from '@/api/index'

const router = useRouter()

// Table data interface
interface BetRecord {
  orderNo: string
  time: string
  type: string
  playMethod: string
  handicap: string
  betAmount: number
  rebate: number
  canWin: number
}

// Table data - populated by API
const tableData = ref<BetRecord[]>([])
const loading = ref(false)

// Pagination state
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

/**
 * Fetch unsettled bet records from backend API.
 * Supports pagination via page and size parameters.
 */
const fetchData = async () => {
  loading.value = true
  try {
    const res = await betApi.getUnsettled(currentPage.value, pageSize.value) as any
    if (res?.code === 200 && res.data) {
      tableData.value = (res.data.list || []).map((item: any) => ({
        orderNo: item.orderNo,
        time: item.time,
        type: item.type,
        playMethod: item.playMethod,
        handicap: item.handicap || '',
        betAmount: Number(item.betAmount) || 0,
        rebate: Number(item.rebate) || 0,
        canWin: Number(item.canWin) || 0
      }))
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('Failed to fetch unsettled bets:', err)
  } finally {
    loading.value = false
  }
}

// Fetch data on component mount
onMounted(() => {
  fetchData()
})

// Summary computed values
const summaryData = computed(() => ({
  totalAmount: tableData.value.reduce((sum, item) => sum + item.betAmount, 0),
  totalRebate: tableData.value.reduce((sum, item) => sum + item.rebate, 0),
  totalCanWin: tableData.value.reduce((sum, item) => sum + item.canWin, 0)
}))

// Pagination handlers
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1 // Reset to first page when page size changes
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchData()
}

// Table footer summary method
const getSummaries = () => {
  return [
    '',
    '',
    '',
    '有效金额合计',
    '',
    `${summaryData.value.totalAmount}(+0)`,
    String(summaryData.value.totalRebate),
    String(summaryData.value.totalCanWin)
  ]
}

// Navigate to game page on "more" click
const onMoreClick = () => {
  router.push('/game')
}
</script>

<template>
  <div class="bet-status-page">
    <!-- Top navigation header -->
    <GameHeader />

    <!-- Main content area -->
    <div class="main-wrapper">
      <div class="main-body">
        <!-- Left: Member sidebar -->
        <MemberSidebar />

        <!-- Center: Main content -->
        <div class="center-content">
          <!-- Title bar -->
          <h3 class="page-title">
            <b class="title-left">未结算</b>
            <span>下注状况</span>
          </h3>

          <!-- Data table -->
          <div class="table-wrapper">
            <el-table
              :data="tableData"
              border
              stripe
              fit
              :show-summary="tableData.length > 0"
              :summary-method="getSummaries"
              class="bet-table"
              empty-text="暂无数据"
            >
              <!-- First column without fixed width to auto-expand and fill remaining space -->
              <el-table-column prop="orderNo" align="center" min-width="231">
                <template #header>
                  <b class="table-header-text">注单号</b>
                </template>
              </el-table-column>
              <el-table-column prop="time" align="center" width="171">
                <template #header>
                  <b class="table-header-text">时间</b>
                </template>
              </el-table-column>
              <el-table-column prop="type" align="center" width="171">
                <template #header>
                  <b class="table-header-text">类型</b>
                </template>
              </el-table-column>
              <el-table-column prop="playMethod" align="center" width="143">
                <template #header>
                  <b class="table-header-text">玩法</b>
                </template>
              </el-table-column>
              <el-table-column prop="handicap" align="center" width="100">
                <template #header>
                  <b class="table-header-text">盘口</b>
                </template>
              </el-table-column>
              <el-table-column prop="betAmount" align="center" width="140">
                <template #header>
                  <b class="table-header-text">下注金额</b>
                </template>
              </el-table-column>
              <el-table-column prop="rebate" align="center" width="80">
                <template #header>
                  <b class="table-header-text">退水</b>
                </template>
              </el-table-column>
              <el-table-column prop="canWin" align="center" width="140">
                <template #header>
                  <b class="table-header-text">可贏</b>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Pagination -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[20, 50, 100]"
              :total="total"
              size="small"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :prev-text="'上一页'"
              :next-text="'下一页'"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
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
.bet-status-page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  padding-bottom: 34px;
  /* Prevent horizontal overflow from causing visible background color bars on the right side */
  overflow-x: hidden;
}

/* Main wrapper - matches GameHome layout */
.main-wrapper {
  width: 100%;
  margin: 5px 0 0;
  padding-right: 10px;
  background: #fff;
  flex: 1;
  box-sizing: border-box;
}

.main-body {
  display: flex;
  align-items: flex-start;
}

.center-content {
  flex: 1;
  padding: 0;
  min-width: 0;
}

/* Title bar - bg-primary4 gradient with border */
.page-title {
  font-size: 16px;
  font-weight: bold;
  background: var(--bw-bg-4, linear-gradient(to bottom, #fff1e4 0%, #ffddc0 100%));
  text-align: center;
  height: 36px;
  line-height: 36px;
  border: 1px solid var(--bw-border-color, #EFBA84);
  border-bottom: none;
  position: relative;
  margin: 5px 0 0 0;
}

.title-left {
  position: absolute;
  left: 20px;
}

/* Table container */
.table-wrapper {
  width: 100%;
  border: 1px solid var(--bw-border-color, #EFBA84);
  border-top: none;
  border-bottom: none;
}

/* Table styles */
.bet-table {
  width: 100%;
  --el-table-border-color: var(--bw-border-color, #EFBA84);
}

.table-header-text {
  font-size: 14px;
  color: var(--el-text-color-regular, #000); /* theme-compatible text color */
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

/* Table footer background */
:deep(.el-table__footer-wrapper) {
  background: var(--bw-table-tfooter-bg-color, #ffe3ec);
}

:deep(.el-table__footer td.el-table__cell) {
  background: var(--bw-table-tfooter-bg-color, #ffe3ec);
}

/* Pagination container */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 8px 0;
  border: 1px solid var(--bw-border-color, #EFBA84);
  border-top: none;
}

:deep(.el-pagination) {
  --el-pagination-button-disabled-bg-color: transparent;
}

:deep(.el-pagination .el-select .el-input) {
  width: 100px;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background: transparent;
  border: 1px solid var(--bw-border-color, #EFBA84);
  border-radius: 2px;
  padding: 0 8px;
}

:deep(.el-pagination .el-pager li) {
  border: 1px solid var(--bw-border-color, #EFBA84);
  border-radius: 2px;
  background: transparent;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: var(--el-color-primary, #013f66);
  color: #fff;
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
  background-color: var(--el-bg-color, #fff); /* theme-compatible background */
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
  color: var(--el-text-color-regular, #333); /* theme-compatible text color */
}

.footer-more {
  flex-shrink: 0;
  padding: 0 12px;
  font-size: 12px;
  color: var(--el-color-danger, red); /* theme-compatible danger color */
  cursor: pointer;
  user-select: none;
}

.footer-more:hover {
  text-decoration: underline;
}
</style>
