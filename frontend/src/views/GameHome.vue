<script setup lang="ts">
import { ref, onMounted } from 'vue'
import GameHeader from '../components/GameHeader.vue'
import MemberSidebar from '../components/MemberSidebar.vue'
import NoticeDialog from '../components/NoticeDialog.vue'

// 控制公告弹窗显示
const showNoticeDialog = ref(false)

onMounted(() => {
  document.title = '游戏首页'
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) {
    favicon.href = '/favicon.png'
  }

  // 页面加载完成后，延迟500ms显示公告弹窗
  setTimeout(() => {
    showNoticeDialog.value = true
  }, 500)
})

// 关闭公告弹窗
const handleCloseNotice = () => {
  showNoticeDialog.value = false
}

/* ============ 两面长龙排行（后续接 API） ============ */
const dragonList = ref([
  { label: '第1球-大', value: '7期' },
  { label: '第2球-小', value: '3期' },
  { label: '第3球-单', value: '3期' },
  { label: '和值-双', value: '2期' },
])

const sumOdds = [
  { num: 0, odd: '399.88' },
  { num: 1, odd: '99.88' },
  { num: 2, odd: '79.88' },
  { num: 3, odd: '39.88' },
  { num: 4, odd: '29.88' },
  { num: 5, odd: '24.88' },
  { num: 6, odd: '19.88' },
  { num: 7, odd: '15.88' },
  { num: 8, odd: '12.88' },
  { num: 9, odd: '10.88' },
  { num: 10, odd: '9.88' },
  { num: 11, odd: '8.88' },
  { num: 12, odd: '6.89' },
  { num: 13, odd: '6.48' },
  { num: 14, odd: '6.48' },
  { num: 15, odd: '6.89' },
  { num: 16, odd: '8.88' },
  { num: 17, odd: '9.88' },
  { num: 18, odd: '10.88' },
  { num: 19, odd: '12.88' },
  { num: 20, odd: '15.88' },
  { num: 21, odd: '19.88' },
  { num: 22, odd: '24.88' },
  { num: 23, odd: '29.875' },
  { num: 24, odd: '39.88' },
  { num: 25, odd: '79.88' },
  { num: 26, odd: '99.88' },
  { num: 27, odd: '399.88' },
]

const sumGroups = Array.from({ length: 4 }, (_, col) =>
  sumOdds.slice(col * 7, col * 7 + 7)
)

const twoSideRows = [
  [
    { label: '大', odd: '1.44' },
    { label: '单', odd: '1.44' },
    { label: '极大', odd: '15.49' },
    { label: '大单', odd: '2.88' },
    { label: '大双', odd: '2.88' },
  ],
  [
    { label: '小', odd: '1.44' },
    { label: '双', odd: '1.44' },
    { label: '极小', odd: '15.49' },
    { label: '小单', odd: '2.88' },
    { label: '小双', odd: '2.88' },
  ],
]

const colorRows = [
  { label: '绿波', odd: '1.58' },
  { label: '蓝波', odd: '1.58' },
  { label: '红波', odd: '1.58' },
]

const patternRows = [
  { label: '豹子', odd: '49.88' },
  { label: '顺子', odd: '6.88' },
  { label: '对子', odd: '1.44' },
  { label: '半顺', odd: '1.34' },
  { label: '杂六', odd: '1.08' },
]

const getBallSrc = (num: number) => {
  const safe = Math.max(0, Math.min(27, num))
  const name = String(safe).padStart(2, '0')
  return new URL(`../assets/游戏/ball_cols_split/ball_${name}.png`, import.meta.url).href
}
</script>

