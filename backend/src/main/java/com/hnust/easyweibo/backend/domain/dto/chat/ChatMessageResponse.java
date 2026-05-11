package com.hnust.easyweibo.backend.domain.dto.chat;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record ChatMessageResponse(
    String id,
    String conversationId,
    String senderId,
    UserResponse sender,
    String content,
    String messageType,
    String fileUrl,
    String fileName,
    String mimeType,
    String createdAt,
    Boolean read,
    Boolean recalled,
    Boolean canRecall
) {
}
