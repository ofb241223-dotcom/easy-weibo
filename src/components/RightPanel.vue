<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from 'lucide-vue-next';
import { topicService, userService } from '../api/services';
import { useAuth } from '../composables/useAuth';
import { useToast } from '../composables/useToast';
import type { Topic, User } from '../types';

const router = useRouter();
const trending = ref<Topic[]>([]);
const recommended = ref<User[]>([]);
const searchQuery = ref('');
const { user, isAuthenticated } = useAuth();
const { showToast } = useToast();
const footerLinks = ['服务条款', '隐私政策', 'Cookie 政策', '© 2026 HNUST Easy WeiBo'];

const loadRecommendedUsers = async () => {
  try {
    const users = await userService.getRecommendedUsers();
    recommended.value = users.filter((candidate) => candidate.id !== user.value?.id);
  } catch (error) {
    recommended.value = [];
    showToast(error instanceof Error ? error.message : '加载推荐用户失败', 'error');
  }
};

const loadTrendingTopics = async () => {
  try {
    trending.value = await topicService.getTrendingTopics();
  } catch (error) {
    trending.value = [];
    showToast(error instanceof Error ? error.message : '加载热门话题失败', 'error');
  }
};

onMounted(async () => {
  await loadTrendingTopics();
  await loadRecommendedUsers();
});

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchQuery.value.trim())}`);
  }
};

const handleShowMoreTopics = () => {
  router.push('/topics');
};

const openTopic = async (topicName: string) => {
  await router.push(`/topics?tag=${encodeURIComponent(topicName)}`);
  requestAnimationFrame(() => window.scrollTo({ top: 0, behavior: 'smooth' }));
};

const handleFollow = async (candidate: User) => {
  if (!isAuthenticated.value || !user.value) {
    showToast('请先登录后再关注用户', 'error');
    router.push('/login');
    return;
  }

  await userService.followUser(candidate.id);
  await loadRecommendedUsers();

  const updatedUser = recommended.value.find((item) => item.id === candidate.id);
  showToast(updatedUser?.isFollowing ? `已关注 @${candidate.username}` : `已取消关注 @${candidate.username}`, 'success');
};
</script>

<template>
  <div class="space-y-4">
    <!-- Search Bar -->
    <div class="sticky top-0 bg-bg-primary py-2 z-10">
      <div class="relative group">
        <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary group-focus-within:text-brand transition-colors" :size="18" />
        <input 
          v-model="searchQuery"
          @keydown.enter="handleSearch"
          type="text" 
          placeholder="搜索 WeiBo" 
          class="w-full bg-bg-secondary rounded-full py-3 pl-12 pr-4 outline-none border border-transparent focus:border-brand focus:bg-bg-primary transition-all"
        />
      </div>
    </div>

    <!-- Trending Topics -->
    <div class="bg-bg-secondary rounded-2xl overflow-hidden">
      <h2 class="text-xl font-bold p-4">热门话题</h2>
      <button
        v-for="topic in trending" 
        :key="topic.id"
        type="button"
        @click="openTopic(topic.name)"
        class="block w-full px-4 py-3 text-left hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
      >
        <p class="text-text-secondary text-xs">正在热议</p>
        <p class="font-bold">#{{ topic.name }}</p>
        <p class="text-text-secondary text-xs">{{ topic.postCount }} 帖子</p>
      </button>
      <button
        type="button"
        @click="handleShowMoreTopics"
        class="w-full text-left p-4 text-brand hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
      >
        显示更多
      </button>
    </div>

    <!-- Recommended Users -->
    <div class="bg-bg-secondary rounded-2xl overflow-hidden">
      <h2 class="text-xl font-bold p-4">推荐关注</h2>
      <article 
        v-for="u in recommended" 
        :key="u.id"
        class="px-4 py-3 flex items-center gap-3 hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
      >
        <router-link :to="`/profile/${u.username}`" class="flex min-w-0 flex-1 items-center gap-3 rounded-2xl">
          <img :src="u.avatar" :alt="u.nickname" class="w-10 h-10 rounded-full object-cover" />
          <div class="min-w-0">
            <p class="font-bold truncate hover:underline">{{ u.nickname }}</p>
            <p class="text-text-secondary text-sm truncate">@{{ u.username }}</p>
          </div>
        </router-link>
        <button
          type="button"
          @click.stop="handleFollow(u)"
          class="bg-text-primary text-bg-primary rounded-full px-4 py-1.5 font-bold text-sm hover:opacity-90 transition-opacity"
        >
          {{ u.isFollowing ? '已关注' : '关注' }}
        </button>
      </article>
      <button
        type="button"
        @click="router.push('/search')"
        class="w-full text-left p-4 text-brand hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
      >
        显示更多
      </button>
    </div>

    <!-- Footer Links -->
    <div class="px-4 pb-2 text-text-secondary text-[13px] leading-7">
      <div class="flex flex-wrap items-center gap-x-3">
        <template v-for="(item, index) in footerLinks" :key="item">
          <span class="hover:text-text-primary transition-colors cursor-default">{{ item }}</span>
          <span
            v-if="index < footerLinks.length - 1"
            class="text-text-secondary/60"
            aria-hidden="true"
          >
            |
          </span>
        </template>
      </div>
    </div>
  </div>
</template>
