import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
        },
        {
          path: 'regions',
          name: 'regions',
          component: () => import('@/views/region/RegionManageView.vue'),
        },
        {
          path: 'audit',
          name: 'audit',
          component: () => import('@/views/audit/AuditQueueView.vue'),
        },
        {
          path: 'posts',
          name: 'posts',
          component: () => import('@/views/post/PostManageView.vue'),
        },
        {
          path: 'reports',
          name: 'reports',
          component: () => import('@/views/governance/ReportManageView.vue'),
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/governance/UserManageView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  if (to.meta.public) {
    return true
  }
  const token = localStorage.getItem('admin_token')
  if (!token && to.name !== 'login') {
    return { name: 'login' }
  }
  return true
})

export default router
