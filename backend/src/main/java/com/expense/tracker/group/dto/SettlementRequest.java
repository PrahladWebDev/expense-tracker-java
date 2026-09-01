package com.expense.tracker.group.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SettlementRequest(
        @NotNull(message = "fromUserId is required")
        Long fromUserId,

        @NotNull(message = "toUserId is required")
        Long toUserId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,

        @Size(max = 255, message = "Note must be under 255 characters")
        String note
) {}
