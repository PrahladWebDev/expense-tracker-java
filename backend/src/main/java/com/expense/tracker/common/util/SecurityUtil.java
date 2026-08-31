package com.expense.tracker.common.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * A small helper to read "who is the currently authenticated user" from
 * Spring Security's SecurityContextHolder (populated by JwtAuthenticationFilter).
 * Every controller/service that needs to enforce "users can only access
 * their own data" calls this instead of re-implementing context lookups.
 */
@Component
public class SecurityUtil {
    public String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
