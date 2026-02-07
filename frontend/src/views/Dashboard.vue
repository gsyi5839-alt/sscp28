<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { healthApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const healthStatus = ref<any>(null)
const loading = ref(false)

onMounted(async () => {
  await authStore.fetchUser()
  await checkHealth()
})

const checkHealth = async () => {
  loading.value = true
  try {
    const response: any = await healthApi.check()
    if (response.code === 200) {
      healthStatus.value = response.data
    }
  } catch {
    ElMessage.warning('无法连接到后端服务')
  }
  loading.value = false
}

const handleLogout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <div class="dashboard">
    <el-container>
      <el-header class="dashboard-header">
        <div class="logo">
          <el-icon :size="28"><Monitor /></el-icon>
          <span>BCBBS 控制面板</span>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-dropdown">
              <el-avatar :size="32" icon="User" />
              <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/change-password')">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/')">
                  <el-icon><House /></el-icon>
                  返回首页
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="dashboard-main">
        <div class="welcome-section">
          <h1>欢迎回来，{{ authStore.user?.nickname || authStore.user?.username }}！👋</h1>
          <p>这是您的控制面板概览</p>
        </div>
        
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8">
            <el-card class="stat-card">
              <template #header>
                <div class="card-header">
                  <el-icon :size="24" color="#409EFF"><User /></el-icon>
                  <span>用户信息</span>
                </div>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="用户名">
                  {{ authStore.user?.username }}
                </el-descriptions-item>
                <el-descriptions-item label="邮箱">
                  {{ authStore.user?.email }}
                </el-descriptions-item>
                <el-descriptions-item label="角色">
                  <el-tag :type="authStore.user?.role === 'ADMIN' ? 'danger' : 'success'">
                    {{ authStore.user?.role }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="8">
            <el-card class="stat-card">
              <template #header>
                <div class="card-header">
                  <el-icon :size="24" color="#67C23A"><Connection /></el-icon>
                  <span>后端状态</span>
                </div>
              </template>
              <div v-if="loading" class="loading-status">
                <el-icon class="is-loading"><Loading /></el-icon>
                检查中...
              </div>
              <div v-else-if="healthStatus" class="health-status">
                <el-tag type="success" size="large">
                  <el-icon><CircleCheck /></el-icon>
                  {{ healthStatus.status }}
                </el-tag>
                <p class="service-name">{{ healthStatus.service }}</p>
              </div>
              <div v-else class="health-status">
                <el-tag type="danger" size="large">
                  <el-icon><CircleClose /></el-icon>
                  离线
                </el-tag>
                <p class="service-name">后端服务不可用</p>
              </div>
              <el-button 
                type="primary" 
                text 
                @click="checkHealth" 
                :loading="loading"
                style="margin-top: 10px"
              >
                刷新状态
              </el-button>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="8">
            <el-card class="stat-card">
              <template #header>
                <div class="card-header">
                  <el-icon :size="24" color="#E6A23C"><InfoFilled /></el-icon>
                  <span>快捷操作</span>
                </div>
              </template>
              <div class="quick-actions">
                <el-button type="primary" @click="router.push('/')">
                  <el-icon><House /></el-icon>
                  返回首页
                </el-button>
                <el-button @click="checkHealth">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}

.dashboard-header {
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  color: #606266;
}

.dashboard-main {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: 24px;
}

.welcome-section h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
}

.welcome-section p {
  color: #909399;
  margin: 0;
}

.stat-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

.loading-status,
.health-status {
  text-align: center;
  padding: 20px 0;
}

.loading-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
}

.service-name {
  margin: 10px 0 0;
  color: #909399;
  font-size: 14px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-actions .el-button {
  width: 100%;
}
</style>

