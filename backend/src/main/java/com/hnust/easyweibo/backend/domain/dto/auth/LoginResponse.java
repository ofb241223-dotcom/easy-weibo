package com.hnust.easyweibo.backend.domain.dto.auth;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record LoginResponse(
    String token,
    UserResponse user
) {
}
