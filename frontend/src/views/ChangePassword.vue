<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { passwordApi } from '../api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Verify user is logged in
onMounted(() => {
  // For UI preview only (avoid redirect when not logged in, for direct browser preview)
  // Example: /change-password?preview=1
  if (route.query.preview === '1') {
    return
  }

  if (!authStore.isAuthenticated) {
    ElMessage.warning('Please login first')
    router.push('/member/login')
  }
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('Please enter old password')
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning('Please enter new password')
    return
  }
  if (!passwordForm.confirmPassword) {
    ElMessage.warning('Please enter confirm password')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('Passwords do not match')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('New password must be at least 6 characters')
    return
  }

  loading.value = true

  try {
    const response: any = await passwordApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    if (response.code === 200) {
      ElMessage.success('Password changed successfully')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''

      // After password change success, redirect directly to user agreement page (without logout)
      setTimeout(() => {
        router.push('/user-agreement')
      }, 1000)
    } else {
      ElMessage.error(response.message || 'Failed to change password')
    }
  } catch (error: any) {
    const message = error.response?.data?.message || 'Failed to change password'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="box">
      <!-- Title -->
      <div class="header" align="center">
        <span class="title-text"> Change Password </span>
      </div>

      <!-- Old Password -->
      <div class="row">
        <div class="label">
          <span class="red">*</span><span class="label-text">Old Password:</span>
        </div>
        <div class="field">
          <input
            v-model="passwordForm.oldPassword"
            type="password"
            class="input"
            @keyup.enter="handleChangePassword"
          />
        </div>
      </div>

      <!-- New Password -->
      <div class="row">
        <div class="label">
          <span class="red">*</span><span class="label-text">New Password:</span>
        </div>
        <div class="field">
          <input
            v-model="passwordForm.newPassword"
            type="password"
            class="input"
            @keyup.enter="handleChangePassword"
          />
        </div>
      </div>

      <!-- Confirm Password -->
      <div class="row row-last">
        <div class="label">
          <span class="red">*</span><span class="label-text">Confirm Password:</span>
        </div>
        <div class="field">
          <input
            v-model="passwordForm.confirmPassword"
            type="password"
            class="input"
            @keyup.enter="handleChangePassword"
          />
        </div>
      </div>

      <!-- Button -->
      <div class="btn-row">
        <button class="btn btn1" :disabled="loading" @click="handleChangePassword">Change Password</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 20px;
}

.box {
  width: 460px;
  height: 152px;
  background: #fff;
  border: 1px solid #efba84;
  box-sizing: border-box;
}

.header {
  height: 30px;
  line-height: 30px;
  width: 458px;
  margin: 0 auto;
  /* Title background should match "Old Password:" row background */
  background: linear-gradient(180deg, #fef8f0 0%, #fef0e0 100%);
  border-bottom: 1px solid #efba84;
}

.title-text {
  color: #333;
  font-size: 14px;
}

.row {
  display: flex;
  height: 30px;
  width: 458px;
  margin: 0 auto;
  border-bottom: 1px solid #efba84;
  box-sizing: border-box;
  /* Screenshot: each row background is light gradient (uniform across row, no left-right disconnect) */
  background: linear-gradient(180deg, #fef8f0 0%, #fef0e0 100%);
}

.row-last {
  border-bottom: 1px solid #efba84;
}

.label {
  width: 153px;
  height: 30px;
  line-height: 30px;
  text-align: right;
  padding-right: 8px;
  font-size: 13px;
  color: #333;
  box-sizing: border-box;
  /* Key: use inherited background from row (avoid left-right color mismatch) */
  background: inherit !important;
}

.label-text {
  display: inline-block;
  margin-right: 5px;
  width: 64px;
}

.red {
  color: #f00;
}

.field {
  flex: 1;
  display: flex;
  align-items: center;
  padding-left: 10px;
  border-left: 1px solid #efba84;
  box-sizing: border-box;
  /* Key: right side must also have background color (this is the "no color" area you marked) */
  background: inherit !important;
}

.input {
  width: 100px;
  height: 24px;
  padding: 0 6px;
  border: 1px solid #efba84;
  font-size: 13px;
  color: #333;
  outline: none;
  box-sizing: border-box;
}

.input:focus {
  border-color: #e87c18;
}

.btn-row {
  height: 32px;
  width: 458px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn.btn1 {
  width: 72px;
  height: 20px;
  background: #e87c18;
  border: none;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
}

.btn.btn1:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
