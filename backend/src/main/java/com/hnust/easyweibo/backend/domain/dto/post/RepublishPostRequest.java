package com.hnust.easyweibo.backend.domain.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RepublishPostRequest(
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 1000, message = "帖子内容不能超过 1000 个字符")
    String content
) {
}
