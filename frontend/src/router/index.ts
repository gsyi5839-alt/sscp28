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
      path: '/line.html',
      name: 'member',
      component: () => import('../views/MemberPanel.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/member',
      redirect: '/line.html'
    },
    {
      path: '/member/login',
      name: 'memberLogin',
      component: () => import('../views/MemberLogin.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/admin/login',
      name: 'agentLogin',
      component: () => import('../views/AgentLogin.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/agent/login',
      redirect: '/admin/login'
    },
    {
      path: '/change-password',
      name: 'changePassword',
      component: () => import('../views/ChangePassword.vue'),
      meta: { requiresAuth: true }
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
      meta: { requiresAuth: true }
    },
    {
      path: '/game',
      name: 'gameHome',
      component: () => import('../views/GameHome.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/bet-status',
      name: 'betStatus',
      component: () => import('../views/BetStatus.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/account-history',
      name: 'accountHistory',
      component: () => import('../views/AccountHistory.vue'),
      meta: { requiresAuth: true }
    },

  ]
})

const protectedRouteNames = new Set(['dashboard', 'changePassword', 'userAgreement', 'gameHome', 'betStatus', 'accountHistory'])

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  const routeName = String(to.name ?? '')
  const requiresAuth = Boolean(to.meta.requiresAuth) || protectedRouteNames.has(routeName)

  if (!requiresAuth) {
    next()
    return
  }

  if (!authStore.isAuthenticated) {
    // Redirect to member login page when not logged in
    next({ name: 'memberLogin' })
    return
  }

  // After page refresh, token may exist but user profile is not in memory yet.
  if (!authStore.user) {
    await authStore.fetchUser()
  }

  if (!authStore.user) {
    next({ name: 'memberLogin' })
    return
  }

  // Any protected page must complete password change first.
  if (authStore.user.needPasswordChange && routeName !== 'changePassword') {
    next({ name: 'changePassword' })
  } else {
    next()
  }
})

export default router

