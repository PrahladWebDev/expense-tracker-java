package com.expense.tracker.expense.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        String description,
        LocalDate expenseDate,
        Long categoryId,
        String categoryName,
        String categoryColor,
        Instant createdAt,
        Instant updatedAt
) {}
