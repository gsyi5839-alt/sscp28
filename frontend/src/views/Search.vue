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

// Special code: redirect directly to "Member/Agent selection page" after input
const SPECIAL_CODE = '138888'

/**
 * Handle search submission
 */
const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()

  if (!keyword) {
    ElMessage.warning('Please enter keyword')
    return
  }

  // Hit special code: don't search, directly enter /member
  if (keyword === SPECIAL_CODE) {
    router.push('/member')
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
          {{ loading ? '...' : 'Search' }}
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
  background: #ffffff;
}

/* Search container - holds logo and search box */
.search-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
  width: 100%;
  max-width: 600px;
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
  height: 26px;
  padding: 0 10px;
  border: 1px solid #cccccc;
  background: #ffffff;
  color: #333333;
  font-size: 14px;
  text-align: center;
  outline: none;
  box-sizing: border-box;
}

.search-input:focus {
  border-color: #333333;
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
  border-radius: 3px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s ease;
  margin-left: 10px;
}

.search-btn:hover {
  background: #5a9530;
}

.search-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

/* ==================== Mobile Responsive (H5) ==================== */
@media screen and (max-width: 768px) {
  .search-page {
    padding-top: 60px;
  }
  
  .search-container {
    gap: 24px;
    padding: 0 16px;
  }
  
  .logo-img {
    width: 150px;
    height: 150px;
  }
  
  .search-input {
    width: 220px;
    height: 24px;
    font-size: 13px;
  }
  
  .search-btn {
    width: 55px;
    height: 24px;
    font-size: 13px;
  }
}

/* Extra small devices */
@media screen and (max-width: 480px) {
  .search-page {
    padding-top: 40px;
  }
  
  .search-container {
    gap: 20px;
  }
  
  .logo-img {
    width: 120px;
    height: 120px;
  }
  
  .search-input {
    width: 180px;
    height: 22px;
    font-size: 12px;
  }
  
  .search-btn {
    width: 50px;
    height: 22px;
    font-size: 12px;
  }
}

/* Ultra small devices - 320px and below */
@media screen and (max-width: 320px) {
  .search-page {
    padding-top: 30px;
  }
  
  .search-container {
    gap: 16px;
    padding: 0 10px;
  }
  
  .logo-img {
    width: 100px;
    height: 100px;
  }
  
  .search-input {
    width: 150px;
    height: 20px;
    font-size: 11px;
    padding: 0 8px;
  }
  
  .search-btn {
    width: 45px;
    height: 20px;
    font-size: 11px;
    margin-left: 6px;
  }
}
</style>

