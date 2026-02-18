import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  // Prefer build-time configuration; otherwise default to same-domain /api (production uses Nginx reverse proxy, local dev uses Vite proxy)
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    // Public endpoints should not carry token. For auth endpoints, only unauthenticated actions are excluded.
    // Endpoints like /auth/me and /auth/change-password must include Bearer token.
    const url = config.url || ''
    const isPublic = url.startsWith('/public/')
    const isAuthWithoutToken =
      url.startsWith('/auth/login') ||
      url.startsWith('/auth/register') ||
      url.startsWith('/auth/role-login') ||
      url.startsWith('/auth/force-change-password')
    if (token && !isPublic && !isAuthWithoutToken) {
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
  // Add t parameter to avoid occasional issues caused by intermediate caching/WAF false positives (backend ignores unknown parameters)
  search: (keyword: string, page = 1, size = 20) =>
    api.get('/public/search', { params: { q: keyword, page, size, t: Date.now() } })
}

// Lines API
export const linesApi = {
  // Add t parameter to avoid occasional issues caused by intermediate caching/WAF false positives (backend ignores unknown parameters)
  getLines: (type: 'MEMBER' | 'AGENT') => api.get('/public/lines', { params: { type, t: Date.now() } })
}

// Captcha API
export const captchaApi = {
  // Add t parameter to bypass any intermediate caching (CDN/proxy/WAF/browser).
  // Captcha tokens are one-time and must never be served from cache.
  getCaptcha: () => api.get('/public/captcha', { params: { t: Date.now() } })
}

// Change Password API
export const passwordApi = {
  changePassword: (data: { oldPassword: string; newPassword: string }) => 
    api.post('/auth/change-password', data)
}

// Lottery API - proxies upstream bw1284.cc lottery data
export const lotteryApi = {
  /** Get current issue info: latest result + next draw countdown */
  getInfo: (lotCode = 720) =>
    api.get('/public/lottery/info', { params: { lotCode, t: Date.now() } }),

  /** Get paginated lottery history list */
  getList: (lotCode = 720, pageNo = 1, pageSize = 30) =>
    api.get('/public/lottery/list', { params: { lotCode, pageNo, pageSize, t: Date.now() } }),
}