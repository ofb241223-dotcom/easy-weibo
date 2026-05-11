package com.hnust.easyweibo.backend.domain.dto.comment;

import java.util.List;

import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;

public record CommentResponse(
    String id,
    String postId,
    String authorId,
    UserResponse author,
    String content,
    List<String> images,
    String createdAt,
    Integer likesCount,
    Boolean isLiked
) {
}
