import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true, title: '登录' } },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'articles', name: 'articles', component: () => import('../views/Articles.vue'), meta: { title: '文章管理' } },
      { path: 'categories', name: 'categories', component: () => import('../views/Categories.vue'), meta: { title: '分类管理' } },
      { path: 'tags', name: 'tags', component: () => import('../views/Tags.vue'), meta: { title: '标签管理' } },
      { path: 'comments', name: 'comments', component: () => import('../views/Comments.vue'), meta: { title: '评论管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(to => {
  const userStore = useUserStore()
  if (to.meta.public || userStore.token) {
    return true
  }
  return '/login'
})

router.afterEach(to => {
  document.title = to.meta.title ? `${to.meta.title} - 博客管理后台` : '博客管理后台'
})

export default router
