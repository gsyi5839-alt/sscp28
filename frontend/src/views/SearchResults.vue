<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchApi } from '../api'

const route = useRoute()
const router = useRouter()
const searchKeyword = ref('')
const results = ref<any[]>([])
const loading = ref(false)

/**
 * Fetch search results from API
 * @param keyword - Search keyword
 */
const fetchResults = async (keyword: string) => {
  if (!keyword) return
  
  loading.value = true
  results.value = []
  
  try {
    const response: any = await searchApi.search(keyword)
    if (response.code === 200) {
      results.value = response.data || []
    }
  } catch (error) {
    console.error('Search error:', error)
  } finally {
    loading.value = false
  }
}

/**
 * Handle new search submission
 */
const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push({
      path: '/search/results',
      query: { q: keyword }
    })
  }
}

// Note: Use @keydown.enter.prevent in template to completely prevent default submit behavior (avoid external redirect when form injected by browser/plugin)

/**
 * Navigate back to search home
 */
const goBack = () => {
  router.push('/')
}

// Watch for query parameter changes
watch(
  () => route.query.q,
  (newKeyword) => {
    if (newKeyword) {
      searchKeyword.value = newKeyword as string
      fetchResults(newKeyword as string)
    }
  },
  { immediate: true }
)

onMounted(() => {
  const keyword = route.query.q as string
  if (keyword) {
    searchKeyword.value = keyword
    fetchResults(keyword)
  }
})
</script>

<template>
  <div class="results-page">
    <!-- Header with logo and search -->
    <header class="results-header">
      <div class="header-content">
        <!-- Logo - clickable to go back -->
        <div class="logo" @click="goBack">
          <div class="logo-icons">
            <span class="icon-box orange"></span>
            <span class="icon-box blue at">@</span>
          </div>
          <div class="logo-text">
            <span class="bw">BW</span>
            <span class="search-text">Search</span>
          </div>
        </div>
        
        <!-- Search box in header -->
        <div class="header-search">
          <input
            v-model="searchKeyword"
            type="text"
            class="search-input"
            @keydown.enter.prevent="handleSearch"
          />
          <button class="search-btn" type="button" @click="handleSearch">Search</button>
        </div>
      </div>
    </header>
    
    <!-- Results section -->
    <main class="results-main">
      <div class="results-container">
        <!-- Loading state -->
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>Searching...</span>
        </div>
        
        <!-- Results list -->
        <div v-else-if="results.length > 0" class="results-list">
          <div v-for="(item, index) in results" :key="index" class="result-item">
            <h3 class="result-title">{{ item.title }}</h3>
            <p class="result-desc">{{ item.description }}</p>
            <a class="result-url" :href="item.url">{{ item.url }}</a>
          </div>
        </div>
        
        <!-- Empty state -->
        <div v-else class="empty-state">
          <el-icon :size="64" color="#cccccc"><Search /></el-icon>
          <p>No results found for "{{ searchKeyword }}"</p>
          <p class="hint">Try different keywords</p>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* Results page container */
.results-page {
  min-height: 100vh;
  background: #ffffff;
}

/* Header styling */
.results-header {
  background: #ffffff;
  border-bottom: 1px solid #eeeeee;
  padding: 16px 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Header logo - smaller version */
.logo {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icons {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.icon-box {
  width: 20px;
  height: 20px;
  border-radius: 3px;
  border: 2px solid;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 10px;
}

.icon-box.orange {
  border-color: #f5a623;
  background: transparent;
}

.icon-box.blue {
  border-color: #4a90d9;
  background: #4a90d9;
  color: #ffffff;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1;
}

.logo-text .bw,
.logo-text .search-text {
  font-size: 16px;
  font-weight: bold;
  color: #f5a623;
}

/* Header search box */
.header-search {
  display: flex;
  flex: 1;
  max-width: 500px;
}

.search-input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: none;
  background: #333333;
  color: #ffffff;
  font-size: 14px;
  outline: none;
}

.search-btn {
  width: 60px;
  height: 36px;
  background: #5cb85c;
  color: #ffffff;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.search-btn:hover {
  background: #4cae4c;
}

/* Main content area */
.results-main {
  padding: 24px;
}

.results-container {
  max-width: 800px;
  margin: 0 auto;
}

/* Loading state */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 0;
  color: #666666;
  font-size: 16px;
}

/* Results list */
.results-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.result-item {
  padding-bottom: 24px;
  border-bottom: 1px solid #eeeeee;
}

.result-title {
  font-size: 18px;
  color: #1a0dab;
  margin: 0 0 8px;
  cursor: pointer;
}

.result-title:hover {
  text-decoration: underline;
}

.result-desc {
  font-size: 14px;
  color: #545454;
  margin: 0 0 8px;
  line-height: 1.6;
}

.result-url {
  font-size: 13px;
  color: #006621;
  text-decoration: none;
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #666666;
}

.empty-state p {
  margin: 16px 0 0;
  font-size: 16px;
}

.empty-state .hint {
  font-size: 14px;
  color: #999999;
}

/* ==================== Mobile Responsive (H5) ==================== */
@media screen and (max-width: 768px) {
  .results-header {
    padding: 12px 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-search {
    width: 100%;
    max-width: none;
  }
  
  .icon-box {
    width: 16px;
    height: 16px;
    font-size: 8px;
  }
  
  .logo-text .bw,
  .logo-text .search-text {
    font-size: 14px;
  }
  
  .search-input {
    height: 32px;
    font-size: 13px;
  }
  
  .search-btn {
    width: 50px;
    height: 32px;
    font-size: 13px;
  }
  
  .results-main {
    padding: 16px;
  }
  
  .result-title {
    font-size: 16px;
  }
  
  .result-desc {
    font-size: 13px;
  }
}
</style>

