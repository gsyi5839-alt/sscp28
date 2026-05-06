<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import MemberPanelMobile from '@/mobile/components/MemberPanelMobile.vue'
import { isResponsiveMobileClient } from '@/mobile/utils/client'

const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = isResponsiveMobileClient()
}
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { linesApi } from '../api'

type TabKey = 'member' | 'agent'

type LineItem = {
  id: number | string
  name: string
  pingMs?: number | null
  url?: string
  type?: string
}

const router = useRouter()
const activeTab = ref<TabKey>('member')
const loading = ref(false)
const speedTesting = ref(false)
const memberLines = ref<LineItem[]>([])
const agentLines = ref<LineItem[]>([])

const STORAGE_KEY_MEMBER = 'cachedMemberLines'
const STORAGE_KEY_AGENT = 'cachedAgentLines'

const DEFAULT_MEMBER_DESKTOP_LINES: LineItem[] = [
  { id: 'm-d-1', name: '会员1', pingMs: 24, url: '/member/login', type: 'MEMBER' },
  { id: 'm-d-2', name: '会员2', pingMs: 49, url: '/member/login', type: 'MEMBER' },
  { id: 'm-d-3', name: '会员3', pingMs: 21, url: '/member/login', type: 'MEMBER' },
  { id: 'm-d-4', name: '会员4', pingMs: 48, url: '/member/login', type: 'MEMBER' }
]

const DEFAULT_MEMBER_MOBILE_LINES: LineItem[] = [
  { id: 'm-m-1', name: '移动端1', pingMs: 36, url: '/member/login', type: 'MEMBER' },
  { id: 'm-m-2', name: '移动端2', pingMs: 48, url: '/member/login', type: 'MEMBER' },
  { id: 'm-m-3', name: '移动端3', pingMs: 26, url: '/member/login', type: 'MEMBER' },
  { id: 'm-m-4', name: '移动端4', pingMs: 22, url: '/member/login', type: 'MEMBER' }
]

const DEFAULT_MEMBER_LINES: LineItem[] = [
  ...DEFAULT_MEMBER_DESKTOP_LINES,
  ...DEFAULT_MEMBER_MOBILE_LINES
]

const DEFAULT_AGENT_LINES: LineItem[] = [
  { id: 'a1', name: '代理1', pingMs: 39, url: '/agent/login', type: 'AGENT' },
  { id: 'a2', name: '代理2', pingMs: 49, url: '/agent/login', type: 'AGENT' },
  { id: 'a3', name: '代理3', pingMs: 35, url: '/agent/login', type: 'AGENT' },
  { id: 'a4', name: '代理4', pingMs: 21, url: '/agent/login', type: 'AGENT' }
]

const normalizeLineName = (value: string | undefined) => (value || '').replace(/\s+/g, '').toLowerCase()

const isMobileLine = (line: LineItem) => {
  const name = normalizeLineName(line.name)
  return name.includes('移动') || name.includes('mobile')
}

const splitMemberLines = (lines: LineItem[]) => {
  let desktop = lines.filter((line) => !isMobileLine(line))
  let mobile = lines.filter(isMobileLine)

  if (desktop.length === 0 && mobile.length === 0) {
    desktop = [...DEFAULT_MEMBER_DESKTOP_LINES]
    mobile = [...DEFAULT_MEMBER_MOBILE_LINES]
  }

  if (mobile.length === 0 && desktop.length > 4) {
    mobile = desktop.slice(4)
    desktop = desktop.slice(0, 4)
  }

  if (desktop.length === 0) {
    desktop = [...DEFAULT_MEMBER_DESKTOP_LINES]
  }
  if (mobile.length === 0) {
    mobile = [...DEFAULT_MEMBER_MOBILE_LINES]
  }

  return {
    desktop: desktop.slice(0, 4),
    mobile: mobile.slice(0, 4)
  }
}

