package com.expense.tracker.group.dto;

import java.math.BigDecimal;

public record ExpenseShareResponse(
        Long userId,
        String fullName,
        BigDecimal shareAmount
) {}
