package com.bcbbs.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Force password change request:
 * - Resolves the deadlock issue where account is disabled for "not changing initial password" and cannot login or access password change page
 * - Allows password change and automatic account unlock after captcha + old password verification
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
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "Captcha token is required")
    private String captchaToken;

    @NotBlank(message = "Captcha code is required")
    private String captchaCode;
}

