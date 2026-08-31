package com.expense.tracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A small, dependency-free rate limiter for the authentication endpoints
 * only (/api/v1/auth/**), guarding against brute-forcing a password via
 * unlimited login attempts.
 *
 * CONCEPT: fixed-window rate limiting
 * We track "how many requests has this client IP made in the current
 * 60-second window" in memory. Once a client exceeds MAX_REQUESTS in a
 * window, further requests get 429 Too Many Requests until the window
 * resets. This is intentionally simple:
 *
 * WHY NOT a production-grade solution (Redis-backed, sliding window,
 * Bucket4j...): a single-instance in-memory counter is a reasonable
 * starting point for learning and for a single-server deployment, but it
 * resets on restart and doesn't share state across multiple backend
 * instances behind a load balancer. If you deploy more than one backend
 * instance, replace the in-memory Map here with a shared store (Redis is
 * the standard choice) so all instances see the same counts.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final long WINDOW_MILLIS = 60_000L;

    private final Map<String, Window> requestCounts = new ConcurrentHashMap<>();

    private record Window(AtomicInteger count, long windowStart) {}

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!request.getRequestURI().startsWith("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = clientIp(request);
        long now = Instant.now().toEpochMilli();

        Window window = requestCounts.compute(clientKey, (key, existing) -> {
            if (existing == null || now - existing.windowStart() > WINDOW_MILLIS) {
                return new Window(new AtomicInteger(1), now);
            }
            existing.count().incrementAndGet();
            return existing;
        });

        if (window.count().get() > MAX_REQUESTS_PER_WINDOW) {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests, please try again shortly\",\"data\":null}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
