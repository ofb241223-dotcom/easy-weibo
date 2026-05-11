package com.hnust.easyweibo.backend.domain.dto.notification;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record NotificationResponse(
    String id,
    String type,
    UserResponse fromUser,
    String postId,
    String message,
    String actionLabel,
    String actionUrl,
    String createdAt,
    Boolean read
) {
}
