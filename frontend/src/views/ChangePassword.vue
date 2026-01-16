<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)

/**
 * 修改密码
 */
const handleChangePassword = async () => {
  // 验证表单
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入原始密码')
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
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }

  loading.value = true

  try {
    // TODO: 调用修改密码API
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('密码修改成功')
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
    // 可以选择跳转到登录页或其他页面
    // router.push('/login')
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    loading.value = false
  }
}

/**
 * 返回
 */
const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="change-password-page">
    <div class="password-container">
      <!-- 标题 -->
      <div class="title-bar">修改密码</div>
      
      <!-- 表单区域 -->
      <div class="form-area">
        <!-- 原始密码 -->
        <div class="form-row">
          <label class="form-label"><span class="required">*</span>原始密码:</label>
          <input
            v-model="passwordForm.oldPassword"
            type="password"
            class="form-input"
            placeholder=""
          />
        </div>

        <!-- 新密码 -->
        <div class="form-row">
          <label class="form-label"><span class="required">*</span>新密码:</label>
          <input
            v-model="passwordForm.newPassword"
            type="password"
            class="form-input"
            placeholder=""
          />
        </div>

        <!-- 确认密码 -->
        <div class="form-row">
          <label class="form-label"><span class="required">*</span>确认密码:</label>
          <input
            v-model="passwordForm.confirmPassword"
            type="password"
            class="form-input"
            placeholder=""
          />
        </div>

        <!-- 提交按钮 -->
        <div class="form-row button-row">
          <button
            class="submit-btn"
            :disabled="loading"
            @click="handleChangePassword"
          >
            {{ loading ? '...' : '修改密码' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 页面容器 */
.change-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  padding: 20px;
}

/* 密码修改容器 - 460x150 */
.password-container {
  width: 460px;
  background: #ffffff;
  border: 1px solid #dddddd;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 标题栏 - 橙色背景 */
.title-bar {
  background: linear-gradient(to bottom, #ffd89a 0%, #ffb84d 100%);
  color: #333333;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 15px;
  border-bottom: 1px solid #dddddd;
  text-align: center;
}

/* 表单区域 */
.form-area {
  padding: 15px 20px;
}

/* 表单行 */
.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

/* 标签 */
.form-label {
  width: 80px;
  text-align: right;
  font-size: 13px;
  color: #333333;
  flex-shrink: 0;
  padding-right: 10px;
}

/* 必填星号 */
.required {
  color: #ff0000;
  margin-right: 2px;
}

/* 输入框 */
.form-input {
  flex: 1;
  height: 26px;
  padding: 0 8px;
  border: 1px solid #cccccc;
  font-size: 13px;
  color: #333333;
  outline: none;
  transition: border-color 0.3s;
}

.form-input:focus {
  border-color: #ff9900;
}

.form-input::placeholder {
  color: #999999;
}

/* 按钮行 */
.button-row {
  justify-content: center;
  margin-top: 15px;
  margin-bottom: 5px;
}

/* 提交按钮 */
.submit-btn {
  width: 100px;
  height: 28px;
  background: linear-gradient(to bottom, #ff9900 0%, #ff7700 100%);
  color: #ffffff;
  border: 1px solid #dd7700;
  border-radius: 3px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.submit-btn:hover {
  opacity: 0.9;
}

.submit-btn:active {
  opacity: 0.8;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式 */
@media screen and (max-width: 500px) {
  .password-container {
    width: 100%;
    max-width: 460px;
  }
}
</style>
