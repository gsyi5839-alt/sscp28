import gameTabBgBrown from '@/assets/顶部导航栏背景图/棕.png'
import gameTabBgRed from '@/assets/顶部导航栏背景图/紫.png'
import gameTabBgGreen from '@/assets/顶部导航栏背景图/绿.png'
import gameTabBgCyan from '@/assets/顶部导航栏背景图/青.png'
import gameTabBgBlue from '@/assets/顶部导航栏背景图/蓝.png'
import headerBgBrown from '@/assets/顶部导航栏背景图/顶部导航栏-棕.png'
import headerBgRed from '@/assets/顶部导航栏背景图/顶部导航栏-紫.png'
import headerBgGreen from '@/assets/顶部导航栏背景图/顶部导航栏-绿.png'
import headerBgCyan from '@/assets/顶部导航栏背景图/顶部导航栏背景-青.png'
import headerBgBlue from '@/assets/顶部导航栏背景图/导航栏背景-蓝.png'

export interface NavItem {
  key: string
  label: string
}

export interface GameItem {
  key: string
  label: string
  badge?: 'new' | 'hot'
}

export type ThemeKey = 'red' | 'green' | 'cyan' | 'blue' | 'brown'

export const topNav: NavItem[] = [
  { key: 'betStatus', label: '下注状况' },
  { key: 'accountHistory', label: '账户历史' },
  { key: 'drawResults', label: '开奖结果' },
  { key: 'profile', label: '个人资料' },
  { key: 'rules', label: '游戏规则' },
  { key: 'settings', label: '设置游戏' },
  { key: 'logout', label: '退出登录' },
]

export const gameNav: GameItem[] = [
  { key: 'caPc28', label: '加拿大pc28', badge: 'new' },
  { key: 'caSsc', label: '加拿大时时彩', badge: 'new' },
  { key: 'aus10', label: '澳洲幸运10' },
  { key: 'aus5', label: '澳洲幸运5' },
  { key: 'happyRacing', label: '欢乐赛车', badge: 'hot' },
  { key: 'happySsc', label: '欢乐时时彩', badge: 'hot' },
]

export const moreGames: GameItem[] = [
  { key: 'luckyPlane', label: '幸运飞艇' },
  { key: 'speedRacing', label: '极速赛车' },
  { key: 'speedSsc', label: '极速时时彩' },
  { key: 'lucky168', label: '168幸运飞艇' },
  { key: 'lottery5', label: '体彩乐透5' },
  { key: 'lottery10', label: '体彩乐透10' },
]

export const themeColors: Array<{ key: ThemeKey; color: string }> = [
  { key: 'red', color: '#b654a7' },
  { key: 'green', color: '#4a8e57' },
  { key: 'cyan', color: '#518594' },
  { key: 'blue', color: '#28a3ef' },
  { key: 'brown', color: '#be9d76' },
]

export const themeGameTabBgMap: Record<ThemeKey, string> = {
  brown: gameTabBgBrown,
  red: gameTabBgRed,
  green: gameTabBgGreen,
  cyan: gameTabBgCyan,
  blue: gameTabBgBlue,
}

export const themeHeaderBgMap: Record<ThemeKey, string> = {
  brown: headerBgBrown,
  red: headerBgRed,
  green: headerBgGreen,
  cyan: headerBgCyan,
  blue: headerBgBlue,
}

export const THEME_STORAGE_KEY = 'bw-member-active-theme-name'
export const THEME_CLASS_LIST: ThemeKey[] = ['red', 'green', 'cyan', 'blue', 'brown']

export const isThemeKey = (value: string | null): value is ThemeKey => (
  !!value && THEME_CLASS_LIST.includes(value as ThemeKey)
)
