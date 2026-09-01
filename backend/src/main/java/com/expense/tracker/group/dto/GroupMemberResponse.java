package com.expense.tracker.group.dto;

import java.time.Instant;

public record GroupMemberResponse(
        Long userId,
        String fullName,
        String email,
        String role,
        Instant joinedAt
) {}
