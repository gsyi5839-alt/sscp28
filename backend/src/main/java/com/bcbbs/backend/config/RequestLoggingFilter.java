package com.bcbbs.backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * API请求日志过滤器
 * 记录所有HTTP请求和响应的详细信息
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements Filter {

    // 专用于API日志的Logger
    private static final Logger API_LOG = LoggerFactory.getLogger("API");
    
    // 需要记录请求体的Content-Type
    private static final String[] LOGGABLE_CONTENT_TYPES = {
            "application/json",
            "application/xml",
            "text/xml",
            "text/plain"
    };
    
    // 不记录日志的路径（如静态资源）
    private static final String[] SKIP_PATHS = {
            "/favicon.ico",
            "/assets/",
            "/static/",
            "/actuator/"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 检查是否需要跳过日志
        String requestURI = httpRequest.getRequestURI();
        if (shouldSkipLogging(requestURI)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 生成请求ID
        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // 包装请求和响应以便读取内容
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录请求开始
            logRequestStart(requestId, wrappedRequest);
            
            // 执行请求
            chain.doFilter(wrappedRequest, wrappedResponse);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录请求结束
            logRequestEnd(requestId, wrappedRequest, wrappedResponse, duration);
            
            // 将响应内容复制到原始响应
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * 检查是否应该跳过日志记录
     */
    private boolean shouldSkipLogging(String uri) {
        for (String skipPath : SKIP_PATHS) {
            if (uri.startsWith(skipPath) || uri.contains(skipPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 记录请求开始
     */
    private void logRequestStart(String requestId, ContentCachingRequestWrapper request) {
        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;
        
        // 获取请求头（隐藏敏感信息）
        String headers = Collections.list(request.getHeaderNames()).stream()
                .filter(name -> !name.equalsIgnoreCase("authorization") && !name.equalsIgnoreCase("cookie"))
                .map(name -> name + "=" + request.getHeader(name))
                .collect(Collectors.joining(", "));
        
        API_LOG.info("[{}] >>> {} {} | IP: {} | Headers: [{}]", 
                requestId, method, fullUrl, clientIp, headers);
    }

    /**
     * 记录请求结束
     */
    private void logRequestEnd(String requestId, ContentCachingRequestWrapper request,
                               ContentCachingResponseWrapper response, long duration) {
        int status = response.getStatus();
        String statusDesc = getStatusDescription(status);
        
        // 获取请求体（如果是JSON类型）
        String requestBody = getRequestBody(request);
        
        // 获取响应体（如果是JSON类型）
        String responseBody = getResponseBody(response);
        
        // 根据状态码决定日志级别
        if (status >= 500) {
            API_LOG.error("[{}] <<< {} {} | Duration: {}ms | Request: {} | Response: {}", 
                    requestId, status, statusDesc, duration, requestBody, responseBody);
        } else if (status >= 400) {
            API_LOG.warn("[{}] <<< {} {} | Duration: {}ms | Request: {} | Response: {}", 
                    requestId, status, statusDesc, duration, requestBody, responseBody);
        } else {
            API_LOG.info("[{}] <<< {} {} | Duration: {}ms | Request: {} | Response: {}", 
                    requestId, status, statusDesc, duration, 
                    truncate(requestBody, 500), truncate(responseBody, 500));
        }
        
        // 记录慢请求
        if (duration > 3000) {
            log.warn("[SLOW_REQUEST] [{}] 请求耗时 {}ms - {} {}", 
                    requestId, duration, request.getMethod(), request.getRequestURI());
        }
    }

    /**
     * 获取请求体
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        if (!isLoggableContentType(request.getContentType())) {
            return "[非JSON内容]";
        }
        
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "[空]";
        }
        
        String body = new String(content, StandardCharsets.UTF_8);
        // 隐藏密码字段
        body = body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"*****\"");
        body = body.replaceAll("\"oldPassword\"\\s*:\\s*\"[^\"]*\"", "\"oldPassword\":\"*****\"");
        body = body.replaceAll("\"newPassword\"\\s*:\\s*\"[^\"]*\"", "\"newPassword\":\"*****\"");
        
        return body;
    }

    /**
     * 获取响应体
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        if (!isLoggableContentType(response.getContentType())) {
            return "[非JSON内容]";
        }
        
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "[空]";
        }
        
        String body = new String(content, StandardCharsets.UTF_8);
        // 隐藏token字段
        body = body.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"*****\"");
        
        return body;
    }

    /**
     * 检查Content-Type是否需要记录
     */
    private boolean isLoggableContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        for (String loggableType : LOGGABLE_CONTENT_TYPES) {
            if (contentType.contains(loggableType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 截断过长的内容
     */
    private String truncate(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...[截断]";
    }

    /**
     * 获取客户端IP
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
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取HTTP状态码描述
     */
    private String getStatusDescription(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "Unknown";
        };
    }
}
