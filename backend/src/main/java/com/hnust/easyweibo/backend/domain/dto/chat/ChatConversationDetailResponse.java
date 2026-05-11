package com.hnust.easyweibo.backend.domain.dto.chat;

import java.util.List;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record ChatConversationDetailResponse(
    String id,
    UserResponse targetUser,
    Integer unreadCount,
    Boolean blockedByCurrentUser,
    Boolean blockedByOtherUser,
    Boolean canSend,
    String restrictionReason,
    List<ChatMessageResponse> messages
) {
}
