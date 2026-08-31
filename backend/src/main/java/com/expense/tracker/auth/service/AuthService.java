package com.expense.tracker.auth.service;

import com.expense.tracker.auth.dto.*;
import com.expense.tracker.auth.entity.RefreshToken;
import com.expense.tracker.auth.repository.RefreshTokenRepository;
import com.expense.tracker.common.exception.DuplicateResourceException;
import com.expense.tracker.common.exception.UnauthorizedException;
import com.expense.tracker.security.JwtService;
import com.expense.tracker.user.entity.Role;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * CONCEPT: Service layer
 * Controllers should stay "thin" - they just translate HTTP <-> Java calls.
 * All business logic (validation rules, orchestration, transactions) lives
 * in the Service layer. This separation of concerns makes logic reusable
 * and independently testable without spinning up a web server.
 *
 * CONCEPT: Constructor injection (via Lombok's @RequiredArgsConstructor)
 * Every `final` field below is a dependency Spring will supply automatically
 * when it creates this bean. WHY CONSTRUCTOR injection over @Autowired on
 * fields?
 *   1. Dependencies are immutable (final) and impossible to forget - the
 *      object literally cannot be constructed without them.
 *   2. It makes unit testing trivial: `new AuthService(mockRepo, mockEncoder...)`
 *      without needing a Spring context at all.
 *   3. It avoids circular dependency issues being hidden until runtime.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // never store plain text
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Delegates to Spring Security's AuthenticationManager, which calls
        // CustomUserDetailsService + PasswordEncoder.matches() under the hood.
        // Throws BadCredentialsException (handled globally) if invalid.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken existing = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (existing.isExpired() || existing.isRevoked()) {
            throw new UnauthorizedException("Refresh token expired or revoked, please log in again");
        }

        // Refresh token ROTATION: revoke the old one, issue a brand new pair.
        // This means a stolen refresh token can only be used once before
        // the legitimate user's next refresh invalidates it (limits damage).
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()).password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name()).build();

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = createAndSaveRefreshToken(user);

        return AuthResponse.of(newAccessToken, newRefreshToken, toSummary(user));
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private AuthResponse issueTokens(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()).password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name()).build();

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = createAndSaveRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken, toSummary(user));
    }

    private String createAndSaveRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
