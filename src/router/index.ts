import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuth } from '../composables/useAuth';

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('../pages/Login.vue'), meta: { title: '登录' } },
  {
    path: '/',
    component: () => import('../components/Layout.vue'),
    meta: { title: '首页' },
    children: [
      { path: '', component: () => import('../pages/Home.vue'), meta: { title: '首页' } },
      { path: 'search', component: () => import('../pages/Search.vue'), meta: { title: '发现' } },
      { path: 'topics', component: () => import('../pages/Topics.vue'), meta: { title: '话题' } },
      { path: 'post/:id', component: () => import('../pages/PostDetail.vue'), meta: { title: '帖子' } },
      { path: 'profile/:username', component: () => import('../pages/Profile.vue'), meta: { title: '个人主页' } },
      { path: 'connections', component: () => import('../pages/Connections.vue'), meta: { requiresAuth: true, title: '关注列表' } },
      { path: 'chat', component: () => import('../pages/Chat.vue'), meta: { requiresAuth: true, title: '聊天' } },
      { path: 'chat/:id', component: () => import('../pages/ChatConversation.vue'), meta: { requiresAuth: true, title: '聊天' } },
      { path: 'settings', component: () => import('../pages/Settings.vue'), meta: { title: '设置' } },
      { path: 'settings/security', component: () => import('../pages/SecuritySettings.vue'), meta: { requiresAuth: true, title: '安全设置' } },
      { path: 'settings/help', component: () => import('../pages/HelpCenter.vue'), meta: { title: '帮助中心' } },
      { path: 'admin', component: () => import('../pages/AdminConsole.vue'), meta: { requiresAuth: true, requiresAdmin: true, title: '控制台' } },
      { 
        path: 'notifications', 
        component: () => import('../pages/Notifications.vue'),
        meta: { requiresAuth: true, title: '通知' }
      }
    ]
  },
  { 
    path: '/compose', 
    component: () => import('../pages/Compose.vue'),
    meta: { requiresAuth: true, title: '发布' }
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  },
});

router.beforeEach((to) => {
  const { isAuthenticated, user } = useAuth();
  if (to.meta.requiresAuth && !isAuthenticated.value) {
    return '/login';
  }
  if (to.meta.requiresAdmin && user.value?.role !== 'ADMIN') {
    return '/';
  }
  return true;
});

router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? to.meta.title : 'Easy WeiBo';
  document.title = title.includes('Easy WeiBo') ? title : `${title} / Easy WeiBo`;
});

export default router;
