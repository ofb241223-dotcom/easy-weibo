package com.hnust.easyweibo.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hnust.easyweibo.backend.domain.dto.chat.ChatMessageResponse;
import com.hnust.easyweibo.backend.domain.dto.chat.ChatSocketEvent;
import com.hnust.easyweibo.backend.websocket.ChatWebSocketHandler;

@Service
public class ChatRealtimeService {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatRealtimeService(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    public void publishMessageCreated(Long conversationId, List<Long> participantIds, ChatMessageResponse message) {
        chatWebSocketHandler.sendToUsers(
            participantIds,
            new ChatSocketEvent("MESSAGE_CREATED", String.valueOf(conversationId), message)
        );
    }

    public void publishMessageRecalled(Long conversationId, List<Long> participantIds, ChatMessageResponse message) {
        chatWebSocketHandler.sendToUsers(
            participantIds,
            new ChatSocketEvent("MESSAGE_RECALLED", String.valueOf(conversationId), message)
        );
    }

    public void publishConversationInvalidated(Long conversationId, List<Long> participantIds) {
        chatWebSocketHandler.sendToUsers(
            participantIds,
            new ChatSocketEvent("CONVERSATION_INVALIDATED", String.valueOf(conversationId), null)
        );
    }
}
