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
 * API request logging filter
 * Logs detailed information for all HTTP requests and responses
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements Filter {

    // Logger dedicated to API logs
    private static final Logger API_LOG = LoggerFactory.getLogger("API");

    // Content-Types that need request body logging
    private static final String[] LOGGABLE_CONTENT_TYPES = {
            "application/json",
            "application/xml",
            "text/xml",
            "text/plain"
    };

    // Paths to skip logging (such as static resources)
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

        // Check if logging should be skipped
        String requestURI = httpRequest.getRequestURI();
        if (shouldSkipLogging(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Generate request ID
        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Wrap request and response to read content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        @SuppressWarnings("null")
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            // Log request start
            logRequestStart(requestId, wrappedRequest);

            // Execute request
            chain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log request end
            logRequestEnd(requestId, wrappedRequest, wrappedResponse, duration);

            // Copy response content to original response
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * Check if logging should be skipped
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
     * Log request start
     */
    private void logRequestStart(String requestId, ContentCachingRequestWrapper request) {
        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        // Get request headers (hide sensitive information)
        String headers = Collections.list(request.getHeaderNames()).stream()
                .filter(name -> !name.equalsIgnoreCase("authorization") && !name.equalsIgnoreCase("cookie"))
                .map(name -> name + "=" + request.getHeader(name))
                .collect(Collectors.joining(", "));

        API_LOG.info("[{}] >>> {} {} | IP: {} | Headers: [{}]",
                requestId, method, fullUrl, clientIp, headers);
    }

    /**
     * Log request end
     */
    private void logRequestEnd(String requestId, ContentCachingRequestWrapper request,
                               ContentCachingResponseWrapper response, long duration) {
        int status = response.getStatus();
        String statusDesc = getStatusDescription(status);

        // Get request body (if JSON type)
        String requestBody = getRequestBody(request);

        // Get response body (if JSON type)
        String responseBody = getResponseBody(response);

        // Determine log level based on status code
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

        // Log slow requests
        if (duration > 3000) {
            log.warn("[SLOW_REQUEST] [{}] Request took {}ms - {} {}",
                    requestId, duration, request.getMethod(), request.getRequestURI());
        }
    }

    /**
     * Get request body
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        if (!isLoggableContentType(request.getContentType())) {
            return "[Non-JSON content]";
        }

        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "[Empty]";
        }

        String body = new String(content, StandardCharsets.UTF_8);
        // Hide password fields
        body = body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"*****\"");
        body = body.replaceAll("\"oldPassword\"\\s*:\\s*\"[^\"]*\"", "\"oldPassword\":\"*****\"");
        body = body.replaceAll("\"newPassword\"\\s*:\\s*\"[^\"]*\"", "\"newPassword\":\"*****\"");

        return body;
    }

    /**
     * Get response body
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        if (!isLoggableContentType(response.getContentType())) {
            return "[Non-JSON content]";
        }

        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "[Empty]";
        }

        String body = new String(content, StandardCharsets.UTF_8);
        // Hide token field
        body = body.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"*****\"");

        return body;
    }

    /**
     * Check if Content-Type needs logging
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
     * Truncate overly long content
     */
    private String truncate(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...[Truncated]";
    }

    /**
     * Get client IP
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
     * Get HTTP status code description
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
