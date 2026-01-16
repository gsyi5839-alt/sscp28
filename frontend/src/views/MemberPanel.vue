<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

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

// Member lines data with ping times
const memberLines = ref([
  { id: 1, name: '会员1', ping: 44, url: '#' },
  { id: 2, name: '会员2', ping: 26, url: '#' },
  { id: 3, name: '会员3', ping: 22, url: '#' },
  { id: 4, name: '会员4', ping: 30, url: '#' }
])

// Agent lines data
const agentLines = ref([
  { id: 1, name: '代理1', ping: 35, url: '#' },
  { id: 2, name: '代理2', ping: 28, url: '#' },
  { id: 3, name: '代理3', ping: 31, url: '#' },
  { id: 4, name: '代理4', ping: 25, url: '#' }
])

/**
 * Switch between member and agent tabs
 */
const switchTab = (tab: string) => {
  activeTab.value = tab
}

/**
 * Test connection speed - refresh ping times with random values
 */
const testSpeed = () => {
  // Generate new random ping times (20-50ms range)
  memberLines.value.forEach(line => {
    line.ping = Math.floor(Math.random() * 30) + 20
  })
  agentLines.value.forEach(line => {
    line.ping = Math.floor(Math.random() * 30) + 20
  })
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
            会员线路
          </button>
          <button 
            class="btn-agent"
            :class="{ active: activeTab === 'agent' }"
            @click="switchTab('agent')"
          >
            代理线路
          </button>
        </div>
        
        <!-- Center Title -->
        <h1 class="title">
          <span class="welcome">欢迎光临</span>
          <span class="brand">冠军</span>
        </h1>
        
        <!-- Right Buttons -->
        <div class="header-right">
          <button class="btn-action" @click="testSpeed">测速</button>
          <button class="btn-action" @click="logout">退出</button>
        </div>
      </div>
      
      <!-- Lines Container - 1201x61, 4 items each 299x60 -->
      <div class="lines-container">
        <div 
          v-for="line in currentLines()" 
          :key="line.id" 
          class="line-item"
          @click="selectLine"
        >
          <span class="line-name">{{ line.name }}</span>
          <span class="line-ping">({{ line.ping }}ms)</span>
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

