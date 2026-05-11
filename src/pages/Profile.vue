<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { chatService, userService, postService, commentService, uploadService } from '../api/services';
import { User, Post, Comment } from '../types';
import PostCard from '../components/PostCard.vue';
import { ArrowLeft, Calendar, MapPin, Link as LinkIcon, MessageCircle } from 'lucide-vue-next';
import { useAuth } from '../composables/useAuth';
import { useToast } from '../composables/useToast';

const route = useRoute();
const router = useRouter();
const username = computed(() => String(route.params.username || ''));
const profileUser = ref<User | null>(null);
const posts = ref<Post[]>([]);
const likedPosts = ref<Post[]>([]);
const repostedPosts = ref<Post[]>([]);
const replies = ref<Comment[]>([]);
const loading = ref(true);
const { user: currentUser, isAuthenticated, setCurrentUser } = useAuth();
const { showToast } = useToast();
const activeTab = ref<'帖子' | '回复' | '喜欢' | '转发'>('帖子');
const isEditModalOpen = ref(false);
const savingProfile = ref(false);
const editNickname = ref('');
const editBio = ref('');
const editAvatar = ref('');
const editCoverUrl = ref('');
const editAvatarFile = ref<File | null>(null);
const editCoverFile = ref<File | null>(null);
const avatarPreviewUrl = ref('');
const coverPreviewUrl = ref('');

const loadProfile = async () => {
  loading.value = true;
  try {
    const u = await userService.getUserByUsername(username.value);
    profileUser.value = u || null;
    if (u) {
      const [authored, liked, reposted, authoredReplies] = await Promise.all([
        postService.getPostsByAuthor(u.id),
        postService.getLikedPostsByUser(u.id),
        postService.getRepostedPostsByUser(u.id),
        commentService.getCommentsByAuthorId(u.id),
      ]);
      posts.value = authored;
      likedPosts.value = liked;
      repostedPosts.value = reposted;
      replies.value = authoredReplies;
    } else {
      posts.value = [];
      likedPosts.value = [];
      repostedPosts.value = [];
      replies.value = [];
    }
  } catch (error) {
    profileUser.value = null;
    posts.value = [];
    likedPosts.value = [];
    repostedPosts.value = [];
    replies.value = [];
    showToast(error instanceof Error ? error.message : '加载个人主页失败，请稍后重试', 'error');
  } finally {
    loading.value = false;
  }
};

onMounted(loadProfile);

watch(
  () => route.params.username,
  async () => {
    activeTab.value = '帖子';
    await loadProfile();
  },
);

const filteredPosts = computed(() => {
  switch (activeTab.value) {
    case '喜欢':
      return likedPosts.value;
    case '转发':
      return repostedPosts.value;
    default:
      return posts.value;
  }
});

const filteredReplies = computed(() => (activeTab.value === '回复' ? replies.value : []));

const emptyStateText = computed(() => {
  switch (activeTab.value) {
    case '喜欢':
      return '这个用户还没有点赞任何帖子。';
    case '转发':
      return '这个用户还没有转发任何帖子。';
    case '回复':
      return '这个用户还没有发布任何回复。';
    default:
      return '这个用户还没有发布任何帖子。';
  }
});

const handleFollow = async () => {
  if (!isAuthenticated.value) {
    router.push('/login');
    return;
  }
  if (profileUser.value) {
    await userService.followUser(profileUser.value.id);
    loadProfile();
  }
};

const handleChat = async () => {
  if (!isAuthenticated.value) {
    router.push('/login');
    return;
  }
  if (!profileUser.value) {
    return;
  }
  try {
    const conversation = await chatService.createConversation(profileUser.value.id);
    await router.push(`/chat/${conversation.id}`);
  } catch (error) {
    showToast(error instanceof Error ? error.message : '创建聊天失败，请稍后重试', 'error');
  }
};

