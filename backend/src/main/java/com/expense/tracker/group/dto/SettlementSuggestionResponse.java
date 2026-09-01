package com.expense.tracker.group.dto;

import java.math.BigDecimal;

/** "fromName should pay toName amount to settle the group's debts." */
public record SettlementSuggestionResponse(
        Long fromUserId,
        String fromName,
        Long toUserId,
        String toName,
        BigDecimal amount
) {}
