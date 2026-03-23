package com.bcbbs.backend.controller;

import com.bcbbs.backend.dto.ApiResponse;
import com.bcbbs.backend.dto.FrontendLogBatchRequest;
import com.bcbbs.backend.dto.FrontendLogEntry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Frontend logging controller
 * Receives log entries from frontend for centralized logging and analysis
 */
@Slf4j
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class FrontendLogController {

    // Dedicated logger for frontend logs
    private static final Logger FRONTEND_LOG = LoggerFactory.getLogger("FRONTEND");

    // Log level constants
    private static final String LEVEL_DEBUG = "DEBUG";
    private static final String LEVEL_INFO = "INFO";
    private static final String LEVEL_WARN = "WARN";
    private static final String LEVEL_ERROR = "ERROR";
    private static final String LEVEL_FATAL = "FATAL";

    /**
     * Receive single frontend log entry
     *
     * @param logEntry  the log entry from frontend
     * @param request   HTTP request for client info
     * @return success response
     */
    @PostMapping("/frontend")
    public ResponseEntity<ApiResponse<Void>> receiveFrontendLog(
            @Valid @RequestBody FrontendLogEntry logEntry,
            HttpServletRequest request) {

        logFrontendEntry(logEntry, request);
        return ResponseEntity.ok(ApiResponse.success("Log received", null));
    }

    /**
     * Receive batch of frontend log entries
     *
     * @param batchRequest batch of log entries
     * @param request      HTTP request for client info
     * @return success response
     */
    @PostMapping("/frontend/batch")
    public ResponseEntity<ApiResponse<Void>> receiveFrontendLogBatch(
            @Valid @RequestBody FrontendLogBatchRequest batchRequest,
            HttpServletRequest request) {

        if (batchRequest.getLogs() != null) {
            for (FrontendLogEntry logEntry : batchRequest.getLogs()) {
                logFrontendEntry(logEntry, request);
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Logs received", null));
    }

    /**
     * Log a single frontend entry with appropriate level
     */
    private void logFrontendEntry(FrontendLogEntry entry, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String logMessage = formatLogMessage(entry, clientIp);

        String level = entry.getLevel() != null ? entry.getLevel().toUpperCase() : LEVEL_INFO;

        switch (level) {
            case LEVEL_DEBUG:
                FRONTEND_LOG.debug(logMessage);
                break;
            case LEVEL_INFO:
                FRONTEND_LOG.info(logMessage);
                break;
            case LEVEL_WARN:
                FRONTEND_LOG.warn(logMessage);
                break;
            case LEVEL_ERROR:
            case LEVEL_FATAL:
                FRONTEND_LOG.error(logMessage);
                break;
            default:
                FRONTEND_LOG.info(logMessage);
        }
    }

    /**
     * Format frontend log entry for backend logging
     */
    private String formatLogMessage(FrontendLogEntry entry, String clientIp) {
        StringBuilder sb = new StringBuilder();
        sb.append("[FRONTEND] ");
        sb.append("[").append(entry.getLevel()).append("] ");
        sb.append("[").append(entry.getSource()).append("] ");
        sb.append("[User:").append(entry.getUserId() != null ? entry.getUserId() : "anonymous").append("] ");
        sb.append("[IP:").append(clientIp).append("] ");
        sb.append("- ").append(entry.getMessage());

        // Add URL if available
        if (entry.getUrl() != null && !entry.getUrl().isEmpty()) {
            sb.append(" | URL: ").append(entry.getUrl());
        }

        // Add stack trace for errors
        if (entry.getStackTrace() != null && !entry.getStackTrace().isEmpty()) {
            sb.append(" | Stack: ").append(truncateStackTrace(entry.getStackTrace()));
        }

        // Add additional info if present
        if (entry.getAdditionalInfo() != null && !entry.getAdditionalInfo().isEmpty()) {
            sb.append(" | Info: ").append(formatAdditionalInfo(entry.getAdditionalInfo()));
        }

        return sb.toString();
    }

    /**
     * Truncate long stack traces
     */
    private String truncateStackTrace(String stackTrace) {
        final int MAX_LENGTH = 1000;
        if (stackTrace.length() <= MAX_LENGTH) {
            return stackTrace;
        }
        return stackTrace.substring(0, MAX_LENGTH) + "...[truncated]";
    }

    /**
     * Format additional info map
     */
    private String formatAdditionalInfo(Map<String, Object> info) {
        try {
            StringBuilder sb = new StringBuilder();
            info.forEach((key, value) -> {
                if (sb.length() > 0) sb.append(", ");
                sb.append(key).append("=");
                if (value != null) {
                    String str = value.toString();
                    // Truncate long values
                    if (str.length() > 100) {
                        sb.append(str, 0, 100).append("...");
                    } else {
                        sb.append(str);
                    }
                } else {
                    sb.append("null");
                }
            });
            return sb.toString();
        } catch (Exception e) {
            return "[format error]";
        }
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
