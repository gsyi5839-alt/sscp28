<script setup lang="ts">
interface LoginForm {
  account: string
  password: string
  captcha: string
}

import userIcon from '@/assets/移动登录图标/用户昵称.png'
import passwordIcon from '@/assets/移动登录图标/你的密码.png'
import captchaIcon from '@/assets/移动登录图标/验证码.png'

defineProps<{
  loginForm: LoginForm
  captchaImage: string
  loading: boolean
}>()

const emit = defineEmits<{
  submit: []
  refreshCaptcha: []
  goDesktop: []
}>()
</script>

<template>
  <div class="login-page">
    <div class="login-main">
      <form class="login-wrapper" @submit.prevent="emit('submit')">
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
            @keydown.enter.prevent="emit('submit')"
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
            @keydown.enter.prevent="emit('submit')"
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
            @keydown.enter.prevent="emit('submit')"
          />
          <button type="button" class="captcha-box" @click="emit('refreshCaptcha')">
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
      <button type="button" class="footer-btn" @click="emit('goDesktop')">桌面版</button>
    </div>
  </div>
</template>

<style scoped>
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
