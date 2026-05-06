<script setup lang="ts">
type TabKey = 'member' | 'agent'

type LineItem = {
  id: number | string
  name: string
  pingMs?: number | null
  url?: string
  type?: string
}

defineProps<{
  activeTab: TabKey
  speedTesting: boolean
  memberDesktopLines: LineItem[]
  memberMobileLines: LineItem[]
  agentDisplayLines: LineItem[]
}>()

const emit = defineEmits<{
  toggle: [tab: TabKey]
  speed: []
  logout: []
  openLine: [line: LineItem]
}>()
</script>

<template>
  <div class="container">
    <div class="flex-space-between header">
      <div class="btn-group left-group">
        <button id="member-btn" class="btn2" :class="{ btn1: activeTab === 'member' }" @click="emit('toggle', 'member')">
          会员线路
        </button>
        <button id="agent-btn" class="btn2" :class="{ btn1: activeTab === 'agent' }" @click="emit('toggle', 'agent')">
          代理线路
        </button>
      </div>

      <h1>欢迎光临&nbsp;&nbsp;&nbsp;<span id="lineName">红运</span></h1>

      <div class="btn-group right-group">
        <button class="btn2" @click="emit('speed')">测速</button>
        <button class="btn2" @click="emit('logout')">退出</button>
      </div>
    </div>

    <div class="content">
      <template v-if="activeTab === 'member'">
        <div id="member-lines-container" class="lines-wrap">
          <div class="group-title">电脑端线路</div>
          <div class="line-items">
            <div v-for="line in memberDesktopLines" :key="`desktop-${line.id}`" class="line-item">
              <button class="line-link member-item" @click="emit('openLine', line)">
                <span>{{ line.name }}</span>
                <span class="sub-text">{{ speedTesting ? '测速中...' : `(${line.pingMs ?? 'N/A'}ms)` }}</span>
              </button>
            </div>
          </div>
        </div>

        <div id="mobile-lines-container" class="lines-wrap">
          <div class="group-title">移动端线路</div>
          <div class="line-items">
            <div v-for="line in memberMobileLines" :key="`mobile-${line.id}`" class="line-item">
              <button class="line-link mobile-item" @click="emit('openLine', line)">
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
            <button class="line-link agent-line-item" @click="emit('openLine', line)">
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
.container {
  width: 100%;
  padding: 10px 5px 20px;
  background: #efefef;
  min-height: 100vh;
  box-sizing: border-box;
}

.flex-space-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header {
  flex-direction: column;
  gap: 15px;
}

.left-group {
  order: 2;
}

.header h1 {
  order: -1;
  padding: 0;
  margin: 0;
  text-align: center;
  font-size: 20px;
  line-height: 25px;
  font-weight: normal;
  color: red;
}

.right-group {
  order: 3;
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
  width: 70px;
  height: 32px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
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

#mobile-lines-container {
  margin-top: 20px;
}

.group-title {
  width: 100%;
  padding: 10px;
  background-color: #f0f0f0;
  border-right: 1px solid #ccc;
  border-bottom: 1px solid #ccc;
  font-weight: 700;
  color: #333;
  box-sizing: border-box;
  font-size: 14px;
}

.line-items {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
}

.line-item {
  width: 50%;
  height: auto;
  line-height: normal;
  background-color: #f0f0f0;
  text-align: center;
  border-right: 1px solid #ccc;
  border-bottom: 1px solid #ccc;
  box-sizing: border-box;
}

.line-link {
  width: 100%;
  min-height: 52px;
  border: none;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  padding: 8px 6px;
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

@media screen and (max-width: 480px) {
  .container {
    padding: 10px 5px;
  }

  .header h1 {
    font-size: 18px;
  }

  .btn2 {
    width: 60px;
    height: 30px;
    font-size: 12px;
  }

  .line-item {
    height: auto;
    line-height: normal;
  }

  .line-link {
    min-height: 50px;
  }

  .sub-text {
    font-size: 11px;
  }
}
</style>
