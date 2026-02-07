package com.bcbbs.backend.config;

import com.bcbbs.backend.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple fixed-window rate limiter for sensitive endpoints.
 * NOTE: In-memory only (per instance); adjust thresholds as needed.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;

    private static final int AUTH_LIMIT_PER_MINUTE = 20;
    private static final int CAPTCHA_LIMIT_PER_MINUTE = 30;

    private static final String[] AUTH_PATHS = {
            "/api/auth/login",
            "/api/auth/role-login",
            "/api/auth/force-change-password"
    };

    private static final String CAPTCHA_PATH = "/api/public/captcha";

    private static final class WindowCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart;

        private WindowCounter(long windowStart) {
            this.windowStart = windowStart;
        }
    }

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String clientIp = getClientIp(request);

        Integer limit = getLimitForUri(uri);
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = uri + ":" + clientIp;
        long now = System.currentTimeMillis();
        WindowCounter counter = counters.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart >= WINDOW_MS) {
                WindowCounter fresh = new WindowCounter(now);
                fresh.count.incrementAndGet();
                return fresh;
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (counter != null && counter.count.get() > limit) {
            writeRateLimitResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Integer getLimitForUri(String uri) {
        if (uri == null) {
            return null;
        }
        if (CAPTCHA_PATH.equals(uri)) {
            return CAPTCHA_LIMIT_PER_MINUTE;
        }
        for (String path : AUTH_PATHS) {
            if (path.equals(uri)) {
                return AUTH_LIMIT_PER_MINUTE;
            }
        }
        return null;
    }

    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Object> body = ApiResponse.error(429, "Too many requests, please try again later");
        response.getWriter().write(
                "{\"code\":" + body.getCode() + ",\"message\":\"" + body.getMessage() + "\"}"
        );
    }

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
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

