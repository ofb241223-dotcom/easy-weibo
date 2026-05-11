<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ArrowLeft,
  Ban,
  Download,
  FileUp,
  ImagePlus,
  MoreHorizontal,
  Send,
  SmilePlus,
  Undo2,
  Video,
  X,
} from 'lucide-vue-next';
import type { ApiConversationMessage } from '../api/services';
import { chatService, mapConversationMessage, uploadService } from '../api/services';
import { useAuth } from '../composables/useAuth';
import { useChatSocket } from '../composables/useChatSocket';
import { useToast } from '../composables/useToast';
import type { ConversationDetail, ConversationMessage } from '../types';
import ImageLightbox from '../components/ImageLightbox.vue';

const route = useRoute();
const router = useRouter();
const { user } = useAuth();
const { showToast } = useToast();

const conversationId = ref(String(route.params.id || ''));
const detail = ref<ConversationDetail | null>(null);
const loading = ref(true);
const sending = ref(false);
const uploadingAttachment = ref(false);
const recallingId = ref<string | null>(null);
const togglingBlock = ref(false);
const emojiOpen = ref(false);
const composing = ref('');
const messagesContainer = ref<HTMLElement | null>(null);
const bottomAnchor = ref<HTMLElement | null>(null);
const attachmentInput = ref<HTMLInputElement | null>(null);
const previewImages = ref<string[]>([]);
const previewIndex = ref(0);
const shouldStickToBottom = ref(true);

const emojiPalette = ['😀', '😂', '🥳', '👍', '👏', '🔥', '✨', '🎉', '❤️', '🙏', '🤝', '📚', '💻', '😄', '😮', '🥲', '😎', '👀'];
const attachmentAccept = 'image/*,.pdf,.doc,.docx,.xls,.xlsx,.zip,.txt';

const waitForLayout = async () => {
  await nextTick();
  await new Promise<void>((resolve) => requestAnimationFrame(() => resolve()));
};

const scrollToBottom = async (behavior: ScrollBehavior = 'smooth') => {
  await waitForLayout();
  bottomAnchor.value?.scrollIntoView({ behavior, block: 'end' });
  const element = messagesContainer.value;
  if (element) {
    element.scrollTo({ top: element.scrollHeight, behavior });
    if (behavior === 'auto') {
      element.scrollTop = element.scrollHeight;
    }
  }
};

type LoadDetailOptions = {
  showLoading?: boolean;
  preserveScroll?: boolean;
};

const syncStickToBottom = () => {
  const element = messagesContainer.value;
  if (!element) {
    shouldStickToBottom.value = true;
    return;
  }
  const distanceToBottom = element.scrollHeight - element.scrollTop - element.clientHeight;
  shouldStickToBottom.value = distanceToBottom < 96;
};

const handleMessagesScroll = () => {
  syncStickToBottom();
};

const handleMediaLoaded = async () => {
  if (shouldStickToBottom.value) {
    await scrollToBottom('auto');
  }
};

const upsertMessage = async (incoming: ConversationMessage) => {
  if (!detail.value) {
    return;
  }
  const current = [...detail.value.messages];
  const existingIndex = current.findIndex((item) => item.id === incoming.id);
  if (existingIndex >= 0) {
    current.splice(existingIndex, 1, incoming);
  } else {
    current.push(incoming);
    current.sort((left, right) => new Date(left.createdAt).getTime() - new Date(right.createdAt).getTime());
  }
  detail.value = {
    ...detail.value,
    lastMessage: incoming.content,
    lastMessageAt: incoming.createdAt,
    messages: current,
  };
  await scrollToBottom('auto');
};

