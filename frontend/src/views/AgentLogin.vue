<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import leftImage from '@/assets/代理登录/1.png'
import rightBgImage from '@/assets/代理登录/2.png'

const router = useRouter()

// Form data
const loginForm = ref({
  account: '',
  password: '',
  captcha: ''
})

// Captcha code - randomly generated
const captchaCode = ref('')
const captchaColors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD']

// Loading state
const loading = ref(false)

/**
 * Generate random captcha code (4 digits)
 */
const generateCaptcha = () => {
  const chars = '0123456789'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captchaCode.value = code
}

/**
 * Get random color for each captcha digit
 */
const getRandomColor = (index: number) => {
  return captchaColors[index % captchaColors.length]
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
  if (loginForm.value.captcha !== captchaCode.value) {
    ElMessage.error('验证码错误')
    generateCaptcha() // Refresh captcha on error
    loginForm.value.captcha = ''
    return
  }

  loading.value = true

  try {
    // TODO: Implement actual login API call
    // For now, simulate login success
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('登录成功')
    // Navigate to change password page
    router.push('/change-password')
  } catch (error) {
    ElMessage.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}

// Initialize captcha on mount
onMounted(() => {
  document.title = '代理登录'
  // Set custom favicon for login page
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) {
    favicon.href = '/favicon.png'
  }
  generateCaptcha()
})
</script>

<template>
  <div class="login-page">
    <!-- Main content area -->
    <div class="login-container">
      <!-- Left side - Image -->
      <div class="left-section">
        <img :src="leftImage" alt="Email" class="left-image" />
      </div>

      <!-- Right side - Login form with background image -->
      <div class="right-section" :style="{ backgroundImage: `url(${rightBgImage})` }">
        <!-- Login form - title is part of background image -->
        <div class="form-content">
          <!-- Account input -->
          <div class="form-row">
            <label class="form-label">账号：</label>
            <input
              v-model="loginForm.account"
              type="text"
              class="form-input"
              placeholder="请输入账号"
            />
          </div>

          <!-- Password input -->
          <div class="form-row">
            <label class="form-label">密码：</label>
            <input
              v-model="loginForm.password"
              type="password"
              class="form-input"
              placeholder="请输入密码"
            />
          </div>

          <!-- Captcha input -->
          <div class="form-row">
            <label class="form-label">验证码：</label>
            <input
              v-model="loginForm.captcha"
              type="text"
              class="form-input captcha-input"
              placeholder="验证码"
              maxlength="4"
            />
            <!-- Captcha display -->
            <div class="captcha-display" @click="generateCaptcha">
              <span
                v-for="(char, index) in captchaCode"
                :key="index"
                class="captcha-char"
                :style="{ color: getRandomColor(index) }"
              >
                {{ char }}
              </span>
            </div>
          </div>

          <!-- Login button -->
          <div class="form-row button-row">
            <button
              class="login-btn"
              :disabled="loading"
              @click="handleLogin"
            >
              {{ loading ? '...' : '登录' }}
            </button>
          </div>
        </div>

        <!-- Divider line inside login box -->
        <div class="divider"></div>
      </div>
    </div>

    <!-- Footer -->
    <div class="footer">
      <div class="footer-line"></div>
      <p class="copyright">Copyright © Microsoft 2024</p>
    </div>
  </div>
</template>

<style scoped>
/* Main page container with subtle pink gradient background */
.login-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #FFF0F0 0%, #FFF5F5 80px, #FFFFFF 120px, #FFFFFF 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 80px 20px 20px 20px;
}

/* Login container - holds left image and right form */
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 1080px;
  min-height: 472px;
  margin: 0 auto;
}

/* Left section - image area */
.left-section {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Left image - 403x383 */
.left-image {
  width: 403px;
  height: 383px;
  object-fit: contain;
}

/* Right section - login form area with background image */
.right-section {
  background-size: 100% 100%;
  background-repeat: no-repeat;
  background-position: center;
  border: none;
  border-radius: 4px;
  padding: 80px 40px 40px 40px;
  width: 374px;
  height: 378px;
  box-sizing: border-box;
  position: relative;
}

/* Form title with icon */
.form-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 30px;
}

/* Title icon - globe emoji */
.title-icon {
  font-size: 20px;
}

/* Title text */
.title-text {
  font-size: 22px;
  color: #cc0000;
  font-weight: 500;
}

/* Form content area */
.form-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Form row - label and input */
.form-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Form label */
.form-label {
  width: 60px;
  text-align: right;
  font-size: 14px;
  color: #666666;
  flex-shrink: 0;
}

/* Form input - 170x28 */
.form-input {
  width: 170px;
  height: 28px;
  padding: 0 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  color: #333333;
  outline: none;
  transition: border-color 0.3s;
}

.form-input:focus {
  border-color: #cc0000;
}

.form-input::placeholder {
  color: #bfbfbf;
}

/* Captcha input - shorter width */
.captcha-input {
  width: 80px;
}

/* Captcha display area */
.captcha-display {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
  height: 28px;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
}

/* Individual captcha character */
.captcha-char {
  font-size: 20px;
  font-weight: bold;
  font-family: 'Arial', sans-serif;
  letter-spacing: 2px;
}

/* Button row */
.button-row {
  justify-content: center;
  margin-top: 10px;
}

/* Login button - 48x24, background #5c2e0d */
.login-btn {
  width: 48px;
  height: 24px;
  background: #5c2e0d;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.login-btn:hover {
  opacity: 0.9;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Divider line inside login box */
.divider {
  margin-top: 30px;
  border-top: 1px solid #e0e0e0;
}

/* Footer section */
.footer {
  width: 100%;
  max-width: 1080px;
  margin-top: 40px;
  text-align: center;
}

/* Footer line - red/brown color for agent theme */
.footer-line {
  border-top: 1px solid #efba84;
  margin-bottom: 20px;
}

/* Copyright text - dark gray, not too black */
.copyright {
  font-size: 14px;
  color: #555555;
  font-style: italic;
  margin: 0;
}

/* Mobile: same as PC layout, allow horizontal scroll */
@media screen and (max-width: 900px) {
  .login-page {
    justify-content: flex-start;
    padding: 80px 0 20px 0;
  }

  .login-container {
    min-width: 800px;
  }
}
</style>

