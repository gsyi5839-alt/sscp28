import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    const message = error.response?.data?.message || 'An error occurred'
    ElMessage.error(message)
    
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/member/login'
    }
    
    return Promise.reject(error)
  }
)

export default api

// Auth API
export const authApi = {
  login: (data: { username: string; password: string }) => 
    api.post('/auth/login', data),
  
  register: (data: { username: string; email: string; password: string; nickname?: string }) => 
    api.post('/auth/register', data),

  roleLogin: (data: { username: string; password: string; role: string; captchaToken: string; captchaCode: string }) =>
    api.post('/auth/role-login', data),

  forceChangePassword: (data: { username: string; role: string; oldPassword: string; newPassword: string; captchaToken: string; captchaCode: string }) =>
    api.post('/auth/force-change-password', data),
  
  getCurrentUser: () => 
    api.get('/auth/me')
}

// Health API
export const healthApi = {
  check: () => api.get('/public/health')
}

// Search API
export const searchApi = {
  search: (keyword: string) => api.get('/public/search', { params: { q: keyword } })
}

// Lines API
export const linesApi = {
  getLines: (type: 'MEMBER' | 'AGENT') => api.get('/public/lines', { params: { type } })
}

// Captcha API
export const captchaApi = {
  getCaptcha: () => api.get('/public/captcha')
}

// Change Password API
export const passwordApi = {
  changePassword: (data: { oldPassword: string; newPassword: string }) => 
    api.post('/auth/change-password', data)
}

