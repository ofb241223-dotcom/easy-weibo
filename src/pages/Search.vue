<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { postService, userService } from '../api/services';
import { Post, User } from '../types';
import PostCard from '../components/PostCard.vue';
import { Search as SearchIcon, X } from 'lucide-vue-next';
import { useToast } from '../composables/useToast';

const route = useRoute();
const router = useRouter();
const posts = ref<Post[]>([]);
const users = ref<User[]>([]);
const loading = ref(false);
const inputValue = ref((route.query.q as string) || '');
const { showToast } = useToast();

const handleSearch = async (query: string) => {
  if (!query) {
    posts.value = [];
    users.value = [];
    return;
  }
  loading.value = true;
  try {
    const normalizedQuery = query.trim();
    if (normalizedQuery.startsWith('#')) {
      users.value = [];
      posts.value = await postService.searchPosts(query);
    } else {
      const [matchedPosts, matchedUsers] = await Promise.all([
        postService.searchPosts(query),
        userService.searchUsers(query),
      ]);
      posts.value = matchedPosts;
      users.value = matchedUsers;
    }
  } catch (error) {
    posts.value = [];
    users.value = [];
    showToast(error instanceof Error ? error.message : '搜索失败，请稍后重试', 'error');
  } finally {
    loading.value = false;
  }
};

watch(() => route.query.q, (newQuery) => {
  inputValue.value = (newQuery as string) || '';
  handleSearch(inputValue.value);
}, { immediate: true });

const onSearchSubmit = () => {
  const query = inputValue.value.trim();
  if (!query) {
    router.replace({ path: '/search' });
    return;
  }
  router.push(`/search?q=${encodeURIComponent(query)}`);
};

const clearSearch = () => {
  inputValue.value = '';
  router.replace({ path: '/search' });
};

const refreshSearchResults = () => {
  handleSearch((route.query.q as string) || '');
};
</script>

<template>
  <div class="min-h-screen">
    <div class="sticky top-0 bg-bg-primary/80 backdrop-blur-md z-10 p-4 border-b border-border">
      <form @submit.prevent="onSearchSubmit" class="relative group">
        <SearchIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary group-focus-within:text-brand transition-colors" :size="18" />
        <input 
          v-model="inputValue"
          type="text" 
          placeholder="搜索帖子或用户" 
          class="w-full bg-bg-secondary rounded-full py-2 pl-12 pr-10 outline-none border border-transparent focus:border-brand focus:bg-bg-primary transition-all"
        />
        <button 
          v-if="inputValue"
          type="button"
          @click="clearSearch"
          class="absolute right-4 top-1/2 -translate-y-1/2 text-text-secondary hover:text-text-primary"
        >
          <X :size="18" />
        </button>
      </form>
    </div>

    <div class="divide-y divide-border">
      <div v-if="!route.query.q" class="p-10 text-center text-text-secondary">
        <p class="text-xl font-bold text-text-primary mb-2">发现新内容</p>
        <p>输入关键词或话题标签开始搜索</p>
      </div>
      <div v-else-if="loading" class="flex justify-center p-10">
        <div class="w-8 h-8 border-4 border-brand border-t-transparent rounded-full animate-spin" />
      </div>
      <template v-else-if="users.length > 0 || posts.length > 0">
        <div v-if="users.length" class="border-b border-border">
          <div class="px-4 py-3 text-sm font-bold text-text-secondary">用户</div>
          <button
            v-for="user in users"
            :key="user.id"
            type="button"
            class="w-full px-4 py-3 flex items-center gap-3 hover:bg-bg-secondary transition-colors text-left"
            @click="router.push(`/profile/${user.username}`)"
          >
            <img :src="user.avatar" :alt="user.nickname" class="w-12 h-12 rounded-full object-cover" />
            <div class="min-w-0">
              <p class="font-bold truncate">{{ user.nickname }}</p>
              <p class="text-sm text-text-secondary truncate">@{{ user.username }}</p>
              <p v-if="user.bio" class="text-sm text-text-secondary truncate mt-0.5">{{ user.bio }}</p>
            </div>
          </button>
        </div>

        <div v-if="posts.length">
          <div class="px-4 py-3 text-sm font-bold text-text-secondary">帖子</div>
          <PostCard v-for="post in posts" :key="post.id" :post="post" @update="refreshSearchResults" @deleted="refreshSearchResults" />
        </div>
      </template>
      <div v-else class="p-10 text-center text-text-secondary">
        <p class="text-xl font-bold text-text-primary mb-2">未找到相关结果</p>
        <p>尝试搜索其他关键词或话题</p>
      </div>
    </div>
  </div>
</template>
