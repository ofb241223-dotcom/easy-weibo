<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuth } from '../composables/useAuth';
import { useToast } from '../composables/useToast';
import { postService } from '../api/services';
import { X, Image as ImageIcon, Maximize2, Minimize2 } from 'lucide-vue-next';
import EmojiPicker from '../components/EmojiPicker.vue';

const COMPOSE_DRAFT_KEY = 'easyweibo_compose_draft';
const route = useRoute();
const router = useRouter();
const { user, isAuthenticated } = useAuth();
const { showToast } = useToast();

const content = ref('');
const publishing = ref(false);
const isFullscreen = ref(route.query.fullscreen === '1');
const images = ref<Array<{ file: File; previewUrl: string }>>([]);
const loadingPost = ref(false);
const editPostId = computed(() => typeof route.query.edit === 'string' ? route.query.edit : '');
const isRepublishMode = computed(() => Boolean(editPostId.value));

if (!isAuthenticated.value) {
  router.push('/login');
}

const handlePublish = async () => {
  if (!content.value.trim() || !user.value) return;
  try {
    publishing.value = true;
    if (isRepublishMode.value) {
      await postService.republishPost(editPostId.value, content.value);
      showToast('帖子已重新发布', 'success');
    } else {
      await postService.createPost(content.value, images.value.map((item) => item.file));
      showToast('发布成功！', 'success');
    }
    window.sessionStorage.removeItem(COMPOSE_DRAFT_KEY);
    router.push('/');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '发布失败，请稍后重试', 'error');
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
  content.value = `${content.value}${content.value ? ' ' : ''}${emoji}`;
};

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value;
  router.replace({
    path: '/compose',
    query: isFullscreen.value ? { fullscreen: '1' } : {},
  });
};

onMounted(() => {
  if (isRepublishMode.value) {
    loadingPost.value = true;
    postService.getPostById(editPostId.value)
      .then((post) => {
        if (!post) {
          showToast('帖子不存在或无法编辑', 'error');
          router.replace('/profile/' + (user.value?.username || ''));
          return;
        }
        content.value = post.content;
      })
      .catch((error) => {
        showToast(error instanceof Error ? error.message : '加载帖子失败', 'error');
        router.replace('/');
      })
      .finally(() => {
        loadingPost.value = false;
      });
  } else {
    const draft = window.sessionStorage.getItem(COMPOSE_DRAFT_KEY);
    if (draft && !content.value) {
      content.value = draft;
    }
  }
});

watch(content, (value) => {
  if (!isRepublishMode.value) {
    window.sessionStorage.setItem(COMPOSE_DRAFT_KEY, value);
  }
});

</script>

<template>
  <div class="min-h-screen bg-bg-secondary/40 px-4 py-5 sm:px-6 lg:px-8">
    <div
      :class="[
        'mx-auto transition-all duration-200',
        isFullscreen
          ? 'fixed inset-0 z-30 max-w-none rounded-none bg-bg-primary px-4 py-5 sm:px-8'
          : 'max-w-4xl'
      ]"
    >
      <div
        :class="[
          'overflow-hidden border border-border bg-bg-primary shadow-sm transition-all duration-200',
          isFullscreen ? 'h-full rounded-none' : 'rounded-[28px]'
        ]"
      >
        <div class="flex items-center justify-between border-b border-border px-4 py-4 sm:px-6">
          <div class="flex items-center gap-3">
            <button @click="router.back()" class="p-2 hover:bg-bg-secondary rounded-full">
              <X :size="22" />
            </button>
            <div>
              <h1 class="text-lg font-bold sm:text-xl">{{ isRepublishMode ? '重新编辑帖子' : '发布内容' }}</h1>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <button
              type="button"
              class="rounded-full border border-border px-3 py-2 text-sm font-medium hover:bg-bg-secondary"
              @click="toggleFullscreen"
            >
              <span class="inline-flex items-center gap-2">
                <Maximize2 v-if="!isFullscreen" :size="16" />
                <Minimize2 v-else :size="16" />
                {{ isFullscreen ? '退出全屏' : '全屏编辑' }}
              </span>
            </button>
          </div>
        </div>

        <div class="flex h-[calc(100%-73px)] flex-col">
          <div class="flex flex-1 gap-4 px-4 py-5 sm:px-6">
            <img :src="user?.avatar" :alt="user?.nickname" class="w-12 h-12 rounded-full object-cover" />
            <div class="flex-1">
              <div v-if="loadingPost" class="py-6 text-sm text-text-secondary">正在加载帖子内容...</div>
              <template v-else>
                <textarea
                  v-model="content"
                  :placeholder="isRepublishMode ? '修改后重新发布这条帖子' : '有什么新鲜事？'"
                  :class="[
                    'w-full bg-transparent text-lg outline-none resize-none leading-8 sm:text-xl',
                    isFullscreen ? 'min-h-[calc(100vh-260px)]' : 'min-h-[280px]'
                  ]"
                ></textarea>

                <p v-if="isRepublishMode" class="mt-3 text-sm text-text-secondary">
                  管理员已撤回这条帖子。修改内容后可重新发布，原有图片会继续保留。
                </p>
              </template>

              <div v-if="images.length > 0" class="mt-4 flex gap-2 overflow-x-auto pb-2">
                <div v-for="(img, i) in images" :key="i" class="relative flex-shrink-0">
                  <img :src="img.previewUrl" class="w-32 h-32 rounded-2xl object-cover" />
                  <button
                    @click="images.splice(i, 1)"
                    class="absolute top-1 right-1 bg-black/50 text-white rounded-full p-1 hover:bg-black/70"
                  >
                    ×
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-auto flex items-center justify-between border-t border-border px-4 py-4 sm:px-6">
            <div class="flex items-center text-brand">
              <label
                v-if="!isRepublishMode"
                class="p-2 hover:bg-brand/10 rounded-full cursor-pointer transition-colors"
              >
                <ImageIcon :size="22" />
                <input type="file" class="hidden" accept="image/*" multiple @change="handleImageUpload" />
              </label>
              <EmojiPicker @select="handleInsertEmoji" />
            </div>
            <div class="flex items-center gap-3">
              <p class="text-sm text-text-secondary">{{ content.trim().length }} 字</p>
              <button
                @click="handlePublish"
                :disabled="!content.trim() || publishing || loadingPost"
                class="bg-brand hover:bg-brand-hover text-bg-primary px-5 py-2 rounded-full font-bold disabled:opacity-50 transition-all"
              >
                {{ publishing ? '提交中...' : (isRepublishMode ? '重新发布' : '发布') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