const handleEditProfile = () => {
  if (!profileUser.value) {
    return;
  }
  editNickname.value = profileUser.value.nickname;
  editBio.value = profileUser.value.bio || '';
  editAvatar.value = profileUser.value.avatar || '';
  editCoverUrl.value = profileUser.value.coverUrl || '';
  avatarPreviewUrl.value = profileUser.value.avatar || '';
  coverPreviewUrl.value = profileUser.value.coverUrl || '';
  editAvatarFile.value = null;
  editCoverFile.value = null;
  isEditModalOpen.value = true;
};

const closeEditModal = () => {
  if (savingProfile.value) {
    return;
  }
  isEditModalOpen.value = false;
};

const handleSaveProfile = async () => {
  if (!profileUser.value) {
    return;
  }

  const nickname = editNickname.value.trim();
  const bio = editBio.value.trim();

  if (nickname.length < 2) {
    showToast('昵称至少 2 个字符', 'error');
    return;
  }

  savingProfile.value = true;

  try {
    let avatar = editAvatar.value.trim() || profileUser.value.avatar;
    let coverUrl = editCoverUrl.value.trim() || profileUser.value.coverUrl;

    if (editAvatarFile.value) {
      avatar = await uploadService.uploadImage(editAvatarFile.value);
    }

    if (editCoverFile.value) {
      coverUrl = await uploadService.uploadImage(editCoverFile.value);
    }

    const updatedUser = await userService.updateProfile({
      nickname,
      bio,
      avatar: avatar || undefined,
      coverUrl: coverUrl || undefined,
    });
    profileUser.value = updatedUser;
    posts.value = posts.value.map((post) => (
      post.authorId === updatedUser.id ? { ...post, author: updatedUser } : post
    ));
    likedPosts.value = likedPosts.value.map((post) => (
      post.authorId === updatedUser.id ? { ...post, author: updatedUser } : post
    ));
    repostedPosts.value = repostedPosts.value.map((post) => (
      post.authorId === updatedUser.id ? { ...post, author: updatedUser } : post
    ));
    replies.value = replies.value.map((reply) => (
      reply.authorId === updatedUser.id ? { ...reply, author: updatedUser } : reply
    ));
    setCurrentUser(updatedUser);
    isEditModalOpen.value = false;
    showToast('个人资料已更新', 'success');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '更新资料失败，请稍后重试', 'error');
  } finally {
    savingProfile.value = false;
  }
};

const handlePreviewUpload = (event: Event, target: 'avatar' | 'cover') => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) {
    return;
  }

  const reader = new FileReader();
  reader.onloadend = () => {
    const preview = reader.result as string;
    if (target === 'avatar') {
      editAvatarFile.value = file;
      avatarPreviewUrl.value = preview;
    } else {
      editCoverFile.value = file;
      coverPreviewUrl.value = preview;
    }
  };
  reader.readAsDataURL(file);
  input.value = '';
};
</script>

