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
  email: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 3, max: 50, message: 'Username must be 3-50 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      const success = await authStore.register({
        username: form.username,
        email: form.email,
        password: form.password,
        nickname: form.nickname || undefined
      })
      loading.value = false
      
      if (success) {
        router.push('/dashboard')
      }
    }
  })
}
</script>

<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <el-icon :size="48" color="#67C23A"><UserFilled /></el-icon>
        <h2>Create Account</h2>
        <p>Join us today</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="Username" prop="username">
          <el-input
            v-model="form.username"
            placeholder="Choose a username"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="Email" prop="email">
          <el-input
            v-model="form.email"
            placeholder="Enter your email"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item label="Nickname" prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="Display name (optional)"
            size="large"
            prefix-icon="Avatar"
          />
        </el-form-item>
        
        <el-form-item label="Password" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="Create a password"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="Confirm your password"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="success"
            size="large"
            :loading="loading"
            @click="handleRegister"
            style="width: 100%"
          >
            Create Account
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <span>Already have an account?</span>
        <router-link to="/login">Sign in</router-link>
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
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
  background: white;
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  margin: 16px 0 8px;
  color: #303133;
}

.register-header p {
  color: #909399;
  margin: 0;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #909399;
}

.register-footer a {
  color: #409EFF;
  text-decoration: none;
  margin-left: 8px;
}

.register-footer a:hover {
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

