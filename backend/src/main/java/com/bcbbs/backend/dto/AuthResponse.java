package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String username;
    private String email;
    private String nickname;
    private String role;
    
    // 是否需要修改密码
    private Boolean needPasswordChange;
    
    // 未修改密码的登录次数
    private Integer loginCountWithoutChange;
}

