<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import iconNew from '@/assets/图标/新.png'
import iconHot from '@/assets/图标/热.png'

type TopNavKey =
  | 'betStatus'
  | 'accountHistory'
  | 'drawResults'
  | 'profile'
  | 'rules'
  | 'settings'
  | 'logout'

type GameNavKey =
  | 'caPc28'
  | 'caSsc'
  | 'aus10'
  | 'aus5'
  | 'happyRacing'
  | 'happySsc'
  | 'more'

const router = useRouter()
const authStore = useAuthStore()

// 备注：你强调“颜色以截图为准，IDE 复制不对”
// 这里先把颜色集中在 CSS 变量里，方便你后续按截图微调（无需改结构）。
const topNav = computed((): Array<{ key: TopNavKey; label: string }> => [
  { key: 'betStatus', label: '下注状况' },
  { key: 'accountHistory', label: '账户历史' },
  { key: 'drawResults', label: '开奖结果' },
  { key: 'profile', label: '个人资料' },
  { key: 'rules', label: '游戏规则' },
  { key: 'settings', label: '设置游戏' },
  { key: 'logout', label: '退出登录' }
])

const gameNav = computed(
  (): Array<{ key: GameNavKey; label: string; badge?: 'new' | 'hot' }> => [
    { key: 'caPc28', label: '加拿大pc28', badge: 'new' },
    { key: 'caSsc', label: '加拿大时时彩', badge: 'new' },
    { key: 'aus10', label: '澳洲幸运10' },
    { key: 'aus5', label: '澳洲幸运5' },
    { key: 'happyRacing', label: '欢乐赛车', badge: 'hot' },
    { key: 'happySsc', label: '欢乐时时彩', badge: 'hot' },
    { key: 'more', label: '更多游戏' }
  ]
)

const activeTopKey = ref<TopNavKey>('betStatus')
const activeGameKey = ref<GameNavKey>('caPc28')

const onTopClick = async (key: TopNavKey) => {
  activeTopKey.value = key
  if (key === 'logout') {
    authStore.logout()
    await router.push('/member/login')
    return
  }
  // 目前仅实现顶部导航栏 UI，后续你确定每个菜单对应的页面后再补路由。
}

const onGameClick = (key: GameNavKey) => {
  activeGameKey.value = key
  // 目前仅实现 UI；后续按你的业务映射到不同玩法页面即可。
}
</script>

<template>
  <div class="header-container">
    <div class="header">
      <!-- 第一行：站点名 + 顶部导航 -->
      <div class="row row-top">
        <div class="brand">海富</div>

        <div class="top-nav">
          <template v-for="(item, idx) in topNav" :key="item.key">
            <span
              class="top-item"
              :class="{ active: item.key === activeTopKey }"
              @click="onTopClick(item.key)"
            >
              {{ item.label }}
            </span>
            <span v-if="idx !== topNav.length - 1" class="sep">|</span>
          </template>
        </div>
      </div>

      <!-- 第二行：游戏导航 -->
      <div class="row row-games">
        <template v-for="item in gameNav" :key="item.key">
          <div
            class="game-item"
            :class="{
              active: item.key === activeGameKey,
              'is-new': item.badge === 'new',
              'is-hot': item.badge === 'hot'
            }"
            @click="onGameClick(item.key)"
          >
            <span class="game-text">{{ item.label }}</span>
            <img v-if="item.badge === 'new'" class="badge" :src="iconNew" alt="new" />
            <img v-else-if="item.badge === 'hot'" class="badge" :src="iconHot" alt="hot" />
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* =========================
 * 颜色 & 尺寸（按截图对齐）
 * ========================= */
.header-container {
  /* 截图：整体导航条高度约 70px */
  height: 70px;
  width: 100%;
  min-width: 1418px;

  /* 先用近似棕色渐变；你给到准确色值后我会替换为“完全一致” */
  background: linear-gradient(180deg, var(--nav-brown-1, #6a3a16) 0%, var(--nav-brown-2, #2b1204) 100%);
  color: var(--nav-text, #ffffff);
  font-weight: 700;
}

.header {
  width: 1418px;
  height: 70px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 6px 12px;
  gap: 6px;
  box-sizing: border-box;
}

.row {
  display: flex;
  align-items: center;
  width: 100%;
}

.row-top {
  height: 30px;
  line-height: 30px;
}

.brand {
  flex: 0 0 auto;
  margin-right: 14px;
  font-size: 16px;
  letter-spacing: 1px;
}

.top-nav {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  overflow: hidden;
  white-space: nowrap;
}

.top-item {
  display: block;
  width: 70px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  font-size: 13px;
  font-weight: 400;
  padding: 0 5px;
  cursor: pointer;
  user-select: none;
  color: var(--nav-text, #ffffff);
}

.top-item:hover {
  color: var(--nav-hover, #fcff00);
}

.sep {
  display: inline-block;
  width: 4px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  opacity: 0.9;
  margin: 0 6px;
}

.row-games {
  height: 22px;
  line-height: 22px;
  gap: 0;
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
  color: var(--nav-text, #ffffff);
}

.game-item:hover {
  color: var(--nav-hover, #fcff00);
}

.game-item.active {
  color: var(--nav-hover, #fcff00);
}

.badge {
  position: absolute;
  top: 1px;
  right: 4px;
  width: 20px;
  height: 20px;
  pointer-events: none;
}

.game-text {
  display: inline-block;
  max-width: 71px; /* 你标注的 text 宽约 71 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: top;
}
</style>

