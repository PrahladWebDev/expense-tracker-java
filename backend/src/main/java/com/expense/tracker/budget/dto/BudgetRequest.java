package com.expense.tracker.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record BudgetRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Budget amount must be positive")
        BigDecimal amount,

        @NotNull
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Month must be in YYYY-MM format")
        String month,

        Long categoryId // null = overall budget for the month
) {}