<template>
  <div class="page">
    <GameHeader />

    <!-- 主体：92%宽度居中，白色背景，无底边框 -->
    <div class="main-wrapper">
      <!-- 三栏布局：左侧栏 + 主内容 + 右侧栏 -->
      <div class="main-body">
        <!-- 左侧：会员信息 -->
        <MemberSidebar />

        <!-- 中间：主内容区域 -->
        <div class="center-content">
          <div class="game-panel">
            <div class="issue-bar">
              <div class="issue-row issue-row-top">
                <div class="issue-left">
                  <span class="text-blue mr10">加拿大pc28</span>
                  <span class="text-red">今日输赢：0</span>
                </div>
                <div class="issue-right">
                  <b class="text-green mr10">3393560</b>
                  <span>期开奖：</span>
                  <img class="ball-img" :src="getBallSrc(3)" alt="3" />
                  <span class="symbol">+</span>
                  <img class="ball-img" :src="getBallSrc(9)" alt="9" />
                  <span class="symbol">+</span>
                  <img class="ball-img" :src="getBallSrc(6)" alt="6" />
                  <span class="symbol">=</span>
                  <img class="ball-img" :src="getBallSrc(18)" alt="18" />
                </div>
              </div>
              <div class="issue-row">
                <div class="issue-left">
                  <b class="text-green">3393562</b>
                  <span class="ml10">期</span>
                  <span class="text-blue ml10">两面盘</span>
                </div>
                <div class="issue-right">
                  <span class="ml40">距离封盘:</span>
                  <b class="time-box time-red ml5">00</b>
                  <span class="time-sep">:</span>
                  <b class="time-box time-red">02</b>
                  <span class="time-sep">:</span>
                  <b class="time-box time-red">52</b>
                  <span class="ml40">距离开奖:</span>
                  <b class="time-box time-green ml5">00</b>
                  <span class="time-sep">:</span>
                  <b class="time-box time-green">03</b>
                  <span class="time-sep">:</span>
                  <b class="time-box time-green">02</b>
                </div>
              </div>
            </div>

            <div class="quick-bar">
              <span class="quick-tab">快捷</span>
              <span class="quick-tab active">一般</span>
              <span class="text-blue ml10">金额</span>
              <input class="amount-input" type="text" />
              <button class="btn btn-ok">确定</button>
              <button class="btn btn-clear">清空</button>
              <button class="btn btn-save">保存</button>
              <span class="ml10">（说明）</span>
              <button class="btn btn-recent">最近开奖</button>
            </div>

            <h5 class="section-title">和值</h5>

            <div class="sum-grid">
              <div v-for="(group, groupIndex) in sumGroups" :key="groupIndex" class="sum-col">
                <div class="sum-head">
                  <div class="sum-head-cell">和值</div>
                  <div class="sum-head-cell">赔率</div>
                  <div class="sum-head-cell">金额</div>
                </div>
                <div v-for="item in group" :key="item.num" class="sum-row">
                  <div class="sum-cell ball-cell">
                    <img class="ball-img" :src="getBallSrc(item.num)" :alt="String(item.num)" />
                  </div>
                  <div class="sum-cell odd-cell">
                    <b class="text-red">{{ item.odd }}</b>
                  </div>
                  <div class="sum-cell input-cell">
                    <input class="cell-input" type="text" />
                  </div>
                </div>
              </div>
            </div>

            <h5 class="section-title two-side-title">两面</h5>
            <div class="two-side-grid">
              <div v-for="(row, index) in twoSideRows" :key="index" class="two-side-row">
                <div v-for="item in row" :key="item.label" class="two-side-item">
                  <span class="label">{{ item.label }}</span>
                  <span class="odd text-red">{{ item.odd }}</span>
                  <div class="input-box">
                    <input class="cell-input" type="text" />
                  </div>
                </div>
              </div>
            </div>

            <h5 class="section-title color-title">色波</h5>
            <div class="color-grid">
              <div v-for="item in colorRows" :key="item.label" class="color-item">
                <span class="label" :class="`label-${item.label}`">{{ item.label }}</span>
                <span class="odd text-red">{{ item.odd }}</span>
                <div class="input-box">
                  <input class="cell-input" type="text" />
                </div>
              </div>
            </div>

            <h5 class="section-title pattern-title">豹子/顺子/对子</h5>
            <div class="pattern-grid">
              <div v-for="item in patternRows" :key="item.label" class="pattern-item">
                <span class="label">{{ item.label }}</span>
                <span class="odd text-red">{{ item.odd }}</span>
                <div class="input-box">
                  <input class="cell-input" type="text" />
                </div>
              </div>
            </div>

            <div class="quick-bar quick-bar-bottom">
              <span class="quick-tab">快捷</span>
              <span class="quick-tab active">一般</span>
              <span class="text-blue ml10">金额</span>
              <input class="amount-input" type="text" />
              <button class="btn btn-ok">确定</button>
              <button class="btn btn-clear">清空</button>
              <button class="btn btn-save">保存</button>
              <span class="ml10">（说明）</span>
              <button class="btn btn-recent">最近开奖</button>
            </div>

            <div class="summary-bar">
              <span class="summary-item">和值</span>
              <span class="summary-item">和值大小</span>
              <span class="summary-item">和值单双</span>
            </div>

            <div class="summary-values">
              <div v-for="n in 24" :key="n" class="summary-value">
                <span class="value-text">{{ [10, 14, 18, 16, 13, 17, 12, 13, 5, 19, 1, 6, 19, 10, 8, 7, 16, 19, 15, 10, 15, 10, 22, 13][n - 1] }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：公告 + 长龙排行 -->
        <div class="right-sidebar">
          <!-- 公告标题 -->
          <div class="announce-header">
            <span class="announce-title">公告</span>
            <span class="more-link">更多</span>
          </div>
          <!-- 公告内容 -->
          <div class="announce-body">
            <p>尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)</p>
          </div>
          <!-- 两面长龙排行 -->
          <div class="dragon-header">两面长龙排行</div>
          <div class="dragon-list">
            <div v-for="item in dragonList" :key="item.label" class="dragon-row">
              <span class="dragon-label">{{ item.label }}</span>
              <span class="dragon-value">{{ item.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部滚动公告栏 -->
    <div class="footer-bar">
      <marquee
        behavior="scroll"
        direction="left"
        scrollamount="3"
        class="footer-marquee"
      >
        尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)
      </marquee>
    </div>

    <!-- 公告弹窗 -->
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
}

