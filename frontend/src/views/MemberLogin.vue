<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { captchaApi } from '../api'
import { useAuthStore } from '../stores/auth'
import MemberLoginMobile from '@/mobile/components/MemberLoginMobile.vue'
import { isResponsiveMobileClient } from '@/mobile/utils/client'
import leftImg from '@/assets/会员登录/左侧.png'
import rightBg from '@/assets/会员登录/右侧.png'

const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = isResponsiveMobileClient()
}

const router = useRouter()
const authStore = useAuthStore()

// Form data
const loginForm = ref({
  account: '',
  password: '',
  captcha: ''
})

// Captcha image from backend
const captchaImage = ref('')
const captchaToken = ref('')

// Loading state
const loading = ref(false)

/**
 * Load captcha code from backend
 */
const generateCaptcha = async () => {
  try {
    const response: any = await captchaApi.getCaptcha()
    if (response.code === 200) {
      captchaImage.value = response.data.image
      captchaToken.value = response.data.token
    }
  } catch {
    captchaImage.value = ''
    captchaToken.value = ''
    ElMessage.error('验证码加载失败')
  }
}

/**
 * Handle login submission
 */
const handleLogin = async () => {
  // Validate form
  if (!loginForm.value.account.trim()) {
    ElMessage.warning('请输入账号')
    return
  }
  if (!loginForm.value.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }
  if (!loginForm.value.captcha.trim()) {
    ElMessage.warning('请输入验证码')
    return
  }
  loading.value = true

  try {
    if (!captchaToken.value) {
      ElMessage.warning('验证码未加载，请刷新')
      return
    }
    const result = await authStore.loginWithRole(
      loginForm.value.account,
      loginForm.value.password,
      'MEMBER',
      captchaToken.value,
      loginForm.value.captcha
    )
    if (result.success) {
      // If password change required, force redirect to change password page
      if (result.needPasswordChange) {
        // Force redirect to change password page
        router.push('/change-password')
        return
      }
      // Password already changed: always redirect to user agreement page on login
      router.push('/user-agreement')
    } else {
      // When account is disabled (due to legacy logic), guide to "force change password" unlock page
      if (result.status === 403 && result.errorMessage && result.errorMessage.includes('Account disabled')) {
        router.push({ path: '/force-change-password', query: { role: 'MEMBER', username: loginForm.value.account } })
        return
      }
      generateCaptcha()
      loginForm.value.captcha = ''
    }
  } catch (error) {
    ElMessage.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}

const goDesktop = () => {
  router.push('/member')
}

// Initialize captcha on mount
onMounted(() => {
  document.title = 'Welcome'
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) {
    favicon.href = '/favicon.png'
  }
  checkMobile()
  generateCaptcha()
})
</script>

<template>
  <!-- Desktop layout -->
  <div v-if="!isMobile" class="desktop-page">
    <div class="desktop-container">
      <div class="desktop-flex-center">
        <div class="desktop-left">
          <img :src="leftImg" alt="mail" class="desktop-mail-img" />
        </div>
        <div class="desktop-right" :style="{ backgroundImage: `url(${rightBg})` }">
          <form class="desktop-form" @submit.prevent="handleLogin">
            <div class="desktop-field">
              <label class="desktop-label">账号：</label>
              <input
                v-model="loginForm.account"
                type="text"
                class="desktop-input"
                placeholder="请输入账号"
                @keydown.enter.prevent="handleLogin"
              />
            </div>
            <div class="desktop-field">
              <label class="desktop-label">密码：</label>
              <input
                v-model="loginForm.password"
                type="password"
                class="desktop-input"
                placeholder="请输入密码"
                @keydown.enter.prevent="handleLogin"
              />
            </div>
            <div class="desktop-field">
              <label class="desktop-label">验证码：</label>
              <input
                v-model="loginForm.captcha"
                type="text"
                class="desktop-input desktop-captcha-input"
                placeholder="验证码"
                maxlength="4"
                @keydown.enter.prevent="handleLogin"
              />
              <button type="button" class="desktop-captcha-box" @click="generateCaptcha">
                <img v-if="captchaImage" :src="captchaImage" alt="captcha" class="desktop-captcha-img" />
                <span v-else class="desktop-captcha-fallback">加载中...</span>
              </button>
            </div>
            <div class="desktop-submit-row">
              <button class="desktop-login-btn" type="submit" :disabled="loading">
                {{ loading ? '...' : '登录' }}
              </button>
            </div>
          </form>
        </div>
      </div>
      <div class="desktop-footer">
        <p class="desktop-copyright">Copyright © Microsoft 2024</p>
      </div>
    </div>
  </div>

  <MemberLoginMobile
    v-else
    :login-form="loginForm"
    :captcha-image="captchaImage"
    :loading="loading"
    @submit="handleLogin"
    @refresh-captcha="generateCaptcha"
    @go-desktop="goDesktop"
  />
</template>

<style scoped>
/* ===================== Desktop Styles ===================== */
.desktop-page {
  min-height: 100vh;
  width: 100%;
  background: #e8f4fb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.desktop-container {
  width: 1080px;
  height: 472px;
  margin: auto;
  text-align: center;
}

.desktop-flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.desktop-left {
  width: 403px;
  flex-shrink: 0;
}

.desktop-mail-img {
  width: 100%;
  height: auto;
  display: block;
}

.desktop-right {
  width: 374px;
  height: 378px;
  flex-shrink: 0;
  background-repeat: no-repeat;
  background-position: left top;
  background-size: 378px 383px;
  text-align: left;
  box-sizing: border-box;
  border: 1px solid #a8d4f0;
  border-radius: 4px;
  overflow: hidden;
}

.desktop-form {
  width: 260px;
  height: 240px;
  padding-top: 80px;
  display: inline-block;
  text-align: left;
}

.desktop-field {
  display: block;
  margin-top: 12px;
  width: 260px;
}

.desktop-label {
  font-size: 13px;
  color: #000;
  white-space: nowrap;
  display: inline-block;
  width: 80px;
  min-width: 80px;
  text-align: right;
  height: 30px;
  line-height: 30px;
  vertical-align: middle;
}

.desktop-input {
  height: 28px;
  border: 1px solid #ccc;
  border-radius: 2px;
  padding: 0 8px;
  font-size: 13px;
  outline: none;
  background: #fff;
  vertical-align: middle;
  box-sizing: border-box;
}

.desktop-input:focus {
  border-color: #6aafe6;
}

.desktop-captcha-input {
  width: 80px;
  flex: none;
}

.desktop-captcha-box {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0;
  display: inline-flex;
  align-items: center;
  vertical-align: middle;
  margin-left: 8px;
}

.desktop-captcha-img {
  height: 27px;
  width: 85px;
  vertical-align: middle;
}

.desktop-captcha-fallback {
  font-size: 12px;
  color: #888;
}

.desktop-submit-row {
  margin-top: 20px;
  padding-left: 80px;
}

.desktop-login-btn {
  padding: 4px 16px;
  border: 1px solid #5c2e0d;
  border-radius: 3px;
  background: #5c2e0d;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  height: 22px;
  line-height: 14px;
}

.desktop-login-btn:hover {
  opacity: 0.85;
}

.desktop-login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.desktop-footer {
  margin-top: 40px;
  font-size: 12px;
  text-align: center;
  border-top: 1px solid currentColor;
  border-color: rgba(0, 0, 0, 0.1);
  padding-top: 10px;
}

.desktop-copyright {
  margin: 0;
  font-size: 12px;
  color: #666;
}
</style>
