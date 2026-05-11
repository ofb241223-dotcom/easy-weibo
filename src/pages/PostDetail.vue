<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { postService, commentService } from '../api/services';
import { Post, Comment as CommentType } from '../types';
import PostCard from '../components/PostCard.vue';
import { ArrowLeft, Image as ImageIcon, SmilePlus, X } from 'lucide-vue-next';
import { useAuth } from '../composables/useAuth';
import { useToast } from '../composables/useToast';
import { formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import ImageLightbox from '../components/ImageLightbox.vue';

const route = useRoute();
const router = useRouter();
const id = route.params.id as string;
const post = ref<Post | null>(null);
const comments = ref<CommentType[]>([]);
const loading = ref(true);
const newComment = ref('');
const emojiOpen = ref(false);
const commentImages = ref<Array<{ file: File; previewUrl: string }>>([]);
const previewImages = ref<string[]>([]);
const previewIndex = ref(0);
const { user, isAuthenticated } = useAuth();
const { showToast } = useToast();
const emojiPalette = ['😀', '😂', '🥳', '👍', '👏', '🔥', '✨', '🎉', '❤️', '🙏', '🤝', '📚', '💻', '😄', '😮', '🥲', '😎', '👀'];

const loadData = async () => {
  loading.value = true;
  try {
    const p = await postService.getPostById(id);
    post.value = p || null;
    comments.value = p ? await commentService.getCommentsByPostId(id) : [];
  } catch (error) {
    post.value = null;
    comments.value = [];
    showToast(error instanceof Error ? error.message : '加载帖子失败，请稍后重试', 'error');
  } finally {
    loading.value = false;
  }
};

onMounted(loadData);

const handleAddComment = async () => {
  if ((!newComment.value.trim() && commentImages.value.length === 0) || !user.value || !post.value) return;
  try {
    await commentService.addComment(post.value.id, newComment.value, commentImages.value.map((item) => item.file));
    newComment.value = '';
    emojiOpen.value = false;
    commentImages.value = [];
    await loadData();
  } catch (error) {
    showToast(error instanceof Error ? error.message : '评论失败，请稍后重试', 'error');
  }
};

const insertEmoji = (emoji: string) => {
  newComment.value += emoji;
};

const handleImageUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  Array.from(target.files || []).forEach((file) => {
    const reader = new FileReader();
    reader.onloadend = () => {
      commentImages.value.push({
        file,
        previewUrl: reader.result as string,
      });
    };
    reader.readAsDataURL(file);
  });
  target.value = '';
};

const openImagePreview = (images: string[], index: number) => {
  previewImages.value = images;
  previewIndex.value = index;
};
</script>