/* ==================== 主体容器 ==================== */
/* 原版：w-92% m-auto bg-[#fff] mt5 b-b-none */
/* 不使用 flex:1，白色区域只占内容需要的高度 */
.main-wrapper {
  width: 92%;
  margin: 5px auto 0;
  background: #fff;
  border-bottom: none;
}

/* 三栏 flex 布局 —— 不强制 min-height，高度由内容决定 */
.main-body {
  display: flex;
}

/* ==================== 中间内容区域 ==================== */
.center-content {
  flex: 1;
  min-height: 500px; /* 中间内容区合理最小高度，后续有真实内容后可去除 */
  border-left: none;
  border-right: none;
}

.placeholder {
  padding: 24px;
  color: #999;
  font-size: 14px;
  text-align: center;
}

.game-panel {
  width: 720px;
  height: 733px;
  margin: 5px 0 30px;
  position: relative;
}

.issue-bar {
  width: 720px;
  height: 56px;
  padding: 5px 20px;
  border: 1px solid #efba84;
  border-bottom: 1px solid #efba84;
  border-radius: 4px 4px 0 0;
  font-size: 12px;
  box-sizing: border-box;
}

.issue-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 25px;
}

.issue-row-top {
  margin-bottom: 5px;
}

.issue-left,
.issue-right {
  display: flex;
  align-items: center;
}

.text-blue {
  color: blue;
}

.text-red {
  color: red;
}

.text-green {
  color: green;
}

.ml5 {
  margin-left: 5px;
}

.ml10 {
  margin-left: 10px;
}

.mr10 {
  margin-right: 10px;
}

.ml40 {
  margin-left: 40px;
}

.ball-img {
  width: 27px;
  height: 27px;
  margin-left: 6px;
  display: inline-block;
}

.symbol {
  margin: 0 6px;
  font-size: 12px;
  color: #333;
}

.time-box {
  display: inline-block;
  width: 16px;
  height: 17px;
  line-height: 17px;
  text-align: center;
  border-radius: 3px;
  font-size: 13px;
}

