package com.hnust.easyweibo.backend.domain.dto.user;

public record UserResponse(
    String id,
    String username,
    String nickname,
    String avatar,
    String coverUrl,
    String bio,
    String role,
    String status,
    String muteStatus,
    String createdAt,
    Integer followersCount,
    Integer followingCount,
    Boolean isFollowing
) {
}
