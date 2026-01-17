<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const goToLogin = () => router.push('/member/login')
const goToRegister = () => router.push('/register')
const goToDashboard = () => router.push('/dashboard')
</script>

<template>
  <div class="home">
    <div class="hero">
      <h1>BCBBS Platform</h1>
      <p class="subtitle">Modern Full-Stack Application</p>
      <p class="description">
        Built with Vue 3 + TypeScript + Element Plus + Spring Boot
      </p>
      <div class="buttons">
        <template v-if="authStore.isAuthenticated">
          <el-button type="primary" size="large" @click="goToDashboard">
            <el-icon><Dashboard /></el-icon>
            Go to Dashboard
          </el-button>
          <el-button size="large" @click="authStore.logout">
            <el-icon><SwitchButton /></el-icon>
            Logout
          </el-button>
        </template>
        <template v-else>
          <el-button type="primary" size="large" @click="goToLogin">
            <el-icon><User /></el-icon>
            Login
          </el-button>
          <el-button size="large" @click="goToRegister">
            <el-icon><UserFilled /></el-icon>
            Register
          </el-button>
        </template>
      </div>
    </div>
    
    <div class="features">
      <div class="feature-card">
        <el-icon :size="48" color="#409EFF"><Monitor /></el-icon>
        <h3>Vue 3 + TypeScript</h3>
        <p>Modern frontend with Composition API</p>
      </div>
      <div class="feature-card">
        <el-icon :size="48" color="#67C23A"><Setting /></el-icon>
        <h3>Spring Boot</h3>
        <p>Robust RESTful API backend</p>
      </div>
      <div class="feature-card">
        <el-icon :size="48" color="#E6A23C"><Lock /></el-icon>
        <h3>JWT Authentication</h3>
        <p>Secure token-based auth</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.hero {
  text-align: center;
  padding: 60px 20px;
  color: white;
}

.hero h1 {
  font-size: 3.5rem;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.subtitle {
  font-size: 1.5rem;
  opacity: 0.9;
  margin-bottom: 20px;
}

.description {
  font-size: 1rem;
  opacity: 0.8;
  margin-bottom: 40px;
}

.buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  max-width: 1000px;
  margin: 60px auto 0;
  padding: 0 20px;
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 40px 30px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-card h3 {
  margin: 20px 0 10px;
  color: #303133;
}

.feature-card p {
  color: #909399;
  margin: 0;
}
</style>

