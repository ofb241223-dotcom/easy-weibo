import { computed, ref } from 'vue';
import type { AiConversation, AiMessage, AiModelId } from '../types';
import { aiService } from '../api/services';

type UiAiMessage = AiMessage & {
  pending?: boolean;
  failed?: boolean;
};

export const AI_MODELS: Array<{ id: AiModelId; label: string; shortLabel: string }> = [
  { id: 'gemini-2.5-flash-lite', label: 'Gemini 2.5 Flash Lite', shortLabel: '2.5 Flash Lite' },
  { id: 'gemini-2.5-flash', label: 'Gemini 2.5 Flash', shortLabel: '2.5 Flash' },
  { id: 'gemini-2.5-pro', label: 'Gemini 2.5 Pro', shortLabel: '2.5 Pro' },
];
export const DEFAULT_AI_MODEL: AiModelId = 'gemini-2.5-flash-lite';

const isOpen = ref(false);
const historyOpen = ref(false);
const conversations = ref<AiConversation[]>([]);
const currentConversationId = ref<string | null>(null);
const messages = ref<UiAiMessage[]>([]);
const draft = ref('');
const selectedModel = ref<AiModelId>(DEFAULT_AI_MODEL);
const loadingConversations = ref(false);
const loadingMessages = ref(false);
const sending = ref(false);
const lastError = ref('');
const initialized = ref(false);

const makeTempMessage = (role: AiMessage['role'], content = '', model: AiModelId = DEFAULT_AI_MODEL): UiAiMessage => ({
  id: `temp-${role}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
  role,
  content,
  createdAt: new Date().toISOString(),
  model,
  pending: true,
});

const syncConversations = async () => {
  conversations.value = await aiService.getConversations();
  initialized.value = true;
};

const syncConversation = async (conversationId: string) => {
  loadingMessages.value = true;
  try {
    const conversation = await aiService.getConversation(conversationId);
    currentConversationId.value = conversation.id;
    messages.value = conversation.messages;
    const lastUserMessage = [...conversation.messages].reverse().find((message) => message.role === 'user');
    selectedModel.value = lastUserMessage?.model ?? DEFAULT_AI_MODEL;
  } finally {
    loadingMessages.value = false;
  }
};

export function useAiChat() {
  const latestAssistantId = computed(() => {
    const reversed = [...messages.value].reverse();
    return reversed.find((message) => message.role === 'assistant' && !message.pending)?.id ?? null;
  });

  const hasMessages = computed(() => messages.value.length > 0);

  const openPanel = async () => {
    isOpen.value = true;
    if (loadingConversations.value) {
      return;
    }
    if (!initialized.value) {
      loadingConversations.value = true;
      try {
        await syncConversations();
      } finally {
        loadingConversations.value = false;
      }
    }
  };

  const closePanel = () => {
    isOpen.value = false;
    historyOpen.value = false;
    lastError.value = '';
  };

  const toggleHistory = async () => {
    if (!initialized.value && !loadingConversations.value) {
      loadingConversations.value = true;
      try {
        await syncConversations();
      } finally {
        loadingConversations.value = false;
      }
    }
    historyOpen.value = !historyOpen.value;
  };

  const selectConversation = async (conversationId: string) => {
    historyOpen.value = false;
    await syncConversation(conversationId);
  };

  const startNewConversation = async () => {
    loadingMessages.value = true;
    try {
      const conversation = await aiService.createConversation();
      currentConversationId.value = conversation.id;
      messages.value = [];
      selectedModel.value = DEFAULT_AI_MODEL;
      historyOpen.value = false;
      lastError.value = '';
      draft.value = '';
      await syncConversations();
    } finally {
      loadingMessages.value = false;
    }
  };

  const deleteConversation = async (conversationId: string) => {
    await aiService.deleteConversation(conversationId);
    conversations.value = conversations.value.filter((conversation) => conversation.id !== conversationId);

    if (currentConversationId.value === conversationId) {
      currentConversationId.value = null;
      messages.value = [];
      selectedModel.value = DEFAULT_AI_MODEL;
      lastError.value = '';
      draft.value = '';
    }
  };

  const sendMessage = async (input?: string, model: AiModelId = selectedModel.value) => {
    const content = (input ?? draft.value).trim();
    if (!content || sending.value) {
      return;
    }

    let conversationId = currentConversationId.value;
    if (!conversationId) {
      const conversation = await aiService.createConversation();
      conversationId = conversation.id;
      currentConversationId.value = conversationId;
      await syncConversations();
    }

    const userMessage = makeTempMessage('user', content, model);
    const assistantMessage = makeTempMessage('assistant', '', model);
    messages.value = [...messages.value, userMessage, assistantMessage];
    draft.value = '';
    sending.value = true;
    lastError.value = '';

    try {
      await aiService.streamMessage(conversationId, content, model, {
        onDelta: (text) => {
          const last = messages.value[messages.value.length - 1];
          if (!last || last.role !== 'assistant') {
            return;
          }
          last.content += text;
        },
        onError: (message) => {
          lastError.value = message;
          const last = messages.value[messages.value.length - 1];
          if (last && last.role === 'assistant') {
            last.failed = true;
            last.pending = false;
            last.content = message;
          }
        },
      });

      await syncConversation(conversationId);
      await syncConversations();
    } catch (error) {
      lastError.value = error instanceof Error ? error.message : 'AI 请求失败，请稍后重试';
      throw error;
    } finally {
      sending.value = false;
    }
  };

  const retryLatestAssistant = async () => {
    if (!currentConversationId.value || sending.value) {
      return;
    }

    const currentMessages = [...messages.value];
    for (let index = currentMessages.length - 1; index >= 0; index -= 1) {
      if (currentMessages[index].role === 'assistant') {
        currentMessages.splice(index, 1);
        break;
      }
    }

    const lastUserMessage = [...currentMessages].reverse().find((message) => message.role === 'user');
    const retryModel = lastUserMessage?.model ?? DEFAULT_AI_MODEL;
    const assistantMessage = makeTempMessage('assistant', '', retryModel);
    messages.value = [...currentMessages, assistantMessage];
    sending.value = true;
    lastError.value = '';

    try {
      await aiService.retryConversation(currentConversationId.value, {
        onDelta: (text) => {
          const last = messages.value[messages.value.length - 1];
          if (!last || last.role !== 'assistant') {
            return;
          }
          last.content += text;
        },
        onError: (message) => {
          lastError.value = message;
          const last = messages.value[messages.value.length - 1];
          if (last && last.role === 'assistant') {
            last.failed = true;
            last.pending = false;
            last.content = message;
          }
        },
      });

      await syncConversation(currentConversationId.value);
      await syncConversations();
    } catch (error) {
      lastError.value = error instanceof Error ? error.message : 'AI 请求失败，请稍后重试';
      throw error;
    } finally {
      sending.value = false;
    }
  };

  const clearLastError = () => {
    lastError.value = '';
  };

  return {
    isOpen,
    historyOpen,
    conversations,
    currentConversationId,
    messages,
    draft,
    selectedModel,
    loadingConversations,
    loadingMessages,
    sending,
    lastError,
    latestAssistantId,
    hasMessages,
    openPanel,
    closePanel,
    toggleHistory,
    selectConversation,
    startNewConversation,
    deleteConversation,
    sendMessage,
    retryLatestAssistant,
    clearLastError,
  };
}
