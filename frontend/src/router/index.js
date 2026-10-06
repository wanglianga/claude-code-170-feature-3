import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue') },
  {
    path: '/',
    component: () => import('../views/MainLayout.vue'),
    redirect: '/workbench',
    children: [
      { path: 'workbench', component: () => import('../views/WorkbenchView.vue'), meta: { title: '值班工作台' } },
      { path: 'events/:id', component: () => import('../views/EventDetailView.vue'), meta: { title: '事件处置' } },
      { path: 'elders', component: () => import('../views/ElderListView.vue'), meta: { title: '老人档案' } },
      { path: 'elders/:id', component: () => import('../views/ElderDetailView.vue'), meta: { title: '老人画像' } },
      { path: 'care-plans', component: () => import('../views/CarePlanListView.vue'), meta: { title: '长期照护跟踪' } },
      { path: 'care-plans/:id', component: () => import('../views/CarePlanDetailView.vue'), meta: { title: '长期照护方案' } },
      { path: 'stats', component: () => import('../views/StatsView.vue'), meta: { title: '照护复盘' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
