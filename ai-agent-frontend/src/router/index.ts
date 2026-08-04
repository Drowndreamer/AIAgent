import { createRouter, createWebHistory } from 'vue-router'

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { title: 'AI Agent 工作台' },
    },
    {
      path: '/love',
      name: 'love',
      component: () => import('../views/LoveChatView.vue'),
      meta: { title: 'AI 恋爱大师' },
    },
    {
      path: '/manus',
      name: 'manus',
      component: () => import('../views/ManusChatView.vue'),
      meta: { title: 'AI 超级智能体' },
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.afterEach((to) => {
  document.title = String(to.meta.title ?? 'AI Agent 工作台')
})
