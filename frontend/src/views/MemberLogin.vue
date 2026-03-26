<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Key, Lock, UserFilled } from '@element-plus/icons-vue'
import { captchaApi } from '../api'
import { useAuthStore } from '../stores/auth'
import leftImg from '@/assets/会员登录/左侧.png'
import rightBg from '@/assets/会员登录/右侧.png'

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
                <span v-else class="desktop-captcha-fallback">点击刷新</span>
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
        <h1 class="welcome-title">登录邮箱</h1>

        <div class="input-shell">
          <span class="icon-wrap">
            <UserFilled class="field-icon" />
          </span>
          <input
            v-model="loginForm.account"
            type="text"
            class="field-input"
            placeholder="请输入您的账号"
            @keydown.enter.prevent="handleLogin"
          />
        </div>

        <div class="input-shell">
          <span class="icon-wrap">
            <Lock class="field-icon" />
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
            <Key class="field-icon" />
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
            <span v-else class="captcha-fallback">点击刷新</span>
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
  background: #d8d0c3;
  display: flex;
  flex-direction: column;
}

.login-main {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: clamp(120px, 25vh, 340px) 38px 24px;
  box-sizing: border-box;
}

.login-wrapper {
  width: min(100%, 760px);
  display: flex;
  flex-direction: column;
}

.welcome-title {
  margin: 0 0 clamp(42px, 6vh, 70px);
  text-align: center;
  color: #ffffff;
  font-size: clamp(28px, 7vw, 62px);
  font-weight: 700;
  letter-spacing: 2px;
  text-shadow: 0 7px 14px rgba(0, 0, 0, 0.16);
}

.input-shell {
  width: 100%;
  height: clamp(56px, 11vw, 100px);
  border-radius: 999px;
  background: #f5f5f5;
  box-shadow: 0 8px 16px rgba(103, 92, 75, 0.16);
  display: flex;
  align-items: center;
  padding: 0 clamp(18px, 3vw, 32px);
  margin-bottom: clamp(20px, 3.2vw, 30px);
  box-sizing: border-box;
}

.icon-wrap {
  width: clamp(34px, 6vw, 52px);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: clamp(12px, 2vw, 20px);
  flex-shrink: 0;
}

.field-icon {
  color: #b8b8b8;
  width: 1em;
  height: 1em;
  font-size: clamp(24px, 4.4vw, 38px);
}

.field-input {
  flex: 1;
  border: none;
  background: transparent;
  color: #8d8d8d;
  font-size: clamp(18px, 3.3vw, 36px);
  outline: none;
  min-width: 0;
  line-height: 1.1;
}

.field-input::placeholder {
  color: #a7a7a7;
}

.captcha-shell {
  padding-right: clamp(12px, 2vw, 18px);
}

.captcha-input {
  max-width: calc(100% - clamp(115px, 24vw, 180px));
}

.captcha-box {
  margin-left: auto;
  width: clamp(110px, 24vw, 180px);
  height: 100%;
  border: none;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  cursor: pointer;
  padding: 0;
  flex-shrink: 0;
}

.captcha-img {
  width: 100%;
  height: clamp(44px, 7.8vw, 68px);
  object-fit: contain;
}

.captcha-fallback {
  font-size: clamp(12px, 2.6vw, 20px);
  color: #8c8c8c;
}

.login-btn {
  margin-top: clamp(26px, 5vw, 44px);
  width: 100%;
  height: clamp(58px, 11vw, 96px);
  border-radius: 999px;
  border: none;
  background: #b49a75;
  color: #fff;
  font-size: clamp(22px, 4.4vw, 46px);
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
  background: #c3ab87;
  padding: 0 24px 18px;
  box-sizing: border-box;
}

.footer-line {
  border-top: 1px solid rgba(255, 255, 255, 0.22);
}

.footer-btn {
  display: block;
  margin: clamp(30px, 4vh, 52px) auto 0;
  width: clamp(128px, 23vw, 220px);
  height: clamp(48px, 8.8vw, 74px);
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.42);
  background: rgba(255, 255, 255, 0.05);
  color: #f4f4f4;
  font-size: clamp(18px, 3.3vw, 34px);
  cursor: pointer;
}

@media (max-width: 420px) {
  .login-page {
    background: linear-gradient(to bottom, #d8d0c3 0%, #d8d0c3 86%, #c3ab87 86%, #c3ab87 100%);
  }
}
</style>
