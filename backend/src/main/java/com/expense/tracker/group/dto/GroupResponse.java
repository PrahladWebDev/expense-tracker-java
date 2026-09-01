package com.expense.tracker.group.dto;

import java.time.Instant;
import java.util.List;

public record GroupResponse(
        Long id,
        String name,
        String description,
        Long createdByUserId,
        String createdByName,
        Instant createdAt,
        String status,
        Instant closedAt,
        List<GroupMemberResponse> members
) {}
