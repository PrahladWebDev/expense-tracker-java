package com.expense.tracker.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Comment text is required")
        @Size(max = 1000, message = "Comment must be under 1000 characters")
        String text
) {}
