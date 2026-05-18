import { computed, onMounted, ref, type Ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getSubNavForGame } from '@/utils/gameSubNav'
import {
  THEME_CLASS_LIST,
  THEME_STORAGE_KEY,
  gameNav,
  isThemeKey,
  moreGames,
  themeColors,
  themeGameTabBgMap,
  themeHeaderBgMap,
  topNav,
  type ThemeKey,
} from './gameHeaderConfig'

type ContentView = 'game' | 'drawResults'

export function useGameHeader(
  betTab: Ref<string>,
  contentView: Ref<ContentView>,
  activeGameKey: Ref<string>,
) {
  const route = useRoute()
  const router = useRouter()
  const authStore = useAuthStore()
  const activeTheme = ref<ThemeKey>('brown')
  const subNav = computed(() => getSubNavForGame(activeGameKey.value))

  const applyTheme = (theme: ThemeKey) => {
    const root = document.documentElement
    root.classList.remove(...THEME_CLASS_LIST)
    root.classList.add(theme)
    localStorage.setItem(THEME_STORAGE_KEY, theme)
  }

  const isTopItemActive = (key: string): boolean => {
    if (key === 'betStatus' && route.name === 'betStatus') return true
    if (key === 'accountHistory' && route.name === 'accountHistory') return true
    if (key === 'drawResults' && contentView.value === 'drawResults') return true
    return false
  }

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
    }
  }

  const showGameView = () => {
    contentView.value = 'game'
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

  const onGameClick = (key: string) => {
    activeGameKey.value = key
    showGameView()
  }

  const onMoreGameClick = (key: string) => {
    activeGameKey.value = key
    showGameView()
  }

  const onSubNavClick = (key: string) => {
    contentView.value = 'game'
    betTab.value = key
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

    const allImages = [...Object.values(themeHeaderBgMap), ...Object.values(themeGameTabBgMap)]
    allImages.forEach(src => {
      const img = new Image()
      img.src = src
    })
  })

  return {
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
  }
}
