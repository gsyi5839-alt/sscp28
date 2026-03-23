package com.bcbbs.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Frontend log entry DTO
 * Receives log entries from frontend for centralized logging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontendLogEntry {

    /**
     * Log level: DEBUG, INFO, WARN, ERROR, FATAL
     */
    @NotBlank(message = "Log level is required")
    @Size(max = 10)
    private String level;

    /**
     * Log message (already masked for sensitive data)
     */
    @NotBlank(message = "Message is required")
    @Size(max = 2000)
    private String message;

    /**
     * ISO 8601 timestamp from frontend
     */
    @NotBlank(message = "Timestamp is required")
    private String timestamp;

    /**
     * Source module/component
     */
    @NotBlank(message = "Source is required")
    @Size(max = 100)
    private String source;

    /**
     * User agent string
     */
    @Size(max = 500)
    private String userAgent;

    /**
     * Current page URL
     */
    @Size(max = 500)
    private String url;

    /**
     * User ID (if logged in)
     */
    @Size(max = 50)
    private String userId;

    /**
     * Stack trace (for errors)
     */
    @Size(max = 5000)
    private String stackTrace;

    /**
     * Additional context information
     */
    private Map<String, Object> additionalInfo;
}
