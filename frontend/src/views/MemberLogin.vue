<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { captchaApi } from '../api'
import { useAuthStore } from '../stores/auth'
import leftImg from '@/assets/会员登录/左侧.png'
import rightBg from '@/assets/会员登录/右侧.png'
import userIcon from '@/assets/移动登录图标/用户昵称.png'
import passwordIcon from '@/assets/移动登录图标/你的密码.png'
import captchaIcon from '@/assets/移动登录图标/验证码.png'

const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
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

  <!-- Mobile layout (existing) -->
  <div v-else class="login-page">
    <div class="login-main">
      <form class="login-wrapper" @submit.prevent="handleLogin">
        <div class="login-header">
          <div class="logo-area">
            <h1 class="welcome-title">登录邮箱</h1>
          </div>
        </div>

        <div class="input-shell account-shell">
          <span class="icon-wrap">
            <img :src="userIcon" alt="账号图标" class="mobile-icon" />
          </span>
          <input
            v-model="loginForm.account"
            type="text"
            class="field-input"
            placeholder="请输入您的账号"
            @keydown.enter.prevent="handleLogin"
          />
        </div>

        <div class="input-shell password-shell">
          <span class="icon-wrap">
            <img :src="passwordIcon" alt="密码图标" class="mobile-icon" />
          </span>
          <input
            v-model="loginForm.password"
            type="password"
            class="field-input"
            placeholder="请输入您的密码"
            @keydown.enter.prevent="handleLogin"
          />
        </div>

        <div class="input-shell captcha-shell">
          <span class="icon-wrap">
            <img :src="captchaIcon" alt="验证码图标" class="mobile-icon" />
          </span>
          <input
            v-model="loginForm.captcha"
            type="text"
            class="field-input captcha-input"
            placeholder="请输入验证码"
            maxlength="4"
            @keydown.enter.prevent="handleLogin"
          />
          <button type="button" class="captcha-box" @click="generateCaptcha">
            <img v-if="captchaImage" :src="captchaImage" alt="captcha" class="captcha-img" />
            <span v-else class="captcha-fallback">加载中...</span>
          </button>
        </div>

        <button class="login-btn" type="submit" :disabled="loading">
          {{ loading ? '...' : '登录' }}
        </button>
      </form>
    </div>

    <div class="login-footer">
      <div class="footer-line"></div>
      <button type="button" class="footer-btn" @click="goDesktop">桌面版</button>
    </div>
  </div>
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

/* ===================== Mobile Styles ===================== */
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5ebe0, #e8d5c4 30%, #d4b896 70%, #be9d76);
  display: flex;
  flex-direction: column;
}

.login-main {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: clamp(90px, 20vh, 220px) 20px 24px;
  box-sizing: border-box;
}

.login-wrapper {
  width: 100%;
  max-width: 760px;
  display: flex;
  flex-direction: column;
}

.login-header {
  width: 360px;
  height: 48px;
  margin: 0 auto 10px;
}

.logo-area {
  width: 360px;
  height: 48px;
}

.welcome-title {
  margin: 0;
  text-align: center;
  font-family: Tahoma, Helvetica, 'Microsoft Yahei', sans-serif;
  color: #ffffff;
  font-size: 32px;
  line-height: 48px;
  font-weight: 700;
  letter-spacing: 2px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.input-shell {
  width: 100%;
  height: 50px;
  border-radius: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  padding: 0 20px;
  margin: 15px auto;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.account-shell {
  background: rgba(255, 255, 255, 0.95);
}

.password-shell {
  background: rgba(255, 255, 255, 0.95);
}

.captcha-shell {
  background: rgba(255, 255, 255, 0.95);
  padding-right: 10px;
}

.icon-wrap {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.mobile-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.field-input {
  flex: 1;
  border: none;
  background: transparent;
  font-family: Tahoma, Helvetica, 'Microsoft Yahei', sans-serif;
  color: #333;
  font-size: 15px;
  outline: none;
  min-width: 0;
  line-height: 20px;
}

.field-input::placeholder {
  color: #a6a6a6;
}

.captcha-input {
  max-width: calc(100% - 96px);
}

.captcha-box {
  margin-left: auto;
  width: 80px;
  height: 36px;
  border-radius: 8px;
  overflow: hidden;
  border: none;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
  flex-shrink: 0;
}

.captcha-img {
  width: 85px;
  height: 27px;
  object-fit: none;
  object-position: center center;
  display: block;
}

.captcha-fallback {
  font-size: clamp(12px, 2.6vw, 20px);
  color: #8c8c8c;
}

.login-btn {
  margin-top: clamp(28px, 5.2vw, 46px);
  width: 100%;
  height: clamp(64px, 11.5vw, 102px);
  border-radius: 999px;
  border: none;
  background: #b39b76;
  color: #fff;
  font-size: clamp(24px, 4.8vw, 48px);
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-footer {
  width: 100%;
  background: transparent;
  padding: 0 24px 22px;
  box-sizing: border-box;
}

.footer-line {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);
}

.footer-btn {
  display: block;
  margin: clamp(28px, 4.2vh, 52px) auto 0;
  width: clamp(132px, 23vw, 220px);
  height: clamp(52px, 9.2vw, 76px);
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.06);
  color: #f4f4f4;
  font-size: clamp(18px, 3.5vw, 34px);
  cursor: pointer;
}

@media (max-width: 420px) {
  .login-page {
    background: linear-gradient(180deg, #f5ebe0, #e8d5c4 30%, #d4b896 70%, #be9d76);
  }
}
</style>
