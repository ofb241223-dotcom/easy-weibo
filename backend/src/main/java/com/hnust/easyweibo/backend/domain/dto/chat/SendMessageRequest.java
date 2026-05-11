package com.hnust.easyweibo.backend.domain.dto.chat;

public record SendMessageRequest(
    String content,
    String messageType,
    String fileUrl,
    String fileName,
    String mimeType
) {
}
