package com.hnust.easyweibo.backend.domain.dto.ai;

public record AiConversationResponse(
    String id,
    String title,
    String createdAt,
    String updatedAt,
    String preview
) {
}
