import { createApp } from 'vue'
import { createPinia } from 'pinia'
import zhCn from 'element-plus/es/locale/lang/zh-cn.mjs'
// Element Plus components are auto-imported by unplugin-vue-components
// Element Plus styles are auto-imported as well
// Only import used icons explicitly to reduce bundle size
import {
  ArrowDown,
  ArrowUp,
  Bell,
  CaretBottom,
  CaretTop,
  Check,
  CircleCheck,
  Clock,
  Close,
  Coin,
  Document,
  HomeFilled,
  InfoFilled,
  Lock,
  Menu,
  Money,
  Plus,
  Refresh,
  RefreshRight,
  Search,
  Setting,
  SwitchButton,
  Trophy,
  User,
  UserFilled,
  Warning,
  ZoomIn
} from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import './style.css'

const app = createApp(App)

// Register only used Element Plus Icons (not all 200+ icons)
const icons = {
  ArrowDown,
  ArrowUp,
  Bell,
  CaretBottom,
  CaretTop,
  Check,
  CircleCheck,
  Clock,
  Close,
  Coin,
  Document,
  HomeFilled,
  InfoFilled,
  Lock,
  Menu,
  Money,
  Plus,
  Refresh,
  RefreshRight,
  Search,
  Setting,
  SwitchButton,
  Trophy,
  User,
  UserFilled,
  Warning,
  ZoomIn
}

for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)

// Configure Element Plus locale globally (components auto-imported by unplugin)
// Use a global property for locale configuration
app.config.globalProperties.$ELEMENT = { locale: zhCn }

app.mount('#app')
