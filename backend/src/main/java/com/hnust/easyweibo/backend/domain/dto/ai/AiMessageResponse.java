package com.hnust.easyweibo.backend.domain.dto.ai;

public record AiMessageResponse(
    String id,
    String role,
    String content,
    String createdAt,
    String model
) {
}
