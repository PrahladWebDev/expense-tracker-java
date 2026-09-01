package com.expense.tracker.group.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * One participant in a group expense split.
 * - SplitType.EQUAL: `value` is ignored (may be null) - just lists who's in.
 * - SplitType.EXACT: `value` is the exact rupee amount this user owes.
 * - SplitType.PERCENTAGE: `value` is this user's percentage (0-100).
 */
public record ExpenseShareInput(
        @NotNull(message = "userId is required for each share")
        Long userId,

        BigDecimal value
) {}
