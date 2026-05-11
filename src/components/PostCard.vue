<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { BarChart3, Bookmark, Heart, MessageCircle, MoreHorizontal, PencilLine, Repeat2, Share2, ShieldAlert, Trash2, EyeOff } from 'lucide-vue-next';
import { formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { postService } from '../api/services';
import { useAuth } from '../composables/useAuth';
import { useToast } from '../composables/useToast';
import type { Post, PostViewRecord, ReportCategory } from '../types';
import PostViewsDrawer from './PostViewsDrawer.vue';
import ImageLightbox from './ImageLightbox.vue';

type ContentSegment =
  | { type: 'text'; value: string }
  | { type: 'tag'; value: string }
  | { type: 'mention'; value: string };

const REPORT_OPTIONS: Array<{ value: ReportCategory; label: string }> = [
  { value: 'spam', label: '垃圾营销' },
  { value: 'abuse', label: '辱骂攻击' },
  { value: 'misinformation', label: '不实信息' },
  { value: 'copyright', label: '侵权内容' },
  { value: 'other', label: '其他问题' },
];

const props = defineProps<{ post: Post }>();
const emit = defineEmits<{
  update: [];
  deleted: [];
}>();

const router = useRouter();
const { isAuthenticated, user } = useAuth();
const { showToast } = useToast();

const isLiked = ref(props.post.isLiked);
const likesCount = ref(props.post.likesCount);
const isReposted = ref(props.post.isReposted);
const repostsCount = ref(props.post.repostsCount);
const isBookmarked = ref(props.post.isBookmarked);
const showActionSheet = ref(false);
const showViewsDrawer = ref(false);
const loadingViews = ref(false);
const postViewRecords = ref<PostViewRecord[]>([]);
const editing = ref(false);
const savingEdit = ref(false);
const deleting = ref(false);
const hiding = ref(false);
const reporting = ref(false);
const showReportModal = ref(false);
const reportCategory = ref<ReportCategory>('spam');
const reportDetails = ref('');
const editContent = ref('');
const previewImages = ref<string[]>([]);
const previewIndex = ref(0);

watch(
  () => props.post,
  (post) => {
    isLiked.value = post.isLiked;
    likesCount.value = post.likesCount;
    isReposted.value = post.isReposted;
    repostsCount.value = post.repostsCount;
    isBookmarked.value = post.isBookmarked;
  },
  { deep: true },
);

const isAuthor = computed(() => user.value?.id === props.post.authorId);
const canInspectViews = computed(() => Boolean(user.value && (user.value.id === props.post.authorId || user.value.role === 'ADMIN')));
const formattedTime = computed(() => (
  formatDistanceToNow(new Date(props.post.createdAt), { addSuffix: true, locale: zhCN })
));

const formatMetric = (value: number) => {
  if (value >= 1000) {
    const compact = (value / 1000).toFixed(value >= 10000 ? 0 : 1).replace(/\.0$/, '');
    return `${compact}K`;
  }
  return String(value);
};

const contentSegments = computed<ContentSegment[]>(() => {
  const source = props.post.content || '';
  const matcher = /(@[A-Za-z0-9_]+|#[^\s#]+)/g;
  const segments: ContentSegment[] = [];
  let lastIndex = 0;

  source.replace(matcher, (match, _capture, offset: number) => {
    if (offset > lastIndex) {
      segments.push({ type: 'text', value: source.slice(lastIndex, offset) });
    }

    segments.push({
      type: match.startsWith('#') ? 'tag' : 'mention',
      value: match,
    });
    lastIndex = offset + match.length;
    return match;
  });

  if (lastIndex < source.length) {
    segments.push({ type: 'text', value: source.slice(lastIndex) });
  }

  return segments.length ? segments : [{ type: 'text', value: source }];
});

const handleLike = async (e: Event) => {
  e.stopPropagation();
  if (!isAuthenticated.value) {
    router.push('/login');
    return;
  }
  try {
    const updatedPost = await postService.likePost(props.post.id);
    isLiked.value = updatedPost.isLiked;
    likesCount.value = updatedPost.likesCount;
  } catch (error) {
    showToast(error instanceof Error ? error.message : '点赞失败，请稍后重试', 'error');
  }
};

const handleBookmark = async (e: Event) => {
  e.stopPropagation();
  if (!isAuthenticated.value) {
    router.push('/login');
    return;
  }
  try {
    const updatedPost = await postService.bookmarkPost(props.post.id);
    isBookmarked.value = updatedPost.isBookmarked;
  } catch (error) {
    showToast(error instanceof Error ? error.message : '收藏失败，请稍后重试', 'error');
  }
};

const handleRepost = async (e: Event) => {
  e.stopPropagation();
  if (!isAuthenticated.value) {
    showToast('请先登录后再转发帖子', 'error');
    router.push('/login');
    return;
  }
  try {
    const updatedPost = await postService.repostPost(props.post.id);
    isReposted.value = updatedPost.isReposted;
    repostsCount.value = updatedPost.repostsCount;
    showToast(updatedPost.isReposted ? '已转发到你的主页' : '已取消转发', 'success');
    emit('update');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '转发失败，请稍后重试', 'error');
  }
};

const handlePostClick = () => {
  router.push(`/post/${props.post.id}`);
};

const handleUserClick = (e: Event) => {
  e.stopPropagation();
  router.push(`/profile/${props.post.author.username}`);
};

const handleTagClick = (e: Event, tag: string) => {
  e.stopPropagation();
  router.push(`/search?q=${encodeURIComponent(tag)}`);
};

const handleMentionClick = (e: Event, mention: string) => {
  e.stopPropagation();
  router.push(`/profile/${mention.slice(1)}`);
};

const handleShare = async (e: Event) => {
  e.stopPropagation();
  try {
    await navigator.clipboard.writeText(`${window.location.origin}/post/${props.post.id}`);
    showToast('帖子链接已复制', 'success');
  } catch {
    showToast('复制链接失败，请重试', 'error');
  }
};

const openImagePreview = (event: Event, images: string[], index: number) => {
  event.stopPropagation();
  previewImages.value = images;
  previewIndex.value = index;
};

const openViewsDrawer = async (event: Event) => {
  event.stopPropagation();
  if (!canInspectViews.value) {
    return;
  }
  loadingViews.value = true;
  showViewsDrawer.value = true;
  try {
    postViewRecords.value = await postService.getPostViews(props.post.id);
  } catch (error) {
    showViewsDrawer.value = false;
    showToast(error instanceof Error ? error.message : '加载浏览记录失败，请稍后重试', 'error');
  } finally {
    loadingViews.value = false;
  }
};

const openEditModal = (e: Event) => {
  e.stopPropagation();
  showActionSheet.value = false;
  editContent.value = props.post.content;
  editing.value = true;
};

const closeEditModal = () => {
  if (!savingEdit.value) {
    editing.value = false;
  }
};

const handleSaveEdit = async () => {
  const content = editContent.value.trim();
  if (!content) {
    showToast('帖子内容不能为空', 'error');
    return;
  }
  if (content.length > 1000) {
    showToast('帖子内容不能超过 1000 个字符', 'error');
    return;
  }

  savingEdit.value = true;
  try {
    await postService.updatePost(props.post.id, content);
    editing.value = false;
    showToast('帖子已更新', 'success');
    emit('update');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '更新帖子失败，请稍后重试', 'error');
  } finally {
    savingEdit.value = false;
  }
};