.time-red {
  color: red;
}

.time-green {
  color: green;
}

.time-sep {
  margin: 0 4px;
  color: #333;
}

.quick-bar {
  width: 720px;
  height: 49px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #efba84;
  border-top: none;
  border-bottom: none;
  background: #fff1e4;
  box-sizing: border-box;
  font-size: 12px;
  gap: 6px;
}

.quick-bar-bottom {
  height: 48px;
  margin-top: 10px;
  background: transparent;
  border-color: transparent;
}

.quick-tab {
  width: 35px;
  text-align: center;
  cursor: pointer;
}

.quick-tab.active {
  background: #ffffbf;
  border: 1px solid #efba84;
  height: 25px;
  line-height: 25px;
}

.amount-input {
  width: 55px;
  height: 24px;
  border: 1px solid #a0b4d8;
  box-sizing: border-box;
}

.btn {
  width: 46px;
  height: 20px;
  border: none;
  font-size: 12px;
  cursor: pointer;
  color: #fff;
}

.btn-ok {
  background: #63a35c;
}

.btn-clear {
  background: #4a90e2;
}

.btn-save {
  background: #f5a623;
}

.btn-recent {
  width: 72px;
  background: #f5a623;
}

.section-title {
  width: 720px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  font-size: 14px;
  font-weight: 400;
  color: #000;
  border: 1px solid #efba84;
  border-bottom: none;
  background: #fff1e4;
  margin: 0;
  box-sizing: border-box;
}

.two-side-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid #efba84;
  border-right: 1px solid #efba84;
  background: #fff1e4;
}

.color-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid #efba84;
  border-right: 1px solid #efba84;
  background: #fff1e4;
}

.sum-grid {
  width: 720px;
  display: flex;
  border: 1px solid #efba84;
  border-top: 1px solid #efba84;
  box-sizing: border-box;
}

.sum-col {
  width: 25%;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
}

.sum-col:last-child {
  border-right: none;
}

.sum-head {
  display: flex;
  height: 30px;
  line-height: 30px;
  background: #fff1e4;
  border-bottom: 1px solid #efba84;
}

.sum-head-cell {
  flex: 1;
  text-align: center;
  border-right: 1px solid #efba84;
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
  border-bottom: 1px solid #efba84;
}

.sum-col .sum-row:last-child {
  border-bottom: none;
}

.sum-row:hover .sum-cell,
.sum-row:focus-within .sum-cell {
  background: #be9d76;
}

