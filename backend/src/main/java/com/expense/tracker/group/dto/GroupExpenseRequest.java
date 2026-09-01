package com.expense.tracker.group.dto;

import com.expense.tracker.group.entity.SplitType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GroupExpenseRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        @Digits(integer = 10, fraction = 2, message = "Amount can have at most 2 decimal places")
        BigDecimal amount,

        @Size(max = 255, message = "Description must be under 255 characters")
        String description,

        @NotNull(message = "Expense date is required")
        @PastOrPresent(message = "Expense date cannot be in the future")
        LocalDate expenseDate,

        @NotNull(message = "paidByUserId is required")
        Long paidByUserId,

        @NotNull(message = "splitType is required")
        SplitType splitType,

        @NotEmpty(message = "At least one participant is required")
        List<ExpenseShareInput> shares
) {}
