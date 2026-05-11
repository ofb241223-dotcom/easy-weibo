package com.hnust.easyweibo.backend.domain.dto.chat;

import jakarta.validation.constraints.NotNull;

public record CreateConversationRequest(
    @NotNull(message = "目标用户不能为空")
    Long targetUserId
) {
}
