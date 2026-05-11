package com.hnust.easyweibo.backend.domain.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiStreamMessageRequest(
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过 4000 个字符")
    String message,
    @NotBlank(message = "模型不能为空")
    @Size(max = 64, message = "模型名称过长")
    String model
) {
}
