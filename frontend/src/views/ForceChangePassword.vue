<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi, captchaApi } from '../api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const username = ref<string>((route.query.username as string) || '')
const role = ref<string>(((route.query.role as string) || 'MEMBER').toUpperCase())

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const captchaCode = ref('')
const captchaToken = ref('')
const inputCaptcha = ref('')
const captchaColors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD']

const loading = ref(false)

const getRandomColor = (index: number) => captchaColors[index % captchaColors.length]

const generateCaptcha = async () => {
  try {
    const response: any = await captchaApi.getCaptcha()
    if (response.code === 200) {
      captchaCode.value = response.data.code
      captchaToken.value = response.data.token
      inputCaptcha.value = ''
    }
  } catch {
    captchaCode.value = ''
    captchaToken.value = ''
    ElMessage.error('Failed to load captcha')
  }
}

const submit = async () => {
  if (!username.value.trim()) return ElMessage.warning('Please enter account')
  if (!oldPassword.value.trim()) return ElMessage.warning('Please enter old password')
  if (!newPassword.value.trim()) return ElMessage.warning('Please enter new password')
  if (newPassword.value.length < 6) return ElMessage.warning('New password must be at least 6 characters')
  if (newPassword.value !== confirmPassword.value) return ElMessage.error('Passwords do not match')
  if (!inputCaptcha.value.trim()) return ElMessage.warning('Please enter captcha')
  if (!captchaToken.value) return ElMessage.warning('Captcha not loaded, please refresh')

  loading.value = true
  try {
    const response: any = await authApi.forceChangePassword({
      username: username.value.trim(),
      role: role.value,
      oldPassword: oldPassword.value,
      newPassword: newPassword.value,
      captchaToken: captchaToken.value,
      captchaCode: inputCaptcha.value.trim()
    })

    if (response.code === 200) {
      ElMessage.success('Password changed successfully, account unlocked')
      authStore.setSession(response.data)

      // Redirect based on role
      if (role.value === 'AGENT') {
        router.push('/game')
      } else {
        const accepted = localStorage.getItem('userAgreementAccepted') === 'true'
        router.push(accepted ? '/game' : '/user-agreement')
      }
    }
  } catch {
    // Error message unified by axios interceptor
    generateCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  document.title = '强制修改密码'
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) favicon.href = '/favicon.png'
  generateCaptcha()
})
</script>

<template>
  <div class="page">
    <div class="box">
      <div class="header">Force Change Password (Unlock Account)</div>

      <div class="row">
        <div class="label"><span class="red">*</span>Account:</div>
        <div class="field">
          <input v-model="username" class="input" placeholder="Enter account" />
        </div>
      </div>

      <div class="row">
        <div class="label"><span class="red">*</span>Role:</div>
        <div class="field">
          <select v-model="role" class="input">
            <option value="MEMBER">Member</option>
            <option value="AGENT">Agent</option>
          </select>
        </div>
      </div>

      <div class="row">
        <div class="label"><span class="red">*</span>Old Password:</div>
        <div class="field">
          <input v-model="oldPassword" type="password" class="input" placeholder="Enter old password" />
        </div>
      </div>

      <div class="row">
        <div class="label"><span class="red">*</span>New Password:</div>
        <div class="field">
          <input v-model="newPassword" type="password" class="input" placeholder="At least 6 characters" />
        </div>
      </div>

      <div class="row">
        <div class="label"><span class="red">*</span>Confirm Password:</div>
        <div class="field">
          <input v-model="confirmPassword" type="password" class="input" placeholder="Re-enter new password" />
        </div>
      </div>

      <div class="row row-last">
        <div class="label"><span class="red">*</span>Captcha:</div>
        <div class="field captcha-field">
          <input v-model="inputCaptcha" class="input captcha-input" placeholder="Captcha" maxlength="4" />
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
      </div>

      <div class="btn-row">
        <button class="btn" :disabled="loading" @click="submit">
          {{ loading ? 'Processing...' : 'Change & Unlock' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  padding: 20px;
}

.box {
  width: 520px;
  background: #fff;
  border: 1px solid #999;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header {
  height: 34px;
  line-height: 34px;
  text-align: center;
  background: linear-gradient(180deg, #fdd9a8 0%, #f9c06a 100%);
  border-bottom: 1px solid #e0c090;
  font-size: 14px;
  color: #333;
}

.row {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #eee;
}

.row-last {
  border-bottom: none;
}

.label {
  width: 110px;
  color: #333;
  font-size: 13px;
}

.field {
  flex: 1;
}

.input {
  width: 100%;
  height: 28px;
  border: 1px solid #ccc;
  padding: 0 10px;
  box-sizing: border-box;
  outline: none;
  font-size: 13px;
}

.red {
  color: #ff0000;
  margin-right: 4px;
}

.btn-row {
  padding: 14px 16px 18px;
  display: flex;
  justify-content: center;
}

.btn {
  width: 160px;
  height: 32px;
  border: none;
  background: #0099ff;
  color: #fff;
  cursor: pointer;
  border-radius: 4px;
}

.btn:disabled {
  background: #9ccff2;
  cursor: not-allowed;
}

.captcha-field {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-display {
  width: 90px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f6f6f6;
  border: 1px solid #ddd;
  cursor: pointer;
  user-select: none;
}

.captcha-char {
  font-weight: 700;
  font-size: 16px;
  margin: 0 1px;
}
</style>

