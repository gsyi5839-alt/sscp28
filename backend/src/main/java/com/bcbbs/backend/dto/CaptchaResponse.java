package com.bcbbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {

    private String token;
    // Base64-encoded SVG data URI for display.
    private String image;
    private LocalDateTime expiresAt;
}

