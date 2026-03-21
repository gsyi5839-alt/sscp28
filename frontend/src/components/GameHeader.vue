<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import logoImg from '@/assets/通用/logo.png'

// Props for controlling header behavior
const props = defineProps<{
  hideSubNav?: boolean  // Hide the sub-nav (两面盘 | 1-3球) on certain pages like BetStatus
}>()

// Expose the "盘面" selection (两面盘 / 1-3球) to the parent page (GameHome)
// so clicking the header sub-nav can switch the actual betting panel.
const betTab = defineModel<'twoSide' | 'balls'>('betTab', { default: 'twoSide' })

// Expose content view state for switching between game panel and draw results
const contentView = defineModel<'game' | 'drawResults'>('contentView', { default: 'game' })

/* ============ 类型 ============ */
interface NavItem { key: string; label: string }
interface GameItem { key: string; label: string; badge?: 'new' | 'hot' }
interface SubNavItem { key: string; label: string }

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

/* ============ 顶部导航 ============ */
const topNav: NavItem[] = [
  { key: 'betStatus', label: '下注状况' },
  { key: 'accountHistory', label: '账户历史' },
  { key: 'drawResults', label: '开奖结果' },
  { key: 'profile', label: '个人资料' },
  { key: 'rules', label: '游戏规则' },
  { key: 'settings', label: '设置游戏' },
  { key: 'logout', label: '退出登录' },
]

/* ============ 游戏导航 ============ */
const gameNav: GameItem[] = [
  { key: 'caPc28', label: '加拿大pc28', badge: 'new' },
  { key: 'caSsc', label: '加拿大时时彩', badge: 'new' },
  { key: 'aus10', label: '澳洲幸运10' },
  { key: 'aus5', label: '澳洲幸运5' },
  { key: 'happyRacing', label: '欢乐赛车', badge: 'hot' },
  { key: 'happySsc', label: '欢乐时时彩', badge: 'hot' },
]

/* ============ 更多游戏下拉 ============ */
const moreGames: GameItem[] = [
  { key: 'luckyPlane', label: '幸运飞艇' },
  { key: 'speedRacing', label: '极速赛车' },
  { key: 'speedSsc', label: '极速时时彩' },
  { key: 'lucky168', label: '168幸运飞艇' },
  { key: 'lottery5', label: '体彩乐透5' },
  { key: 'lottery10', label: '体彩乐透10' },
]

/* ============ 子导航（menus 栏） ============ */
const subNav: SubNavItem[] = [
  { key: 'twoSides', label: '两面盘' },
  { key: 'ball13', label: '1-3球' },
]

/* ============ 主题色 ============ */
type ThemeKey = 'red' | 'green' | 'cyan' | 'blue' | 'brown'

const themeColors: Array<{ key: ThemeKey; color: string }> = [
  { key: 'red', color: '#b654a7' },
  { key: 'green', color: '#4a8e57' },
  { key: 'cyan', color: '#518594' },
  { key: 'blue', color: '#28a3ef' },
  { key: 'brown', color: '#be9d76' },
]

/* ============ 状态 ============ */
const activeGameKey = ref('caPc28')
const activeTheme = ref<ThemeKey>('brown') // 默认棕色主题
const THEME_STORAGE_KEY = 'bw-member-active-theme-name'
const THEME_CLASS_LIST: ThemeKey[] = ['red', 'green', 'cyan', 'blue', 'brown']

const isThemeKey = (value: string | null): value is ThemeKey => {
  return !!value && THEME_CLASS_LIST.includes(value as ThemeKey)
}

const applyTheme = (theme: ThemeKey) => {
  const root = document.documentElement
  root.classList.remove(...THEME_CLASS_LIST)
  root.classList.add(theme)
  localStorage.setItem(THEME_STORAGE_KEY, theme)
}

/* ============ 事件处理 ============ */
const onTopClick = async (key: string) => {
  if (key === 'logout') {
    authStore.logout()
    await router.push('/member/login')
    return
  }
  if (key === 'betStatus') {
    await router.push('/bet-status')
    return
  }
  if (key === 'accountHistory') {
    await router.push('/account-history')
    return
  }
  if (key === 'drawResults') {
    contentView.value = 'drawResults'
    const nextQuery = { ...route.query, view: 'drawResults' }
    if (router.currentRoute.value.name !== 'gameHome') {
      await router.push({ name: 'gameHome', query: nextQuery })
      return
    }
    await router.replace({ name: 'gameHome', query: nextQuery })
    return
  }
  // Other navigation functions to be implemented
}

const onGameClick = (key: string) => {
  activeGameKey.value = key
  // Reset to game view when switching games
  contentView.value = 'game'
  // If not on GameHome page, navigate to it
  if (router.currentRoute.value.name !== 'gameHome') {
    router.push({ name: 'gameHome' })
    return
  }
  if (route.query.view) {
    const nextQuery = { ...route.query }
    delete nextQuery.view
    router.replace({ name: 'gameHome', query: nextQuery })
  }
}

