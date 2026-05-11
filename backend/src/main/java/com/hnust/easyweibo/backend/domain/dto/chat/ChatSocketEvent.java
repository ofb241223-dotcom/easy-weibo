package com.hnust.easyweibo.backend.domain.dto.chat;

public class ChatSocketEvent {
    private String type;
    private String conversationId;
    private ChatMessageResponse message;

    public ChatSocketEvent() {
    }

    public ChatSocketEvent(String type, String conversationId, ChatMessageResponse message) {
        this.type = type;
        this.conversationId = conversationId;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public ChatMessageResponse getMessage() {
        return message;
    }

    public void setMessage(ChatMessageResponse message) {
        this.message = message;
    }
}
