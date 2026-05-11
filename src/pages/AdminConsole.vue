<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ArrowLeft,
  Ban,
  CheckCircle2,
  FileText,
  Flag,
  LayoutDashboard,
  MessageSquare,
  RefreshCcw,
  Search,
  ShieldAlert,
  Trash2,
  UserRound,
  Volume2,
  VolumeX,
} from 'lucide-vue-next';
import { adminService } from '../api/services';
import { useToast } from '../composables/useToast';
import type { AdminOverview, AdminReport, AdminReportPost, Comment, Post, User } from '../types';

const router = useRouter();
const route = useRoute();
const { showToast } = useToast();

const TAB_KEYS = ['overview', 'users', 'posts', 'comments', 'reports'] as const;
type AdminTab = typeof TAB_KEYS[number];

type ModerationDialog = {
  title: string;
  description: string;
  confirmLabel: string;
  successMessage: string;
  danger?: boolean;
  run: (reason: string) => Promise<void>;
};

const tabs: Array<{ key: AdminTab; label: string; icon: typeof LayoutDashboard }> = [
  { key: 'overview', label: '概览', icon: LayoutDashboard },
  { key: 'users', label: '用户', icon: UserRound },
  { key: 'posts', label: '帖子', icon: FileText },
  { key: 'comments', label: '评论', icon: MessageSquare },
  { key: 'reports', label: '举报', icon: Flag },
];

const overview = ref<AdminOverview | null>(null);
const reports = ref<AdminReport[]>([]);
const recentPosts = ref<Post[]>([]);
const users = ref<User[]>([]);
const posts = ref<Post[]>([]);
const comments = ref<Comment[]>([]);

const bootstrapping = ref(true);
const activeTab = ref<AdminTab>('overview');
const loadingTab = ref(false);
const refreshingOverview = ref(false);

const userQuery = ref('');
const userFilter = ref('');
const postQuery = ref('');
const postStatusFilter = ref('');
const commentQuery = ref('');
const reportStatusFilter = ref<'ALL' | 'OPEN' | 'RESOLVED'>('OPEN');

const dialog = ref<ModerationDialog | null>(null);
const dialogReason = ref('');
const submittingDialog = ref(false);

const normalizeTab = (value: unknown): AdminTab => (
  typeof value === 'string' && TAB_KEYS.includes(value as AdminTab) ? value as AdminTab : 'overview'
);

const setActiveTab = async (tab: AdminTab) => {
  const changed = activeTab.value !== tab;
  activeTab.value = tab;
  const nextQuery = { ...route.query };
  if (tab === 'overview') {
    delete nextQuery.tab;
  } else {
    nextQuery.tab = tab;
  }
  await router.replace({ path: '/admin', query: nextQuery });
  if (changed) {
    await loadActiveTab();
  }
};

const reportItems = computed(() => {
  if (reportStatusFilter.value === 'ALL') {
    return reports.value;
  }
  return reports.value.filter((item) => item.status === reportStatusFilter.value);
});

const openReportsCount = computed(() => reports.value.filter((item) => item.status === 'OPEN').length);

const tabDescription = computed(() => {
  switch (activeTab.value) {
    case 'users':
      return '搜索用户、查看状态，并执行封禁或禁言。';
    case 'posts':
      return '按状态查看帖子，并执行撤回或删除。';
    case 'comments':
      return '查看评论列表，并对违规评论执行删除。';
    case 'reports':
      return '处理举报并对违规帖子执行治理动作。';
    default:
      return '';
  }
});

const cardItems = computed(() => [
  {
    key: 'users' as const,
    label: '用户总数',
    value: overview.value?.usersCount ?? 0,
    helper: '查看用户状态与治理',
    icon: UserRound,
  },
  {
    key: 'posts' as const,
    label: '帖子总数',
    value: overview.value?.postsCount ?? 0,
    helper: '查看帖子状态与处理',
    icon: FileText,
  },
  {
    key: 'comments' as const,
    label: '评论总数',
    value: overview.value?.commentsCount ?? 0,
    helper: '查看评论并执行删除',
    icon: MessageSquare,
  },
  {
    key: 'reports' as const,
    label: '待处理举报',
    value: overview.value?.openReportsCount ?? 0,
    helper: '优先处理开放中的举报',
    icon: ShieldAlert,
  },
  {
    key: 'posts' as const,
    label: '总浏览量',
    value: overview.value?.viewsCount ?? 0,
    helper: '查看高曝光内容表现',
    icon: FileText,
  },
]);