.sum-cell {
  flex: 1;
  text-align: center;
  border-right: 1px solid #efba84;
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

.sum-cell:last-child {
  border-right: none;
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

.two-side-grid {
  width: 720px;
  border: 1px solid #efba84;
  border-top: 1px solid #efba84;
  box-sizing: border-box;
}

.two-side-row {
  display: flex;
  height: 30px;
  line-height: 30px;
  border-bottom: 1px solid #efba84;
}

.two-side-row:last-child {
  border-bottom: none;
}

.two-side-item {
  width: 20%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
}

.two-side-item:last-child {
  border-right: none;
}

.two-side-item .label {
  width: 30px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  background: #fff1e4;
  color: #000;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  display: inline-block;
  font-size: 13px;
}

.two-side-item .odd {
  width: 56.83px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.two-side-item .odd b {
  display: inline-block;
  width: 56.83px;
  height: 28px;
  line-height: 28px;
  text-align: center;
}

.two-side-item .input-box {
  width: 56px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.color-grid,
.pattern-grid {
  width: 720px;
  display: flex;
  border: 1px solid #efba84;
  border-top: 1px solid #efba84;
  box-sizing: border-box;
}

.color-item {
  width: 33.33%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
}

.color-item:last-child {
  border-right: none;
}

.color-item .label {
  width: 60px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
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
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-item .odd b {
  display: inline-block;
  width: 27.77px;
  height: 30px;
  line-height: 30px;
  text-align: center;
}

.color-item .input-box {
  width: 89px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.pattern-title {
  border-top: none;
  border-bottom: none;
  border-left: 1px solid #efba84;
  border-right: 1px solid #efba84;
  background: #fff1e4;
}

.pattern-item {
  width: 20%;
  display: flex;
  align-items: center;
  height: 30px;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
}

.pattern-item:last-child {
  border-right: none;
}

.pattern-item .label {
  width: 32px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
  font-weight: 700;
  font-size: 12px;
  box-sizing: border-box;
}

.pattern-item .odd {
  width: 56px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-right: 1px solid #efba84;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pattern-item .input-box {
  width: 55px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.pattern-item .cell-input {
  width: 50px;
  height: 20px;
}

.summary-bar {
  width: 720px;
  height: 30px;
  line-height: 30px;
  display: flex;
  border: 1px solid #efba84;
  border-top: none;
  background: #fff1e4;
  box-sizing: border-box;
}

.summary-item {
  flex: 1;
  text-align: center;
}

.summary-values {
  width: 718px;
  height: 49px;
  display: flex;
  border: 1px solid #efba84;
  border-top: none;
  box-sizing: border-box;
}

.summary-value {
  flex: 1;
  text-align: center;
  border-right: 1px solid #efba84;
  background: #ffe3ec;
  box-sizing: border-box;
}

.summary-value:last-child {
  border-right: none;
}

.value-text {
  display: block;
  padding-top: 5px;
}

/* ==================== 右侧公告栏 ==================== */
.right-sidebar {
  width: 160px;
  flex-shrink: 0;
  margin-left: 10px;
}

/* 公告标题行 */
.announce-header {
  height: 45px;
  line-height: 45px;
  padding: 0 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #efba84;
  background: linear-gradient(to bottom, #ab6939 0%, #3a1c04 100%);
  color: #fff;
  font-weight: 400;
  font-size: 13px;
}

.announce-title {
  font-size: 13px;
  font-weight: 400;
  color: #fff;
}

.more-link {
  font-size: 13px;
  font-weight: 400;
  color: #fff;
  cursor: pointer;
}

.more-link:hover {
  text-decoration: underline;
}

/* 公告内容 */
.announce-body {
  padding: 10px;
  width: 160px;
  height: 177px;
  font-size: 13px;
  line-height: 26px;
  color: #000;
  border: 1px solid #efba84;
  border-top: none;
  box-sizing: border-box;
  overflow: hidden;
  text-align: left;
  word-break: break-word;
  white-space: normal;
}

.announce-body p {
  margin: 0;
}

/* 两面长龙排行标题 */
.dragon-header {
  height: 45px;
  line-height: 45px;
  text-align: center;
  font-size: 14px;
  font-weight: 400;
  color: #fff;
  background: linear-gradient(to bottom, #ab6939 0%, #3a1c04 100%);
  border-bottom: 1px solid #efba84;
}

/* 长龙列表 */
.dragon-list {
  padding: 0;
  border: 1px solid #efba84;
  border-top: none;
}

.dragon-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 26px;
  line-height: 26px;
  font-size: 13px;
  border-bottom: 1px solid #efba84;
}

.dragon-row:last-child {
  border-bottom: none;
}

.dragon-label {
  width: 50%;
  height: 25px;
  line-height: 25px;
  padding-left: 10px;
  color: #000;
  border-right: 1px solid #efba84;
  background: #fff1e4;
  box-sizing: border-box;
}

.dragon-value {
  width: 50%;
  padding-left: 10px;
  color: red;
  font-weight: 400;
  box-sizing: border-box;
}

/* ==================== 底部滚动公告栏 ==================== */
/* margin-top: auto 配合 page 的 flex-column，始终推到页面最底部 */
.footer-bar {
  width: 92%;
  margin: 0 auto;
  margin-top: auto;
  height: 30px;
  line-height: 30px;
  background: #2b1204;
  overflow: hidden;
}

.footer-marquee {
  color: red;
  font-size: 13px;
  font-weight: 400;
  line-height: 30px;
}
</style>