const onMoreGameClick = (key: string) => {
  activeGameKey.value = key
  // Reset to game view when switching games
  contentView.value = 'game'
  // If not on GameHome page, navigate to it
  if (router.currentRoute.value.name !== 'gameHome') {
    router.push({ name: 'gameHome' })
    return
  }
  if (route.query.view) {
    const nextQuery = { ...route.query }
    delete nextQuery.view
    router.replace({ name: 'gameHome', query: nextQuery })
  }
}

const onSubNavClick = (key: string) => {
  // Map header sub-nav keys to actual betting tab values used in GameHome.
  // twoSides -> twoSide, ball13 -> balls
  const newTab = key === 'twoSides' ? 'twoSide' : 'balls'
  contentView.value = 'game'
  
  // If on GameHome page, just switch the tab
  if (router.currentRoute.value.name === 'gameHome') {
    betTab.value = newTab
    if (route.query.view) {
      const nextQuery = { ...route.query }
      delete nextQuery.view
      router.replace({ name: 'gameHome', query: nextQuery })
    }
  } else {
    // On other pages (e.g. BetStatus), navigate to GameHome with the tab parameter
    router.push({ name: 'gameHome', query: { tab: newTab } })
  }
}

const onThemeClick = (key: ThemeKey) => {
  activeTheme.value = key
  applyTheme(key)
}

onMounted(() => {
  const saved = localStorage.getItem(THEME_STORAGE_KEY)
  const nextTheme: ThemeKey = isThemeKey(saved) ? saved : 'brown'
  activeTheme.value = nextTheme
  applyTheme(nextTheme)
})
</script>

<template>
  <div class="header-container">
    <!-- ====== 主导航区域 (70px) ====== -->
    <div class="header">
      <div class="header-inner">
        <!-- 第一行：Logo + 顶部导航 -->
        <div class="row-top">
          <!-- Logo -->
          <div class="brand">
            <img :src="logoImg" alt="BW" class="brand-logo" />
            <span class="brand-name">海源</span>
          </div>

          <!-- 顶部功能导航 -->
          <div class="top-nav">
            <template v-for="(item, idx) in topNav" :key="item.key">
              <span class="top-item" @click="onTopClick(item.key)">
                {{ item.label }}
              </span>
              <span v-if="idx !== topNav.length - 1" class="sep">|</span>
            </template>
          </div>
        </div>

        <!-- 第二行：游戏导航 -->
        <div class="row-games">
          <!-- 主要游戏 -->
          <div
            v-for="item in gameNav"
            :key="item.key"
            class="game-item"
            :class="{
              active: item.key === activeGameKey,
              'is-new': item.badge === 'new',
              'is-hot': item.badge === 'hot',
            }"
            @click="onGameClick(item.key)"
          >
            {{ item.label }}
          </div>

          <!-- 更多游戏（下拉） -->
          <el-popover
            placement="bottom"
            :width="124"
            trigger="hover"
            popper-class="more-games-popover"
          >
            <template #reference>
              <div class="game-item">更多游戏</div>
            </template>
            <div
              v-for="(mg, idx) in moreGames"
              :key="mg.key"
              class="more-game-item"
              :class="{ 'mb5': idx !== moreGames.length - 1 }"
              @click="onMoreGameClick(mg.key)"
            >
              {{ mg.label }}
            </div>
          </el-popover>
        </div>
      </div>
    </div>

    <!-- ====== 子导航栏 (31px) ====== -->
    <div class="menus">
      <!-- 主题颜色切换 -->
      <div class="theme-colors">
        <div
          v-for="tc in themeColors"
          :key="tc.key"
          class="theme-dot"
          :style="{ background: tc.color }"
          :class="{ 'theme-active': tc.key === activeTheme }"
          @click="onThemeClick(tc.key)"
        />
      </div>

      <!-- 子导航项 - 可通过 hideSubNav prop 隐藏 -->
      <div v-if="!props.hideSubNav" class="menus-center">
        <div class="sub-nav">
          <template v-for="(item, idx) in subNav" :key="item.key">
            <span
              class="sub-item"
              :class="{
                active: (betTab === 'twoSide' ? 'twoSides' : 'ball13') === item.key,
                'sub-item-ball13': item.key === 'ball13'
              }"
              @click="onSubNavClick(item.key)"
            >
              {{ item.label }}
            </span>
            <span v-if="idx !== subNav.length - 1" class="sub-sep">|</span>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========================= 整体容器 ========================= */
.header-container {
  width: 100%;
  /* Removed min-width: 1418px to prevent horizontal overflow causing colored bars on the right side */
  /* Use max-width and overflow-x to contain content without forcing viewport expansion */
  max-width: 100vw;
  overflow-x: hidden;
  color: #ffffff;
  font-weight: 700;
}

