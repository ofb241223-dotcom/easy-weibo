<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, MessageCircle, Search } from 'lucide-vue-next';
import { chatService, userService } from '../api/services';
import { useAuth } from '../composables/useAuth';
import { useToast } from '../composables/useToast';
import type { Conversation, User } from '../types';

const router = useRouter();
const { user } = useAuth();
const { showToast } = useToast();

const conversations = ref<Conversation[]>([]);
const candidates = ref<User[]>([]);
const loading = ref(true);
const query = ref('');

const loadData = async () => {
  if (!user.value) {
    return;
  }

  loading.value = true;
  try {
    const [conversationList, mutuals] = await Promise.all([
      chatService.getConversations(),
      userService.getRelationshipList(user.value.id, 'mutual'),
    ]);

    conversations.value = conversationList;

    const byId = new Map<string, User>();
    for (const member of mutuals) {
      byId.set(member.id, member);
    }
    for (const conversation of conversationList) {
      byId.set(conversation.targetUser.id, conversation.targetUser);
    }
    candidates.value = Array.from(byId.values());
  } catch (error) {
    showToast(error instanceof Error ? error.message : '加载聊天列表失败，请稍后重试', 'error');
  } finally {
    loading.value = false;
  }
};

onMounted(loadData);

const filteredCandidates = computed(() => {
  const keyword = query.value.trim().toLowerCase();
  if (!keyword) {
    return candidates.value;
  }
  return candidates.value.filter((member) => (
    member.nickname.toLowerCase().includes(keyword) || member.username.toLowerCase().includes(keyword)
  ));
});

const openConversation = (conversationId: string) => {
  router.push(`/chat/${conversationId}`);
};

const startConversation = async (target: User) => {
  try {
    const conversation = await chatService.createConversation(target.id);
    await loadData();
    await router.push(`/chat/${conversation.id}`);
  } catch (error) {
    showToast(error instanceof Error ? error.message : '创建会话失败，请稍后重试', 'error');
  }
};
</script>

<template>
  <div class="min-h-screen bg-bg-secondary/30">
    <div class="sticky top-0 z-10 border-b border-border bg-bg-primary/90 px-4 py-3 backdrop-blur-md">
      <div class="flex items-center gap-4">
        <button type="button" class="rounded-full p-2 hover:bg-bg-secondary" @click="router.back()">
          <ArrowLeft :size="20" />
        </button>
        <div>
          <h1 class="text-xl font-bold">聊天</h1>
        </div>
      </div>
    </div>

    <div class="space-y-4 px-4 py-5">
      <label class="relative block">
        <Search class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary" :size="18" />
        <input
          v-model="query"
          type="text"
          placeholder="搜索用户并开始聊天"
          class="w-full rounded-full border border-border bg-bg-primary py-4 pl-12 pr-4 text-lg outline-none transition focus:border-text-primary"
        />
      </label>

      <div v-if="loading" class="flex justify-center py-12">
        <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
      </div>

      <template v-else>
        <section class="rounded-[32px] border border-border bg-bg-primary shadow-sm">
          <div class="border-b border-border px-5 py-4">
            <h2 class="text-lg font-bold">可开始聊天的联系人</h2>
          </div>

          <div v-if="filteredCandidates.length === 0" class="px-5 py-8 text-sm text-text-secondary">
            这里只显示互关好友和已有聊天对象。若想联系陌生人，请先进入对方主页点击“聊天”。
          </div>

          <button
            v-for="member in filteredCandidates"
            :key="member.id"
            type="button"
            class="flex w-full items-center gap-4 border-t border-border px-5 py-4 text-left transition first:border-t-0 hover:bg-bg-secondary/40"
            @click="startConversation(member)"
          >
            <img :src="member.avatar" :alt="member.nickname" class="h-14 w-14 rounded-full object-cover" />
            <div class="min-w-0 flex-1">
              <p class="truncate text-lg font-bold">{{ member.nickname }}</p>
              <p class="truncate text-sm text-text-secondary">@{{ member.username }}</p>
            </div>
            <span class="rounded-full border border-border px-4 py-2 text-sm font-medium">聊天</span>
          </button>
        </section>

        <section class="rounded-[32px] border border-border bg-bg-primary shadow-sm">
          <div class="border-b border-border px-5 py-4">
            <h2 class="text-lg font-bold">最近会话</h2>
          </div>

          <div v-if="conversations.length === 0" class="px-5 py-8 text-sm text-text-secondary">
            还没有聊天记录。可以先从上面的联系人列表，或从对方主页开始一段对话。
          </div>

          <div v-else class="max-h-[calc(100vh-260px)] overflow-y-auto">
            <button
              v-for="conversation in conversations"
              :key="conversation.id"
              type="button"
              class="flex w-full items-center gap-4 border-t border-border px-5 py-4 text-left transition first:border-t-0 hover:bg-bg-secondary/40"
              @click="openConversation(conversation.id)"
            >
              <img :src="conversation.targetUser.avatar" :alt="conversation.targetUser.nickname" class="h-14 w-14 rounded-full object-cover" />
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between gap-3">
                  <p class="truncate text-lg font-bold">{{ conversation.targetUser.nickname }}</p>
                  <span class="shrink-0 text-sm text-text-secondary">{{ conversation.lastMessageAt.slice(5, 16).replace('T', ' ') }}</span>
                </div>
                <div class="mt-1 flex items-center justify-between gap-3">
                  <p class="truncate text-sm text-text-secondary">{{ conversation.lastMessage || '还没有聊天记录' }}</p>
                  <span
                    v-if="conversation.unreadCount > 0"
                    class="inline-flex min-w-[22px] items-center justify-center rounded-full bg-text-primary px-2 py-1 text-[11px] font-bold text-bg-primary"
                  >
                    {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
                  </span>
                </div>
              </div>
            </button>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>
