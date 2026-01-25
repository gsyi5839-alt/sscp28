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

// Note: You emphasized "colors based on screenshot, IDE copy is incorrect"
// Here I've centralized colors in CSS variables for easy fine-tuning later (no need to change structure).
const topNav = computed((): Array<{ key: TopNavKey; label: string }> => [
  { key: 'betStatus', label: 'Bet Status' },
  { key: 'accountHistory', label: 'Account History' },
  { key: 'drawResults', label: 'Draw Results' },
  { key: 'profile', label: 'Profile' },
  { key: 'rules', label: 'Rules' },
  { key: 'settings', label: 'Settings' },
  { key: 'logout', label: 'Logout' }
])

const gameNav = computed(
  (): Array<{ key: GameNavKey; label: string; badge?: 'new' | 'hot' }> => [
    { key: 'caPc28', label: 'Canada PC28', badge: 'new' },
    { key: 'caSsc', label: 'Canada SSC', badge: 'new' },
    { key: 'aus10', label: 'Australia Lucky 10' },
    { key: 'aus5', label: 'Australia Lucky 5' },
    { key: 'happyRacing', label: 'Happy Racing', badge: 'hot' },
    { key: 'happySsc', label: 'Happy SSC', badge: 'hot' },
    { key: 'more', label: 'More Games' }
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
  // Currently only implements top nav UI, will add routing for each menu later when you define corresponding pages.
}

const onGameClick = (key: GameNavKey) => {
  activeGameKey.value = key
  // Currently only implements UI; can map to different game pages according to your business later.
}
</script>

<template>
  <div class="header-container">
    <div class="header">
      <!-- First row: Site name + Top navigation -->
      <div class="row row-top">
        <div class="brand">Sea Fortune</div>

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

      <!-- Second row: Game navigation -->
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
 * Colors & Sizes (Aligned with screenshot)
 * ========================= */
.header-container {
  /* Screenshot: overall nav bar height about 70px */
  height: 70px;
  width: 100%;
  min-width: 1418px;

  /* Using approximate brown gradient; will replace with exact values after you provide them for "complete match" */
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
  max-width: 71px; /* You marked text width about 71 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: top;
}
</style>

