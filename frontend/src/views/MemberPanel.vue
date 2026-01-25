<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { linesApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('member') // 'member' or 'agent'

// Set page title on mount
onMounted(() => {
  document.title = '资讯网'
  // Clear favicon on member panel page
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) {
    favicon.href = 'data:,'  // Empty favicon
  }
})

type LineItem = {
  id: number | string
  name: string
  pingMs?: number | null
  url?: string
  type?: string
}

const STORAGE_KEY_MEMBER = 'cachedMemberLines'
const STORAGE_KEY_AGENT = 'cachedAgentLines'

const DEFAULT_MEMBER_LINES: LineItem[] = [
  { id: 'm1', name: 'Member Line 1', pingMs: 35, url: '/member/login', type: 'MEMBER' },
  { id: 'm2', name: 'Member Line 2', pingMs: 42, url: '/member/login', type: 'MEMBER' },
  { id: 'm3', name: 'Member Line 3', pingMs: 55, url: '/member/login', type: 'MEMBER' },
  { id: 'm4', name: 'Member Line 4', pingMs: 68, url: '/member/login', type: 'MEMBER' }
]

const DEFAULT_AGENT_LINES: LineItem[] = [
  { id: 'a1', name: 'Agent Line 1', pingMs: 40, url: '/agent/login', type: 'AGENT' },
  { id: 'a2', name: 'Agent Line 2', pingMs: 48, url: '/agent/login', type: 'AGENT' },
  { id: 'a3', name: 'Agent Line 3', pingMs: 60, url: '/agent/login', type: 'AGENT' },
  { id: 'a4', name: 'Agent Line 4', pingMs: 75, url: '/agent/login', type: 'AGENT' }
]

function readCachedLines(key: string, fallback: LineItem[]) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return fallback
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) && parsed.length > 0 ? parsed : fallback
  } catch {
    return fallback
  }
}

function writeCachedLines(key: string, value: any[]) {
  try {
    if (Array.isArray(value) && value.length > 0) {
      localStorage.setItem(key, JSON.stringify(value))
    }
  } catch {
    // ignore
  }
}

const memberLines = ref<LineItem[]>([])
const agentLines = ref<LineItem[]>([])
const loading = ref(false)

/**
 * Switch between member and agent tabs
 */
const switchTab = (tab: string) => {
  activeTab.value = tab
}

/**
 * Refresh lines data from the backend.
 */
const testSpeed = async () => {
  await loadLines()
}

/**
 * Logout and return to home page
 */
const logout = () => {
  router.push('/')
}

/**
 * Navigate to login page when clicking any line
 * Member lines go to member login, Agent lines go to agent login
 */
const selectLine = () => {
  if (activeTab.value === 'member') {
    router.push('/member/login')
  } else {
    router.push('/agent/login')
  }
}

// Current displayed lines based on active tab
const currentLines = () => {
  return activeTab.value === 'member' ? memberLines.value : agentLines.value
}

/**
 * Load member and agent lines from API.
 */
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
      // Fallback: when API fails/returns empty, use local cache or default lines to avoid blank page
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
      ElMessage.warning('Line API occasionally blocked, using local fallback lines')
    }
  } catch {
    // Final fallback
    memberLines.value = readCachedLines(STORAGE_KEY_MEMBER, DEFAULT_MEMBER_LINES)
    agentLines.value = readCachedLines(STORAGE_KEY_AGENT, DEFAULT_AGENT_LINES)
    ElMessage.error('Failed to load lines, using local fallback lines')
  } finally {
    loading.value = false
  }
}

// Load lines on mount
onMounted(() => {
  loadLines()
})
</script>

