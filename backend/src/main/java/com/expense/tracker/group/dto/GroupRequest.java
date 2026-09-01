package com.expense.tracker.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupRequest(
        @NotBlank(message = "Group name is required")
        @Size(max = 120, message = "Group name must be under 120 characters")
        String name,

        @Size(max = 500, message = "Description must be under 500 characters")
        String description
) {}
