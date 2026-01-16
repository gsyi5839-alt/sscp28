<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      const success = await authStore.login(form.username, form.password)
      loading.value = false
      
      if (success) {
        router.push('/dashboard')
      }
    }
  })
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="48" color="#409EFF"><User /></el-icon>
        <h2>Welcome Back</h2>
        <p>Sign in to your account</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="Username" prop="username">
          <el-input
            v-model="form.username"
            placeholder="Enter your username"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="Password" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="Enter your password"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            Sign In
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span>Don't have an account?</span>
        <router-link to="/register">Register here</router-link>
      </div>
      
      <div class="back-home">
        <router-link to="/">
          <el-icon><ArrowLeft /></el-icon>
          Back to Home
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 16px 0 8px;
  color: #303133;
}

.login-header p {
  color: #909399;
  margin: 0;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #909399;
}

.login-footer a {
  color: #409EFF;
  text-decoration: none;
  margin-left: 8px;
}

.login-footer a:hover {
  text-decoration: underline;
}

.back-home {
  text-align: center;
  margin-top: 20px;
}

.back-home a {
  color: #909399;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.back-home a:hover {
  color: #409EFF;
}
</style>

