package com.expense.tracker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * CONCEPT: Spring Security configuration
 * This class wires together everything Spring Security needs:
 *   - which endpoints require authentication
 *   - how passwords are hashed
 *   - where our custom JWT filter sits in the filter chain
 *   - that we use STATELESS sessions (no server-side session/cookie - the
 *     JWT itself carries the identity on every request)
 *
 * CONCEPT: BCrypt
 * BCryptPasswordEncoder hashes passwords with a computationally expensive,
 * SALTED algorithm. "Salted" means a random value is mixed in before
 * hashing, so two users with the same password get different hashes, and
 * the hash can't be reversed - only compared (encoder.matches(raw, hash)).
 * WHY: storing plain-text passwords means a single database leak exposes
 * every user's real password. BCrypt makes brute-forcing computationally
 * expensive even if the hash is stolen.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable()) // CSRF protection is for cookie-based sessions; irrelevant for stateless JWT APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // Authorization (not just authentication): only requests from a
                        // user whose JWT carries the ROLE_ADMIN authority reach these
                        // routes. A logged-in USER gets a 403, not a 401 - they proved who
                        // they are, they're just not allowed here.
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // STATELESS: Spring Security will never create or use an HttpSession.
                // Every request must carry its own valid JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                // Insert our custom filter BEFORE Spring's built-in username/password filter,
                // since we're authenticating via JWT, not a login form.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Rate limiting runs even earlier than JWT auth - reject excess
                // /auth/** traffic before we spend any effort parsing tokens.
                .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