<template>
  <div class="member-panel">
    <!-- Content Wrapper for centering -->
    <div class="content-wrapper">
      <!-- Header Section -->
      <div class="header">
        <!-- Left Buttons -->
        <div class="header-left">
          <button
            class="btn-member"
            :class="{ active: activeTab === 'member' }"
            @click="switchTab('member')"
          >
            Member Lines
          </button>
          <button
            class="btn-agent"
            :class="{ active: activeTab === 'agent' }"
            @click="switchTab('agent')"
          >
            Agent Lines
          </button>
        </div>

        <!-- Center Title -->
        <h1 class="title">
          <span class="welcome">Welcome</span>
          <span class="brand">Champion</span>
        </h1>

        <!-- Right Buttons -->
        <div class="header-right">
          <button class="btn-action" @click="testSpeed">Test Speed</button>
          <button class="btn-action" @click="logout">Logout</button>
        </div>
      </div>
      
      <!-- Lines Container - 1201x61, 4 items each 299x60 -->
      <div class="lines-container">
        <div v-if="!loading && currentLines().length === 0" class="empty-lines">
          No lines available, please try again later or click "Test Speed" to refresh
        </div>

        <div 
          v-for="line in currentLines()" 
          :key="line.id" 
          class="line-item"
          @click="selectLine"
        >
          <span class="line-name">{{ line.name }}</span>
          <span class="line-ping">({{ line.pingMs ?? 'N/A' }}ms)</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container - full page
 * NOTE:
 * The user expects "center" to mean both horizontally and vertically centered
 * on desktop. Previously we used top padding, which makes it look "not centered".
 */
.member-panel {
  min-height: 100vh;
  background: #ffffff;
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  align-items: center; /* vertical center */
  justify-content: center; /* horizontal center */
}

/* Content wrapper - keep the fixed design width (1201px) */
.content-wrapper {
  width: 1200px;
  min-width: 1200px;
  margin: 0 auto;
}

/* Header section - aligned with lines container */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  width: 100%;
}

/* Left buttons container */
.header-left {
  display: flex;
  gap: 8px;
}

/* Member button - blue #0099FF, size 80x30 */
.btn-member {
  width: 80px;
  height: 30px;
  background: #0099FF;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.btn-member:hover {
  opacity: 0.9;
}

/* Agent button - orange #FF6600, size 80x30 */
.btn-agent {
  width: 80px;
  height: 30px;
  background: #FF6600;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.btn-agent:hover {
  opacity: 0.9;
}

/* Title section */
.title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin: 0;
}

/* Welcome text - red #FF0000 */
.welcome {
  font-size: 28px;
  color: #FF0000;
  font-weight: normal;
}

/* Brand text - red #FF0000 */
.brand {
  font-size: 28px;
  color: #FF0000;
  font-weight: normal;
}

/* Right buttons container */
.header-right {
  display: flex;
  gap: 8px;
}

/* Action buttons - blue #0099FF, size 80x30 */
.btn-action {
  width: 80px;
  height: 30px;
  background: #0099FF;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.btn-action:hover {
  opacity: 0.9;
}

/* Lines container - 1201x61 total, aligned with header */
.lines-container {
  display: flex;
  width: 1200px;
  height: 60px;
  margin-top: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  box-sizing: border-box;
}

.empty-lines {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999999;
  font-size: 14px;
}

/* Single line item - fill container height */
.line-item {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: #ffffff;
  border-right: 1px solid #e0e0e0;
  cursor: pointer;
  transition: background 0.2s ease;
  box-sizing: border-box;
}

/* Last item - no right border */
.line-item:last-child {
  border-right: none;
}

/* Hover state - show light green background */
.line-item:hover {
  background: rgba(235, 255, 240, 1);
}

/* Active/Click state - slightly darker green */
.line-item:active {
  background: rgba(200, 240, 210, 1);
}

/* Hover state - text turns red */
.line-item:hover .line-name,
.line-item:hover .line-ping {
  color: #FF0000;
}

/* Line name - green #008000 */
.line-name {
  font-size: 16px;
  color: #008000;
  font-weight: 500;
}

/* Ping time - light gray */
.line-ping {
  font-size: 14px;
  color: #999999;
}

/* ==================== Mobile Responsive (H5) ==================== */
/* Keep same layout as PC, page scrolls horizontally from left */
@media screen and (max-width: 1024px) {
  .member-panel {
    align-items: flex-start;
    justify-content: flex-start; /* Start from left on mobile */
    padding: 40px 0;
  }

  .content-wrapper {
    margin: 0; /* No auto margin, start from left */
    padding-left: 10px;
  }
}
</style>

