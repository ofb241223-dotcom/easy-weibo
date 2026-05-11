import { onBeforeUnmount, watch, type Ref } from 'vue';
import type { ApiConversationMessage } from '../api/services';
import { API_ORIGIN, getAuthToken } from '../api/http';

type ChatSocketEvent = {
  type: 'MESSAGE_CREATED' | 'MESSAGE_RECALLED' | 'CONVERSATION_INVALIDATED';
  conversationId: string;
  message?: ApiConversationMessage;
};

type UseChatSocketOptions = {
  onMessageCreated: (message: ApiConversationMessage) => void;
  onMessageRecalled: (message: ApiConversationMessage) => void;
  onConversationInvalidated: () => void;
};

export function useChatSocket(conversationId: Ref<string>, options: UseChatSocketOptions) {
  let socket: WebSocket | null = null;
  let reconnectTimer: number | null = null;
  let manuallyClosed = false;

  const disconnect = () => {
    manuallyClosed = true;
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
    if (socket) {
      socket.close();
      socket = null;
    }
  };

  const scheduleReconnect = () => {
    if (reconnectTimer || manuallyClosed || !conversationId.value || !getAuthToken()) {
      return;
    }
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null;
      connect();
    }, 2000);
  };

  const connect = () => {
    const token = getAuthToken();
    if (!token || !conversationId.value) {
      return;
    }

    manuallyClosed = false;
    const wsUrl = `${API_ORIGIN.replace(/^http/i, 'ws')}/ws/chat?token=${encodeURIComponent(token)}`;
    socket = new WebSocket(wsUrl);

    socket.onmessage = (event) => {
      const payload = JSON.parse(event.data) as ChatSocketEvent;
      if (payload.conversationId !== conversationId.value) {
        return;
      }

      if (payload.type === 'MESSAGE_CREATED' && payload.message) {
        options.onMessageCreated(payload.message);
      } else if (payload.type === 'MESSAGE_RECALLED' && payload.message) {
        options.onMessageRecalled(payload.message);
      } else if (payload.type === 'CONVERSATION_INVALIDATED') {
        options.onConversationInvalidated();
      }
    };

    socket.onerror = () => {
      socket?.close();
    };

    socket.onclose = () => {
      socket = null;
      scheduleReconnect();
    };
  };

  watch(
    conversationId,
    (value) => {
      disconnect();
      if (value) {
        connect();
      }
    },
    { immediate: true },
  );

  onBeforeUnmount(() => {
    disconnect();
  });

  return {
    disconnect,
  };
}
