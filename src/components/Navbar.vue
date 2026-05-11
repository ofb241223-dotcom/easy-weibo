<script setup lang="ts">
import { useRoute } from 'vue-router';
import { Search, Bell, User, PlusSquare } from 'lucide-vue-next';
import { useAuth } from '../composables/useAuth';
import { useNotifications } from '../composables/useNotifications';
import HomeFilledIcon from './icons/HomeFilledIcon.vue';

const route = useRoute();
const { user } = useAuth();
const { unreadCount } = useNotifications();
</script>

<template>
  <!-- Mobile Top Bar -->
  <div class="md:hidden sticky top-0 bg-bg-primary/80 backdrop-blur-md border-b border-border z-20 px-4 py-3 flex items-center justify-between">
    <img
      src="/favicon.svg"
      alt="HNUST logo"
      class="w-8 h-8 object-contain"
    />
    <h1 class="font-bold text-lg">HNUST Easy WeiBo</h1>
    <div class="w-8 h-8 rounded-full overflow-hidden">
      <img v-if="user" :src="user.avatar" :alt="user.nickname" class="w-full h-full object-cover" />
      <div v-else class="w-full h-full bg-bg-secondary" />
    </div>
  </div>

  <!-- Mobile Bottom Navigation -->
  <nav class="md:hidden fixed bottom-0 left-0 right-0 bg-bg-primary border-t border-border z-20 flex justify-around items-center py-2 px-4">
    <router-link to="/" class="p-2" :class="route.path === '/' ? 'text-brand' : 'text-text-secondary'">
      <HomeFilledIcon :size="24" />
    </router-link>
    <router-link to="/search" class="p-2" :class="route.path === '/search' ? 'text-brand' : 'text-text-secondary'">
      <Search :size="24" />
    </router-link>
    <router-link to="/compose" class="bg-brand text-bg-primary p-2 rounded-full shadow-lg -mt-8 border-4 border-bg-primary">
      <PlusSquare :size="24" />
    </router-link>
    <router-link to="/notifications" class="p-2" :class="route.path === '/notifications' ? 'text-brand' : 'text-text-secondary'">
      <span class="relative inline-flex">
        <Bell :size="24" />
        <span
          v-if="unreadCount > 0"
          class="absolute -right-2 -top-1 inline-flex min-w-[16px] items-center justify-center rounded-full bg-text-primary px-1 py-0.5 text-[10px] font-bold leading-none text-bg-primary"
        >
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </span>
    </router-link>
    <router-link :to="user ? `/profile/${user.username}` : '/login'" class="p-2" :class="route.path.startsWith('/profile') ? 'text-brand' : 'text-text-secondary'">
      <User :size="24" />
    </router-link>
  </nav>
</template>
