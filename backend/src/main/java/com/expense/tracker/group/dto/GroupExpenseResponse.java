package com.expense.tracker.group.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record GroupExpenseResponse(
        Long id,
        BigDecimal amount,
        String description,
        LocalDate expenseDate,
        Long paidByUserId,
        String paidByName,
        String splitType,
        List<ExpenseShareResponse> shares,
        boolean hasReceipt,
        String receiptOriginalName,
        Instant createdAt
) {}