const loadDetail = async (options: LoadDetailOptions = {}) => {
  if (!conversationId.value) {
    detail.value = null;
    return;
  }

  const showLoading = options.showLoading ?? true;
  const preserveScroll = options.preserveScroll ?? false;
  const previousScrollTop = messagesContainer.value?.scrollTop ?? 0;

  if (showLoading) {
    loading.value = true;
  }
  try {
    detail.value = await chatService.getConversation(conversationId.value);
    if (detail.value.unreadCount > 0) {
      await chatService.markConversationRead(conversationId.value);
      detail.value = await chatService.getConversation(conversationId.value);
    }
    if (showLoading || shouldStickToBottom.value) {
      await scrollToBottom('auto');
    } else if (preserveScroll && messagesContainer.value) {
      await waitForLayout();
      messagesContainer.value.scrollTop = previousScrollTop;
    }
    syncStickToBottom();
  } catch (error) {
    if (showLoading) {
      detail.value = null;
      showToast(error instanceof Error ? error.message : '加载会话失败，请稍后重试', 'error');
    }
  } finally {
    if (showLoading) {
      loading.value = false;
    }
  }
};

useChatSocket(conversationId, {
  onMessageCreated: async (message) => {
    await upsertMessage(mapConversationMessage(message as ApiConversationMessage));
    if (message.senderId !== user.value?.id) {
      try {
        await chatService.markConversationRead(conversationId.value);
      } catch {
        // Ignore realtime read failures and leave the optimistic view intact.
      }
    }
  },
  onMessageRecalled: async (message) => {
    await upsertMessage(mapConversationMessage(message as ApiConversationMessage));
  },
  onConversationInvalidated: async () => {
    await loadDetail({ showLoading: false, preserveScroll: true });
  },
});

onMounted(() => {
  void loadDetail();
});

watch(
  () => route.params.id,
  async (value) => {
    conversationId.value = String(value || '');
    await loadDetail();
  },
);

const handleSend = async () => {
  if (!detail.value || !detail.value.canSend || !composing.value.trim()) {
    return;
  }

  shouldStickToBottom.value = true;
  sending.value = true;
  try {
    const message = await chatService.sendMessage(detail.value.id, composing.value.trim());
    composing.value = '';
    emojiOpen.value = false;
    await upsertMessage(message);
  } catch (error) {
    showToast(error instanceof Error ? error.message : '发送失败，请稍后重试', 'error');
  } finally {
    sending.value = false;
  }
};

const handleAttachmentClick = () => {
  attachmentInput.value?.click();
};

const handleAttachmentSelected = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';

  if (!file || !detail.value || !detail.value.canSend) {
    return;
  }

  shouldStickToBottom.value = true;
  uploadingAttachment.value = true;
  try {
    const uploaded = await uploadService.uploadChatAttachment(file);
    const message = await chatService.sendAttachment(detail.value.id, uploaded);
    await upsertMessage(message);
  } catch (error) {
    showToast(error instanceof Error ? error.message : '发送附件失败，请稍后重试', 'error');
  } finally {
    uploadingAttachment.value = false;
  }
};

const recallMessage = async (messageId: string) => {
  recallingId.value = messageId;
  try {
    const message = await chatService.recallMessage(messageId);
    await upsertMessage(message);
  } catch (error) {
    showToast(error instanceof Error ? error.message : '撤回消息失败，请稍后重试', 'error');
  } finally {
    recallingId.value = null;
  }
};

const toggleBlock = async () => {
  if (!detail.value) {
    return;
  }
  togglingBlock.value = true;
  try {
    if (detail.value.blockedByCurrentUser) {
      await chatService.unblockUser(detail.value.targetUser.id);
      showToast('已解除拉黑', 'success');
    } else {
      await chatService.blockUser(detail.value.targetUser.id);
      showToast('已拉黑该用户', 'success');
    }
    await loadDetail();
  } catch (error) {
    showToast(error instanceof Error ? error.message : '操作失败，请稍后重试', 'error');
  } finally {
    togglingBlock.value = false;
  }
};

const insertEmoji = (emoji: string) => {
  composing.value += emoji;
};

const openImagePreview = (image: string) => {
  previewImages.value = [image];
  previewIndex.value = 0;
};

const formatMessageTime = (value: string) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

const canSend = computed(() => detail.value?.canSend ?? false);
</script>

