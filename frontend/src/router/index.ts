import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Search.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/search/results',
      name: 'searchResults',
      component: () => import('../views/SearchResults.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/platform',
      name: 'platform',
      component: () => import('../views/Home.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/member',
      name: 'member',
      component: () => import('../views/MemberPanel.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/member/login',
      name: 'memberLogin',
      component: () => import('../views/MemberLogin.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/agent/login',
      name: 'agentLogin',
      component: () => import('../views/AgentLogin.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/change-password',
      name: 'changePassword',
      component: () => import('../views/ChangePassword.vue'),
      meta: { requiresAuth: false }  // 登录后立即跳转，组件内自行验证
    },
    {
      path: '/force-change-password',
      name: 'forceChangePassword',
      component: () => import('../views/ForceChangePassword.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/user-agreement',
      name: 'userAgreement',
      component: () => import('../views/UserAgreement.vue'),
      meta: { requiresAuth: false }  // 登录后显示，组件内自行验证
    },
    {
      path: '/game',
      name: 'gameHome',
      component: () => import('../views/GameHome.vue'),
      meta: { requiresAuth: false }
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // 未登录时跳转到会员登录页面
    next({ name: 'memberLogin' })
  } else {
    next()
  }
})

export default router

