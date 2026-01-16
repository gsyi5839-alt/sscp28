import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api'
import { ElMessage } from 'element-plus'

interface User {
  username: string
  email: string
  nickname: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearAuth = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  const login = async (username: string, password: string) => {
    try {
      const response: any = await authApi.login({ username, password })
      if (response.code === 200) {
        setToken(response.data.token)
        user.value = {
          username: response.data.username,
          email: response.data.email,
          nickname: response.data.nickname,
          role: response.data.role
        }
        ElMessage.success('Login successful!')
        return true
      }
      return false
    } catch {
      return false
    }
  }

  const register = async (data: { username: string; email: string; password: string; nickname?: string }) => {
    try {
      const response: any = await authApi.register(data)
      if (response.code === 200) {
        setToken(response.data.token)
        user.value = {
          username: response.data.username,
          email: response.data.email,
          nickname: response.data.nickname,
          role: response.data.role
        }
        ElMessage.success('Registration successful!')
        return true
      }
      return false
    } catch {
      return false
    }
  }

  const fetchUser = async () => {
    if (!token.value) return
    try {
      const response: any = await authApi.getCurrentUser()
      if (response.code === 200) {
        user.value = response.data
      }
    } catch {
      clearAuth()
    }
  }

  const logout = () => {
    clearAuth()
    ElMessage.success('Logged out successfully')
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    register,
    logout,
    fetchUser
  }
})

