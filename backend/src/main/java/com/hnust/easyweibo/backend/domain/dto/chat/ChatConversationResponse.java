package com.hnust.easyweibo.backend.domain.dto.chat;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record ChatConversationResponse(
    String id,
    UserResponse targetUser,
    String lastMessage,
    String lastMessageAt,
    Integer unreadCount,
    Boolean blockedByCurrentUser,
    Boolean blockedByOtherUser,
    Boolean canSend,
    String restrictionReason
) {
}
