package com.bcbbs.backend.config;

import com.bcbbs.backend.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler
 * Handles all exceptions thrown from Controller layer and logs detailed information
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Logger dedicated to security logs
    private static final Logger SECURITY_LOG = LoggerFactory.getLogger("SECURITY");

    // Logger dedicated to business logs
    private static final Logger BUSINESS_LOG = LoggerFactory.getLogger("BUSINESS");

    /**
     * Generate unique error tracking ID
     */
    private String generateErrorId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Get full stack trace
     */
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Log detailed error information
     */
    private void logError(String errorId, HttpServletRequest request, Exception e, String errorType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n");
        logBuilder.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
        logBuilder.append(String.format("║ Error ID: %s | Type: %s\n", errorId, errorType));
        logBuilder.append("╠══════════════════════════════════════════════════════════════════════════════╣\n");
        logBuilder.append(String.format("║ Time: %s\n", timestamp));
        logBuilder.append(String.format("║ Request URI: %s\n", request.getRequestURI()));
        logBuilder.append(String.format("║ Request Method: %s\n", request.getMethod()));
        logBuilder.append(String.format("║ Client IP: %s\n", getClientIp(request)));
        logBuilder.append(String.format("║ User-Agent: %s\n", request.getHeader("User-Agent")));
        logBuilder.append(String.format("║ Request Params: %s\n", getRequestParams(request)));
        logBuilder.append("╠══════════════════════════════════════════════════════════════════════════════╣\n");
        logBuilder.append(String.format("║ Exception Class: %s\n", e.getClass().getName()));
        logBuilder.append(String.format("║ Exception Message: %s\n", e.getMessage()));
        logBuilder.append("╠══════════════════════════════════════════════════════════════════════════════╣\n");
        logBuilder.append("║ Stack Trace:\n");
        logBuilder.append(getStackTrace(e));
        logBuilder.append("╚══════════════════════════════════════════════════════════════════════════════╝\n");

        log.error(logBuilder.toString());
    }

    /**
     * Log simplified warning information
     */
    private void logWarn(String errorId, HttpServletRequest request, Exception e, String errorType) {
        log.warn("[{}] {} - URI: {}, Method: {}, IP: {}, Message: {}",
                errorId, errorType, request.getRequestURI(), request.getMethod(),
                getClientIp(request), e.getMessage());
    }

    /**
     * Get client real IP
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
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // For multiple proxies, take the first IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Get request parameters
     */
    private String getRequestParams(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.isEmpty()) {
            return "None";
        }
        StringBuilder params = new StringBuilder();
        parameterMap.forEach((key, values) -> {
            // Hide sensitive parameters
            if (key.toLowerCase().contains("password") || key.toLowerCase().contains("token")) {
                params.append(key).append("=*****, ");
            } else {
                params.append(key).append("=").append(String.join(",", values)).append(", ");
            }
        });
        return params.length() > 2 ? params.substring(0, params.length() - 2) : params.toString();
    }

    // ==================== Authentication/Authorization Exceptions ====================

    /**
     * Handle authentication exceptions (login failure, etc.)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        SECURITY_LOG.warn("[{}] Authentication failed - URI: {}, IP: {}, Message: {}",
                errorId, request.getRequestURI(), getClientIp(request), e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "Authentication failed: " + e.getMessage(), errorId));
    }

    /**
     * Handle bad credentials exception (username or password error)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        SECURITY_LOG.warn("[{}] Bad credentials - URI: {}, IP: {}",
                errorId, request.getRequestURI(), getClientIp(request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "Username or password is incorrect", errorId));
    }

    /**
     * Handle access denied exception (insufficient permissions)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        SECURITY_LOG.warn("[{}] Access denied - URI: {}, IP: {}, Message: {}",
                errorId, request.getRequestURI(), getClientIp(request), e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "Insufficient permissions, access denied", errorId));
    }

    // ==================== Parameter Validation Exceptions ====================

    /**
     * Handle parameter validation exception (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorId = generateErrorId();

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logWarn(errorId, request, e, "Parameter validation failed");
        log.debug("[{}] Validation details: {}", errorId, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "Parameter validation failed: " + errors, errorId));
    }

    /**
     * Handle bind exception
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(
            BindException e, HttpServletRequest request) {
        String errorId = generateErrorId();

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logWarn(errorId, request, e, "Parameter binding failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "Parameter binding failed: " + errors, errorId));
    }

    /**
     * Handle missing request parameter exception
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logWarn(errorId, request, e, "Missing request parameter");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "Missing required parameter: " + e.getParameterName(), errorId));
    }

    /**
     * Handle parameter type mismatch exception
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logWarn(errorId, request, e, "Parameter type mismatch");

        Class<?> requiredType = e.getRequiredType();
        String message = String.format("Parameter '%s' type error, expected type: %s",
                e.getName(), requiredType != null ? requiredType.getSimpleName() : "unknown");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, message, errorId));
    }

    // ==================== HTTP Request Exceptions ====================

    /**
     * Handle request method not supported exception
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logWarn(errorId, request, e, "Request method not supported");

        String message = String.format("Request method %s not supported, supported methods: %s",
                e.getMethod(), String.join(", ", e.getSupportedMethods() != null ? e.getSupportedMethods() : new String[]{}));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(405, message, errorId));
    }

    /**
     * Handle 404 exception
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logWarn(errorId, request, e, "Resource not found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "Requested resource not found: " + request.getRequestURI(), errorId));
    }

    // ==================== Business Logic Exceptions ====================

    /**
     * Handle business logic exception (IllegalArgumentException)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        BUSINESS_LOG.warn("[{}] Business parameter error - URI: {}, Message: {}",
                errorId, request.getRequestURI(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage(), errorId));
    }

    /**
     * Handle business state exception (IllegalStateException)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(
            IllegalStateException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        BUSINESS_LOG.error("[{}] Business state error - URI: {}, Message: {}",
                errorId, request.getRequestURI(), e.getMessage());
        logError(errorId, request, e, "Business state exception");

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, "Business state exception: " + e.getMessage(), errorId));
    }

    /**
     * Handle null pointer exception
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(
            NullPointerException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logError(errorId, request, e, "Null pointer exception");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error, please contact administrator [Error ID: " + errorId + "]", errorId));
    }

    // ==================== Fallback Exception Handling ====================

    /**
     * Handle runtime exception
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logError(errorId, request, e, "Runtime exception");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error, please contact administrator [Error ID: " + errorId + "]", errorId));
    }

    /**
     * Handle all other exceptions (fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(
            Exception e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logError(errorId, request, e, "Unknown exception");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error, please contact administrator [Error ID: " + errorId + "]", errorId));
    }
}
