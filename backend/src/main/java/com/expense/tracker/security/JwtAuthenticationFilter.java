package com.expense.tracker.security;

import com.expense.tracker.auth.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CONCEPT: Security Filter
 * A "filter" in the Servlet/Spring Security world intercepts EVERY incoming
 * HTTP request before it reaches your @RestController. Spring Security is
 * built as a CHAIN of filters (CORS filter, authentication filter,
 * authorization filter...) - each does one job and passes the request along.
 *
 * WHY OncePerRequestFilter: guarantees this filter's logic runs exactly once
 * per request even if the request is internally forwarded/included.
 *
 * WHAT THIS FILTER DOES (on every request):
 *   1. Read the "Authorization: Bearer <token>" header
 *   2. Extract and validate the JWT
 *   3. If valid, look up the user and tell Spring Security
 *      "this request is authenticated as this user" by populating the
 *      SecurityContextHolder
 *   4. Pass control to the next filter in the chain
 * If there's no token, or it's invalid, we simply do nothing here -
 * SecurityConfig later decides whether the target endpoint requires auth,
 * and rejects unauthenticated requests to protected endpoints with a 401/403.
 */
@Component
@RequiredArgsConstructor // Lombok generates a constructor for all `final` fields = constructor injection
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7); // strip "Bearer "
        final String userEmail = jwtService.extractUsername(jwt);

        // Only authenticate if we have an email AND the request isn't already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
