<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Search, Bell, User, Settings, LogOut, PlusSquare, Moon, Sun, Hash, LayoutDashboard, UserRoundPlus, MessageCircle } from 'lucide-vue-next';
import { useAuth } from '../composables/useAuth';
import { useNotifications } from '../composables/useNotifications';
import { useTheme } from '../composables/useTheme';
import HomeFilledIcon from './icons/HomeFilledIcon.vue';

const router = useRouter();
const route = useRoute();
const { user, logout, isAuthenticated } = useAuth();
const { unreadCount } = useNotifications();
const { theme, toggleTheme } = useTheme();

const navItems = computed(() => [
  { icon: HomeFilledIcon, label: '首页', path: '/', matchPrefix: '/' },
  { icon: Search, label: '发现', path: '/search', matchPrefix: '/search' },
  { icon: Hash, label: '话题', path: '/topics', matchPrefix: '/topics' },
  { icon: Bell, label: '通知', path: '/notifications', matchPrefix: '/notifications' },
  { icon: User, label: '个人主页', path: user.value ? `/profile/${user.value.username}` : '/login', matchPrefix: '/profile' },
  { icon: UserRoundPlus, label: '关注列表', path: '/connections', matchPrefix: '/connections' },
  { icon: MessageCircle, label: '聊天', path: '/chat', matchPrefix: '/chat' },
  ...(user.value?.role === 'ADMIN' ? [{ icon: LayoutDashboard, label: '控制台', path: '/admin', matchPrefix: '/admin' }] : []),
  { icon: Settings, label: '设置', path: '/settings', matchPrefix: '/settings' },
]);

const isItemActive = (path: string, matchPrefix: string) => {
  if (matchPrefix === '/') {
    return route.path === '/';
  }
  return route.path === matchPrefix || route.path.startsWith(`${matchPrefix}/`);
};

const handleLogout = () => {
  logout();
  router.push('/login');
};
</script>

<template>
  <div class="flex flex-col h-full py-4 px-2 lg:px-4">
    <div class="mb-8 px-1 lg:px-2">
      <div class="flex items-center gap-3">
        <img
          src="/favicon.svg"
          alt="HNUST logo"
          class="w-10 h-10 object-contain shrink-0"
        />
        <span class="hidden lg:block text-2xl font-bold tracking-tight text-text-primary">
          Easy WeiBo
        </span>
      </div>
    </div>

    <nav class="space-y-2">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="flex items-center gap-4 border-b-2 border-transparent p-3 transition-colors hover:bg-bg-secondary"
        :class="{ 'border-text-primary bg-bg-secondary font-bold': isItemActive(item.path, item.matchPrefix), 'font-normal': !isItemActive(item.path, item.matchPrefix) }"
      >
        <span class="relative inline-flex">
          <component :is="item.icon" :size="26" />
          <span
            v-if="item.path === '/notifications' && unreadCount > 0"
            class="absolute -right-2 -top-1 inline-flex min-w-[18px] items-center justify-center rounded-full bg-text-primary px-1.5 py-0.5 text-[11px] font-bold leading-none text-bg-primary"
          >
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
        </span>
        <span class="hidden lg:block text-xl">{{ item.label }}</span>
      </router-link>

      <button
        @click="toggleTheme"
        class="w-full flex items-center gap-4 p-3 rounded-full transition-colors hover:bg-bg-secondary text-left"
      >
        <Moon v-if="theme === 'light'" :size="26" />
        <Sun v-else :size="26" />
        <span class="hidden lg:block text-xl">{{ theme === 'light' ? '深色模式' : '浅色模式' }}</span>
      </button>
    </nav>

    <div v-if="isAuthenticated" class="mt-6">
      <button 
        @click="router.push('/compose')"
        class="w-full bg-brand hover:bg-brand-hover text-bg-primary rounded-full p-3 lg:py-3 lg:px-6 flex items-center justify-center gap-2 transition-transform active:scale-95 shadow-lg"
      >
        <PlusSquare :size="24" />
        <span class="hidden lg:block font-bold text-lg">发布</span>
      </button>
    </div>

    <div class="flex-1" />

    <div v-if="isAuthenticated" class="space-y-4">

      <div
        class="flex items-center gap-3 rounded-full p-3 hover:bg-bg-secondary cursor-pointer group relative"
        @click="router.push(user ? `/profile/${user.username}` : '/login')"
      >
        <img :src="user?.avatar" :alt="user?.nickname" class="w-10 h-10 rounded-full object-cover" />
        <div class="hidden lg:block flex-1 min-w-0">
          <p class="font-bold truncate">{{ user?.nickname }}</p>
          <p class="text-text-secondary text-sm truncate">@{{ user?.username }}</p>
        </div>
        <button 
          @click.stop="handleLogout"
          class="hidden lg:block p-2 hover:bg-bg-secondary hover:text-text-primary rounded-full transition-colors"
          title="退出登录"
        >
          <LogOut :size="20" />
        </button>
      </div>
    </div>
    
    <div v-else class="mt-auto">
      <button 
        @click="router.push('/login')"
        class="w-full bg-brand hover:bg-brand-hover text-bg-primary rounded-full py-3 font-bold text-lg"
      >
        登录
      </button>
    </div>
  </div>
</template>