const loadOverview = async () => {
  refreshingOverview.value = true;
  try {
    const [overviewData, reportData, recent] = await Promise.all([
      adminService.getOverview(),
      adminService.getReports(),
      adminService.getRecentPosts(6),
    ]);
    overview.value = overviewData;
    reports.value = reportData;
    recentPosts.value = recent;
  } catch (error) {
    showToast(error instanceof Error ? error.message : '加载后台概览失败，请稍后重试', 'error');
  } finally {
    refreshingOverview.value = false;
  }
};

const loadUsers = async () => {
  let status = '';
  let muteStatus = '';
  if (userFilter.value.startsWith('status:')) {
    status = userFilter.value.slice('status:'.length);
  } else if (userFilter.value.startsWith('mute:')) {
    muteStatus = userFilter.value.slice('mute:'.length);
  }
  users.value = await adminService.getUsers(userQuery.value, status, muteStatus);
};

const loadPosts = async () => {
  posts.value = await adminService.getPosts(postQuery.value, postStatusFilter.value);
};

const loadComments = async () => {
  comments.value = await adminService.getComments(commentQuery.value);
};

const loadReports = async () => {
  reports.value = await adminService.getReports();
};

const loadActiveTab = async () => {
  loadingTab.value = true;
  try {
    if (activeTab.value === 'users') {
      await loadUsers();
    } else if (activeTab.value === 'posts') {
      await loadPosts();
    } else if (activeTab.value === 'comments') {
      await loadComments();
    } else if (activeTab.value === 'reports') {
      await loadReports();
    }
  } catch (error) {
    showToast(error instanceof Error ? error.message : '加载后台数据失败，请稍后重试', 'error');
  } finally {
    loadingTab.value = false;
  }
};

const refreshDashboard = async () => {
  await loadOverview();
  await loadActiveTab();
};

const openPostDetail = (postId: string) => {
  router.push(`/post/${postId}`);
};

const openUserProfile = (username: string) => {
  router.push(`/profile/${username}`);
};

const openCommentThread = (postId: string) => {
  router.push(`/post/${postId}`);
};

const openDialog = (config: ModerationDialog) => {
  dialog.value = config;
  dialogReason.value = '';
};

const closeDialog = () => {
  if (submittingDialog.value) {
    return;
  }
  dialog.value = null;
  dialogReason.value = '';
};

const confirmDialog = async () => {
  if (!dialog.value) {
    return;
  }
  const reason = dialogReason.value.trim();
  if (!reason) {
    showToast('请填写处理原因', 'error');
    return;
  }

  submittingDialog.value = true;
  try {
    const successMessage = dialog.value.successMessage;
    await dialog.value.run(reason);
    dialog.value = null;
    dialogReason.value = '';
    showToast(successMessage, 'success');
    await refreshDashboard();
  } catch (error) {
    showToast(error instanceof Error ? error.message : '操作失败，请稍后重试', 'error');
  } finally {
    submittingDialog.value = false;
  }
};

const handleResolveReport = async (id: string) => {
  try {
    await adminService.resolveReport(id);
    showToast('举报已处理', 'success');
    await refreshDashboard();
  } catch (error) {
    showToast(error instanceof Error ? error.message : '处理举报失败，请稍后重试', 'error');
  }
};

const handleSearch = () => {
  loadActiveTab();
};

