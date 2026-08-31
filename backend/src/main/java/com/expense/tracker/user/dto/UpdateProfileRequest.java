package com.expense.tracker.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Deliberately does NOT include email or password. Changing either of those
 * has real security implications (email is our login identifier; password
 * changes should require re-entering the current password) that deserve
 * their own dedicated endpoints rather than being folded into a generic
 * "update profile" call. Keeping this endpoint narrow avoids accidentally
 * building an account-takeover surface.
 */
public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be under 100 characters")
        String fullName
) {}
