package com.expense.tracker.auth.controller;

import com.expense.tracker.auth.dto.*;
import com.expense.tracker.auth.service.AuthService;
import com.expense.tracker.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONCEPT: REST Controller
 * @RestController = @Controller + @ResponseBody: every method's return
 * value is serialized directly to the HTTP response body as JSON (via
 * Jackson), instead of being resolved to a server-rendered view template.
 *
 * @RequestMapping("/api/v1/auth") is the shared base path (API VERSIONING:
 * prefixing routes with /v1 lets us introduce breaking changes in /v2 later
 * without breaking existing frontend clients).
 *
 * HTTP METHODS used here:
 *   POST /register -> creates a new resource (a User)
 *   POST /login     -> not strictly RESTful "resource creation", but POST is
 *                       used because credentials must go in the body, not a
 *                       URL (URLs get logged; bodies are not, over HTTPS)
 *   POST /refresh, /logout -> also POST, since they have side effects
 *                       (issuing/revoking tokens) rather than just reading
 *
 * These endpoints are all listed in SecurityConfig as `.permitAll()` -
 * you can't be authenticated with a JWT before you HAVE a JWT.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Logged in successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
}