const memberDesktopLines = computed(() => splitMemberLines(memberLines.value).desktop)
const memberMobileLines = computed(() => splitMemberLines(memberLines.value).mobile)
const agentDisplayLines = computed(() =>
  agentLines.value.length > 0 ? agentLines.value.slice(0, 4) : DEFAULT_AGENT_LINES
)

const readCachedLines = (key: string, fallback: LineItem[]) => {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return fallback
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) && parsed.length > 0 ? parsed : fallback
  } catch {
    return fallback
  }
}

const writeCachedLines = (key: string, value: LineItem[]) => {
  try {
    if (Array.isArray(value) && value.length > 0) {
      localStorage.setItem(key, JSON.stringify(value))
    }
  } catch {
    // no-op
  }
}

const loadLines = async () => {
  loading.value = true
  try {
    const [memberRes, agentRes] = await Promise.allSettled([
      linesApi.getLines('MEMBER'),
      linesApi.getLines('AGENT')
    ])

    const memberResponse: any = memberRes.status === 'fulfilled' ? memberRes.value : null
    const agentResponse: any = agentRes.status === 'fulfilled' ? agentRes.value : null

    if (memberResponse?.code === 200 && Array.isArray(memberResponse.data) && memberResponse.data.length > 0) {
      memberLines.value = memberResponse.data
      writeCachedLines(STORAGE_KEY_MEMBER, memberResponse.data)
    } else {
      memberLines.value = readCachedLines(STORAGE_KEY_MEMBER, DEFAULT_MEMBER_LINES)
    }

    if (agentResponse?.code === 200 && Array.isArray(agentResponse.data) && agentResponse.data.length > 0) {
      agentLines.value = agentResponse.data
      writeCachedLines(STORAGE_KEY_AGENT, agentResponse.data)
    } else {
      agentLines.value = readCachedLines(STORAGE_KEY_AGENT, DEFAULT_AGENT_LINES)
    }

    if (
      (memberRes.status === 'rejected' || agentRes.status === 'rejected') &&
      (memberLines.value.length > 0 || agentLines.value.length > 0)
    ) {
      ElMessage.warning('线路加载不稳定，已使用可用线路')
    }
  } catch {
    memberLines.value = readCachedLines(STORAGE_KEY_MEMBER, DEFAULT_MEMBER_LINES)
    agentLines.value = readCachedLines(STORAGE_KEY_AGENT, DEFAULT_AGENT_LINES)
    ElMessage.error('加载线路失败，已使用备用线路')
  } finally {
    loading.value = false
  }
}

const onToggle = (tab: TabKey) => {
  activeTab.value = tab
}

const randomPing = () => Math.floor(Math.random() * (50 - 20 + 1) + 20)

const refreshPingValues = () => {
  memberLines.value = memberLines.value.map((l) => ({ ...l, pingMs: randomPing() }))
  agentLines.value = agentLines.value.map((l) => ({ ...l, pingMs: randomPing() }))
}

const onSpeed = async () => {
  speedTesting.value = true
  await loadLines()
  setTimeout(() => {
    refreshPingValues()
    speedTesting.value = false
  }, 1000)
}

const onLogout = () => {
  router.push('/')
}

const openLine = (line: LineItem) => {
  const target = (line.url || '').trim()
  if (!target) {
    router.push(activeTab.value === 'member' ? '/member/login' : '/agent/login')
    return
  }
  if (/^https?:\/\//i.test(target)) {
    window.open(target, '_blank')
    return
  }
  router.push(target)
}

onMounted(() => {
  document.title = '资讯网'
  const favicon = document.getElementById('favicon') as HTMLLinkElement | null
  if (favicon) favicon.href = 'data:,'
  checkMobile()
  speedTesting.value = true
  loadLines().then(() => {
    setTimeout(() => {
      refreshPingValues()
      speedTesting.value = false
    }, 1000)
  })
})
</script>

