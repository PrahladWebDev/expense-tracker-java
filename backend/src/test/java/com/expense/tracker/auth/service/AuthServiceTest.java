package com.expense.tracker.auth.service;

import com.expense.tracker.auth.dto.RegisterRequest;
import com.expense.tracker.auth.repository.RefreshTokenRepository;
import com.expense.tracker.common.exception.DuplicateResourceException;
import com.expense.tracker.security.JwtService;
import com.expense.tracker.user.entity.Role;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        // Critically: if the email is already taken, we must never reach the
        // password-hashing/save step at all.
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_hashesPasswordBeforeSaving_andNeverStoresPlainText() {
        // ReflectionTestUtils sets the @Value-injected field directly, since
        // there's no Spring context in a plain Mockito unit test to resolve
        // ${app.jwt.refresh-token-expiration-ms} from application.yml.
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);

        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "plainTextPassword");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainTextPassword")).thenReturn("$2a$10$hashedvalue");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            u.setCreatedAt(java.time.Instant.now());
            if (u.getRole() == null) u.setRole(Role.USER);
            return u;
        });
        when(jwtService.generateAccessToken(any())).thenReturn("fake-access-token");

        authService.register(request);

        verify(passwordEncoder).encode("plainTextPassword");
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals("$2a$10$hashedvalue") // the hash, never "plainTextPassword"
        ));
    }
}
