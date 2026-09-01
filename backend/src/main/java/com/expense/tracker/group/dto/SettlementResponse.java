package com.expense.tracker.group.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SettlementResponse(
        Long id,
        Long fromUserId,
        String fromName,
        Long toUserId,
        String toName,
        BigDecimal amount,
        String note,
        Instant settledAt
) {}
