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
    ElMessage.warning('请先登录')
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
    ElMessage.warning('请输入旧密码')
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (!passwordForm.confirmPassword) {
    ElMessage.warning('请输入确认密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }

  loading.value = true

  try {
    const response: any = await passwordApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    if (response.code === 200) {
      ElMessage.success('密码修改成功')
      // Mark local session as password-changed, so protected pages are no longer blocked.
      if (authStore.user) {
        authStore.user.needPasswordChange = false
        authStore.user.loginCountWithoutChange = 0
      }
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''

      // After password change success, redirect directly to user agreement page (without logout)
      setTimeout(() => {
        router.push('/user-agreement')
      }, 1000)
    } else {
      ElMessage.error(response.message || '密码修改失败')
    }
  } catch (error: any) {
    const message = error.response?.data?.message || '密码修改失败'
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
        <span class="title-text">修改密码</span>
      </div>

      <!-- Old Password -->
      <div class="row">
        <div class="label">
          <span class="red">*</span><span class="label-text">旧密码：</span>
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
          <span class="red">*</span><span class="label-text">新密码：</span>
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
          <span class="red">*</span><span class="label-text">确认密码：</span>
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
        <button class="btn btn1" :disabled="loading" @click="handleChangePassword">修改密码</button>
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
  white-space: nowrap;
  /* Key: use inherited background from row (avoid left-right color mismatch) */
  background: inherit !important;
}

.label-text {
  display: inline-block;
  margin-right: 5px;
  width: 70px;
  white-space: nowrap;
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
  /* Right input area should be plain white. */
  background: #ffffff !important;
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
