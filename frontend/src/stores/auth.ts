import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api'
import { ElMessage } from 'element-plus'

interface User {
  username: string
  email: string
  nickname: string
  role: string
  needPasswordChange?: boolean
  loginCountWithoutChange?: number
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

  const setSession = (data: any) => {
    if (data?.token) setToken(data.token)
    user.value = {
      username: data.username,
      email: data.email,
      nickname: data.nickname,
      role: data.role,
      needPasswordChange: data.needPasswordChange,
      loginCountWithoutChange: data.loginCountWithoutChange
    }
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
        return true
      }
      return false
    } catch {
      return false
    }
  }

  const loginWithRole = async (
    username: string,
    password: string,
    role: string,
    captchaToken: string,
    captchaCode: string
  ): Promise<{ success: boolean; needPasswordChange?: boolean; loginCount?: number; status?: number; errorMessage?: string }> => {
    // Login with role and captcha for member/agent access
    try {
      const response: any = await authApi.roleLogin({
        username,
        password,
        role,
        captchaToken,
        captchaCode
      })
      if (response.code === 200) {
        setSession(response.data)
        return {
          success: true,
          needPasswordChange: response.data.needPasswordChange,
          loginCount: response.data.loginCountWithoutChange
        }
      }
      return { success: false, status: response.code, errorMessage: response.message }
    } catch (err: any) {
      return {
        success: false,
        status: err?.response?.status,
        errorMessage: err?.response?.data?.message
      }
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
  }

  return {
    token,
    user,
    isAuthenticated,
    setSession,
    login,
    loginWithRole,
    register,
    logout,
    fetchUser
  }
})

