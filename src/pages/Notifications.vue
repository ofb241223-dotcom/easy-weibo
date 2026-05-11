<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { notificationService } from '../api/services';
import { Notification } from '../types';
import { Heart, MessageCircle, UserPlus, AtSign, ArrowLeft, ShieldAlert } from 'lucide-vue-next';
import { formatDistanceToNow } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { useToast } from '../composables/useToast';
import { refreshUnreadCount, useNotifications } from '../composables/useNotifications';

const router = useRouter();
const notifications = ref<Notification[]>([]);
const loading = ref(true);
const markingAll = ref(false);
const { showToast } = useToast();
const { clearUnreadCount, decrementUnreadCount } = useNotifications();

const unreadCount = computed(() => notifications.value.filter((item) => !item.read).length);

const loadNotifications = async () => {
  try {
    notifications.value = await notificationService.getNotifications();
    if (notifications.value.some((item) => !item.read)) {
      await refreshUnreadCount();
    } else {
      clearUnreadCount();
    }
  } catch (error) {
    notifications.value = [];
    showToast(error instanceof Error ? error.message : '加载通知失败，请稍后重试', 'error');
  } finally {
    loading.value = false;
  }
};

onMounted(loadNotifications);

const getIcon = (type: Notification['type']) => {
  switch (type) {
    case 'like': return Heart;
    case 'comment': return MessageCircle;
    case 'follow': return UserPlus;
    case 'mention': return AtSign;
    case 'system': return ShieldAlert;
  }
};

const getMessage = (notification: Notification) => {
  if (notification.type === 'system') {
    return notification.message || '系统通知';
  }

  switch (notification.type) {
    case 'like': return '赞了你的帖子';
    case 'comment': return '回复了你的帖子';
    case 'follow': return '关注了你';
    case 'mention': return '在帖子中提到了你';
  }
};

const handleNotificationClick = async (n: Notification) => {
  await notificationService.markAsRead(n.id);
  if (!n.read) {
    n.read = true;
    decrementUnreadCount();
  }
  if (n.actionUrl) router.push(n.actionUrl);
  else if (n.postId) router.push(`/post/${n.postId}`);
  else router.push(`/profile/${n.fromUser.username}`);
};

const handleMarkAllAsRead = async () => {
  if (!unreadCount.value) {
    return;
  }

  markingAll.value = true;
  try {
    await notificationService.markAllAsRead();
    notifications.value = notifications.value.map((item) => ({ ...item, read: true }));
    clearUnreadCount();
    showToast('已将所有通知标记为已读', 'success');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '操作失败，请稍后重试', 'error');
  } finally {
    markingAll.value = false;
  }
};
</script>

<template>
  <div class="min-h-screen">
    <div class="sticky top-0 bg-bg-primary/80 backdrop-blur-md z-10 px-4 py-2 border-b border-border flex items-center gap-6">
      <button @click="router.back()" class="p-2 hover:bg-bg-secondary rounded-full">
        <ArrowLeft :size="20" />
      </button>
      <div class="flex min-w-0 flex-1 items-center justify-between gap-4">
        <h1 class="text-xl font-bold">通知</h1>
        <button
          v-if="unreadCount > 0"
          type="button"
          class="rounded-full border border-border px-3 py-1.5 text-sm font-medium hover:bg-bg-secondary disabled:opacity-60"
          :disabled="markingAll"
          @click="handleMarkAllAsRead"
        >
          {{ markingAll ? '处理中...' : '全部已读' }}
        </button>
      </div>
    </div>

    <div class="divide-y divide-border">
      <div v-if="loading" class="flex justify-center p-10">
        <div class="w-8 h-8 border-4 border-brand border-t-transparent rounded-full animate-spin" />
      </div>
      <template v-else-if="notifications.length > 0">
        <div 
          v-for="n in notifications" 
          :key="n.id" 
          :class="['p-4 flex gap-4 hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition-colors', !n.read ? 'bg-brand/5' : '']"
          @click="handleNotificationClick(n)"
        >
          <div class="mt-1">
            <component :is="getIcon(n.type)" :size="24" class="text-brand" />
          </div>
          <div class="flex-1 space-y-2">
            <img :src="n.fromUser.avatar" :alt="n.fromUser.nickname" class="w-8 h-8 rounded-full object-cover" />
            <p>
              <template v-if="n.type === 'system'">
                <span class="font-bold">{{ n.fromUser.nickname }}</span> {{ getMessage(n) }}
              </template>
              <template v-else>
                <span class="font-bold">{{ n.fromUser.nickname }}</span> {{ getMessage(n) }}
              </template>
            </p>
            <p class="text-text-secondary text-sm">
              {{ formatDistanceToNow(new Date(n.createdAt), { addSuffix: true, locale: zhCN }) }}
            </p>
            <button
              v-if="n.actionLabel && n.actionUrl"
              type="button"
              class="rounded-full border border-border px-3 py-1.5 text-sm font-medium hover:bg-bg-secondary"
              @click.stop="handleNotificationClick(n)"
            >
              {{ n.actionLabel }}
            </button>
          </div>
        </div>
      </template>
      <div v-else class="p-10 text-center text-text-secondary">
        <p class="text-xl font-bold text-text-primary mb-2">暂无通知</p>
        <p>当有人与你互动时，你会在这里看到通知。</p>
      </div>
    </div>
  </div>
</template>
