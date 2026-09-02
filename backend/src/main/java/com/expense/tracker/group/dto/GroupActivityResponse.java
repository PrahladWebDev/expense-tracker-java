package com.expense.tracker.group.dto;

import java.time.Instant;

public record GroupActivityResponse(
        Long id,
        String type,
        String message,
        Long actorUserId,
        String actorName,
        Instant createdAt
) {}
