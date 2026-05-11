package com.hnust.easyweibo.backend.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "用户名不能为空")
    String username,
    @NotBlank(message = "昵称不能为空")
    String nickname,
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在 6 到 64 个字符之间")
    String password
) {
}
