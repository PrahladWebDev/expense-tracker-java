package com.expense.tracker.user.controller;

import com.expense.tracker.common.response.ApiResponse;
import com.expense.tracker.user.dto.ProfileResponse;
import com.expense.tracker.user.dto.UpdateProfileRequest;
import com.expense.tracker.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * "/me" is a common REST convention for "the currently authenticated
 * user's own resource" - it avoids needing a numeric ID in the URL (which
 * would otherwise tempt a client into requesting /users/7 and require us to
 * defend against IDOR - Insecure Direct Object Reference - on this
 * endpoint too). Authentication itself is what selects the row.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(authentication.getName())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(authentication.getName(), request), "Profile updated"));
    }
}
