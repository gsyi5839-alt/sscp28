package com.bcbbs.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 强制修改密码请求：
 * - 解决账号因“未修改初始密码”被停用后无法登录、无法进入修改密码页的死锁问题
 * - 通过 captcha + 原密码校验后允许修改密码并自动解锁账号
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForceChangePasswordRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "新密码至少6位")
    private String newPassword;

    @NotBlank(message = "Captcha token is required")
    private String captchaToken;

    @NotBlank(message = "Captcha code is required")
    private String captchaCode;
}