const handleDelete = async (e: Event) => {
  e.stopPropagation();
  showActionSheet.value = false;
  if (!window.confirm('确定要删除这条帖子吗？此操作无法撤销。')) {
    return;
  }

  deleting.value = true;
  try {
    await postService.deletePost(props.post.id);
    showToast('帖子已删除', 'success');
    emit('deleted');
    emit('update');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '删除帖子失败，请稍后重试', 'error');
  } finally {
    deleting.value = false;
  }
};

const handleHide = async (e: Event) => {
  e.stopPropagation();
  showActionSheet.value = false;
  if (!isAuthenticated.value) {
    showToast('请先登录后再隐藏帖子', 'error');
    router.push('/login');
    return;
  }
  if (!window.confirm('隐藏后这条帖子将不会再出现在你的信息流中，确定继续吗？')) {
    return;
  }

  hiding.value = true;
  try {
    await postService.hidePost(props.post.id);
    showToast('已隐藏这条帖子', 'success');
    emit('deleted');
    emit('update');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '隐藏失败，请稍后重试', 'error');
  } finally {
    hiding.value = false;
  }
};

const openReportModal = (e: Event) => {
  e.stopPropagation();
  showActionSheet.value = false;
  if (!isAuthenticated.value) {
    showToast('请先登录后再举报帖子', 'error');
    router.push('/login');
    return;
  }
  reportCategory.value = 'spam';
  reportDetails.value = '';
  showReportModal.value = true;
};