const formatTime = (value?: string) => {
  if (!value) {
    return '暂无';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

const postExcerpt = (content: string, limit = 120) => (
  content.length <= limit ? content : `${content.slice(0, limit)}...`
);

const formatMetric = (value: number) => {
  if (value >= 1000) {
    const compact = (value / 1000).toFixed(value >= 10000 ? 0 : 1).replace(/\.0$/, '');
    return `${compact}K`;
  }
  return String(value);
};

const statusBadgeClass = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'bg-emerald-50 text-emerald-700 ring-emerald-100 dark:bg-emerald-500/10 dark:text-emerald-300 dark:ring-emerald-500/20';
    case 'OPEN':
      return 'bg-amber-50 text-amber-700 ring-amber-100 dark:bg-amber-500/10 dark:text-amber-300 dark:ring-amber-500/20';
    case 'MUTED':
    case 'WITHDRAWN':
      return 'bg-orange-50 text-orange-700 ring-orange-100 dark:bg-orange-500/10 dark:text-orange-300 dark:ring-orange-500/20';
    case 'BANNED':
    case 'DELETED':
      return 'bg-red-50 text-red-700 ring-red-100 dark:bg-red-500/10 dark:text-red-300 dark:ring-red-500/20';
    case 'RESOLVED':
      return 'bg-slate-100 text-slate-700 ring-slate-200 dark:bg-slate-700/40 dark:text-slate-200 dark:ring-slate-600';
    default:
      return 'bg-slate-100 text-slate-700 ring-slate-200 dark:bg-slate-700/40 dark:text-slate-200 dark:ring-slate-600';
  }
};

const openBanDialog = (target: User, next: 'ban' | 'unban') => {
  openDialog({
    title: next === 'ban' ? '封禁用户' : '解除封禁',
    description: `对象：${target.nickname}（@${target.username}）`,
    confirmLabel: next === 'ban' ? '确认封禁' : '确认解封',
    successMessage: next === 'ban' ? '用户已封禁' : '用户已解封',
    danger: next === 'ban',
    run: (reason) => next === 'ban'
      ? adminService.banUser(target.id, reason)
      : adminService.unbanUser(target.id, reason),
  });
};

const openMuteDialog = (target: User, next: 'mute' | 'unmute') => {
  openDialog({
    title: next === 'mute' ? '禁言用户' : '解除禁言',
    description: `对象：${target.nickname}（@${target.username}）`,
    confirmLabel: next === 'mute' ? '确认禁言' : '确认解除',
    successMessage: next === 'mute' ? '用户已禁言' : '用户已解除禁言',
    danger: next === 'mute',
    run: (reason) => next === 'mute'
      ? adminService.muteUser(target.id, reason)
      : adminService.unmuteUser(target.id, reason),
  });
};

type ManagedPost = Pick<Post, 'id' | 'content' | 'status' | 'author'>;
type ManagedReportPost = Pick<AdminReportPost, 'id' | 'content' | 'status' | 'author'>;

const openWithdrawDialog = (target: ManagedPost | ManagedReportPost) => {
  openDialog({
    title: '撤回帖子',
    description: `对象：${target.author.nickname} 的帖子`,
    confirmLabel: '确认撤回',
    successMessage: '帖子已撤回',
    danger: true,
    run: (reason) => adminService.withdrawPost(target.id, reason),
  });
};

const openDeletePostDialog = (target: ManagedPost | ManagedReportPost) => {
  openDialog({
    title: '删除帖子',
    description: `对象：${target.author.nickname} 的帖子`,
    confirmLabel: '确认删除',
    successMessage: '帖子已删除',
    danger: true,
    run: (reason) => adminService.deletePost(target.id, reason),
  });
};

const openDeleteCommentDialog = (target: Comment) => {
  openDialog({
    title: '删除评论',
    description: `对象：${target.author.nickname} 的评论`,
    confirmLabel: '确认删除',
    successMessage: '评论已删除',
    danger: true,
    run: (reason) => adminService.deleteComment(target.id, reason),
  });
};

onMounted(async () => {
  activeTab.value = normalizeTab(route.query.tab);
  await loadOverview();
  await loadActiveTab();
  bootstrapping.value = false;
});

watch(
  () => route.query.tab,
  async (tab) => {
    const next = normalizeTab(tab);
    if (next === activeTab.value) {
      return;
    }
    activeTab.value = next;
    await loadActiveTab();
  },
);
</script>

