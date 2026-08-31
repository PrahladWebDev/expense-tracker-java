package com.expense.tracker.security;

import com.expense.tracker.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CONCEPT: AuthenticationEntryPoint
 * This is what Spring Security calls when a request reaches an endpoint
 * that requires authentication but none was established (missing token,
 * expired token, invalid signature, etc.).
 *
 * WHY THIS CLASS EXISTS: without registering one explicitly, Spring
 * Security falls back to Http403ForbiddenEntryPoint whenever form login
 * and HTTP Basic are both disabled (which they are here — this is a
 * stateless JWT API). That means every unauthenticated request came back
 * as 403 Forbidden instead of 401 Unauthorized.
 *
 * That distinction isn't cosmetic: 403 means "I know who you are and you
 * can't do this"; 401 means "I don't know who you are, please
 * re-authenticate". The frontend's axios interceptor specifically listens
 * for 401 to trigger a silent token refresh — with the default 403, that
 * refresh logic never ran, so an expired access token looked identical to
 * a permissions error and only a manual logout/login could clear it.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401, not the default 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.error("Authentication required or token expired"))
        );
    }
}