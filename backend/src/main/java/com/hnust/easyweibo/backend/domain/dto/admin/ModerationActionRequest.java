package com.hnust.easyweibo.backend.domain.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModerationActionRequest(
    @NotBlank(message = "处理原因不能为空")
    @Size(max = 255, message = "处理原因不能超过 255 个字符")
    String reason
) {
}
