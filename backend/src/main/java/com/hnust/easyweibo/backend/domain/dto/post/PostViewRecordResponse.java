package com.hnust.easyweibo.backend.domain.dto.post;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record PostViewRecordResponse(
    String id,
    UserResponse viewer,
    String viewedAt
) {
}
