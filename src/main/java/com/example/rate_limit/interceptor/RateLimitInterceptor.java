package com.example.rate_limit.interceptor;

import com.example.rate_limit.dto.RateLimitStatus;
import com.example.rate_limit.util.RedisLuaTokenBucketRateLimiter;
import com.example.rate_limit.util.RedisRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Instant;


@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisLuaTokenBucketRateLimiter rateLimiter;

    public RateLimitInterceptor(RedisLuaTokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {

        // Skip rate limiting for health check endpoints
        String path = request.getRequestURI();
        if (isExcludedPath(path)) {
            return true;
        }

        String userId = extractUserId(request);
        if (userId == null) {

            sendErrorResponse(response, HttpStatus.BAD_REQUEST,
                    "Missing or invalid user identification");
            return false;
        }

        if (!rateLimiter.allowRequest(userId)) {

            addRateLimitHeaders(response, userId);

            sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded. Please try again later.");
            return false;
        }

        addRateLimitHeaders(response, userId);
        return true;
    }

    private String extractUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.trim().isEmpty()) {
            return userId.trim();
        }

        // Fallback to IP address for anonymous rate limiting
        String clientIp = getClientIpAddress(request);
        return clientIp != null ? "ip:" + clientIp : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For", "X-Real-IP", "X-Originating-IP",
                "X-Cluster-Client-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/actuator/") ||
                path.equals("/health") ||
                path.equals("/metrics");
    }

    private void addRateLimitHeaders(HttpServletResponse response, String userId) {
        try {
            RateLimitStatus status =
                    rateLimiter.getRateLimitStatus(userId);

            response.setHeader("X-RateLimit-Limit", "100");
            response.setHeader("X-RateLimit-Remaining",
                    String.valueOf(status.getRemainingTokens()));
            response.setHeader("X-RateLimit-Reset",
                    String.valueOf(status.getLastRefillTimestamp() + 60000));

        } catch (Exception e) {

        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status,
                                   String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                status.getReasonPhrase(),
                message,
                Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
    }
}
