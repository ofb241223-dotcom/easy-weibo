package com.hnust.easyweibo.backend.domain.dto.admin;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record AdminReportPostResponse(
    String id,
    String content,
    String status,
    String createdAt,
    UserResponse author
) {
}
