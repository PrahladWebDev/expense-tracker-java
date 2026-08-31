package com.expense.tracker.admin.controller;

import com.expense.tracker.admin.dto.AdminUserSummary;
import com.expense.tracker.common.response.ApiResponse;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The FIRST endpoint in this project that actually requires Role.ADMIN.
 * Authorization is enforced in SecurityConfig via
 * `.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")` - Spring Security
 * checks the "ROLE_ADMIN" authority (granted in CustomUserDetailsService)
 * BEFORE the request ever reaches this controller. A USER-role account
 * presenting a perfectly valid JWT still gets a 403 Forbidden here, because
 * authentication (who are you) and authorization (what are you allowed to
 * do) are two separate checks - this endpoint is the demonstration of the
 * second one.
 *
 * There's currently no signup flow that produces an ADMIN user (every
 * self-registration is forced to Role.USER in AuthService, on purpose - you
 * don't want arbitrary signups granting themselves admin). To test this
 * endpoint locally, promote a user manually:
 *   UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserSummary>>> listUsers() {
        List<AdminUserSummary> users = userRepository.findAll().stream()
                .map(u -> new AdminUserSummary(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(), u.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}
