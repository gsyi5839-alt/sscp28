<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

/* ============ Member Info ============ */
const username = computed(() => authStore.user?.username || 'ww12506')
const panelType = computed(() => 'A盘')

/* ============ Credit Info (API integration pending) ============ */
const creditLimit = computed(() => 0)
const availableCredit = computed(() => 0)
const betAmount = computed(() => 0)
const frozenAmount = computed(() => 0)

/* ============ Left Side Game Quick Access ============ */
const sideGames = [
  { key: 'bw128', label: 'BW128开奖网', url: 'https://bw1283.cc/#/home' },
  { key: 'caPc28', label: '加拿大PC28', url: 'https://bw1284.cc/#/detail/1725113792562?lotCode=720' },
  { key: 'caSsc', label: '加拿大时时彩', url: 'https://bw1284.cc/#/detail/1725113905463?lotCode=719' },
  { key: 'happyRacing', label: '欢乐赛车', url: 'https://bw1283.cc/#/detail/1725113915131?lotCode=763' },
  { key: 'happySsc', label: '欢乐时时彩', url: 'https://bw1285.cc/#/detail/1725113923036?lotCode=762' },
]

const onGameClick = (url: string) => {
  // Open game link in new tab
  window.open(url, '_blank')
}
</script>

<template>
  <div class="member-sidebar">
    <!-- 标题：会员信息 -->
    <div class="sidebar-title">会员信息</div>

    <!-- 用户名 -->
    <div class="username">{{ username }}({{ panelType }})</div>

    <!-- 额度信息 -->
    <div class="info-list">
      <div class="info-row">
        <span class="info-label">信用额度：</span>
        <span class="info-value">{{ creditLimit }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">可用额度：</span>
        <span class="info-value">{{ availableCredit }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">下注金额：</span>
        <span class="info-value">{{ betAmount }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">冻结金额：</span>
        <span class="info-value">{{ frozenAmount }}</span>
      </div>
    </div>

    <!-- 游戏快捷按钮 -->
    <!-- 原版：el-button type="primary" size="large" block w100% mb10 -->
    <div class="game-buttons">
      <el-button
        v-for="game in sideGames"
        :key="game.key"
        type="primary"
        size="large"
        class="game-btn"
        @click="onGameClick(game.url)"
      >
        {{ game.label }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.member-sidebar {
  width: 200px;
  flex-shrink: 0;
  /* Align top with game-panel top border (game-panel has margin-top: 5px) */
  margin-top: 5px;
}

/* Uniform content width (consistent with design 184px) */
.sidebar-title,
.username,
.info-list,
.info-row {
  width: 184px;
  margin: 0 auto;
  box-sizing: border-box;
}

/* ========== Title: Member Info ========== */
/* Original: uno-b-b color-[#fff] font-400 text-center h45 line-height-45 gradient-bg3 */
.sidebar-title {
  height: 45px;
  line-height: 45px;
  text-align: center;
  color: #fff;
  font-weight: 400;
  font-size: 13px;
  /* Original .uno-b-b: border-bottom 1px solid var(--bw-border-color, #efba84) */
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  /* Original gradient-bg3 = --bw-linear-bg */
  background: var(--bw-linear-bg, linear-gradient(to bottom, #a6744d 0%, #351c0c 100%));
}

/* ========== Username ========== */
/* Original: uno-b-b font-400 color-[red] text-center h32 line-height-32 */
.username {
  height: 32px;
  line-height: 32px;
  text-align: center;
  color: red;
  font-weight: 400;
  font-size: 13px;
  /* Original .uno-b-b: border-bottom 1px solid var(--bw-border-color, #efba84) */
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  border-left: 1px solid var(--bw-border-color, #efba84);
  border-right: 1px solid var(--bw-border-color, #efba84);
}

/* ========== Credit Info (table style) ========== */
/* Original border color: --bw-border-color: #EFBA84 */
/* No border-top needed, .username's border-bottom provides top border */
.info-list {
  padding: 0;
  border-left: 1px solid var(--bw-border-color, #efba84);
  border-right: 1px solid var(--bw-border-color, #efba84);
}

.info-row {
  display: flex;
  align-items: center;
  height: 32px;
  line-height: 32px;
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
}

.info-label {
  width: 90px;
  font-size: 12px;
  color: #000;
  font-weight: 400;
  flex-shrink: 0;
  text-align: center;
  /* Original: border-right: 1px solid var(--bw-border-color, #efba84) */
  border-right: 1px solid var(--bw-border-color, #efba84);
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 0;
  /* Original: background-color: var(--bw-form-item-label-bg-color) = var(--bw-form-item-label-bg-color, #fff1e4) */
  background-color: var(--bw-form-item-label-bg-color, #fff1e4);
}

.info-value {
  flex: 1;
  text-align: center;
  color: red;
  font-size: 12px;
}

/* ========== Game Quick Access Buttons ========== */
/* Original: el-button--primary el-button--large block w100% mb10 */
.game-buttons {
  padding: 5px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.game-btn {
  display: block !important;
  width: 184px !important;
  height: 40px !important;
  line-height: 40px !important;
  margin-bottom: 5px;
  margin-left: 0 !important;
  text-align: center !important;
  justify-content: center !important;
  padding: 0 !important;
  /* Original button background color var(--el-color-primary, #5c2e0d) (dark brown) */
  --el-button-bg-color: var(--el-color-primary, #5c2e0d) !important;
  --el-button-border-color: var(--el-color-primary, #5c2e0d) !important;
  --el-button-hover-bg-color: var(--el-color-primary-light-3, rgba(92, 46, 13, 0.9)) !important;
  --el-button-hover-border-color: var(--el-color-primary-light-3, rgba(92, 46, 13, 0.9)) !important;
  --el-button-active-bg-color: var(--el-color-primary, #5c2e0d) !important;
  --el-button-active-border-color: var(--el-color-primary, #5c2e0d) !important;
}

.game-btn:last-child {
  margin-bottom: 0;
}
</style>