<template>
  <div v-if="loading" class="flex justify-center p-10">
    <div class="w-8 h-8 border-4 border-brand border-t-transparent rounded-full animate-spin" />
  </div>
  <div v-else-if="!post" class="p-10 text-center">
    <h1 class="text-2xl font-bold">帖子不存在</h1>
    <button @click="router.push('/')" class="text-brand mt-4 hover:underline">回到首页</button>
  </div>
  <div v-else class="min-h-screen">
    <div class="sticky top-0 bg-bg-primary/80 backdrop-blur-md z-10 px-4 py-2 flex items-center gap-6">
      <button @click="router.back()" class="p-2 hover:bg-bg-secondary rounded-full">
        <ArrowLeft :size="20" />
      </button>
      <h1 class="text-xl font-bold">帖子</h1>
    </div>

    <PostCard :post="post" @update="loadData" @deleted="router.push('/')" />

    <div v-if="isAuthenticated" class="border-b border-border p-4">
      <div class="rounded-[28px] border border-border bg-bg-primary p-4 shadow-sm">
        <div class="flex items-start gap-4">
          <img :src="user?.avatar" :alt="user?.nickname" class="mt-1 h-12 w-12 rounded-full object-cover" />
          <div class="min-w-0 flex-1">
            <textarea
              v-model="newComment"
              rows="3"
              maxlength="1000"
              placeholder="发布你的回复"
              class="min-h-[96px] w-full resize-none bg-transparent text-lg outline-none"
              @keydown.enter.exact.prevent="handleAddComment"
            ></textarea>

            <div v-if="commentImages.length > 0" class="mt-4 flex gap-2 overflow-x-auto pb-2">
              <div v-for="(img, index) in commentImages" :key="`${img.file.name}-${index}`" class="relative shrink-0">
                <img :src="img.previewUrl" class="h-24 w-24 rounded-2xl object-cover" />
                <button
                  type="button"
                  class="absolute right-1 top-1 rounded-full bg-black/60 px-2 py-1 text-xs text-white hover:bg-black/80"
                  @click="commentImages.splice(index, 1)"
                >
                  ×
                </button>
              </div>
            </div>

            <div class="mt-4 flex items-center justify-between gap-3">
              <div class="relative flex items-center gap-2">
                <label class="inline-flex h-11 w-11 cursor-pointer items-center justify-center rounded-full border border-border hover:bg-bg-secondary">
                  <ImageIcon :size="18" />
                  <input type="file" class="hidden" accept="image/*" multiple @change="handleImageUpload" />
                </label>
                <button
                  type="button"
                  class="inline-flex h-11 w-11 items-center justify-center rounded-full border border-border hover:bg-bg-secondary"
                  @click="emojiOpen = !emojiOpen"
                >
                  <SmilePlus :size="18" />
                </button>

                <div
                  v-if="emojiOpen"
                  class="absolute bottom-14 left-0 z-10 w-72 rounded-3xl border border-border bg-bg-primary p-4 shadow-lg"
                >
                  <div class="mb-3 flex items-center justify-between">
                    <p class="text-sm font-bold">选择表情</p>
                    <button type="button" class="rounded-full p-1 hover:bg-bg-secondary" @click="emojiOpen = false">
                      <X :size="14" />
                    </button>
                  </div>
                  <div class="grid grid-cols-6 gap-2">
                    <button
                      v-for="emoji in emojiPalette"
                      :key="emoji"
                      type="button"
                      class="rounded-2xl border border-border px-2 py-2 text-xl hover:bg-bg-secondary"
                      @click="insertEmoji(emoji)"
                    >
                      {{ emoji }}
                    </button>
                  </div>
                </div>
              </div>

              <button
                @click="handleAddComment"
                :disabled="!newComment.trim() && commentImages.length === 0"
                class="rounded-full bg-text-primary px-6 py-3 font-bold text-bg-primary hover:opacity-90 disabled:opacity-50"
              >
                回复
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="p-6 text-center border-b border-border">
      <p class="text-text-secondary mb-2">登录后即可参与讨论</p>
      <button @click="router.push('/login')" class="text-brand font-bold hover:underline">立即登录</button>
    </div>

    <div class="divide-y divide-border">
      <div v-for="comment in comments" :key="comment.id" class="p-4 flex gap-3 hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
        <img 
          :src="comment.author.avatar" 
          :alt="comment.author.nickname" 
          class="w-10 h-10 rounded-full object-cover cursor-pointer"
          @click="router.push(`/profile/${comment.author.username}`)"
        />
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-1">
            <span class="font-bold truncate hover:underline cursor-pointer" @click="router.push(`/profile/${comment.author.username}`)">
              {{ comment.author.nickname }}
            </span>
            <span class="text-text-secondary text-sm truncate">@{{ comment.author.username }}</span>
            <span class="text-text-secondary text-sm">·</span>
            <span class="text-text-secondary text-sm">
              {{ formatDistanceToNow(new Date(comment.createdAt), { addSuffix: true, locale: zhCN }) }}
            </span>
          </div>
          <p class="mt-1 text-[15px]">{{ comment.content }}</p>
          <div v-if="comment.images?.length" class="mt-3 flex flex-wrap gap-2">
            <button
              v-for="(image, index) in comment.images"
              :key="`${comment.id}-${index}`"
              type="button"
              class="block overflow-hidden rounded-2xl bg-transparent"
              @click="openImagePreview(comment.images || [], index)"
            >
              <img
                :src="image"
                alt="Comment image"
                loading="lazy"
                class="h-24 w-24 rounded-2xl object-cover transition hover:scale-[1.02]"
              />
            </button>
          </div>
        </div>
      </div>
    </div>

    <ImageLightbox
      :open="previewImages.length > 0"
      :images="previewImages"
      :index="previewIndex"
      @close="previewImages = []"
      @update:index="previewIndex = $event"
    />
  </div>
</template>
