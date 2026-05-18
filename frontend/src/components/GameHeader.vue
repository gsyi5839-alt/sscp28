<script setup lang="ts">
import logoImg from '@/assets/通用/logo.png'
import { useGameHeader } from './useGameHeader'

const props = defineProps<{
  hideSubNav?: boolean
}>()

const betTab = defineModel<string>('betTab', { default: 'twoSide' })
const contentView = defineModel<'game' | 'drawResults'>('contentView', { default: 'game' })
const activeGameKey = defineModel<string>('activeGameKey', { default: 'caPc28' })

const {
  activeTheme,
  gameNav,
  isTopItemActive,
  moreGames,
  onGameClick,
  onMoreGameClick,
  onSubNavClick,
  onThemeClick,
  onTopClick,
  subNav,
  themeColors,
  themeGameTabBgMap,
  themeHeaderBgMap,
  topNav,
} = useGameHeader(betTab, contentView, activeGameKey)
</script>

<template>
  <div class="header-container">
    <!-- Main navigation area (70px) -->
    <div class="header" :style="{ backgroundImage: `url(${themeHeaderBgMap[activeTheme]})` }">
      <div class="header-inner">
        <!-- First row: logo and top navigation -->
        <div class="row-top">
          <!-- Logo -->
          <div class="brand">
            <img :src="logoImg" alt="BW" class="brand-logo" />
            <span class="brand-name">海源</span>
          </div>

          <!-- Top action navigation -->
          <div class="top-nav">
            <template v-for="(item, idx) in topNav" :key="item.key">
              <span
                class="top-item"
                :class="{ active: isTopItemActive(item.key) }"
                @click="onTopClick(item.key)"
              >
                {{ item.label }}
              </span>
              <span v-if="idx !== topNav.length - 1" class="sep">|</span>
            </template>
          </div>
        </div>

        <!-- Second row: game navigation -->
        <div class="row-games">
          <!-- Primary games -->
          <div
            v-for="item in gameNav"
            :key="item.key"
            class="game-item"
            :class="{
              active: item.key === activeGameKey,
              'is-new': item.badge === 'new',
              'is-hot': item.badge === 'hot',
            }"
            :style="{ backgroundImage: `url(${themeGameTabBgMap[activeTheme]})` }"
            @click="onGameClick(item.key)"
          >
            {{ item.label }}
          </div>

          <!-- More games dropdown -->
          <el-popover
            placement="bottom"
            :width="124"
            trigger="hover"
            popper-class="more-games-popover"
          >
            <template #reference>
              <div class="game-item" :style="{ backgroundImage: `url(${themeGameTabBgMap[activeTheme]})` }">更多游戏</div>
            </template>
            <div
              v-for="(mg, idx) in moreGames"
              :key="mg.key"
              class="more-game-item"
              :class="{ 'mb5': idx !== moreGames.length - 1 }"
              :style="{ backgroundImage: `url(${themeGameTabBgMap[activeTheme]})` }"
              @click="onMoreGameClick(mg.key)"
            >
              {{ mg.label }}
            </div>
          </el-popover>
        </div>
      </div>
    </div>

    <!-- Sub navigation bar (31px) -->
    <div class="menus">
      <!-- Theme color switcher -->
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

      <!-- Sub navigation items can be hidden via hideSubNav prop. -->
      <div v-if="!props.hideSubNav" class="menus-center">
        <div class="sub-nav">
          <template v-for="(item, idx) in subNav" :key="item.key">
            <span
              class="sub-item"
              :class="{ active: betTab === item.key }"
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
/* Overall container */
.header-container {
  width: 100%;
  min-width: 1418px;
  color: #ffffff;
  font-weight: 700;
}

/* Main navigation (70px) */
.header {
  position: relative;
  height: 70px;
  width: 100%;
  background: no-repeat center / cover;
  border-bottom: none;
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

/* First row: logo and top navigation */
.row-top {
  display: flex;
  align-items: center;
  height: 30px;
  line-height: 30px;
  margin-bottom: 4px;
  margin-top: -1px;
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
  color: var(--bw-header-color, #be9d76);
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

.top-item:hover,
.top-item.active {
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

/* Second row: game navigation */
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
  background-size: 100% 100%;
  background-repeat: no-repeat;
  background-position: center;
  border: none;
  box-sizing: border-box;
  transition: color 0.15s;
}

.game-item:hover {
  color: #fcff00;
}

.game-item.active {
  color: #fcff00;
}

/* New badge */
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

/* Hot badge */
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

/* Sub navigation menus (31px) */
.menus {
  height: 31px;
  width: 100%;
  background: var(--bw-bg-3, #fff7ef);
  border-top: 1px solid var(--bw-border-color, #efba84);
  border-bottom: 1px solid var(--bw-border-color, #efba84);
  display: flex;
  align-items: center;
}

/* Theme color dots */
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

/* Sub navigation items */
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

/* More games dropdown items */
.more-game-item {
  width: 100px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  font-size: 13px;
  font-weight: 400;
  color: #ffffff;
  cursor: pointer;
  background-size: 100% 100%;
  background-repeat: no-repeat;
  background-position: center;
  border: none;
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
