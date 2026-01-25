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
    
    // Whether password change is required
    private Boolean needPasswordChange;

    // Number of logins without password change
    private Integer loginCountWithoutChange;
}