const closeReportModal = () => {
  if (!reporting.value) {
    showReportModal.value = false;
  }
};

const handleSubmitReport = async () => {
  reporting.value = true;
  try {
    await postService.reportPost(props.post.id, {
      category: reportCategory.value,
      details: reportDetails.value.trim() || undefined,
    });
    showReportModal.value = false;
    showToast('举报已提交，管理员会尽快处理', 'success');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '举报失败，请稍后重试', 'error');
  } finally {
    reporting.value = false;
  }
};

</script>

<template>
  <div
    class="p-4 border-b border-border hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition-colors"
    @click="handlePostClick"
  >
    <div class="flex gap-3">
      <img
        :src="post.author.avatar"
        :alt="post.author.nickname"
        loading="lazy"
        class="w-12 h-12 rounded-full object-cover flex-shrink-0 hover:opacity-90 transition-opacity"
        @click="handleUserClick"
      />

      <div class="min-w-0 flex-1">
        <div class="flex items-center justify-between gap-3">
          <div class="flex min-w-0 items-center gap-1" @click="handleUserClick">
            <span class="truncate font-bold hover:underline">{{ post.author.nickname }}</span>
            <span class="truncate text-sm text-text-secondary">@{{ post.author.username }}</span>
            <span class="text-sm text-text-secondary">·</span>
            <span class="whitespace-nowrap text-sm text-text-secondary">{{ formattedTime }}</span>
          </div>

          <div class="relative">
            <button
              type="button"
              aria-label="更多操作"
              class="rounded-full p-2 text-text-secondary transition-colors hover:bg-brand/10 hover:text-brand"
              @click.stop="showActionSheet = true"
            >
              <MoreHorizontal :size="18" />
            </button>
          </div>
        </div>

        <div class="mt-1 whitespace-pre-wrap break-words text-[15px] leading-normal">
          <div
            v-if="post.status === 'WITHDRAWN' && isAuthor"
            class="mb-3 rounded-2xl border border-black/10 bg-black/5 px-3 py-2 text-sm text-text-secondary dark:border-white/10 dark:bg-white/5"
          >
            这条帖子已被管理员撤回，仅你自己可见。你可以直接编辑或在通知页点“重新编辑”后再发布。
          </div>
          <template v-for="(segment, index) in contentSegments" :key="`${segment.type}-${index}-${segment.value}`">
            <span v-if="segment.type === 'text'">{{ segment.value }}</span>
            <button
              v-else-if="segment.type === 'tag'"
              type="button"
              class="text-brand hover:underline"
              @click="handleTagClick($event, segment.value)"
            >
              {{ segment.value }}
            </button>
            <button
              v-else
              type="button"
              class="text-brand hover:underline"
              @click="handleMentionClick($event, segment.value)"
            >
              {{ segment.value }}
            </button>
          </template>
        </div>

        <div
          v-if="post.images && post.images.length > 0"
          :class="['mt-3 grid gap-0.5 overflow-hidden rounded-2xl border border-border', post.images.length === 1 ? 'grid-cols-1' : 'grid-cols-2']"
        >
          <button
            v-for="(img, idx) in post.images"
            :key="idx"
            type="button"
            class="block overflow-hidden bg-transparent"
            @click="openImagePreview($event, post.images || [], idx)"
          >
            <img
              :src="img"
              alt="Post content"
              loading="lazy"
              class="aspect-video h-full w-full object-cover transition hover:scale-[1.01]"
            />
          </button>
        </div>

        <div class="mt-3 flex items-center justify-between text-text-secondary">
          <div class="grid flex-1 grid-cols-4 gap-1 sm:max-w-[500px]">
            <button
              class="group inline-flex items-center gap-1.5 rounded-full pr-3 text-[15px] transition-colors hover:text-text-primary"
              @click.stop="router.push(`/post/${post.id}#comments`)"
            >
              <span class="inline-flex h-9 w-9 items-center justify-center rounded-full transition-colors group-hover:bg-black/5 dark:group-hover:bg-white/10">
                <MessageCircle :size="20" stroke-width="1.9" />
              </span>
              <span>{{ formatMetric(post.commentsCount) }}</span>
            </button>

            <button
              type="button"
              class="group inline-flex items-center gap-1.5 rounded-full pr-3 text-[15px] transition-colors"
              :class="isReposted ? 'text-text-primary' : 'hover:text-text-primary'"
              @click="handleRepost"
            >
              <span
                class="inline-flex h-9 w-9 items-center justify-center rounded-full transition-colors group-hover:bg-black/5 dark:group-hover:bg-white/10"
              >
                <Repeat2 :size="20" stroke-width="1.9" />
              </span>
              <span>{{ formatMetric(repostsCount) }}</span>
            </button>

            <button
              class="group inline-flex items-center gap-1.5 rounded-full pr-3 text-[15px] transition-colors"
              :class="isLiked ? 'text-text-primary' : 'hover:text-text-primary'"
              @click="handleLike"
            >
              <span
                class="inline-flex h-9 w-9 items-center justify-center rounded-full transition-colors group-hover:bg-black/5 dark:group-hover:bg-white/10"
              >
                <Heart :size="20" stroke-width="1.9" :fill="isLiked ? 'currentColor' : 'none'" />
              </span>
              <span>{{ formatMetric(likesCount) }}</span>
            </button>

            <button
              v-if="canInspectViews"
              type="button"
              class="group inline-flex items-center gap-1.5 rounded-full pr-3 text-[15px] transition-colors hover:text-text-primary"
              @click="openViewsDrawer"
            >
              <span class="inline-flex h-9 w-9 items-center justify-center rounded-full transition-colors group-hover:bg-black/5 dark:group-hover:bg-white/10">
                <BarChart3 :size="20" stroke-width="1.9" />
              </span>
              <span>{{ formatMetric(post.viewsCount) }}</span>
            </button>
            <div v-else class="inline-flex items-center gap-1.5 rounded-full pr-3 text-[15px]">
              <span class="inline-flex h-9 w-9 items-center justify-center rounded-full">
                <BarChart3 :size="20" stroke-width="1.9" />
              </span>
              <span>{{ formatMetric(post.viewsCount) }}</span>
            </div>
          </div>

          <div class="ml-3 flex items-center gap-1">
            <button
              class="group inline-flex h-9 w-9 items-center justify-center rounded-full transition-colors"
              :class="isBookmarked ? 'text-text-primary' : 'hover:text-text-primary'"
              @click="handleBookmark"
            >
              <span class="inline-flex h-9 w-9 items-center justify-center rounded-full transition-colors group-hover:bg-black/5 dark:group-hover:bg-white/10">
                <Bookmark :size="20" stroke-width="1.9" :fill="isBookmarked ? 'currentColor' : 'none'" />
              </span>
            </button>

            <button
              type="button"
              class="group inline-flex h-9 w-9 items-center justify-center rounded-full transition-colors hover:text-text-primary"
              @click="handleShare"
            >
              <span class="inline-flex h-9 w-9 items-center justify-center rounded-full transition-colors group-hover:bg-black/5 dark:group-hover:bg-white/10">
                <Share2 :size="20" stroke-width="1.9" />
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showActionSheet"
      class="fixed inset-0 z-30 flex items-end justify-center bg-black/35 px-4 pb-6 sm:items-center sm:pb-0"
      @click.self="showActionSheet = false"
    >
      <div class="w-full max-w-sm rounded-[28px] border border-border bg-bg-primary p-4 shadow-2xl" @click.stop>
        <div class="mb-3">
          <h3 class="text-lg font-bold">帖子操作</h3>
          <p class="mt-1 text-sm text-text-secondary">
            {{ isAuthor ? '管理你发布的内容。' : '管理你看到的这条帖子。' }}
          </p>
        </div>

        <div class="space-y-2">
          <template v-if="isAuthor">
            <button
              type="button"
              class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-medium hover:bg-bg-secondary"
              @click="openEditModal"
            >
              <PencilLine :size="16" />
              编辑帖子
            </button>
            <button
              type="button"
              class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-medium text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10"
              :disabled="deleting"
              @click="handleDelete"
            >
              <Trash2 :size="16" />
              {{ deleting ? '删除中...' : '删除帖子' }}
            </button>
          </template>
          <template v-else>
            <button
              type="button"
              class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-medium hover:bg-bg-secondary"
              :disabled="hiding"
              @click="handleHide"
            >
              <EyeOff :size="16" />
              {{ hiding ? '隐藏中...' : '隐藏此帖' }}
            </button>
            <button
              type="button"
              class="flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-medium hover:bg-bg-secondary"
              @click="openReportModal"
            >
              <ShieldAlert :size="16" />
              举报帖子
            </button>
          </template>
        </div>

        <div class="mt-4 flex justify-end">
          <button
            type="button"
            class="rounded-full px-4 py-2 text-sm font-medium text-text-secondary hover:bg-bg-secondary"
            @click="showActionSheet = false"
          >
            关闭
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="editing"
      class="fixed inset-0 z-40 flex items-center justify-center bg-black/45 px-4"
      @click.self="closeEditModal"
    >
      <div class="w-full max-w-2xl rounded-3xl border border-border bg-bg-primary p-6 shadow-2xl" @click.stop>
        <div class="flex items-center justify-between gap-4">
          <div>
            <h3 class="text-xl font-bold">编辑帖子</h3>
            <p class="mt-1 text-sm text-text-secondary">保存后会同步更新首页、详情页和个人主页。</p>
          </div>
          <button type="button" class="rounded-full px-3 py-2 text-sm hover:bg-bg-secondary" @click="closeEditModal">
            取消
          </button>
        </div>

        <textarea
          v-model="editContent"
          class="mt-6 min-h-[220px] w-full resize-none rounded-3xl border border-border bg-bg-primary px-4 py-4 text-[15px] leading-7 outline-none transition focus:border-text-primary"
          maxlength="1000"
        ></textarea>

        <div class="mt-3 flex items-center justify-between text-sm text-text-secondary">
          <span>仅支持修改文字内容，图片保持不变。</span>
          <span>{{ editContent.trim().length }}/1000</span>
        </div>

        <div class="mt-6 flex justify-end">
          <button
            type="button"
            class="rounded-full bg-text-primary px-5 py-3 font-bold text-bg-primary hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="savingEdit"
            @click="handleSaveEdit"
          >
            {{ savingEdit ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="showReportModal"
      class="fixed inset-0 z-40 flex items-center justify-center bg-black/45 px-4"
      @click.self="closeReportModal"
    >
      <div class="w-full max-w-lg rounded-3xl border border-border bg-bg-primary p-6 shadow-2xl" @click.stop>
        <div class="flex items-center justify-between gap-4">
          <div>
            <h3 class="text-xl font-bold">举报帖子</h3>
            <p class="mt-1 text-sm text-text-secondary">选择举报类型，并补充必要说明。</p>
          </div>
          <button type="button" class="rounded-full px-3 py-2 text-sm hover:bg-bg-secondary" @click="closeReportModal">
            取消
          </button>
        </div>

        <div class="mt-5 space-y-4">
          <label class="block">
            <span class="mb-2 block text-sm font-bold">举报类型</span>
            <select
              v-model="reportCategory"
              class="w-full rounded-2xl border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary"
            >
              <option v-for="option in REPORT_OPTIONS" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>

          <label class="block">
            <span class="mb-2 block text-sm font-bold">补充说明</span>
            <textarea
              v-model="reportDetails"
              rows="4"
              maxlength="255"
              placeholder="可选，帮助管理员更快判断问题。"
              class="w-full rounded-2xl border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary"
            ></textarea>
          </label>
        </div>

        <div class="mt-6 flex justify-end">
          <button
            type="button"
            class="rounded-full bg-text-primary px-5 py-3 font-bold text-bg-primary hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="reporting"
            @click="handleSubmitReport"
          >
            {{ reporting ? '提交中...' : '提交举报' }}
          </button>
        </div>
      </div>
    </div>

    <PostViewsDrawer
      :open="showViewsDrawer"
      :loading="loadingViews"
      :records="postViewRecords"
      @close="showViewsDrawer = false"
    />
    <ImageLightbox
      :open="previewImages.length > 0"
      :images="previewImages"
      :index="previewIndex"
      @close="previewImages = []"
      @update:index="previewIndex = $event"
    />
  </div>
</template>
