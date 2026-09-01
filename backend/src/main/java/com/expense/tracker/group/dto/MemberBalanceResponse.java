package com.expense.tracker.group.dto;

import java.math.BigDecimal;

/**
 * netBalance > 0  -> the group owes this member money (they overpaid).
 * netBalance < 0  -> this member owes the group money.
 * netBalance == 0 -> settled up.
 * totalPaid/totalShare are informational (expense-only, before settlements).
 */
public record MemberBalanceResponse(
        Long userId,
        String fullName,
        BigDecimal totalPaid,
        BigDecimal totalShare,
        BigDecimal netBalance
) {}
