package com.expense.tracker.group.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long userId,
        String userName,
        String text,
        Instant createdAt
) {}
