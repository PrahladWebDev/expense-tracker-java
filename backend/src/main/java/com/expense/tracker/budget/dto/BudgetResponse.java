package com.expense.tracker.budget.dto;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        BigDecimal amount,
        String month,
        Long categoryId,
        String categoryName,
        BigDecimal spent,
        BigDecimal remaining,
        double percentUsed
) {}
