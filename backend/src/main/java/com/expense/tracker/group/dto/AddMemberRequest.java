package com.expense.tracker.group.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Members are added by the email address of an existing registered user. */
public record AddMemberRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        String email
) {}