<template>
  <div class="min-h-screen bg-bg-secondary/40">
    <div class="sticky top-0 z-10 border-b border-border bg-bg-primary/90 px-4 py-3 backdrop-blur-md">
      <div class="flex items-center gap-4">
        <button type="button" class="rounded-full p-2 hover:bg-bg-secondary" @click="router.push('/')">
          <ArrowLeft :size="20" />
        </button>
        <div class="min-w-0">
          <h1 class="text-xl font-bold">管理员控制台</h1>
          <p class="text-sm text-text-secondary">查看关键数据、处理举报，并执行用户与内容治理。</p>
        </div>
      </div>
    </div>

    <div class="space-y-6 p-4 sm:p-6">
      <div class="grid grid-cols-5 border-b border-border">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          class="inline-flex min-w-0 items-center justify-center gap-2 border-b-2 border-transparent px-4 py-4 text-base font-bold transition-colors"
          :class="activeTab === tab.key
            ? 'border-text-primary text-text-primary'
            : 'text-text-secondary hover:bg-bg-secondary/50'"
          @click="setActiveTab(tab.key)"
        >
          <component :is="tab.icon" :size="16" />
          {{ tab.label }}
        </button>
      </div>

      <div v-if="bootstrapping" class="flex justify-center py-14">
        <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
      </div>

      <template v-else>
        <section class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-5">
          <button
            v-for="card in cardItems"
            :key="card.key"
            type="button"
            class="rounded-[28px] border border-border bg-bg-primary p-5 text-left shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
            @click="setActiveTab(card.key)"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-sm font-medium text-text-secondary">{{ card.label }}</p>
                <p class="mt-3 text-4xl font-black tracking-tight">{{ formatMetric(card.value) }}</p>
                <p class="mt-3 text-sm text-text-secondary">{{ card.helper }}</p>
              </div>
              <div class="rounded-2xl bg-bg-secondary p-3">
                <component :is="card.icon" :size="20" />
              </div>
            </div>
          </button>
        </section>

        <section v-if="activeTab === 'overview'" class="grid gap-6 xl:grid-cols-[1.2fr,0.8fr]">
          <div class="rounded-[28px] border border-border bg-bg-primary p-5 shadow-sm">
            <div class="flex items-center justify-between gap-4">
              <div>
                <h2 class="text-lg font-bold">最近帖子</h2>
                <p class="mt-1 text-sm text-text-secondary">快速检查近期内容与潜在问题。</p>
              </div>
              <button
                type="button"
                class="rounded-full border border-border px-3 py-2 text-sm font-medium hover:bg-bg-secondary"
                @click="setActiveTab('posts')"
              >
                查看全部
              </button>
            </div>

            <div class="mt-5 space-y-3">
              <button
                v-for="post in recentPosts"
                :key="post.id"
                type="button"
                class="w-full rounded-3xl border border-border p-4 text-left transition hover:bg-bg-secondary/40"
                @click="openPostDetail(post.id)"
              >
                <div class="min-w-0">
                  <div class="flex flex-wrap items-center gap-2">
                    <span class="font-bold">{{ post.author.nickname }}</span>
                    <span class="text-sm text-text-secondary">@{{ post.author.username }}</span>
                    <span
                      class="inline-flex rounded-full px-2 py-1 text-xs font-semibold ring-1 ring-inset"
                      :class="statusBadgeClass(post.status)"
                    >
                      {{ post.status }}
                    </span>
                  </div>
                  <p class="mt-2 break-words text-sm leading-6">{{ postExcerpt(post.content, 110) }}</p>
                  <p class="mt-3 text-xs text-text-secondary">
                    {{ formatTime(post.createdAt) }} · {{ formatMetric(post.viewsCount) }} 浏览
                  </p>
                </div>
              </button>
              <div v-if="recentPosts.length === 0" class="rounded-3xl border border-dashed border-border p-6 text-sm text-text-secondary">
                暂无最近帖子。
              </div>
            </div>
          </div>

          <div class="rounded-[28px] border border-border bg-bg-primary p-5 shadow-sm">
            <div class="flex items-center justify-between gap-4">
              <div>
                <h2 class="text-lg font-bold">待处理举报</h2>
                <p class="mt-1 text-sm text-text-secondary">优先处理开放中的举报单。</p>
              </div>
              <button
                type="button"
                class="rounded-full border border-border px-3 py-2 text-sm font-medium hover:bg-bg-secondary"
                @click="setActiveTab('reports')"
              >
                查看全部
              </button>
            </div>

            <div class="mt-5 space-y-3">
              <div
                v-for="report in reportItems.slice(0, 4)"
                :key="report.id"
                class="rounded-3xl border border-border p-4"
              >
                <div class="flex items-start justify-between gap-4">
                  <div class="min-w-0">
                    <div class="flex flex-wrap items-center gap-2">
                      <span class="font-bold">{{ report.reporter.nickname }}</span>
                      <span class="text-sm text-text-secondary">@{{ report.reporter.username }}</span>
                      <span
                        class="inline-flex rounded-full px-2 py-1 text-xs font-semibold ring-1 ring-inset"
                        :class="statusBadgeClass(report.status)"
                      >
                        {{ report.status }}
                      </span>
                    </div>
                    <p class="mt-2 text-sm">类型：{{ report.category }}</p>
                    <p v-if="report.details" class="mt-1 break-words text-sm text-text-secondary">
                      {{ report.details }}
                    </p>
                  </div>
                  <button
                    v-if="report.status === 'OPEN'"
                    type="button"
                    class="rounded-full border border-border px-3 py-2 text-sm font-medium hover:bg-bg-secondary"
                    @click="handleResolveReport(report.id)"
                  >
                    处理
                  </button>
                </div>
              </div>
              <div v-if="openReportsCount === 0" class="rounded-3xl border border-dashed border-border p-6 text-sm text-text-secondary">
                当前没有待处理举报。
              </div>
            </div>
          </div>
        </section>

        <section v-else class="rounded-[28px] border border-border bg-bg-primary p-5 shadow-sm">
          <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <h2 class="text-lg font-bold">
                {{ tabs.find((item) => item.key === activeTab)?.label }}
              </h2>
              <p class="mt-1 text-sm text-text-secondary">{{ tabDescription }}</p>
            </div>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-full border border-border px-4 py-2 text-sm font-medium hover:bg-bg-secondary"
              @click="refreshDashboard"
            >
              <RefreshCcw :size="16" />
              刷新数据
            </button>
          </div>

          <div v-if="activeTab === 'users'" class="mt-5 space-y-4">
            <div class="grid gap-3 md:grid-cols-[1.4fr,1fr,auto]">
              <label class="relative">
                <Search class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary" :size="16" />
                <input
                  v-model="userQuery"
                  type="text"
                  placeholder="搜索用户名或昵称"
                  class="w-full rounded-full border border-border bg-bg-primary py-3 pl-11 pr-4 outline-none transition focus:border-text-primary"
                  @keyup.enter="handleSearch"
                />
              </label>
              <select v-model="userFilter" class="rounded-full border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary" @change="handleSearch">
                <option value="">全部筛选条件</option>
                <option value="status:ACTIVE">账号正常</option>
                <option value="status:BANNED">已封禁</option>
                <option value="mute:NORMAL">可发言</option>
                <option value="mute:MUTED">已禁言</option>
              </select>
              <button type="button" class="rounded-full bg-text-primary px-5 py-3 font-bold text-bg-primary hover:opacity-90" @click="handleSearch">
                查询
              </button>
            </div>

            <div v-if="loadingTab" class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="member in users"
                :key="member.id"
                class="rounded-3xl border border-border p-4 transition hover:bg-bg-secondary/30"
              >
                <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
                  <button type="button" class="flex min-w-0 flex-1 items-center gap-4 text-left" @click="openUserProfile(member.username)">
                    <img :src="member.avatar" :alt="member.nickname" class="h-12 w-12 rounded-full object-cover" />
                    <div class="min-w-0">
                      <div class="flex flex-wrap items-center gap-2">
                        <span class="truncate font-bold hover:underline">
                          {{ member.nickname }}
                        </span>
                        <span class="truncate text-sm text-text-secondary">@{{ member.username }}</span>
                        <span class="inline-flex rounded-full px-2 py-1 text-xs font-semibold ring-1 ring-inset" :class="statusBadgeClass(member.status)">
                          {{ member.status }}
                        </span>
                        <span class="inline-flex rounded-full px-2 py-1 text-xs font-semibold ring-1 ring-inset" :class="statusBadgeClass(member.muteStatus)">
                          {{ member.muteStatus }}
                        </span>
                        <span class="inline-flex rounded-full bg-bg-secondary px-2 py-1 text-xs font-semibold">{{ member.role }}</span>
                      </div>
                      <p class="mt-1 text-sm text-text-secondary">{{ member.bio || '这个用户还没有填写简介。' }}</p>
                      <p class="mt-2 text-xs text-text-secondary">
                        注册于 {{ formatTime(member.createdAt) }} · {{ member.followersCount }} 粉丝 · {{ member.followingCount }} 正在关注
                      </p>
                    </div>
                  </button>

                  <div class="flex flex-wrap gap-2">
                    <button
                      v-if="member.role !== 'ADMIN'"
                      type="button"
                      class="rounded-full border border-border px-4 py-2 text-sm font-medium hover:bg-bg-secondary"
                      @click="openBanDialog(member, member.status === 'BANNED' ? 'unban' : 'ban')"
                    >
                      <span class="inline-flex items-center gap-2">
                        <component :is="member.status === 'BANNED' ? CheckCircle2 : Ban" :size="16" />
                        {{ member.status === 'BANNED' ? '解除封禁' : '封禁账号' }}
                      </span>
                    </button>
                    <button
                      v-if="member.role !== 'ADMIN'"
                      type="button"
                      class="rounded-full border border-border px-4 py-2 text-sm font-medium hover:bg-bg-secondary"
                      @click="openMuteDialog(member, member.muteStatus === 'MUTED' ? 'unmute' : 'mute')"
                    >
                      <span class="inline-flex items-center gap-2">
                        <component :is="member.muteStatus === 'MUTED' ? Volume2 : VolumeX" :size="16" />
                        {{ member.muteStatus === 'MUTED' ? '解除禁言' : '禁言用户' }}
                      </span>
                    </button>
                  </div>
                </div>
              </div>
              <div v-if="users.length === 0" class="rounded-3xl border border-dashed border-border p-8 text-center text-text-secondary">
                没有匹配到任何用户。
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'posts'" class="mt-5 space-y-4">
            <div class="grid gap-3 md:grid-cols-[1.3fr,0.8fr,auto]">
              <label class="relative">
                <Search class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary" :size="16" />
                <input
                  v-model="postQuery"
                  type="text"
                  placeholder="搜索帖子内容或作者"
                  class="w-full rounded-full border border-border bg-bg-primary py-3 pl-11 pr-4 outline-none transition focus:border-text-primary"
                  @keyup.enter="handleSearch"
                />
              </label>
              <select v-model="postStatusFilter" class="rounded-full border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary" @change="handleSearch">
                <option value="">全部状态</option>
                <option value="ACTIVE">正常</option>
                <option value="WITHDRAWN">已撤回</option>
                <option value="DELETED">已删除</option>
              </select>
              <button type="button" class="rounded-full bg-text-primary px-5 py-3 font-bold text-bg-primary hover:opacity-90" @click="handleSearch">
                查询
              </button>
            </div>

            <div v-if="loadingTab" class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="post in posts"
                :key="post.id"
                class="rounded-3xl border border-border p-4 transition hover:bg-bg-secondary/30"
              >
                <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                  <button type="button" class="min-w-0 flex-1 text-left" @click="openPostDetail(post.id)">
                    <div class="flex flex-wrap items-center gap-2">
                      <span class="font-bold hover:underline">
                        {{ post.author.nickname }}
                      </span>
                      <span class="text-sm text-text-secondary">@{{ post.author.username }}</span>
                      <span class="inline-flex rounded-full px-2 py-1 text-xs font-semibold ring-1 ring-inset" :class="statusBadgeClass(post.status)">
                        {{ post.status }}
                      </span>
                    </div>
                    <p class="mt-2 break-words text-sm leading-6">{{ postExcerpt(post.content, 160) }}</p>
                    <p class="mt-3 text-xs text-text-secondary">
                      {{ formatTime(post.createdAt) }} · {{ formatMetric(post.viewsCount) }} 浏览 · {{ formatMetric(post.likesCount) }} 赞 · {{ formatMetric(post.commentsCount) }} 评论 · {{ formatMetric(post.repostsCount) }} 转发
                    </p>
                  </button>
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-if="post.status === 'ACTIVE'"
                      type="button"
                      class="rounded-full border border-border px-4 py-2 text-sm font-medium hover:bg-bg-secondary"
                      @click="openWithdrawDialog(post)"
                    >
                      撤回
                    </button>
                    <button
                      v-if="post.status !== 'DELETED'"
                      type="button"
                      class="rounded-full border border-red-200 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 dark:border-red-500/20 dark:hover:bg-red-500/10"
                      @click="openDeletePostDialog(post)"
                    >
                      删除
                    </button>
                  </div>
                </div>
              </div>
              <div v-if="posts.length === 0" class="rounded-3xl border border-dashed border-border p-8 text-center text-text-secondary">
                没有匹配到任何帖子。
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'comments'" class="mt-5 space-y-4">
            <div class="grid gap-3 md:grid-cols-[1fr,auto]">
              <label class="relative">
                <Search class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary" :size="16" />
                <input
                  v-model="commentQuery"
                  type="text"
                  placeholder="搜索评论内容或作者"
                  class="w-full rounded-full border border-border bg-bg-primary py-3 pl-11 pr-4 outline-none transition focus:border-text-primary"
                  @keyup.enter="handleSearch"
                />
              </label>
              <button type="button" class="rounded-full bg-text-primary px-5 py-3 font-bold text-bg-primary hover:opacity-90" @click="handleSearch">
                查询
              </button>
            </div>

            <div v-if="loadingTab" class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="comment in comments"
                :key="comment.id"
                class="rounded-3xl border border-border p-4 transition hover:bg-bg-secondary/30"
              >
                <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                  <button type="button" class="min-w-0 flex-1 text-left" @click="openCommentThread(comment.postId)">
                    <div class="flex flex-wrap items-center gap-2">
                      <span class="font-bold hover:underline">
                        {{ comment.author.nickname }}
                      </span>
                      <span class="text-sm text-text-secondary">@{{ comment.author.username }}</span>
                    </div>
                    <p class="mt-2 break-words text-sm leading-6">{{ comment.content }}</p>
                    <p class="mt-3 text-xs text-text-secondary">{{ formatTime(comment.createdAt) }}</p>
                  </button>
                  <button
                    type="button"
                    class="shrink-0 rounded-full border border-red-200 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 dark:border-red-500/20 dark:hover:bg-red-500/10"
                    @click="openDeleteCommentDialog(comment)"
                  >
                    <span class="inline-flex items-center gap-2">
                      <Trash2 :size="16" />
                      删除评论
                    </span>
                  </button>
                </div>
              </div>
              <div v-if="comments.length === 0" class="rounded-3xl border border-dashed border-border p-8 text-center text-text-secondary">
                没有匹配到任何评论。
              </div>
            </div>
          </div>

          <div v-else class="mt-5 space-y-4">
            <div class="grid gap-3 md:grid-cols-[1fr,auto]">
              <select v-model="reportStatusFilter" class="rounded-full border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary">
                <option value="OPEN">只看待处理</option>
                <option value="RESOLVED">只看已处理</option>
                <option value="ALL">全部举报</option>
              </select>
              <button type="button" class="rounded-full bg-text-primary px-5 py-3 font-bold text-bg-primary hover:opacity-90" @click="loadReports">
                刷新举报
              </button>
            </div>

            <div v-if="loadingTab" class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="report in reportItems"
                :key="report.id"
                class="rounded-3xl border border-border p-4"
              >
                <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                  <div class="min-w-0">
                    <div class="flex flex-wrap items-center gap-2">
                      <span class="font-bold">{{ report.reporter.nickname }}</span>
                      <span class="text-sm text-text-secondary">@{{ report.reporter.username }}</span>
                      <span class="inline-flex rounded-full px-2 py-1 text-xs font-semibold ring-1 ring-inset" :class="statusBadgeClass(report.status)">
                        {{ report.status }}
                      </span>
                      <span class="inline-flex rounded-full bg-bg-secondary px-2 py-1 text-xs font-semibold">
                        {{ report.category }}
                      </span>
                    </div>
                    <p class="mt-2 text-sm text-text-secondary">提交时间：{{ formatTime(report.createdAt) }}</p>
                    <p v-if="report.details" class="mt-2 break-words text-sm leading-6">
                      {{ report.details }}
                    </p>
                    <button
                      v-if="report.post"
                      type="button"
                      class="mt-3 block w-full rounded-2xl bg-bg-secondary px-4 py-3 text-left transition hover:bg-bg-secondary/70"
                      @click="openPostDetail(report.post.id)"
                    >
                      <p class="text-xs text-text-secondary">
                        帖子状态：
                        <span class="font-semibold text-text-primary">{{ report.post.status }}</span>
                      </p>
                      <p class="mt-2 break-words text-sm leading-6">{{ postExcerpt(report.post.content, 150) }}</p>
                      <p class="mt-2 text-xs text-text-secondary">
                        作者：{{ report.post.author.nickname }} · {{ formatTime(report.post.createdAt) }}
                      </p>
                    </button>
                    <p v-if="report.resolvedBy" class="mt-3 text-xs text-text-secondary">
                      处理人：{{ report.resolvedBy.nickname }} · {{ formatTime(report.resolvedAt) }}
                    </p>
                  </div>

                  <div class="flex shrink-0 flex-col items-stretch gap-2 xl:w-44">
                    <button
                      v-if="report.status === 'OPEN'"
                      type="button"
                      class="inline-flex w-full items-center justify-center gap-2 whitespace-nowrap rounded-full border border-border px-4 py-2 text-sm font-medium hover:bg-bg-secondary"
                      @click="handleResolveReport(report.id)"
                    >
                      <CheckCircle2 :size="16" />
                      标记处理
                    </button>
                    <button
                      v-if="report.status === 'OPEN' && report.post && report.post.status === 'ACTIVE'"
                      type="button"
                      class="inline-flex w-full items-center justify-center whitespace-nowrap rounded-full border border-border px-4 py-2 text-sm font-medium hover:bg-bg-secondary"
                      @click="openWithdrawDialog(report.post)"
                    >
                      撤回帖子
                    </button>
                    <button
                      v-if="report.status === 'OPEN' && report.post && report.post.status !== 'DELETED'"
                      type="button"
                      class="inline-flex w-full items-center justify-center whitespace-nowrap rounded-full border border-red-200 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 dark:border-red-500/20 dark:hover:bg-red-500/10"
                      @click="openDeletePostDialog(report.post)"
                    >
                      删除帖子
                    </button>
                  </div>
                </div>
              </div>
              <div v-if="reportItems.length === 0" class="rounded-3xl border border-dashed border-border p-8 text-center text-text-secondary">
                当前筛选条件下没有举报记录。
              </div>
            </div>
          </div>
        </section>
      </template>
    </div>

    <div
      v-if="dialog"
      class="fixed inset-0 z-40 flex items-center justify-center bg-black/45 px-4"
      @click.self="closeDialog"
    >
      <div class="w-full max-w-xl rounded-[28px] border border-border bg-bg-primary p-6 shadow-2xl">
        <div class="flex items-start justify-between gap-4">
          <div>
            <h3 class="text-xl font-bold">{{ dialog.title }}</h3>
            <p class="mt-2 text-sm text-text-secondary">{{ dialog.description }}</p>
          </div>
          <button type="button" class="rounded-full px-3 py-2 text-sm hover:bg-bg-secondary" @click="closeDialog">
            取消
          </button>
        </div>

        <div class="mt-6">
          <label class="block">
            <span class="mb-2 block text-sm font-bold">处理原因</span>
            <textarea
              v-model="dialogReason"
              rows="5"
              maxlength="255"
              placeholder="请填写原因，这会通过系统通知发送给对方。"
              class="w-full rounded-3xl border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary"
            ></textarea>
          </label>
        </div>

        <div class="mt-6 flex justify-end gap-3">
          <button
            type="button"
            class="rounded-full border border-border px-4 py-2 font-medium hover:bg-bg-secondary"
            @click="closeDialog"
          >
            取消
          </button>
          <button
            type="button"
            class="rounded-full px-5 py-2.5 font-bold text-bg-primary transition disabled:opacity-60"
            :class="dialog.danger ? 'bg-red-600 hover:bg-red-700' : 'bg-text-primary hover:opacity-90'"
            :disabled="submittingDialog"
            @click="confirmDialog"
          >
            {{ submittingDialog ? '提交中...' : dialog.confirmLabel }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
