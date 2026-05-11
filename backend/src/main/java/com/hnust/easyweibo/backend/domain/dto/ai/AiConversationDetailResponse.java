package com.hnust.easyweibo.backend.domain.dto.ai;

import java.util.List;

public record AiConversationDetailResponse(
    String id,
    String title,
    String createdAt,
    String updatedAt,
    List<AiMessageResponse> messages
) {
}
