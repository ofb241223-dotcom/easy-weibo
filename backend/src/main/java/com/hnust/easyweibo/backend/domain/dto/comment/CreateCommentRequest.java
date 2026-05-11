package com.hnust.easyweibo.backend.domain.dto.comment;

import java.util.List;

public record CreateCommentRequest(
    String content,
    List<String> imageUrls
) {
}
