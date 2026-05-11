<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Image as ImageIcon, Maximize2 } from 'lucide-vue-next';
import { postService } from '../api/services';
import EmojiPicker from '../components/EmojiPicker.vue';
import PostCard from '../components/PostCard.vue';
import { useAuth } from '../composables/useAuth';
import { useToast } from '../composables/useToast';
import type { Post } from '../types';

const COMPOSE_DRAFT_KEY = 'easyweibo_compose_draft';
const router = useRouter();
const posts = ref<Post[]>([]);
const loading = ref(true);
const publishing = ref(false);
const newPostContent = ref('');
const images = ref<Array<{ file: File; previewUrl: string }>>([]);

const { user, isAuthenticated } = useAuth();
const { showToast } = useToast();

const loadPosts = async () => {
  loading.value = true;
  try {
    posts.value = await postService.getPosts();
  } catch (error) {
    showToast(error instanceof Error ? error.message : '加载帖子失败，请稍后重试', 'error');
    posts.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadPosts();
});

const handleCreatePost = async () => {
  if (!newPostContent.value.trim() || !user.value) return;
  
  try {
    publishing.value = true;
    await postService.createPost(newPostContent.value, images.value.map((item) => item.file));
    window.sessionStorage.removeItem(COMPOSE_DRAFT_KEY);
    newPostContent.value = '';
    images.value = [];
    showToast('发布成功！', 'success');
    await loadPosts();
  } catch (e) {
    showToast(e instanceof Error ? e.message : '发布失败，请重试', 'error');
  } finally {
    publishing.value = false;
  }
};

const handleImageUpload = (e: Event) => {
  const target = e.target as HTMLInputElement;
  Array.from(target.files || []).forEach((file) => {
    const reader = new FileReader();
    reader.onloadend = () => {
      images.value.push({
        file,
        previewUrl: reader.result as string,
      });
    };
    reader.readAsDataURL(file);
  });
  target.value = '';
};

const handleInsertEmoji = (emoji: string) => {
  newPostContent.value = `${newPostContent.value}${newPostContent.value ? ' ' : ''}${emoji}`;
};

const openFullscreenEditor = () => {
  window.sessionStorage.setItem(COMPOSE_DRAFT_KEY, newPostContent.value);
  router.push('/compose?fullscreen=1');
};

</script>

<template>
  <div class="divide-y divide-border">
    <!-- Header -->
    <div class="sticky top-0 bg-bg-primary/80 backdrop-blur-md z-10 px-4 py-3">
      <h1 class="text-xl font-bold">首页</h1>
    </div>

    <!-- Compose Area -->
    <div v-if="isAuthenticated" class="p-4 flex gap-4">
      <img :src="user?.avatar" :alt="user?.nickname" class="w-12 h-12 rounded-full object-cover" />
      <div class="flex-1">
        <div class="mb-3 flex justify-end">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-full border border-border px-3 py-2 text-sm font-medium hover:bg-bg-secondary"
            @click="openFullscreenEditor"
          >
            <Maximize2 :size="16" />
            全屏编辑
          </button>
        </div>
        <textarea 
          v-model="newPostContent"
          placeholder="有什么新鲜事？" 
          class="w-full bg-transparent text-xl outline-none resize-none min-h-[100px]"
        ></textarea>
        
        <!-- Image Previews -->
          <div v-if="images.length > 0" class="flex gap-2 mb-4 overflow-x-auto pb-2">
            <div v-for="(img, i) in images" :key="i" class="relative flex-shrink-0">
              <img :src="img.previewUrl" class="w-24 h-24 rounded-xl object-cover" />
              <button 
                @click="images.splice(i, 1)"
                class="absolute top-1 right-1 bg-black/50 text-white rounded-full p-1 hover:bg-black/70"
              >
              ×
            </button>
          </div>
        </div>

        <div class="flex items-center justify-between pt-3 border-t border-border">
          <div class="flex items-center text-brand">
            <label class="p-2 hover:bg-brand/10 rounded-full cursor-pointer transition-colors">
              <ImageIcon :size="20" />
              <input type="file" class="hidden" accept="image/*" multiple @change="handleImageUpload" />
            </label>
            <EmojiPicker @select="handleInsertEmoji" />
          </div>
          <button 
            @click="handleCreatePost"
            :disabled="!newPostContent.trim() || publishing"
            class="bg-brand hover:bg-brand-hover text-bg-primary px-6 py-2 rounded-full font-bold disabled:opacity-50 transition-all active:scale-95"
          >
            {{ publishing ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Feed -->
    <div class="min-h-screen">
      <div v-if="loading" class="flex justify-center p-10">
        <div class="w-8 h-8 border-4 border-brand border-t-transparent rounded-full animate-spin" />
      </div>
      <template v-else-if="posts.length > 0">
        <PostCard 
          v-for="post in posts" 
          :key="post.id" 
          :post="post" 
          @update="loadPosts" 
        />
      </template>
      <div v-else class="p-10 text-center text-text-secondary">
        <p class="text-xl font-bold text-text-primary mb-2">欢迎来到 HNUST Easy WeiBo</p>
        <p>目前还没有帖子，快来发布第一条吧！</p>
      </div>
    </div>
  </div>
</template>
