package com.hnust.easyweibo.backend.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 24, message = "用户名长度需在 3 到 24 个字符之间")
    String username,
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 32, message = "昵称长度需在 2 到 32 个字符之间")
    String nickname,
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在 6 到 64 个字符之间")
    String password
) {
}
