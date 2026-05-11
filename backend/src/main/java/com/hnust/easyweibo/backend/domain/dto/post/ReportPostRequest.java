package com.hnust.easyweibo.backend.domain.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReportPostRequest(
    @NotBlank(message = "举报类型不能为空")
    @Pattern(
        regexp = "spam|abuse|misinformation|copyright|other",
        message = "举报类型不合法"
    )
    String category,
    @Size(max = 255, message = "补充说明不能超过 255 个字符")
    String details
) {
}
