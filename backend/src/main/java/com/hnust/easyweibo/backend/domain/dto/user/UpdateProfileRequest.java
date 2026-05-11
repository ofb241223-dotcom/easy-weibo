package com.hnust.easyweibo.backend.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 32, message = "昵称长度需在 2 到 32 个字符之间")
    String nickname,
    @Size(max = 255, message = "简介不能超过 255 个字符")
    String bio,
    @Size(max = 255, message = "头像地址不能超过 255 个字符")
    String avatar,
    @Size(max = 255, message = "封面地址不能超过 255 个字符")
    String coverUrl
) {
}