<template>
  <div v-if="loading" class="flex justify-center p-10">
    <div class="w-8 h-8 border-4 border-brand border-t-transparent rounded-full animate-spin" />
  </div>
  <div v-else-if="!profileUser" class="p-10 text-center">
    <h1 class="text-2xl font-bold">用户不存在</h1>
    <button @click="router.push('/')" class="text-brand mt-4 hover:underline">回到首页</button>
  </div>
  <div v-else class="min-h-screen">
    <div class="sticky top-0 bg-bg-primary/80 backdrop-blur-md z-10 px-4 py-2 flex items-center gap-6">
      <button @click="router.back()" class="p-2 hover:bg-bg-secondary rounded-full">
        <ArrowLeft :size="20" />
      </button>
      <div>
        <h1 class="text-xl font-bold">{{ profileUser.nickname }}</h1>
        <p class="text-text-secondary text-sm">{{ posts.length }} 帖子</p>
      </div>
    </div>

    <div class="relative">
      <div class="relative h-48 sm:h-60 overflow-hidden border-b border-border bg-bg-secondary">
        <img
          v-if="profileUser.coverUrl"
          :src="profileUser.coverUrl"
          :alt="`${profileUser.nickname} 的封面图`"
          class="absolute inset-0 h-full w-full object-cover"
        />
        <div
          class="absolute inset-0 opacity-70 [background-image:radial-gradient(circle_at_top_left,rgba(0,0,0,0.06),transparent_42%),linear-gradient(to_right,rgba(0,0,0,0.04)_1px,transparent_1px),linear-gradient(to_bottom,rgba(0,0,0,0.04)_1px,transparent_1px)] [background-size:auto,24px_24px,24px_24px]"
          :class="{ 'mix-blend-soft-light': profileUser.coverUrl }"
        />
      </div>
      <div class="absolute left-4 -bottom-10 sm:-bottom-12 border-4 border-bg-primary rounded-full overflow-hidden w-24 h-24 sm:w-28 sm:h-28 bg-bg-secondary shadow-sm">
        <img :src="profileUser.avatar" :alt="profileUser.nickname" loading="lazy" class="w-full h-full object-cover" />
      </div>
    </div>

    <div class="flex justify-end items-start px-4 pt-3 pb-1 min-h-[52px] sm:min-h-[62px]">
        <button v-if="currentUser?.id === profileUser.id" @click="handleEditProfile" class="border border-border rounded-full px-4 py-2 font-bold hover:bg-bg-secondary transition-colors">
          编辑个人资料
        </button>
        <div v-else class="flex flex-wrap items-center gap-2">
          <button 
            @click="handleFollow"
            :class="profileUser.isFollowing 
              ? 'border border-border rounded-full px-4 py-2 font-bold hover:bg-bg-secondary hover:text-text-primary hover:border-text-secondary transition-all group' 
              : 'bg-text-primary text-bg-primary rounded-full px-4 py-2 font-bold hover:opacity-90 transition-opacity'"
          >
            <span class="group-hover:hidden">{{ profileUser.isFollowing ? '正在关注' : '关注' }}</span>
            <span class="hidden group-hover:inline">取消关注</span>
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-full border border-border px-4 py-2 font-bold hover:bg-bg-secondary transition-colors"
            @click="handleChat"
          >
            <MessageCircle :size="16" />
            聊天
          </button>
        </div>
    </div>

    <div class="px-4 pb-2 space-y-1.5">
      <div>
        <h2 class="text-xl sm:text-2xl font-bold leading-tight">{{ profileUser.nickname }}</h2>
        <p class="text-text-secondary">@{{ profileUser.username }}</p>
      </div>
      <p v-if="profileUser.bio" class="text-[15px]">{{ profileUser.bio }}</p>
      <div class="flex flex-wrap gap-x-4 gap-y-2 text-text-secondary text-sm">
        <div class="flex items-center gap-1"><MapPin :size="16" /> 湖南省 湘潭市</div>
        <div class="flex items-center gap-1"><LinkIcon :size="16" /> <span class="text-brand hover:underline cursor-pointer">hnust.edu.cn</span></div>
        <div class="flex items-center gap-1"><Calendar :size="16" /> 2026年4月加入</div>
      </div>
      <div class="flex gap-4 pt-2">
        <div class="hover:underline cursor-pointer"><span class="font-bold">{{ profileUser.followingCount }}</span> <span class="text-text-secondary">正在关注</span></div>
        <div class="hover:underline cursor-pointer"><span class="font-bold">{{ profileUser.followersCount }}</span> <span class="text-text-secondary">关注者</span></div>
      </div>
    </div>

      <div class="mt-4 flex border-b border-border">
      <button
        v-for="tab in ['帖子', '回复', '喜欢', '转发']"
        :key="tab"
        type="button"
        @click="activeTab = tab as typeof activeTab.value"
        class="flex-1 border-b-2 border-transparent py-4 font-bold text-text-secondary transition-colors hover:bg-bg-secondary/50"
        :class="{ 'border-text-primary text-text-primary': activeTab === tab }"
      >
        {{ tab }}
      </button>
    </div>

    <div class="divide-y divide-border">
      <template v-if="activeTab === '回复'">
        <div
          v-for="reply in filteredReplies"
          :key="reply.id"
          class="p-4 hover:bg-bg-secondary/60 transition-colors cursor-pointer"
          @click="router.push(`/post/${reply.postId}`)"
        >
          <p class="text-sm text-text-secondary mb-2">回复了帖子 #{{ reply.postId }}</p>
          <p class="text-[15px] leading-6">{{ reply.content }}</p>
          <div v-if="reply.images?.length" class="mt-3 flex flex-wrap gap-2">
            <img
              v-for="(image, index) in reply.images"
              :key="`${reply.id}-${index}`"
              :src="image"
              alt="Reply image"
              loading="lazy"
              class="h-20 w-20 rounded-2xl object-cover"
            />
          </div>
          <p class="text-xs text-text-secondary mt-3">{{ new Date(reply.createdAt).toLocaleString('zh-CN') }}</p>
        </div>
      </template>
      <PostCard v-for="post in filteredPosts" :key="post.id" :post="post" @update="loadProfile" />
      <div v-if="activeTab === '回复' ? !filteredReplies.length : !filteredPosts.length" class="p-8 text-center text-text-secondary">
        {{ emptyStateText }}
      </div>
    </div>

    <div
      v-if="isEditModalOpen"
      class="fixed inset-0 z-40 flex items-center justify-center bg-black/45 px-4"
      @click.self="closeEditModal"
    >
      <div class="w-full max-w-2xl rounded-3xl bg-bg-primary p-6 shadow-2xl">
        <div class="flex items-center justify-between gap-4">
          <div>
            <h3 class="text-xl font-bold">编辑个人资料</h3>
            <p class="mt-1 text-sm text-text-secondary">修改昵称、简介、头像和封面图。</p>
          </div>
          <button
            type="button"
            class="rounded-full px-3 py-2 text-sm font-medium text-text-secondary hover:bg-bg-secondary"
            @click="closeEditModal"
          >
            关闭
          </button>
        </div>

        <div class="mt-6 space-y-4">
          <label class="block">
            <span class="mb-2 block text-sm font-bold">昵称</span>
            <input
              v-model.trim="editNickname"
              type="text"
              maxlength="32"
              class="w-full rounded-2xl border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary"
            />
          </label>

          <label class="block">
            <span class="mb-2 block text-sm font-bold">简介</span>
            <textarea
              v-model="editBio"
              maxlength="255"
              rows="4"
              class="w-full rounded-2xl border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary"
            ></textarea>
          </label>

          <div class="grid gap-4 md:grid-cols-2">
            <div class="space-y-3">
              <span class="block text-sm font-bold">头像</span>
              <div class="flex items-center gap-4">
                <img :src="avatarPreviewUrl || editAvatar" alt="头像预览" class="h-20 w-20 rounded-full object-cover border border-border" />
                <label class="inline-flex cursor-pointer items-center rounded-full border border-border px-4 py-2 text-sm font-medium hover:bg-bg-secondary">
                  上传头像
                  <input type="file" class="hidden" accept="image/*" @change="(event) => handlePreviewUpload(event, 'avatar')" />
                </label>
              </div>
            </div>

            <div class="space-y-3">
              <span class="block text-sm font-bold">封面图</span>
              <div class="space-y-3">
                <div class="h-24 overflow-hidden rounded-2xl border border-border bg-bg-secondary">
                  <img
                    v-if="coverPreviewUrl || editCoverUrl"
                    :src="coverPreviewUrl || editCoverUrl"
                    alt="封面预览"
                    class="h-full w-full object-cover"
                  />
                </div>
                <label class="inline-flex cursor-pointer items-center rounded-full border border-border px-4 py-2 text-sm font-medium hover:bg-bg-secondary">
                  上传封面
                  <input type="file" class="hidden" accept="image/*" @change="(event) => handlePreviewUpload(event, 'cover')" />
                </label>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-6 flex justify-end gap-3">
          <button
            type="button"
            class="rounded-full border border-border px-5 py-3 font-medium hover:bg-bg-secondary"
            :disabled="savingProfile"
            @click="closeEditModal"
          >
            取消
          </button>
          <button
            type="button"
            class="rounded-full bg-text-primary px-5 py-3 font-bold text-bg-primary hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="savingProfile"
            @click="handleSaveProfile"
          >
            {{ savingProfile ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
