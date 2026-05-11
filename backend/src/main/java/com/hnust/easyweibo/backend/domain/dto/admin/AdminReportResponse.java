package com.hnust.easyweibo.backend.domain.dto.admin;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record AdminReportResponse(
    String id,
    String category,
    String details,
    String status,
    String createdAt,
    String resolvedAt,
    UserResponse reporter,
    UserResponse resolvedBy,
    AdminReportPostResponse post
) {
}
