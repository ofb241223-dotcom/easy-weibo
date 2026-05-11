<script setup lang="ts">
import { computed, onMounted, ref, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, Flame, Hash } from 'lucide-vue-next';
import { postService, topicService } from '../api/services';
import { useToast } from '../composables/useToast';
import PostCard from '../components/PostCard.vue';
import type { Post, Topic } from '../types';

const route = useRoute();
const router = useRouter();
const { showToast } = useToast();

const topics = ref<Topic[]>([]);
const posts = ref<Post[]>([]);
const loadingTopics = ref(false);
const loadingPosts = ref(false);

const activeTag = computed(() => {
  const value = route.query.tag;
  return typeof value === 'string' && value.trim() ? value.trim() : '';
});

const loadTopics = async () => {
  loadingTopics.value = true;
  try {
    topics.value = await topicService.getTrendingTopics();
  } catch (error) {
    topics.value = [];
    showToast(error instanceof Error ? error.message : '加载热门话题失败，请稍后重试', 'error');
  } finally {
    loadingTopics.value = false;
  }
};

const loadPosts = async () => {
  if (!activeTag.value) {
    posts.value = [];
    return;
  }
  loadingPosts.value = true;
  try {
    posts.value = await postService.getPostsByTag(activeTag.value);
  } catch (error) {
    posts.value = [];
    showToast(error instanceof Error ? error.message : '加载话题帖子失败，请稍后重试', 'error');
  } finally {
    loadingPosts.value = false;
  }
};

const openTopic = async (topicName: string) => {
  await router.push(`/topics?tag=${encodeURIComponent(topicName)}`);
  requestAnimationFrame(() => window.scrollTo({ top: 0, behavior: 'smooth' }));
};

const refreshTopicPosts = async () => {
  await loadPosts();
};

onMounted(async () => {
  await loadTopics();
  await loadPosts();
});

watch(
  () => route.query.tag,
  async () => {
    await loadPosts();
    await nextTick();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  },
);
</script>

<template>
  <div class="min-h-screen">
    <div class="sticky top-0 z-10 border-b border-border bg-bg-primary/90 px-4 py-3 backdrop-blur-md">
      <div class="flex items-center gap-4">
        <button type="button" class="rounded-full p-2 hover:bg-bg-secondary" @click="router.back()">
          <ArrowLeft :size="20" />
        </button>
        <div>
          <h1 class="text-xl font-bold">话题</h1>
        </div>
      </div>
    </div>

    <section class="border-b border-border px-4 py-5">
      <div class="flex items-center gap-2 text-lg font-bold">
        <Flame :size="18" />
        热门话题
      </div>

      <div v-if="loadingTopics" class="flex justify-center py-10">
        <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
      </div>

      <div v-else class="mt-4 grid gap-3 md:grid-cols-2">
        <button
          v-for="topic in topics"
          :key="topic.id"
          type="button"
          class="rounded-[24px] border border-border bg-bg-primary p-4 text-left transition hover:-translate-y-0.5 hover:shadow-sm"
          :class="activeTag === topic.name ? 'border-text-primary shadow-sm' : ''"
          @click="openTopic(topic.name)"
        >
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="flex items-center gap-2 text-lg font-bold">
                <Hash :size="16" />
                {{ topic.name }}
              </p>
              <p class="mt-2 text-sm text-text-secondary">{{ topic.postCount }} 条帖子正在讨论这个话题</p>
            </div>
            <span
              v-if="activeTag === topic.name"
              class="rounded-full bg-text-primary px-3 py-1 text-xs font-bold text-bg-primary"
            >
              当前查看
            </span>
          </div>
        </button>
      </div>
    </section>

    <section v-if="activeTag" class="divide-y divide-border">
      <div class="px-4 py-4">
        <h2 class="text-xl font-bold">#{{ activeTag }}</h2>
        <p class="mt-1 text-sm text-text-secondary">查看与该话题相关的最新内容。</p>
      </div>

      <div v-if="loadingPosts" class="flex justify-center p-10">
        <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
      </div>

      <template v-else-if="posts.length > 0">
        <PostCard
          v-for="post in posts"
          :key="post.id"
          :post="post"
          @update="refreshTopicPosts"
          @deleted="refreshTopicPosts"
        />
      </template>

      <div v-else class="p-10 text-center text-text-secondary">
        <p class="mb-2 text-xl font-bold text-text-primary">这个话题还没有内容</p>
        <p>你可以先去发布第一条相关帖子。</p>
      </div>
    </section>
  </div>
</template>