/* ========================= 主导航 (70px) ========================= */
.header {
  position: relative;
  height: 70px;
  width: 100%;
  background: var(--bw-linear-bg, linear-gradient(to bottom, #a6744d 0%, #351c0c 100%));
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  box-shadow: inset 0 1px 0 rgba(255, 197, 138, 0.55), inset 0 -1px 0 rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

/* Top glossy light layer (simulates light reflection) */
.header::before {
  content: none;
}

/* Subtle beam falloff for depth */
.header::after {
  content: none;
}

.header-inner {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: none;
  height: 70px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 20px;
  box-sizing: border-box;
}

/* ---------- 第一行：Logo + 顶部导航 ---------- */
.row-top {
  display: flex;
  align-items: center;
  height: 30px;
  line-height: 30px;
  margin-bottom: 4px;
  margin-top: 9px;
}

.brand {
  position: relative;
  display: flex;
  align-items: center;
  flex: 0 0 220px;
  width: 220px;
  min-width: 220px;
  height: 30px;
  margin-right: 0;
}

.brand-logo {
  width: 90px;
  height: 70px;
  object-fit: contain;
  position: absolute;
  left: 0;
  top: -9px;
}

.brand-name {
  position: absolute;
  left: 95px;
  top: 12px;
  font-size: 24px;
  color: #f8bb00;
  line-height: 1;
  white-space: nowrap;
}

.top-nav {
  display: flex;
  align-items: center;
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  height: 30px;
  line-height: 30px;
}

.top-item {
  display: block;
  flex: 0 0 70px;
  width: 70px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  font-size: 13px;
  font-weight: 400;
  padding-left: 5px;
  padding-right: 5px;
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  box-sizing: border-box;
  color: #ffffff;
  transition: color 0.15s;
}

.top-item:hover {
  color: #fcff00;
}

.sep {
  display: inline-block;
  height: 22px;
  line-height: 22px;
  text-align: center;
  opacity: 0.7;
  font-weight: 400;
  font-size: 13px;
}

/* ---------- 第二行：游戏导航 ---------- */
.row-games {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 22px;
  line-height: 22px;
  padding-left: 220px;
}

.game-item {
  position: relative;
  width: 100px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  font-size: 13px;
  font-weight: 400;
  cursor: pointer;
  user-select: none;
  color: #ffffff;
  background: var(--bw-linear-bg, linear-gradient(to bottom, #a6744d 0%, #351c0c 100%));
  border: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  transition: color 0.15s;
}

.game-item:hover {
  color: #fcff00;
}

.game-item.active {
  color: #fcff00;
}

/* "新" 角标 */
.game-item.is-new::after {
  content: '';
  position: absolute;
  top: -6px;
  right: 2px;
  width: 20px;
  height: 20px;
  background: url('@/assets/图标/新.png') no-repeat center / contain;
  pointer-events: none;
}

/* "热" 角标 */
.game-item.is-hot::after {
  content: '';
  position: absolute;
  top: -6px;
  right: 2px;
  width: 20px;
  height: 20px;
  background: url('@/assets/图标/热.png') no-repeat center / contain;
  pointer-events: none;
}

/* ========================= 子导航栏 menus (31px) ========================= */
.menus {
  height: 31px;
  width: 100%;
  background: var(--bw-bg-3, #fff7ef);
  border-top: 1px solid var(--bw-border-color, #efba84);
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  display: flex;
  align-items: center;
}

/* ---------- 主题色圆点 ---------- */
.theme-colors {
  display: flex;
  align-items: center;
  padding-left: 40px;
  margin-right: 65px;
}

.theme-dot {
  width: 18px;
  height: 18px;
  margin-right: 3px;
  cursor: pointer;
  border-radius: 2px;
  border: 1px solid transparent;
  transition: border-color 0.2s;
}

.theme-dot:hover,
.theme-dot.theme-active {
  border-color: #fff;
  box-shadow: 0 0 3px rgba(0,0,0,0.3);
}

/* ---------- 子导航项 ---------- */
.menus-center {
  flex: 1;
}

.sub-nav {
  display: flex;
  align-items: center;
}

.sub-item {
  display: inline-block;
  min-width: auto;
  width: auto;
  height: 17px;
  line-height: 17px;
  margin: 0 5px;
  padding: 0;
  text-align: center;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  color: var(--bw-default-color, #351c0c);
  transition: color 0.15s;
  white-space: nowrap;
}

.sub-item-ball13 {
  width: 34.72px;
}

.sub-item:hover {
  color: #ff0000;
}

.sub-item.active {
  color: #ff0000 !important;
  /* Active: keep font color highlight only (no background fill). */
  background: transparent !important;
  border: none !important;
}

.sub-sep {
  color: #fff;
  font-size: 13px;
  line-height: 17px;
  font-weight: 700;
}

/* ========================= 更多游戏下拉项 ========================= */
.more-game-item {
  width: 100px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  font-size: 13px;
  font-weight: 400;
  color: #ffffff;
  cursor: pointer;
  background: var(--bw-linear-bg, linear-gradient(to bottom, #a6744d 0%, #351c0c 100%));
  border: 1px solid var(--bw-border-color, #efba84);
  box-sizing: border-box;
  transition: color 0.15s;
}

.more-game-item:hover {
  color: #fcff00;
}

.mb5 {
  margin-bottom: 5px;
}
</style>