<template>
  <MemberPanelMobile
    v-if="isMobile"
    :active-tab="activeTab"
    :speed-testing="speedTesting"
    :member-desktop-lines="memberDesktopLines"
    :member-mobile-lines="memberMobileLines"
    :agent-display-lines="agentDisplayLines"
    @toggle="onToggle"
    @speed="onSpeed"
    @logout="onLogout"
    @open-line="openLine"
  />

  <div v-else class="container">
    <div class="flex-space-between header">
      <div class="btn-group left-group">
        <button id="member-btn" class="btn2" :class="{ btn1: activeTab === 'member' }" @click="onToggle('member')">
          会员线路
        </button>
        <button id="agent-btn" class="btn2" :class="{ btn1: activeTab === 'agent' }" @click="onToggle('agent')">
          代理线路
        </button>
      </div>

      <h1>欢迎光临&nbsp;&nbsp;&nbsp;<span id="lineName">红运</span></h1>

      <div class="btn-group right-group">
        <button class="btn2" @click="onSpeed">测速</button>
        <button class="btn2" @click="onLogout">退出</button>
      </div>
    </div>

    <div class="content">
      <template v-if="activeTab === 'member'">
        <div id="member-lines-container" class="lines-wrap">
          <div class="line-items">
            <div v-for="line in memberDesktopLines" :key="`desktop-${line.id}`" class="line-item">
              <button class="line-link member-item" @click="openLine(line)">
                <span>{{ line.name }}</span>
                <span class="sub-text">{{ speedTesting ? '测速中...' : `(${line.pingMs ?? 'N/A'}ms)` }}</span>
              </button>
            </div>
          </div>
        </div>
      </template>

      <div v-else id="agent-lines-container" class="lines-wrap">
        <div class="line-items">
          <div v-for="line in agentDisplayLines" :key="`agent-${line.id}`" class="line-item">
            <button class="line-link agent-line-item" @click="openLine(line)">
              <span>{{ line.name }}</span>
              <span class="sub-text">{{ speedTesting ? '测速中...' : `(${line.pingMs ?? 'N/A'}ms)` }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* === Desktop-first layout (matches original design) === */
.container {
  width: 1200px;
  margin: auto;
  padding-top: 20%;
  min-height: 100vh;
  box-sizing: border-box;
  background: #fff;
}

.flex-space-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header {
  flex-direction: row;
  align-items: center;
  gap: 0;
}

.left-group {
  order: 0;
}

.header h1 {
  order: 0;
  padding: 0;
  margin: 0;
  text-align: center;
  font-size: 30px;
  font-weight: normal;
  color: red;
}

.right-group {
  order: 0;
}

#lineName {
  color: red;
}

.btn-group {
  padding: 0;
  margin: 0;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.btn2 {
  width: 80px;
  height: 30px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  color: #fff;
  background: #09f;
  padding: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn2:hover {
  opacity: 0.8;
}

.btn2.btn1 {
  background: #f60;
}

.btn2.btn1:hover {
  opacity: 0.8;
}

.content {
  margin-top: 20px;
}

.lines-wrap {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  border-top: 1px solid #ccc;
  border-left: 1px solid #ccc;
}

.line-items {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
}

.line-item {
  width: 25%;
  height: 60px;
  line-height: 58px;
  background-color: #f9fdfa;
  text-align: center;
  border-right: 1px solid #ccc;
  border-bottom: 1px solid #ccc;
  box-sizing: border-box;
}

.line-link {
  width: 100%;
  height: 100%;
  border: none;
  background-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  padding: 0;
  box-sizing: border-box;
  cursor: pointer;
  text-align: center;
}

.line-link:hover {
  text-decoration: underline;
  background: #ebfff0;
}

.line-link:hover .sub-text {
  color: red;
}

.member-item,
.mobile-item,
.agent-line-item {
  font-size: 16px;
  color: green;
}

.line-link:hover .member-item,
.line-link:hover .mobile-item,
.line-link:hover .agent-line-item {
  color: red;
}

.sub-text {
  color: #686868;
  font-size: 13px;
  font-weight: 400;
}
</style>