<template>
  <div class="min-h-screen bg-bg-primary">
    <div class="sticky top-0 z-10 border-b border-border bg-bg-primary/95 px-4 py-3 backdrop-blur-md">
      <div class="flex items-center justify-between gap-4">
        <div class="flex min-w-0 items-center gap-3">
          <button type="button" class="rounded-full p-2 hover:bg-bg-secondary" @click="router.push('/chat')">
            <ArrowLeft :size="22" />
          </button>

          <template v-if="detail">
            <img :src="detail.targetUser.avatar" :alt="detail.targetUser.nickname" class="h-11 w-11 rounded-full object-cover" />
            <div class="min-w-0">
              <button type="button" class="truncate text-left font-bold hover:underline" @click="router.push(`/profile/${detail.targetUser.username}`)">
                {{ detail.targetUser.nickname }}
              </button>
              <p class="truncate text-sm text-text-secondary">@{{ detail.targetUser.username }}</p>
            </div>
          </template>
          <template v-else>
            <h1 class="text-xl font-bold">聊天</h1>
          </template>
        </div>

        <div class="flex items-center gap-2">
          <button type="button" class="rounded-full p-2 hover:bg-bg-secondary">
            <Video :size="20" />
          </button>
          <button type="button" class="rounded-full p-2 hover:bg-bg-secondary" :disabled="togglingBlock" @click="toggleBlock">
            <Ban :size="20" />
          </button>
          <button type="button" class="rounded-full p-2 hover:bg-bg-secondary">
            <MoreHorizontal :size="20" />
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex min-h-[calc(100vh-72px)] justify-center py-16">
      <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
    </div>

    <template v-else-if="detail">
      <div ref="messagesContainer" class="h-[calc(100vh-250px)] overflow-y-auto bg-bg-secondary/30 px-4 py-5 pb-8" @scroll="handleMessagesScroll">
        <div class="mx-auto max-w-4xl space-y-6">
          <article
            v-for="message in detail.messages"
            :key="message.id"
            class="space-y-2"
          >
            <div class="flex items-start" :class="message.senderId === user?.id ? 'justify-end' : 'justify-start'">
              <div class="flex max-w-[78%] items-start gap-3" :class="message.senderId === user?.id ? 'flex-row-reverse' : ''">
                <img :src="message.sender.avatar" :alt="message.sender.nickname" class="h-10 w-10 rounded-full object-cover" />
                <div class="flex max-w-full flex-col" :class="message.senderId === user?.id ? 'items-end' : 'items-start'">
                  <div class="mb-2 flex items-center gap-3 px-1 text-xs text-text-secondary" :class="message.senderId === user?.id ? 'justify-end' : ''">
                    <span>{{ formatMessageTime(message.createdAt) }}</span>
                    <button
                      v-if="message.canRecall"
                      type="button"
                      class="inline-flex items-center gap-1 hover:text-text-primary"
                      :disabled="recallingId === message.id"
                      @click="recallMessage(message.id)"
                    >
                      <Undo2 :size="12" />
                      {{ recallingId === message.id ? '撤回中' : '撤回' }}
                    </button>
                    <span v-else-if="message.recalled" class="inline-flex items-center gap-1">
                      <Undo2 :size="12" />
                      已撤回
                    </span>
                  </div>

                  <div
                    class="inline-block max-w-full rounded-[24px] px-4 py-3 shadow-sm"
                    :class="message.senderId === user?.id ? 'bg-text-primary text-bg-primary' : message.recalled ? 'border border-border bg-bg-primary text-text-secondary' : 'border border-border bg-bg-primary text-text-primary'"
                  >
                    <template v-if="message.messageType === 'IMAGE' && message.fileUrl && !message.recalled">
                      <button type="button" class="block overflow-hidden rounded-2xl" @click="openImagePreview(message.fileUrl)">
                        <img :src="message.fileUrl" :alt="message.fileName || '聊天图片'" class="max-h-72 rounded-2xl object-cover transition hover:scale-[1.01]" @load="handleMediaLoaded" />
                      </button>
                      <p v-if="message.content" class="mt-3 whitespace-pre-wrap break-words text-[15px] leading-7">{{ message.content }}</p>
                    </template>
                    <template v-else-if="message.messageType === 'FILE' && message.fileUrl && !message.recalled">
                      <a :href="message.fileUrl" target="_blank" rel="noreferrer" class="flex items-center gap-3 rounded-2xl border border-current/15 px-3 py-3">
                        <FileUp :size="18" />
                        <div class="min-w-0">
                          <p class="truncate font-bold">{{ message.fileName || '附件' }}</p>
                          <p class="truncate text-xs opacity-80">{{ message.mimeType || '文件消息' }}</p>
                        </div>
                        <Download :size="16" class="shrink-0" />
                      </a>
                      <p v-if="message.content" class="mt-3 whitespace-pre-wrap break-words text-[15px] leading-7">{{ message.content }}</p>
                    </template>
                    <template v-else>
                      <p class="whitespace-pre-wrap break-words text-[15px] leading-7">{{ message.content }}</p>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </article>
          <div ref="bottomAnchor" class="h-px w-full" />
        </div>
      </div>

      <div class="border-t border-border bg-bg-primary px-4 py-3">
        <div class="mx-auto max-w-4xl rounded-[28px] border border-border bg-bg-primary shadow-sm">
          <div class="flex items-center gap-2 border-b border-border px-4 py-3">
            <button
              type="button"
              class="inline-flex h-11 w-11 items-center justify-center rounded-full border border-border hover:bg-bg-secondary disabled:opacity-60"
              :disabled="!canSend || uploadingAttachment"
              @click="emojiOpen = !emojiOpen"
            >
              <SmilePlus :size="18" />
            </button>
            <button
              type="button"
              class="inline-flex h-11 w-11 items-center justify-center rounded-full border border-border hover:bg-bg-secondary disabled:opacity-60"
              :disabled="!canSend || uploadingAttachment"
              @click="handleAttachmentClick"
            >
              <ImagePlus :size="18" />
            </button>
            <button
              type="button"
              class="inline-flex h-11 w-11 items-center justify-center rounded-full border border-border hover:bg-bg-secondary disabled:opacity-60"
              :disabled="!canSend || uploadingAttachment"
              @click="handleAttachmentClick"
            >
              <FileUp :size="18" />
            </button>
            <input ref="attachmentInput" type="file" class="hidden" :accept="attachmentAccept" @change="handleAttachmentSelected" />
          </div>

          <div
            v-if="emojiOpen"
            class="border-b border-border p-4"
          >
            <div class="mb-3 flex items-center justify-between">
              <p class="text-sm font-bold">选择表情</p>
              <button type="button" class="rounded-full p-1 hover:bg-bg-secondary" @click="emojiOpen = false">
                <X :size="14" />
              </button>
            </div>
            <div class="grid grid-cols-7 gap-2">
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

          <div v-if="!canSend" class="border-b border-border px-4 py-3 text-sm text-text-secondary">
            {{ detail.restrictionReason }}
          </div>

          <div class="flex items-end gap-3 px-4 py-4">
            <textarea
              v-model="composing"
              rows="3"
              maxlength="2000"
              placeholder="发送消息"
              class="min-h-[96px] flex-1 resize-none bg-transparent text-[15px] outline-none disabled:bg-bg-secondary"
              :disabled="!canSend || sending || uploadingAttachment"
              @keydown.enter.exact.prevent="handleSend"
            />
            <button
              type="button"
              class="inline-flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-text-primary text-bg-primary shadow-sm hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="!canSend || !composing.trim() || sending || uploadingAttachment"
              @click="handleSend"
            >
              <Send :size="18" />
            </button>
          </div>
        </div>
      </div>
    </template>

    <ImageLightbox
      :open="previewImages.length > 0"
      :images="previewImages"
      :index="previewIndex"
      @close="previewImages = []"
      @update:index="previewIndex = $event"
    />
  </div>
</template>
