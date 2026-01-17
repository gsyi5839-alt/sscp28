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
 * 全局异常处理器
 * 统一处理所有Controller层抛出的异常，并记录详细日志
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 专用于安全日志的Logger
    private static final Logger SECURITY_LOG = LoggerFactory.getLogger("SECURITY");
    
    // 专用于业务日志的Logger
    private static final Logger BUSINESS_LOG = LoggerFactory.getLogger("BUSINESS");

    /**
     * 生成唯一的错误追踪ID
     */
    private String generateErrorId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 获取完整的堆栈信息
     */
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 记录详细的错误日志
     */
    private void logError(String errorId, HttpServletRequest request, Exception e, String errorType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n");
        logBuilder.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
        logBuilder.append(String.format("║ 错误ID: %s | 类型: %s\n", errorId, errorType));
        logBuilder.append("╠══════════════════════════════════════════════════════════════════════════════╣\n");
        logBuilder.append(String.format("║ 时间: %s\n", timestamp));
        logBuilder.append(String.format("║ 请求URI: %s\n", request.getRequestURI()));
        logBuilder.append(String.format("║ 请求方法: %s\n", request.getMethod()));
        logBuilder.append(String.format("║ 客户端IP: %s\n", getClientIp(request)));
        logBuilder.append(String.format("║ User-Agent: %s\n", request.getHeader("User-Agent")));
        logBuilder.append(String.format("║ 请求参数: %s\n", getRequestParams(request)));
        logBuilder.append("╠══════════════════════════════════════════════════════════════════════════════╣\n");
        logBuilder.append(String.format("║ 异常类: %s\n", e.getClass().getName()));
        logBuilder.append(String.format("║ 异常信息: %s\n", e.getMessage()));
        logBuilder.append("╠══════════════════════════════════════════════════════════════════════════════╣\n");
        logBuilder.append("║ 堆栈跟踪:\n");
        logBuilder.append(getStackTrace(e));
        logBuilder.append("╚══════════════════════════════════════════════════════════════════════════════╝\n");
        
        log.error(logBuilder.toString());
    }

    /**
     * 记录简化的警告日志
     */
    private void logWarn(String errorId, HttpServletRequest request, Exception e, String errorType) {
        log.warn("[{}] {} - URI: {}, Method: {}, IP: {}, Message: {}", 
                errorId, errorType, request.getRequestURI(), request.getMethod(), 
                getClientIp(request), e.getMessage());
    }

    /**
     * 获取客户端真实IP
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
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.isEmpty()) {
            return "无";
        }
        StringBuilder params = new StringBuilder();
        parameterMap.forEach((key, values) -> {
            // 隐藏敏感参数
            if (key.toLowerCase().contains("password") || key.toLowerCase().contains("token")) {
                params.append(key).append("=*****, ");
            } else {
                params.append(key).append("=").append(String.join(",", values)).append(", ");
            }
        });
        return params.length() > 2 ? params.substring(0, params.length() - 2) : params.toString();
    }

    // ==================== 认证/授权相关异常 ====================

    /**
     * 处理认证异常（登录失败等）
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        SECURITY_LOG.warn("[{}] 认证失败 - URI: {}, IP: {}, Message: {}", 
                errorId, request.getRequestURI(), getClientIp(request), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "认证失败: " + e.getMessage(), errorId));
    }

    /**
     * 处理凭证错误异常（用户名或密码错误）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        SECURITY_LOG.warn("[{}] 凭证错误 - URI: {}, IP: {}", 
                errorId, request.getRequestURI(), getClientIp(request));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "用户名或密码错误", errorId));
    }

    /**
     * 处理访问拒绝异常（权限不足）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        SECURITY_LOG.warn("[{}] 访问拒绝 - URI: {}, IP: {}, Message: {}", 
                errorId, request.getRequestURI(), getClientIp(request), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "权限不足，拒绝访问", errorId));
    }

    // ==================== 参数校验相关异常 ====================

    /**
     * 处理参数校验异常（@Valid）
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
        
        logWarn(errorId, request, e, "参数校验失败");
        log.debug("[{}] 校验详情: {}", errorId, errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "参数校验失败: " + errors, errorId));
    }

    /**
     * 处理绑定异常
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
        
        logWarn(errorId, request, e, "参数绑定失败");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "参数绑定失败: " + errors, errorId));
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logWarn(errorId, request, e, "缺少请求参数");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "缺少必需参数: " + e.getParameterName(), errorId));
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logWarn(errorId, request, e, "参数类型不匹配");
        
        String message = String.format("参数 '%s' 类型错误，期望类型: %s", 
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, message, errorId));
    }

    // ==================== HTTP请求相关异常 ====================

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logWarn(errorId, request, e, "请求方法不支持");
        
        String message = String.format("不支持 %s 请求方法，支持的方法: %s", 
                e.getMethod(), String.join(", ", e.getSupportedMethods() != null ? e.getSupportedMethods() : new String[]{}));
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(405, message, errorId));
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logWarn(errorId, request, e, "资源不存在");
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "请求的资源不存在: " + request.getRequestURI(), errorId));
    }

    // ==================== 业务相关异常 ====================

    /**
     * 处理业务逻辑异常（IllegalArgumentException）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        BUSINESS_LOG.warn("[{}] 业务参数错误 - URI: {}, Message: {}", 
                errorId, request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage(), errorId));
    }

    /**
     * 处理业务状态异常（IllegalStateException）
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(
            IllegalStateException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        BUSINESS_LOG.error("[{}] 业务状态错误 - URI: {}, Message: {}", 
                errorId, request.getRequestURI(), e.getMessage());
        logError(errorId, request, e, "业务状态异常");
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, "业务状态异常: " + e.getMessage(), errorId));
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(
            NullPointerException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logError(errorId, request, e, "空指针异常");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误，请联系管理员 [错误ID: " + errorId + "]", errorId));
    }

    // ==================== 兜底异常处理 ====================

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logError(errorId, request, e, "运行时异常");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误，请联系管理员 [错误ID: " + errorId + "]", errorId));
    }

    /**
     * 处理所有其他异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(
            Exception e, HttpServletRequest request) {
        String errorId = generateErrorId();
        logError(errorId, request, e, "未知异常");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误，请联系管理员 [错误ID: " + errorId + "]", errorId));
    }
}
