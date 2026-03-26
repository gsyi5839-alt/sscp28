<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import logoImage from '@/assets/SHOUYE/image.png'

const router = useRouter()

// Clear favicon on search page
onMounted(() => {
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) {
    favicon.href = 'data:,'  // Empty favicon
  }
})
const searchKeyword = ref('')
const loading = ref(false)

// Special code: redirect directly to "Member/Agent speed test selection page" after input
const SPECIAL_CODE = '138888'

/**
 * Handle search submission
 */
const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()

  if (!keyword) {
    ElMessage.warning('请输入关键词')
    return
  }

  // Hit special code: don't search, directly enter /line.html
  if (keyword === SPECIAL_CODE) {
    router.push('/line.html')
    return
  }

  // Non-special code: redirect to Baidu search
  const baiduSearchUrl = `https://www.baidu.com/s?wd=${encodeURIComponent(keyword)}`
  window.location.href = baiduSearchUrl
}

// Note: Use @keydown.enter.prevent in template to completely prevent default submit behavior (avoid external redirect when form injected by browser/plugin)
</script>

<template>
  <div class="search-page">
    <div class="search-container">
      <!-- Logo Image Section - 200x200 -->
      <div class="logo-section">
        <img :src="logoImage" alt="BW Search" class="logo-img" />
      </div>
      
      <!-- Search Input Section -->
      <div class="search-box">
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder=""
          @keydown.enter.prevent="handleSearch"
        />
        <button
          class="search-btn"
          type="button"
          :disabled="loading"
          @click="handleSearch"
        >
          {{ loading ? '...' : '搜索' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container - position content in upper area like original design */
.search-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 100px;
  background: #efefef;
}

/* Search container - holds logo and search box */
.search-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
  width: 100%;
}

/* Logo section styling */
.logo-section {
  display: flex;
  justify-content: center;
}

/* Logo image - 200x200 */
.logo-img {
  width: 200px;
  height: 200px;
  object-fit: contain;
}

/* Search box container */
.search-box {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Search input field - white background with gray border */
.search-input {
  width: 300px;
  height: 22px;
  margin: 0;
  padding: 0;
  border: 1px solid #8a8a8a;
  border-radius: 4px;
  background: #ffffff;
  color: #333333;
  font-size: 14px;
  text-align: center;
  outline: none;
  box-sizing: content-box;
}

.search-input:focus {
  border: 2px solid rgb(39, 94, 197);
  border-radius: 4px;
  box-shadow: none;
}

.search-input::placeholder {
  color: #999999;
}

/* Search button - 60x27, background: #68a937, color: #fff */
.search-btn {
  width: 60px;
  height: 27px;
  background: #68a937;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  margin-left: 10px;
  position: relative;
  top: 1px;
}

.search-btn:hover {
  background: #5a9530;
}

.search-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}
</style>
