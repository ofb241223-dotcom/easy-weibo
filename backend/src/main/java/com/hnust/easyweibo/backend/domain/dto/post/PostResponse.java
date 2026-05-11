package com.hnust.easyweibo.backend.domain.dto.post;

import java.util.List;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record PostResponse(
    String id,
    String authorId,
    UserResponse author,
    String content,
    List<String> images,
    String status,
    String createdAt,
    Integer likesCount,
    Integer repostsCount,
    Integer commentsCount,
    Integer viewsCount,
    Boolean isLiked,
    Boolean isReposted,
    Boolean isBookmarked,
    List<String> tags
) {
}
